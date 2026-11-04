#!/usr/bin/env bash
# slot-build.sh <slot> [gradle args...]       default args: spotlessCheck build
# Runs ./gradlew --no-daemon in <root>/wt/<slot> with env.sh, logging to
# <root>/logs/build-<slot>-<stamp>.log (and logs/build-<slot>-latest.log).
# No daemon: every build is one JVM that exits with the build, so nothing is left running on the
# shared box. Gradle's own heap is 4g (GRADLE_USER_HOME/gradle.properties); forked test and
# compile JVMs are capped by the buildscript. Exit status is Gradle's.
#
# Dev runs (runServer, runClient) use the slot's run/ dir; slot-build.sh writes the dev server's
# server.properties there first, with server-port from REMOTE_DEV_PORT (default REMOTE_DEV_PORT_MIN, kept inside REMOTE_DEV_PORT_MIN..MAX; see env.sh).
# REMOTE_BUILD_TIMEOUT (seconds, default 3600) kills the build (exit 124) after that long.
# REMOTE_STDIN=<file or fifo> feeds Gradle's stdin (default /dev/null; slot-devserver.sh uses a fifo), and
# REMOTE_LOG=<path> overrides the log path.
set -uo pipefail
BR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
. "$BR/env.sh"
slot="${1:?usage: slot-build.sh <slot> [gradle args...]}"; shift
[[ "$slot" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "bad slot name: $slot" >&2; exit 2; }
W="$BR/wt/$slot"
[ -x "$W/gradlew" ] || { echo "no checkout at $W (run slot-sync.sh first)" >&2; exit 2; }
[ $# -gt 0 ] || set -- spotlessCheck build
PORT="${REMOTE_DEV_PORT:-$REMOTE_DEV_PORT_MIN}"
if ! [[ "$PORT" =~ ^[0-9]+$ ]] || [ "$PORT" -lt "$REMOTE_DEV_PORT_MIN" ] || [ "$PORT" -gt "$REMOTE_DEV_PORT_MAX" ]; then
    echo "REMOTE_DEV_PORT must be $REMOTE_DEV_PORT_MIN-$REMOTE_DEV_PORT_MAX, got $PORT" >&2; exit 2
fi
exec 8>"$BR/wt/.$slot.lock"; flock -n 8 || { echo "slot $slot is busy" >&2; exit 3; }

# Dev server settings: our port, loopback only, offline (dev accounts), eula accepted.
mkdir -p "$W/run/server"
for d in "$W/run" "$W/run/server"; do
    echo "eula=true" > "$d/eula.txt"
    p="$d/server.properties"
    touch "$p"
    for kv in "server-port=$PORT" "server-ip=localhost" "online-mode=false" "view-distance=6" "snooper-enabled=false"; do
        k="${kv%%=*}"
        if grep -q "^$k=" "$p"; then sed -i "s|^$k=.*|$kv|" "$p"; else echo "$kv" >> "$p"; fi
    done
done

mkdir -p "$BR/logs"
stamp="$(date +%Y%m%d-%H%M%S)"
log="${REMOTE_LOG:-$BR/logs/build-$slot-$stamp.log}"
ln -sfn "$(basename "$log")" "$BR/logs/build-$slot-latest.log"
echo "slot=$slot sha=$(git -C "$W" rev-parse HEAD) args=$* log=$log" | tee "$log"
cd "$W"
# timeout leads its own process group and signals the whole group (Gradle and any JVM it forks).
timeout -k 30 "${REMOTE_BUILD_TIMEOUT:-3600}" ./gradlew --no-daemon --console=plain "$@" >> "$log" 2>&1 < "${REMOTE_STDIN:-/dev/null}" &
gpid=$!
echo "$gpid" > "$BR/logs/build-$slot.pid"
trap 'kill -TERM -"$gpid" 2>/dev/null; sleep 15; kill -KILL -"$gpid" 2>/dev/null; rm -f "$BR/logs/build-$slot.pid"; exit 130' INT TERM HUP
wait "$gpid"; rc=$?
[ "$rc" = 124 ] && echo "lc-build: timed out after ${REMOTE_BUILD_TIMEOUT:-3600}s" | tee -a "$log"
rm -f "$BR/logs/build-$slot.pid"
grep -E "^BUILD (SUCCESSFUL|FAILED)|What went wrong" -A3 "$log" | head -8
if ls build/test-results/test/*.xml >/dev/null 2>&1; then
    grep -h -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' build/test-results/test/*.xml \
        | awk -F'"' '{t+=$2;s+=$4;f+=$6;e+=$8} END {print "TESTS total="t" skipped="s" failures="f" errors="e}'
fi
echo "exit=$rc log=$log"
exit "$rc"
