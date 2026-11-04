"""A deterministic check gate for a local worker model.

Drives the worker CLI in a loop: run the worker with a brief, run a
build/check command, and if the check fails, re-run the worker with the
trimmed error lines, up to a round limit.  No AI is involved in the loop
itself -- the loop is plain shell commands and file diffs.  The gate
finishes with status PASS (the check passed) or STUCK (round limit, a
repeated error signature, or a dead job).

Usage:
    gate.py --brief BRIEF.md --check "make check" [options]

Output is written under --out (default: a qwen-gate directory in the
system temp area): per-round prompt files, per-round check logs, a
rounds.jsonl event log, and a final result.json.
"""

import argparse
import hashlib
import json
import os
import pathlib
import re
import subprocess
import sys
import tempfile
import time


def extract_error_lines(text, limit=30, width=300):
    """Return the interesting lines of a failing check's output.

    Keep the lines matching the known error markers (case sensitive),
    including javac's indented ``symbol:`` and ``location:`` detail
    lines that follow a "cannot find symbol" error; if none match,
    fall back to the last `limit` non-empty lines.
    At most `limit` lines (the first ones) are kept, each cut to `width`
    characters with trailing whitespace removed.
    """
    pattern = re.compile(
        r"error:|FAILED|What went wrong|Exception|AssertionFailedError|expected:|BUILD FAILED|symbol:|location:"
    )
    lines = text.splitlines()
    matched = [line for line in lines if pattern.search(line)]
    if matched:
        picked = matched
    else:
        picked = [line for line in lines if line.strip()][-limit:]
    return [line[:width].rstrip() for line in picked[:limit]]


def signature(lines):
    """Return a stable digest of error lines.

    Normalises every ``:<digits>`` to ``:N`` and every run of whitespace
    to one space, joins with a newline, and returns the sha1 hex digest
    of the UTF-8 bytes.  An empty list gives the sha1 of the empty
    string.
    """
    normalised = []
    for line in lines:
        line = re.sub(r":\d+", ":N", line)
        line = re.sub(r"\s+", " ", line)
        normalised.append(line)
    return hashlib.sha1("\n".join(normalised).encode("utf-8")).hexdigest()


def files_in(lines):
    """Return the .java file paths named in the lines, in first-seen order.

    A path is any run of ``[A-Za-z0-9_./\\-]+`` ending in ``.java``.
    If a path contains ``src/``, it is cut to start at ``src/``.
    """
    pattern = re.compile(r"[A-Za-z0-9_./\\-]+\.java")
    seen = set()
    out = []
    for line in lines:
        for match in pattern.findall(line):
            if "src/" in match:
                match = match[match.index("src/"):]
            if match not in seen:
                seen.add(match)
                out.append(match)
    return out


def snapshot(root, patterns):
    """Map relative path to file bytes for every file matching the globs.

    For each pattern, glob under `root` and collect regular files only.
    A pattern ending with ``/**`` is globbed as the pattern with the
    ``/**`` replaced by ``/**/*`` so nested directories are found.  Keys
    are paths relative to `root` with forward slashes.
    """
    root_path = _root_path(root)
    result = {}
    for pattern in patterns:
        if pattern.endswith("/**"):
            pattern = pattern[:-3] + "/**/*"
        for path in root_path.glob(pattern):
            if path.is_file():
                relative = path.relative_to(root_path).as_posix()
                result[relative] = path.read_bytes()
    return result


def restore(root, before, patterns):
    """Put files back to their pre-worker state; return what was fixed.

    Takes a new snapshot with the same patterns.  Files whose bytes
    differ from `before` are rewritten with the old bytes; files that are
    new (not in `before`) are deleted; files in `before` that are gone
    are recreated (parent directories included).  Returns the sorted
    list of relative paths that had to be fixed; empty means untouched.
    """
    root_path = _root_path(root)
    after = snapshot(root, patterns)
    fixed = []
    for relative, new_bytes in after.items():
        old_bytes = before.get(relative)
        if old_bytes is None:
            root_path.joinpath(*relative.split("/")).unlink()
            fixed.append(relative)
        elif new_bytes != old_bytes:
            path = root_path.joinpath(*relative.split("/"))
            path.write_bytes(old_bytes)
            fixed.append(relative)
    for relative, old_bytes in before.items():
        if relative not in after:
            path = root_path.joinpath(*relative.split("/"))
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(old_bytes)
            fixed.append(relative)
    return sorted(fixed)


