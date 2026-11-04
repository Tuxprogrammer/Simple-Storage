"""A deterministic pre-check for known-bad names in Java sources.

Finds names from newer-Minecraft or newer-Java APIs in the sources so
the gate catches them before the compiler runs.  The list of known-bad
names comes from docs/agent-setup/slips.tsv, one row per name:
``<Java regex><TAB><advice>``; lines starting with ``#`` are comments and
rows without a TAB are skipped.

Usage:
    namecheck.py [--root DIR] [--slips FILE] [--subdir SUBDIR] [--limit N]

Every finding line starts with ``error:`` so the gate's error filter
keeps it.  Exit codes: 0 clean, 1 findings, 2 slips file missing.
"""

import argparse
import os
import re
import sys


def load_slips(path):
    """Read a slips file into a list of (compiled_regex, advice).

    Lines starting with ``#`` are comments, blank lines are ignored, and
    rows without a TAB are skipped.  Returns the list of
    ``(re.compile(regex), advice)`` tuples in file order.
    """
    slips = []
    with open(path, "r", encoding="utf-8") as handle:
        for line in handle:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            if line.lstrip().startswith("#"):
                continue
            if "\t" not in line:
                continue
            regex, advice = line.split("\t", 1)
            slips.append((re.compile(regex), advice.strip()))
    return slips


def is_code_line(line):
    """True unless the line is a comment line.

    False when the stripped line starts with ``//``, ``*``, or ``/*``;
    True otherwise.
    """
    stripped = line.strip()
    if stripped.startswith("//") or stripped.startswith("*") \
            or stripped.startswith("/*"):
        return False
    return True


def scan_text(text, slips, name):
    """Scan source text against the slips and return finding lines.

    One entry per (line, slip) match on code lines, formatted exactly:
    ``error: {name}:{lineno}: {matched_text} -> {advice}`` where lineno
    starts at 1 and matched_text is match.group(0).  A line containing
    ``namecheck:ignore`` is skipped entirely, and a trailing ``// ...``
    comment is cut from the line before matching.
    """
    findings = []
    for lineno, line in enumerate(text.splitlines(), start=1):
        if "namecheck:ignore" in line:
            continue
        if not is_code_line(line):
            continue
        cut = line.find("//")
        if cut != -1:
            line = line[:cut]
        for regex, advice in slips:
            for match in regex.finditer(line):
                findings.append(
                    "error: %s:%d: %s -> %s"
                    % (name, lineno, match.group(0), advice)
                )
    return findings


def scan_tree(root, slips, subdir="src"):
    """Walk root/subdir for *.java files and scan each one.

    Files are visited in sorted path order; each file is scanned with
    name = the path relative to root using ``/`` separators.  Returns
    the findings of all files in walk order.
    """
    findings = []
    base = os.path.join(root, subdir)
    if not os.path.isdir(base):
        return findings
    for dirpath, dirnames, filenames in os.walk(base):
        dirnames.sort()
        for filename in sorted(filenames):
            if not filename.endswith(".java"):
                continue
            full_path = os.path.join(dirpath, filename)
            relative = os.path.relpath(full_path, root).replace(os.sep, "/")
            with open(full_path, "r", encoding="utf-8") as handle:
                text = handle.read()
            findings.extend(scan_text(text, slips, relative))
    return findings


def main(argv):
    """Run namecheck and report findings.

    Returns 0 with a ``namecheck: clean`` line when nothing matched, 1
    with the finding lines (at most --limit, then ``... and N more``)
    when something matched, and 2 with ``error: slips file not found:
    PATH`` when the slips file is missing.
    """
    parser = argparse.ArgumentParser(prog="namecheck")
    parser.add_argument("--root", default=".")
    parser.add_argument("--slips", default=None)
    parser.add_argument("--subdir", default="src")
    parser.add_argument("--limit", type=int, default=30)
    args = parser.parse_args(argv)
    if args.slips is None:
        args.slips = os.path.join(args.root, "docs", "agent-setup",
                                  "slips.tsv")

    if not os.path.isfile(args.slips):
        print("error: slips file not found: %s" % args.slips)
        return 2

    slips = load_slips(args.slips)
    findings = scan_tree(args.root, slips, args.subdir)
    for line in findings[:args.limit]:
        print(line)
    if len(findings) > args.limit:
        print("... and %d more" % (len(findings) - args.limit))
    if findings:
        return 1
    print("namecheck: clean")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
