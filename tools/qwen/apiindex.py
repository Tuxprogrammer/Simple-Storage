"""A searchable index of the public API of the jars the mod compiles against.

The index is built from ``javap`` output and written as one TSV line per
member: "<fully.qualified.Class><TAB><kind><TAB><member signature>" where
kind is one of class, field, method, ctor.  A ``find`` subcommand suggests
real API names for a wrong one, ranked exact > prefix > substring > fuzzy
(difflib ratio >= 0.75).

Usage:
    apiindex.py build --out FILE --jar JAR [--jar JAR ...]
                      [--package-prefix P ...] [--javap javap]
    apiindex.py find --index FILE QUERY [--limit N]
    apiindex.py check --index FILE CLASS

Only the Python 3 standard library is used, plus the ``javap`` executable
for the build subcommand.
"""

import argparse
import difflib
import os
import re
import subprocess
import sys
import zipfile

DEFAULT_PREFIXES = [
    "net.minecraft.",
    "net.minecraftforge.",
    "cpw.mods.fml.",
    "gregtech.",
    "ic2.",
    "codechicken.",
    "appeng.",
]

JAVAP_BATCH = 200


def parse_javap(text):
    """Parse the output of ``javap -public -cp JAR CLASS...``.

    Returns a list of (class_name, kind, signature) tuples.  The header
    line of a class (ending in "{" and containing " class ", " interface ",
    " enum " or " @interface ") starts a class: its name is the word after
    that keyword, cut at "<" if generic.  Inside the class, a line ending
    in ";" is a member: "ctor" when the name before "(" equals the simple
    name of the current class, "method" when it has a "(", otherwise
    "field".  A line "}" ends the class.  Everything else is ignored.
    """
    rows = []
    current = None
    simple = None
    for raw in text.splitlines():
        line = raw.strip()
        if not line:
            continue
        if current is not None and line == "}":
            current = None
            simple = None
            continue
        if line.endswith("{"):
            for keyword in (" class ", " interface ", " enum ", " @interface "):
                if keyword in line:
                    name = line.split(keyword, 1)[1].split(None, 1)[0]
                    if "<" in name:
                        name = name.split("<", 1)[0]
                    current = name
                    simple = simple_name(name)
                    rows.append((name, "class",
                                 line.rsplit("{", 1)[0].strip()))
                    break
            continue
        if current is None or not line.endswith(";"):
            continue
        signature = line[:-1].strip()
        if "(" in signature:
            call_name = signature.rsplit("(", 1)[0].split()[-1]
            kind = "ctor" if call_name.rsplit(".", 1)[-1] == simple \
                else "method"
        else:
            kind = "field"
        rows.append((current, kind, signature))
    return rows


def member_name(kind, signature):
    """Return the comparable name of a row's signature.

    For method and ctor: the identifier just before the first "(".  For
    field: the last identifier of the signature.  For class: the simple
    class name (last word before " extends"/" implements"/end, cut at
    "<", part after the last ".").
    """
    if kind == "ctor":
        return simple_name(signature.split("(", 1)[0].split()[-1])
    if kind == "method":
        return signature.split("(", 1)[0].split()[-1]
    if kind == "field":
        return signature.split()[-1]
    words = []
    for word in signature.split():
        if word in ("extends", "implements"):
            break
        words.append(word)
    name = words[-1] if words else ""
    if "<" in name:
        name = name.split("<", 1)[0]
    return name.rsplit(".", 1)[-1]


def simple_name(class_name):
    """Return the simple name: last dotted segment, then last '$'-segment.

    "a.b.Outer$Inner" -> "Inner".
    """
    last = class_name.rsplit(".", 1)[-1]
    return last.rsplit("$", 1)[-1]


def format_line(class_name, kind, signature):
    """Return the TSV line for one index entry."""
    return class_name + "\t" + kind + "\t" + signature


def parse_line(line):
    """Parse one TSV line into (class_name, kind, signature).

    Returns None when the line does not have exactly three tab-separated
    fields.
    """
    fields = line.split("\t")
    if len(fields) != 3:
        return None
    return (fields[0], fields[1], fields[2])


def load_index(path):
    """Load an index file, returning the parsed tuples, skipping bad lines."""
    rows = []
    with open(path) as handle:
        for line in handle:
            line = line.rstrip("\n")
            if not line:
                continue
            row = parse_line(line)
            if row is not None:
                rows.append(row)
    return rows