def build_fix_prompt(brief, round_no, lines, files, touched):
    """Build the prompt for a fix round after a failed check.

    The original brief comes first, then a FIX ROUND section telling the
    worker to fix only the named errors, optionally listing the files
    named in the errors and the protected files whose changes were
    undone, and ending with the error lines in a fenced block.
    """
    parts = [
        brief,
        "",
        "FIX ROUND %d: the build check failed after your last edit." % round_no,
        "Fix only the errors below. Read the named files first, change the "
        "smallest amount of code that removes the errors, and do not edit any "
        "test file unless the original task above told you to.",
    ]
    if files:
        parts.append("Files named in the errors:")
        for path in files:
            parts.append("- %s" % path)
    if touched:
        parts.append(
            "Your last edit changed protected files and those changes were "
            "undone: %s" % ", ".join(touched)
        )
    parts.append("Errors:")
    parts.append("```")
    parts.extend(lines)
    parts.append("```")
    return "\n".join(parts)


def parse_job_output(text):
    """Parse the output of ``wait <job id>`` into job facts.

    The first line is ``job <id>: <state>``.  The meta line (the last
    line starting with ``[qwen worker``) carries ``turns=N``, ``out=N
    tok``, ``Ns run`` and the final outcome word.  Missing meta values
    default to 0 and the outcome word defaults to "unknown".  If the
    text is empty or has no ``job ... :`` first line the state is
    "died".  The reply is the text between the first line and the meta
    line, stripped.
    """
    result = {"state": "died", "turns": 0, "out_tokens": 0, "secs": 0,
              "outcome": "unknown", "reply": ""}
    lines = text.splitlines()
    if not lines:
        return result
    first = lines[0].strip()
    match = re.match(r"job \S+?:\s+(\S+)", first)
    if not match:
        return result
    result["state"] = match.group(1)
    meta_index = None
    for index in range(len(lines) - 1, -1, -1):
        if lines[index].strip().startswith("[qwen worker"):
            meta_index = index
            break
    if meta_index is not None:
        meta = lines[meta_index]
        turns = re.search(r"turns=(\d+)", meta)
        tokens = re.search(r"out=(\d+) tok", meta)
        secs = re.search(r"(\d+)s run", meta)
        if turns:
            result["turns"] = int(turns.group(1))
        if tokens:
            result["out_tokens"] = int(tokens.group(1))
        if secs:
            result["secs"] = int(secs.group(1))
        outcome = re.search(r"\|\s*([A-Za-z0-9_]+)\s*\]", meta)
        if outcome:
            result["outcome"] = outcome.group(1)
        result["reply"] = "\n".join(lines[1:meta_index]).strip()
    else:
        result["reply"] = "\n".join(lines[1:]).strip()
    return result


def run_check(cmd, cwd, timeout, log_path):
    """Run the check command and record its output.

    Runs `cmd` as a shell in `cwd` with the given timeout, joins stdout
    and stderr, writes the text to `log_path` (creating parent
    directories), and returns the tuple ``(returncode, text)``.  On
    timeout returns ``(124, "check timed out after <timeout>s")`` and
    writes that to the log too.
    """
    try:
        proc = subprocess.run(
            cmd, shell=True, cwd=cwd, capture_output=True, text=True,
            timeout=timeout,
        )
        text = proc.stdout + proc.stderr
    except subprocess.TimeoutExpired:
        text = "check timed out after %ds" % timeout
        proc = None
    log_dir = os.path.dirname(log_path)
    if log_dir:
        os.makedirs(log_dir, exist_ok=True)
    with open(log_path, "w", encoding="utf-8") as handle:
        handle.write(text)
    returncode = 124 if proc is None else proc.returncode
    return (returncode, text)


