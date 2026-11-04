#!/usr/bin/env python3
"""Test double for the qwen worker CLI.

Implements the same two subcommands as the real worker (`start` and
`wait`) but is driven entirely by a JSON script held at the path named
by the FAKE_QWEN_SCRIPT environment variable, so tests can run the gate
loop without a model.

Environment variables:

  FAKE_QWEN_SCRIPT
      Path to a JSON file holding a list; element i describes what the
      worker does in its (i+1)-th job. Each element is an object with
      optional keys: `write` (object mapping a path relative to the
      job's cwd to the text to write; parent directories are created),
      `delete` (list of relative paths to remove if they exist),
      `state` (default "done"), `outcome` (default "success") and
      `reply` (default "ok"). If there are more jobs than elements,
      the last element is repeated.

  FAKE_QWEN_STATE
      Path to a JSON state file holding
      {"jobs": [{"id", "cwd", "prompt_file", "applied", "round"}, ...]}.
      It is created when missing and is used to count jobs across
      separate process runs.
"""

import json
import os
import sys

DEFAULT_STATE = "done"
DEFAULT_OUTCOME = "success"
DEFAULT_REPLY = "ok"

START_FLAGS_WITH_VALUE = (
    "--prompt-file",
    "--cwd",
    "--mode",
    "--max-turns",
    "--timeout-s",
    "--max-output-tokens",
)


def load_json(path, default):
    """Load a JSON document from path, returning default when missing or invalid."""
    try:
        with open(path, "r", encoding="utf-8") as handle:
            return json.load(handle)
    except (OSError, ValueError):
        return default


def save_json(path, document):
    """Write a JSON document to path, creating parent directories if needed."""
    parent = os.path.dirname(path)
    if parent and not os.path.isdir(parent):
        os.makedirs(parent, exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(document, handle, indent=2)
        handle.write("\n")


def load_script():
    """Load the JSON driver script, treating a missing file as an empty list."""
    script_path = os.environ.get("FAKE_QWEN_SCRIPT", "")
    script = load_json(script_path, []) if script_path else []
    return script if isinstance(script, list) else []


def script_element(script, n):
    """Return the script element for job number n, repeating the last if exhausted."""
    if not script:
        return {}
    return script[min(n, len(script)) - 1]


def apply_element(element, cwd):
    """Perform the write and delete entries of a script element, relative to cwd."""
    for rel, text in (element.get("write") or {}).items():
        target = os.path.join(cwd, rel)
        parent = os.path.dirname(target)
        if parent and not os.path.isdir(parent):
            os.makedirs(parent, exist_ok=True)
        with open(target, "w", encoding="utf-8") as handle:
            handle.write(text)
    for rel in element.get("delete") or []:
        target = os.path.join(cwd, rel)
        if os.path.lexists(target):
            os.remove(target)


def load_state(state_path):
    """Load the state file, creating it with an empty job list when missing."""
    state = load_json(state_path, None)
    if not isinstance(state, dict) or not isinstance(state.get("jobs"), list):
        state = {"jobs": []}
        save_json(state_path, state)
    return state


def run_start(argv):
    """Implement the `start` subcommand; return the process exit code."""
    values = {}
    thinking = False
    i = 0
    while i < len(argv):
        arg = argv[i]
        if arg == "--thinking":
            thinking = True
        elif arg in START_FLAGS_WITH_VALUE:
            if i + 1 >= len(argv):
                print("start: flag %s needs a value" % arg, file=sys.stderr)
                return 2
            values[arg] = argv[i + 1]
            i += 1
        else:
            print("start: unknown flag %s" % arg, file=sys.stderr)
            return 2
        i += 1

    if "--prompt-file" not in values:
        print("start: missing --prompt-file", file=sys.stderr)
        return 2
    if "--cwd" not in values:
        print("start: missing --cwd", file=sys.stderr)
        return 2

    state_path = os.environ.get("FAKE_QWEN_STATE", "")
    state = load_state(state_path)
    n = len(state["jobs"]) + 1
    job_id = "fake-%d" % n
    element = script_element(load_script(), n)
    cwd = values["--cwd"]
    apply_element(element, cwd)
    state["jobs"].append({
        "id": job_id,
        "cwd": cwd,
        "prompt_file": values["--prompt-file"],
        "applied": True,
        "round": 1,
        "thinking": thinking,
        "state": element.get("state", DEFAULT_STATE),
        "outcome": element.get("outcome", DEFAULT_OUTCOME),
        "reply": element.get("reply", DEFAULT_REPLY),
    })
    save_json(state_path, state)
    print(job_id)
    return 0


def run_wait(argv):
    """Implement the `wait` subcommand; return the process exit code."""
    if len(argv) != 1:
        print("no such job %s" % (argv[0] if argv else ""), file=sys.stdout)
        return 1
    job_id = argv[0]
    state_path = os.environ.get("FAKE_QWEN_STATE", "")
    state = load_json(state_path, {"jobs": []})
    for job in state.get("jobs", []):
        if job.get("id") == job_id:
            print("job %s: %s" % (job_id, job.get("state", DEFAULT_STATE)))
            print()
            print(job.get("reply", DEFAULT_REPLY))
            print()
            print("[qwen worker %s | no-think | mode=edit | turns=3 | "
                  "out=100 tok | 1s run, 0s queued | %s]"
                  % (job_id, job.get("outcome", DEFAULT_OUTCOME)))
            print("=" * 60)
            return 0
    print("no such job %s" % job_id)
    return 1


def main(argv):
    """Dispatch to the start or wait subcommand; return the process exit code."""
    if len(argv) >= 1 and argv[0] == "start":
        return run_start(argv[1:])
    if len(argv) >= 1 and argv[0] == "wait":
        return run_wait(argv[1:])
    print("fake_qwen: expected subcommand start or wait", file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