def lookup(index, query, limit=10):
    """Rank index rows for a query, best first.

    Ranks: (1) simple/member name equals the query exactly, (2) name
    starts with the query, (3) name contains the query, (4) difflib
    SequenceMatcher ratio >= 0.75.  Comparison is case-insensitive.  Within
    a rank the index order is kept; display lines are de-duplicated and
    cut to `limit`.
    """
    q = query.lower()
    buckets = [[], [], [], []]
    for entry in index:
        name = member_name(entry[1], entry[2]).lower()
        if name == q:
            rank = 0
        elif name.startswith(q):
            rank = 1
        elif q in name:
            rank = 2
        elif difflib.SequenceMatcher(None, name, q).ratio() >= 0.75:
            rank = 3
        else:
            continue
        buckets[rank].append(entry)
    lines = []
    seen = set()
    for bucket in buckets:
        for entry in bucket:
            line = format_display(entry)
            if line not in seen:
                seen.add(line)
                lines.append(line)
        if len(lines) >= limit:
            break
    return lines[:limit]


def format_display(entry):
    """Return the two-space-separated display line for an index entry."""
    return entry[0] + "  " + entry[1] + "  " + entry[2]


def list_jar_classes(jar):
    """Return the jar's entry names ending in ".class", in zip order.

    Uses Python's zipfile module so the build does not need an ``unzip``
    executable.
    """
    with zipfile.ZipFile(jar) as zf:
        return [name for name in zf.namelist()
                if name.endswith(".class")]


def _jar_classes(jar):
    """Return the dotted class names of the .class entries of a jar.

    Entries containing a "$" with a digit right after the dollar (anonymous
    inner classes) are skipped.
    """
    names = []
    for line in list_jar_classes(jar):
        line = line.strip()
        if not line.endswith(".class"):
            continue
        stem = line[:-len(".class")]
        if re.search(r"\$\d", stem):
            continue
        names.append(stem.replace("/", "."))
    return names


def main(argv):
    """Run the CLI.  Returns the process exit code."""
    parser = argparse.ArgumentParser(prog="apiindex.py")
    sub = parser.add_subparsers(dest="command")

    build = sub.add_parser("build")
    build.add_argument("--out", required=True)
    build.add_argument("--jar", action="append", required=True)
    build.add_argument("--package-prefix", action="append", default=None)
    build.add_argument("--javap", default="javap")

    find = sub.add_parser("find")
    find.add_argument("--index", required=True)
    find.add_argument("query")
    find.add_argument("--limit", type=int, default=10)

    check = sub.add_parser("check")
    check.add_argument("--index", required=True)
    check.add_argument("cls", metavar="CLASS")

    args = parser.parse_args(argv)
    if args.command is None:
        parser.print_usage()
        return 2

    if args.command == "build":
        prefixes = args.package_prefix if args.package_prefix \
            else DEFAULT_PREFIXES
        classes = []
        for jar in args.jar:
            for name in _jar_classes(jar):
                if any(name.startswith(prefix) for prefix in prefixes):
                    classes.append(name)
        classes = sorted(set(classes))
        rows = set()
        for i in range(0, len(classes), JAVAP_BATCH):
            batch = classes[i:i + JAVAP_BATCH]
            proc = subprocess.run(
                [args.javap, "-public", "-cp", args.jar[0]] + batch,
                capture_output=True, text=True)
            for row in parse_javap(proc.stdout):
                rows.add(format_line(*row))
        lines = sorted(rows)
        with open(args.out, "w") as handle:
            for line in lines:
                handle.write(line + "\n")
        n_classes = len({line.split("\t", 2)[0]
                         for line in rows if line.split("\t", 2)[1] == "class"})
        print("index: %d lines, %d classes -> %s"
              % (len(lines), n_classes, args.out))
        return 0

    if args.command == "find":
        if not os.path.exists(args.index):
            print("error: index not found: " + args.index +
                  " (build it with tools/remote/api-index.sh)")
            return 2
        index = load_index(args.index)
        results = lookup(index, args.query, limit=args.limit)
        for line in results:
            print(line)
        return 0 if results else 1

    if args.command == "check":
        if not os.path.exists(args.index):
            print("error: index not found: " + args.index +
                  " (build it with tools/remote/api-index.sh)")
            return 2
        index = load_index(args.index)
        members = [row for row in index
                   if row[0] == args.cls and row[1] != "class"]
        if members:
            print(len(members))
            return 0
        print("error: class not in index: " + args.cls)
        return 1

    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
