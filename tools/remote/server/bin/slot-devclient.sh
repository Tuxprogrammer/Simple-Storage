#!/usr/bin/env bash
# slot-devclient.sh <slot> [--timeout <s>]    default timeout: 420s
# Runs the dev client (./gradlew runClient) of <root>/wt/<slot> under a virtual display
# (Xvnc on the first free display in REMOTE_DISPLAY_MIN..MAX; Xvnc because Xvfb breaks
# LWJGL 2 display-mode enumeration), logging to <root>/logs/devclient-<slot>-<stamp>.log.
# Reports status=menu when the log shows 'Sound engine started' (main menu reached),
# status=exit when the gradle process exits first, status=timeout when the budget runs out.
# On menu it waits 5 s and, if ImageMagick's import exists, captures a screenshot to
# <root>/logs/devclient-<slot>-<stamp>.png. Always stops only what it started
# (the gradle process group and the Xvnc pid), writes and prints a
# <root>/logs/devclient-<slot>.done file (status=, wall_seconds, log=). Exit 0 only on status=menu.
# REMOTE_LOG=<path> overrides the log path.
set -uo pipefail
BR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
. "$BR/env.sh"
slot="${1:?usage: slot-devclient.sh <slot> [--timeout <s>]}"; shift
TIMEOUT=420
while [ $# -gt 0 ]; do
    case "$1" in
    --timeout)
        [ $# -ge 2 ] || { echo "missing value for --timeout" >&2; exit 2; }
        TIMEOUT="$2"; shift 2 ;;
    *) echo "unknown arg: $1" >&2; exit 2 ;;
    esac
done
[[ "$TIMEOUT" =~ ^[0-9]+$ ]] || { echo "bad timeout: $TIMEOUT" >&2; exit 2; }
[[ "$slot" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "bad slot name: $slot" >&2; exit 2; }
W="$BR/wt/$slot"
[ -x "$W/gradlew" ] || { echo "no checkout at $W (run slot-sync.sh first)" >&2; exit 2; }
exec 8>"$BR/wt/.$slot.lock"; flock -n 8 || { echo "slot $slot is busy" >&2; exit 3; }

# Pick a free display: no X socket and no lock file for it.
N=""
for d in $(seq "$REMOTE_DISPLAY_MIN" "$REMOTE_DISPLAY_MAX"); do
    if [ ! -e "/tmp/.X11-unix/X$d" ] && [ ! -e "/tmp/.X$d-lock" ]; then N="$d"; break; fi
done
[ -n "$N" ] || { echo "no free display in $REMOTE_DISPLAY_MIN-$REMOTE_DISPLAY_MAX" >&2; exit 4; }

mkdir -p "$BR/logs"
stamp="$(date +%Y%m%d-%H%M%S)"
log="${REMOTE_LOG:-$BR/logs/devclient-$slot-$stamp.log}"
png="$BR/logs/devclient-$slot-$stamp.png"
donef="$BR/logs/devclient-$slot.done"

XVNC_PID=""
GPID=""
trap '
    [ -n "${GPID:-}" ] && kill -TERM -"$GPID" 2>/dev/null
    [ -n "${XVNC_PID:-}" ] && kill "$XVNC_PID" 2>/dev/null
' INT TERM HUP

# Xvnc (not Xvfb): Xvfb's screen layout confuses LWJGL 2 display-mode enumeration.
Xvnc ":$N" -geometry 1280x720 -depth 24 -SecurityTypes None -localhost -rfbport $((5900+N)) \
    > "$BR/logs/devclient-$slot-xvnc.log" 2>&1 &
XVNC_PID=$!
sleep 3
if ! kill -0 "$XVNC_PID" 2>/dev/null; then
    echo "Xvnc :$N failed, see $BR/logs/devclient-$slot-xvnc.log" >&2; kill "$XVNC_PID" 2>/dev/null; exit 4
fi

cd "$W"
# timeout leads its own process group, so killing -"$GPID" reaches Gradle and any JVM it forks.
DISPLAY=:$N LIBGL_ALWAYS_SOFTWARE=1 timeout -k 30 "$TIMEOUT" \
    ./gradlew --no-daemon --console=plain runClient >> "$log" 2>&1 < /dev/null &
GPID=$!
echo "slot=$slot display=:$N timeout=$TIMEOUT log=$log" | tee -a "$log"

# Poll: menu reached, gradle exited, or the timeout budget ran out.
status=""; t0=$SECONDS
while :; do
    if grep -q 'Sound engine started' "$log" 2>/dev/null; then status="menu"; break; fi
    if ! kill -0 "$GPID" 2>/dev/null; then status="exit"; break; fi
    [ $((SECONDS - t0)) -ge "$TIMEOUT" ] && { status="timeout"; break; }
    sleep 5
done
wall=$((SECONDS - t0))
[ "$status" = "timeout" ] && echo "devclient: timed out after ${TIMEOUT}s" | tee -a "$log"

if [ "$status" = "menu" ]; then
    sleep 5
    if command -v import >/dev/null 2>&1; then
        DISPLAY=:$N import -window root "$png" 2>>"$log" && echo "screenshot=$png"
    fi
fi

# Stop only what this script started.
kill -TERM -"$GPID" 2>/dev/null; sleep 5; kill -KILL -"$GPID" 2>/dev/null
kill "$XVNC_PID" 2>/dev/null
GPID=""; XVNC_PID=""
trap - INT TERM HUP

{
    echo "status=$status"
    echo "wall_seconds=$wall"
    echo "log=$log"
} | tee "$donef"
[ "$status" = "menu" ] && exit 0 || exit 1