def main(argv):
    """Run the gate loop over the rounds and report PASS or STUCK.

    Returns 0 for PASS, 1 for STUCK, and 2 for a usage or setup error
    (missing brief file, bad arguments), printing the reason to stderr.
    Progress goes to stderr one line per step, prefixed ``[gate] ``;
    stdout gets exactly one final line.
    """
    parser = argparse.ArgumentParser(prog="gate")
    parser.add_argument("--brief", required=True)
    parser.add_argument("--check", required=True)
    parser.add_argument("--cwd", default=".")
    parser.add_argument("--max-rounds", type=int, default=3)
    parser.add_argument("--mode", default="edit")
    parser.add_argument("--max-turns", type=int, default=30)
    parser.add_argument("--job-timeout", type=int, default=1200)
    parser.add_argument("--check-timeout", type=int, default=1800)
    parser.add_argument("--protect", action="append",
                        default=["src/test/**"])
    parser.add_argument("--allow-protected-edits", action="store_true")
    parser.add_argument("--error-lines", type=int, default=30)
    parser.add_argument("--thinking-from-round", type=int, default=0)
    parser.add_argument(
        "--out",
        default=os.path.join(tempfile.gettempdir(), "qwen-gate",
                             time.strftime("%Y%m%d-%H%M%S")),
    )
    args = parser.parse_args(argv)

    try:
        with open(args.brief, "r", encoding="utf-8") as handle:
            brief = handle.read()
    except OSError as exc:
        print("[gate] setup error: %s" % exc, file=sys.stderr)
        return 2

    os.makedirs(args.out, exist_ok=True)
    cli = os.environ.get(
        "QWEN_AGENTS_CLI",
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "qwen_cli.py"),
    )
    protect = [] if args.allow_protected_edits else args.protect
    cwd = args.cwd

    last_lines = []
    prev_sig = None
    last_reply = ""
    touched = []

    for round_no in range(1, args.max_rounds + 1):
        prompt = brief
        snap = None
        if round_no > 1:
            if protect:
                snap = snapshot(cwd, protect)
            prompt = build_fix_prompt(
                brief, round_no, last_lines, files_in(last_lines), touched,
            )
        touched = []

        prompt_path = os.path.join(args.out, "round-%d.prompt.md" % round_no)
        with open(prompt_path, "w", encoding="utf-8") as handle:
            handle.write(prompt)

        start_args = [
            sys.executable, cli, "start",
            "--prompt-file", prompt_path,
            "--cwd", cwd,
            "--mode", args.mode,
            "--max-turns", str(args.max_turns),
            "--timeout-s", str(args.job_timeout),
        ]
        if args.thinking_from_round and round_no >= args.thinking_from_round:
            start_args.append("--thinking")
        try:
            started = subprocess.run(
                start_args, capture_output=True, text=True,
            )
        except OSError as exc:
            print("[gate] start-failed: %s" % exc, file=sys.stderr)
            return 2
        if started.returncode != 0:
            print("[gate] round %d start failed: %s"
                  % (round_no, started.stderr.strip()), file=sys.stderr)
            _finish("STUCK", "start-failed", round_no, args.out,
                    last_reply, last_lines)
            return 1
        job_id = started.stdout.strip().splitlines()[-1].strip() \
            if started.stdout.strip() else "unknown"

        print("[gate] round %d: waiting for job %s" % (round_no, job_id),
              file=sys.stderr)
        try:
            waited = subprocess.run(
                [sys.executable, cli, "wait", job_id],
                capture_output=True, text=True,
                timeout=args.job_timeout + 120,
                env=dict(os.environ,
                         QWEN_WAIT_TIMEOUT_S=str(args.job_timeout + 60)),
            )
        except subprocess.TimeoutExpired:
            waited = None
        if waited is None:
            parsed = {"state": "timeout", "turns": 0, "out_tokens": 0,
                      "secs": 0, "outcome": "unknown", "reply": ""}
        else:
            parsed = parse_job_output(waited.stdout)
        last_reply = parsed["reply"]
        print("[gate] round %d: job %s state=%s outcome=%s"
              % (round_no, job_id, parsed["state"], parsed["outcome"]),
              file=sys.stderr)

        if parsed["state"] in ("failed", "cancelled", "timeout", "died"):
            print("[gate] stuck: job-%s" % parsed["state"], file=sys.stderr)
            _finish("STUCK", "job-" + parsed["state"], round_no, args.out,
                    last_reply, last_lines)
            return 1

        if round_no > 1 and protect and snap is not None:
            touched = restore(cwd, snap, protect)
            if touched:
                print("[gate] round %d: restored %d protected file(s)"
                      % (round_no, len(touched)), file=sys.stderr)

        log_path = os.path.join(args.out, "round-%d.check.log" % round_no)
        print("[gate] round %d: running check" % round_no, file=sys.stderr)
        exit_code, text = run_check(
            args.check, cwd, args.check_timeout, log_path,
        )
        print("[gate] round %d: check exited %d" % (round_no, exit_code),
              file=sys.stderr)

        if exit_code == 0:
            lines = []
            sig = signature(lines)
            _record(args.out, round_no, job_id, parsed, exit_code, lines,
                    sig, touched, round_no >= args.thinking_from_round
                    and args.thinking_from_round > 0)
            print("[gate] PASS at round %d" % round_no, file=sys.stderr)
            _finish("PASS", "", round_no, args.out, last_reply, last_lines)
            return 0

        lines = extract_error_lines(text, args.error_lines)
        sig = signature(lines)
        _record(args.out, round_no, job_id, parsed, exit_code, lines, sig,
                touched, round_no >= args.thinking_from_round
                and args.thinking_from_round > 0)
        if prev_sig is not None and sig == prev_sig:
            print("[gate] stuck: repeat", file=sys.stderr)
            _finish("STUCK", "repeat", round_no, args.out, last_reply, lines)
            return 1
        last_lines = lines
        prev_sig = sig
    # (a passing round is recorded in rounds.jsonl before PASS)

    print("[gate] stuck: max-rounds", file=sys.stderr)
    _finish("STUCK", "max-rounds", args.max_rounds, args.out, last_reply,
            last_lines)
    return 1


