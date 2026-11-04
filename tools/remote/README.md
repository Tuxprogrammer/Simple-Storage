# Remote builds and runs

Builds, tests and Minecraft runs belong on a shared build host, not on a development laptop. [`remote.sh`](remote.sh) drives that host over ssh: it checks out a commit into a numbered **slot**, builds it, boots the dev server, runs the real GTNH server and client, and copies logs and screenshots back into the git-ignored `reference/remote/`. Nothing about the host is committed.

## Setup

1. Copy [`.env.example`](../../.env.example) to `.env` in the main checkout (git-ignored, mode 600) and set at least `REMOTE_HOST` (ssh target with key login), `REMOTE_ROOT` (the project directory on the host) and `REMOTE_REPO_URL`. The ranges for ports and Xvfb displays are there too; pick ones no one else on the host uses. Worktrees read the main checkout's `.env`.
2. `tools/remote/remote.sh install-tools` copies [`server/`](server/) (tracked files only) to `REMOTE_ROOT`.
3. Once by hand on the host, under `REMOTE_ROOT`: JDKs in `shared/` (`jdk8`, `jdk17`, `jdk21`; see `server/env.sh`), a `gradle/gradle.properties` listing them with auto-download disabled, `xvfb` and ImageMagick (installed or unpacked under `shared/xvfb`), the GTNH server pack unpacked to `realpack/server/` (accept the EULA, set `online-mode=false`), and for the client a Prism-style instance copied to `realpack/client/instance/` then `python3 -I realpack/client-prepare.py realpack/client`. Optional per-host settings can also live in `REMOTE_ROOT/remote.env`.

The scripts find the root from their own location, so none of them names the host or a path.

## Commands

| Command | What it does | Fetched to `reference/remote/` |
|---|---|---|
| `remote.sh sync <slot> [<rev>] [--bundle]` | Checks out `<rev>` (default `HEAD`) in slot `<slot>` | |
| `remote.sh build <slot> [<rev>] [-- <gradle args>]` | Syncs, then `./gradlew --no-daemon` (default `spotlessCheck build`) | `builds/` |
| `remote.sh devserver <slot> [<rev>] [-- --timeout <s> --cmds '<cmds>']` | Syncs, runs `runServer` until "Done", sends the commands, stops it | `devserver/` |
| `remote.sh devclient <slot> [<rev>] [-- --timeout <s>]` | Syncs, runs `runClient` under Xvnc (not Xvfb: LWJGL 2 display-mode enumeration fails there) until the main menu logs `Sound engine started`; takes a screenshot | `devclient/` |
| `tools/remote/work.sh <format\|test\|build\|run\|server\|clean> [args]` | The edit-build loop from a laptop that must never run Gradle: uploads the working tree (uncommitted edits too) to a work directory on the host, runs Gradle there, prints only the useful lines, and for `format` and `build` copies the Spotless-formatted `src/` back. `server [--timeout <s>] [--flags-off]` boots the dev server, runs the in-game scenarios and stops it | `reference/remote/` is not used; logs stay on the host (`logs/work-*.log`) |
| `remote.sh realpack [--slot <slot>] [-- <run.sh args>]` | Boots the real-pack server, with the slot's built jar if `--slot` is given | `realpack/` |
| `remote.sh client [-- <client-run.sh args>]` | Runs the real-pack client under Xvfb and takes a screenshot | `client/` |
| `remote.sh fetch <path>` | Copies a file or folder under `REMOTE_ROOT` | `fetched/` |
| `remote.sh status` | Lists running processes under the root and the latest run markers | |
| `remote.sh install-tools` | Copies `server/` to the host | |

`<rev>` is resolved to a sha locally. If an `origin/*` branch already contains it, the host fetches it over https; otherwise, or with `--bundle`, a git bundle is copied across so unpushed work can be built.

### Real-pack server (`server/realpack/run.sh`)

`--mod <jar>`, `--fresh`, `--world <name>`, `--level-type <t>`, `--seed <s>`, `--cmds '<cmd>; wait:<s>; until:<regex>'`, `--jvm`, `--timeout`, `--run-timeout`, `--set-config '<file under config/>:<key>=<value>'` (one config value for this run only, restored afterwards), `--view-distance <3-15>`, and `--instance <0-7>`. Instance 0 is `realpack/server/` on `REMOTE_PORT_BASE`; 1 to 7 are copies without worlds on `REMOTE_PORT_BASE+20+n`, each with its own lock, so headless runs from several slots go in parallel. Give each parallel lane its own instance. In `--cmds`, `wait:` and `until:` are steps for the script, not server commands; an `until:` waits for a console line written since the last command.

Each run writes `runs/<id>/` with the console, the FML log, any crash report and a `run.done` summary (status, boot seconds, peak RSS). Wait on `run.done` (skill `waiting-on-remote-jobs`) and read it with `tools/agent/run-summary.py`.

### Client (`server/realpack/client-run.sh`)

`--display <n>` (inside `REMOTE_DISPLAY_MIN..MAX`), `--server <host:port>`, `--shot <name>`, `--wait <s>`, `--extra-shot <name>:<seconds>` (repeatable), `--mod <jar>`, `--jvm`, `--hold <file>`, `--restore-options`, `--timeout`, `--user`, `--heap`. The client uses an offline account, so it can join only a server with `online-mode=false`. Joining often fails once in the FML handshake on a local server; run it again.

## Shared-host rules

- Stay inside your port and display ranges; stop only processes the scripts started (they track pids).
- One run per slot and per server instance at a time (flock). A busy slot or instance exits with code 3; report it and do something else rather than polling.
- Builds run `--no-daemon` with a timeout, so nothing stays resident.
- A shot-list runner (boot server and client for a world, take every shot, write a review page) is not included: it is specific to what a mod renders. Build one on top of `client-run.sh --hold` if you need it.
