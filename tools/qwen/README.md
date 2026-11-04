# qwen check gate

A deterministic loop around the local qwen worker: run the worker with a brief, run a build or test command, and when it fails send the trimmed error lines back to the worker, up to a round limit. No model sits in the loop, so compiler output and re-briefs never enter an orchestrating agent's context. The orchestrator writes the brief, starts the gate in the background and reads one result line.

| File | What it is |
|---|---|
| `gate.py` | The gate: `python3 tools/qwen/gate.py --brief FILE --check CMD [options]` |
| `qwen_cli.py` | `start` and `wait` command line front for the worker plugin's dispatcher (the plugin has no `start` of its own) |
| `fake_qwen.py` | Scripted stand-in for the worker, used by the tests |
| `namecheck.py` | Pre-check: greps `src/**/*.java` for the known-bad names in `docs/agent-setup/slips.tsv` (newer-Minecraft and newer-Java names), prints `error:` lines, exit 1 on any. Silence a legitimate hit with `// namecheck:ignore` |
| `apiindex.py` | `build` a TSV index of public members from `javap`, `find` real names for a wrong one, `check` a class exists. Built on the host by `tools/remote/api-index.sh` into the git-ignored `reference/api/index.tsv` |
| `test_namecheck.py`, `test_apiindex.py`, `test_gate.py` | `python3 -m unittest tools/qwen/test_gate.py` (plain Python, no Gradle, no model) |

## Use

```sh
python3 tools/qwen/gate.py \
    --brief briefs/my-change.md \
    --check 'tools/remote/work.sh run compileJava compileTestJava' \
    --max-rounds 3
```

Run it with `run_in_background` and wait for the notification. The last stdout line is the verdict:

- `PASS rounds=N out=<dir>`: the check exited 0 (exit code 0).
- `STUCK(<reason>) rounds=N out=<dir>`: exit code 1. Reasons: `max-rounds`, `repeat` (the same normalized error twice in a row), `job-<state>` (the worker failed, timed out or died), `start-failed`.
- Exit code 2: usage or setup error.

`<dir>` holds `result.json` (status, reason, rounds, last worker reply, last error lines), `rounds.jsonl` (one line per round: job, turns, output tokens, seconds, check exit, error count, signature, touched protected files, thinking) and per-round `round-N.prompt.md` and `round-N.check.log`. On STUCK, read `result.json` and decide whether the brief needs to change; do not patch the files by hand.

## Rules the gate enforces

- **Protected files.** From round 2 on, files matching `--protect` (default `src/test/**`) are snapshotted before each fix round and restored afterwards, and the next prompt says so. This stops the worker from passing a check by weakening tests. `--allow-protected-edits` turns it off for tasks that are about tests.
- **No loops on the same error.** The error signature ignores line numbers and whitespace; two equal signatures in a row stop the run.
- **A worker that ends with an error outcome** (for example `error_max_turns`) still gets its files checked; only failed, cancelled, timed out or died jobs stop the run.
- **Thinking** stays off. `--thinking-from-round N` turns it on from round N, for the case where a logic error survives a plain retry.

## Briefs by file

The worker can read an absolute path, so a brief can live in a file and the `qwen_start` prompt can say "read this file and do what it says". The gate does that for you: `--brief` is read and passed through `--prompt-file`.

## Settings

`QWEN_AGENTS_CLI` selects the worker CLI (default: `qwen_cli.py` next to `gate.py`; the tests point it at `fake_qwen.py`). `QWEN_AGENTS_SERVER` selects the dispatcher script `qwen_cli.py` imports (default under the user's `~/.claude/local-plugins/qwen-agents/server/`).

## Cheap checks first, API hints on failure

Chain the pre-check before the build so a wrong 1.7.10 name costs one second, not a build:

```sh
python3 tools/qwen/gate.py --brief FILE \
    --check 'python3 tools/qwen/namecheck.py && tools/remote/work.sh test' \
    --api-index reference/api/index.tsv
```

With `--api-index`, each fix prompt gets a "Real names from the API index" section for every `symbol:` the compiler could not find (up to 3 candidates per symbol, no model involved). Briefs should also tell the worker it may grep the index file. Regenerate the index with `tools/remote/api-index.sh` when the pack or the dependency pins change. When a mistake shows up twice, add a row to `docs/agent-setup/slips.tsv` and to `forge-1710-names.md`.