def _finish(status, reason, rounds, out, last_reply, last_errors):
    """Write result.json and print the single final stdout line."""
    with open(os.path.join(out, "result.json"), "w", encoding="utf-8") as handle:
        json.dump(
            {
                "status": status,
                "reason": reason,
                "rounds": rounds,
                "out": out,
                "last_reply": last_reply,
                "last_errors": last_errors,
            },
            handle,
            indent=2,
        )
        handle.write("\n")
    if status == "PASS":
        print("PASS rounds=%d out=%s" % (rounds, out))
    else:
        print("STUCK(%s) rounds=%d out=%s" % (reason, rounds, out))


def _record(out, round_no, job, parsed, check_exit, lines, sig, touched,
            thinking):
    """Append one JSON line for this round to rounds.jsonl."""
    record = {
        "round": round_no,
        "job": job,
        "state": parsed["state"],
        "outcome": parsed["outcome"],
        "turns": parsed["turns"],
        "out_tokens": parsed["out_tokens"],
        "secs": parsed["secs"],
        "check_exit": check_exit,
        "error_count": len(lines),
        "signature": sig,
        "touched": touched,
        "thinking": bool(thinking),
    }
    with open(os.path.join(out, "rounds.jsonl"), "a", encoding="utf-8") as handle:
        handle.write(json.dumps(record) + "\n")


def _root_path(root):
    """Return the pathlib root used for snapshotting."""
    return pathlib.Path(root)


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
