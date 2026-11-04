#!/usr/bin/env bash
# work.sh: the edit-build loop against the build host, from a development laptop that never runs Gradle itself.
# It uploads the working tree (uncommitted edits included) to $REMOTE_ROOT/wt/work on the host, runs Gradle
# there, and brings formatted sources back. See README.md.
#
#   work.sh format    [gradle args...]   spotlessApply, then copy src/ back
#   work.sh test      [gradle args...]   default args: test
#   work.sh build     [gradle args...]   default args: spotlessApply build; also copies src/ back
#   work.sh run       <gradle args...>   any gradle task args, no copy back
#   work.sh server    [--timeout <s>]    runServer the dev server, which runs the in-game autotest
#                  [--flags-off]        scenarios and stops itself; success is "Scenarios: n passed, 0 failed", n > 0
#                                       --flags-off boots with every behavior module off (writes
#                                       run/server/config/simplestoragenh.cfg); without it that file is removed
#   work.sh clean     [gradle args...]   remove the host work directory contents (nothing else)
#
# Settings come from the git-ignored .env at the main checkout's root (see .env.example): REMOTE_HOST (ssh
# target) and REMOTE_ROOT (the project's directory on that host) are required; the rest have defaults.
# Environment variables of the same name win over .env. The log stays on the host at $ROOT/logs/work-<cmd>.log.
set -uo pipefail

die() { echo "work: $*" >&2; exit 2; }

HERE="$(git rev-parse --show-toplevel 2>/dev/null || git -C "$(dirname "$0")" rev-parse --show-toplevel)"
common="$(git -C "$HERE" rev-parse --path-format=absolute --git-common-dir)"
MAIN="$(dirname "$common")"
ENV_FILE="${REMOTE_ENV_FILE:-$MAIN/.env}"
read_env() { [ -f "$ENV_FILE" ] && sed -n "s/^$1=//p" "$ENV_FILE" | tail -1 | sed 's/^"\(.*\)"$/\1/'; }
HOST="${REMOTE_HOST:-$(read_env REMOTE_HOST)}"
ROOT="${REMOTE_ROOT:-$(read_env REMOTE_ROOT)}"
[ -n "$HOST" ] || die "set REMOTE_HOST in $ENV_FILE"
[ -n "$ROOT" ] || die "set REMOTE_ROOT in $ENV_FILE"
SSH=(ssh -o BatchMode=yes -o ServerAliveInterval=30 "$HOST")
WORK="$ROOT/wt/work"
LOG="$ROOT/logs/work-"

[ $# -ge 1 ] || die "usage: $0 format|test|build|run|server|clean [gradle args...]"
cmd="$1"; shift
case "$cmd" in
    format) args=("${@:-spotlessApply}"); copyback=1 ;;
    test)   args=("${@:-test}"); copyback=0 ;;
    build)  [ $# -eq 0 ] && set -- spotlessApply build; args=("$@"); copyback=1 ;;
    run)    [ $# -ge 1 ] || die "usage: $0 run <gradle task...>"; args=("$@"); copyback=0 ;;
    server)
        srv_timeout=600
        srv_flags_off=0
        while [ $# -gt 0 ]; do
            case "$1" in
                --timeout) shift; [ $# -ge 1 ] || die "usage: $0 server --timeout <s>"; srv_timeout="$1" ;;
                --timeout=*) srv_timeout="${1#--timeout=}" ;;
                --flags-off) srv_flags_off=1 ;;
                *) die "unknown server option: $1 (usage: $0 server [--timeout <s>] [--flags-off])" ;;
            esac; shift
        done
        [[ "$srv_timeout" =~ ^[0-9]+$ ]] || die "--timeout must be an integer number of seconds, got '$srv_timeout'"
        args=("--timeout=$srv_timeout"); copyback=0 ;;
    clean)  args=("$@"); copyback=0 ;;
    *) die "unknown command: $cmd (usage: $0 format|test|build|run|server|clean [gradle args...])" ;;
esac

# Upload the working tree (uncommitted edits included). Build/run artifacts and host-only directories are
# excluded; --delete mirrors the tree so host-only leftovers go away.
rsync -a --delete \
    --exclude=.git --exclude=build --exclude=run --exclude=reference --exclude=.gradle --exclude=.env \
    --exclude=.idea --exclude=wt --exclude='*.log' \
    "$HERE/" "$HOST:$WORK/"

if [ "$cmd" = clean ]; then
    "${SSH[@]}" "mkdir -p '$WORK' && find '$WORK' -mindepth 1 -maxdepth 1 -exec rm -rf {} + 2>/dev/null || true"
    echo "cleaned: $HOST:$WORK/"
    exit 0
fi

