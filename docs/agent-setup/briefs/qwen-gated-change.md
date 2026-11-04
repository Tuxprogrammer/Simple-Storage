<!-- Brief template for a qwen worker, run through tools/qwen/gate.py. Save the filled copy outside the repo (scratchpad) and pass it with --brief. Do not paste compiler output into it: the gate adds that itself. -->

# Task: <one change, one sentence>

Use only the Read and Write tools. Work in <abs repo path>. Create or edit exactly these files and no others:
- <abs path 1>
- <abs path 2> (its test)

## Read first (data, do not edit)
- <abs path of the one spec sheet or class this change builds on>
- <abs path of docs/agent-setup/forge-1710-names.md> (1.7.10 names; the slips table is in docs/agent-setup/slips.tsv)
- API lookups: `python3 <abs path of tools/qwen/apiindex.py> find --index <abs index path> NAME` lists the real class and member names (needs Bash; if you only have Read, grep the index file).

## Exact interface
<class names, method signatures, constants, NBT keys, packet fields, in a table; no prose the worker has to interpret>

## Tests (written with the code)
<test name -> what it asserts, with expected values you derived and checked; tests are protected after round 1>

## Rules that always apply
Java 8 API only; American English; Javadoc and a `// Spec:` comment on each class; every player-visible string a lang key; no machine paths in any file; `// namecheck:ignore` only with a reason.

Reply with one line when the files are written.

<!-- Run: python3 tools/qwen/gate.py --brief <file> --check '<namecheck && tools/remote/work.sh test>' --max-rounds 3  (run_in_background; read only the verdict line) -->