if [ "$cmd" = server ]; then
    # Dev server settings: offline (dev accounts), eula accepted, small view distance; replace existing
    # keys, as slot-build.sh does. run/ is excluded from the upload, so this is created on the host.
    "${SSH[@]}" "
        d='$WORK/run/server'; mkdir -p \"\$d\"
        echo 'eula=true' > \"\$d/eula.txt\"
        p=\"\$d/server.properties\"; touch \"\$p\"
        for kv in online-mode=false server-port=\${REMOTE_DEV_PORT_MIN:-26110} view-distance=6; do
            k=\"\${kv%%=*}\"
            if grep -q \"^\\\$k=\" \"\$p\"; then sed -i \"s|^\\\$k=.*|\\\$kv|\" \"\$p\"; else echo \"\\\$kv\" >> \"\$p\"; fi
        done
        f=\"\$d/config/simplestoragenh.cfg\"
        if [ $srv_flags_off -eq 1 ]; then
            mkdir -p \"\$d/config\"
            printf 'modules {\\n    B:network=false\\n    B:storage=false\\n    B:terminal=false\\n    B:wireless=false\\n}\\n' > \"\$f\"
        else
            rm -f \"\$f\"
        fi" || die "could not set up $HOST:$WORK/run/server"
    srv_timeout="${args[0]#--timeout=}"
    code="$("${SSH[@]}" "mkdir -p '$ROOT/logs' && cd '$WORK' && . '$ROOT/env.sh' && \
        export JAVA_TOOL_OPTIONS=\"\$JAVA_TOOL_OPTIONS -Dsimplestoragenh.autotest=true -Dsimplestoragenh.testHooks=true\"; \
        timeout -k 30 $srv_timeout ./gradlew --no-daemon --console=plain runServer > '$LOG$cmd.log' 2>&1 < /dev/null; echo \"exit=\$?\"" \
        | sed -n 's/^exit=//p' | tail -1)"
    [ -n "$code" ] || code=1
    # Print the useful lines of the log, and pass iff the scenarios all passed with at least one run.
    "${SSH[@]}" "cat '$LOG$cmd.log' 2>/dev/null" | awk -v code="$code" '
        /Registered / { if (r < 3) { print; r++ } }
        /\[scenario\]/ { if (s < 60) { print; s++ } }
        /ERROR/ && /simplestorage/ { if (e < 10) { print; e++ } }
        END {
            print "exit=" code
            if (match($0, /^Scenarios: ([0-9]+) passed, 0 failed$/, m) && m[1] > 0) exit 0
            exit (code == 0) ? 1 : 2
        }'
    exit "$?"
fi

# Run Gradle on the host (env.sh sets JAVA_HOME and GRADLE_USER_HOME); the log stays there. The exit code is
# echoed, not returned: with only -u (not -e), a failing ssh must not kill this script before the summary.
code="$("${SSH[@]}" "mkdir -p '$ROOT/logs' && cd '$WORK' && . '$ROOT/env.sh' && timeout -k 30 \${REMOTE_BUILD_TIMEOUT:-3600} ./gradlew --no-daemon --console=plain $(printf '%q ' "${args[@]}") > '$LOG$cmd.log' 2>&1 < /dev/null; echo \"exit=\$?\"" | sed -n 's/^exit=//p' | tail -1)"
[ -n "$code" ] || code=1

# Print only the useful lines of the log (fetched through ssh cat).
"${SSH[@]}" "cat '$LOG$cmd.log' 2>/dev/null" | awk '
    /error:/ { if (n < 30) print }
    /FAILED/ && /Test|> Task/ { if (m < 30) print }
    /^BUILD (SUCCESSFUL|FAILED)/ { print }
    /What went wrong/ { print; c = 3; next }
    c > 0 { print; c-- }
    { n += ($0 ~ /error:/); m += ($0 ~ /FAILED/ && $0 ~ /Test|> Task/) }
'

# TESTS summary from the host's JUnit XML.
sums="$("${SSH[@]}" "grep -h -o 'tests=\"[0-9]*\" skipped=\"[0-9]*\" failures=\"[0-9]*\" errors=\"[0-9]*\"' '$WORK/build/test-results/test/'*.xml 2>/dev/null" \
        | awk -F'"' '{ t += $2; s += $4; f += $6; e += $8 } END { if (t + s + f + e) printf "TESTS: %d run, %d skipped, %d failures, %d errors\n", t, s, f, e }')"
[ -n "$sums" ] && echo "$sums"

# Copy formatted sources back (no --delete: only overwrite what spotless touched).
if [ "$copyback" = 1 ]; then
    rsync -a "$HOST:$WORK/src/" "$HERE/src/"
    echo "copied src/ back"
fi

echo "log: $HOST:$LOG$cmd.log"
exit "$code"
