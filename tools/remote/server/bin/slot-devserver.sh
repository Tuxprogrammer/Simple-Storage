#!/usr/bin/env bash
# slot-devserver.sh <slot> [--timeout <s>] [--autotest] [--cmds '<cmd>; <cmd>; wait:<s>; until:<regex>']
# Headless dev-server check: runs `gradlew runServer` in wt/<slot> through slot-build.sh (port from
# REMOTE_DEV_PORT, default REMOTE_DEV_PORT_MIN), waits for "Done (", sends --cmds, then "stop", and waits for Gradle to
# exit. Kills only the build it started (slot-build.sh's process group) if boot or stop times out.
# [--autotest] exports -Dsimplestoragenh.autotest=true -Dsimplestoragenh.testHooks=true so the mod runs its in-game
# scenarios and stops the server itself; then waits for a "Scenarios: N passed, M failed" log line (or the server to
# exit) and sets status ok / scenarios-failed / scenarios-missing from the last such line.
# Writes logs/devserver-<slot>-<stamp>.log and logs/devserver-<slot>.done (status, boot_seconds, ...).
set -uo pipefail
BR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
slot="${1:?usage: slot-devserver.sh <slot> [--timeout <s>] [--autotest] [--cmds '...']}"; shift
TIMEOUT=900 CMDS="" AUTOTEST=0
while [ $# -gt 0 ]; do
    case "$1" in
        --timeout) TIMEOUT="$2"; shift 2 ;;
        --autotest) AUTOTEST=1; shift ;;
        --cmds) CMDS="$2"; shift 2 ;;
        *) echo "unknown argument: $1" >&2; exit 2 ;;
    esac
done
[[ "$slot" =~ ^[A-Za-z0-9_.-]+$ ]] || { echo "bad slot name: $slot" >&2; exit 2; }
stamp="$(date +%Y%m%d-%H%M%S)"
log="$BR/logs/devserver-$slot-$stamp.log"
done_f="$BR/logs/devserver-$slot.done"
fifo="$BR/logs/devserver-$slot.fifo"
rm -f "$done_f" "$fifo"; mkfifo "$fifo"
exec 3<>"$fifo"
T0=$(date +%s) STATUS=error BOOT_S="" RC=""
if [ "$AUTOTEST" = 1 ]; then
    export JAVA_EXTRA_OPTIONS="${JAVA_EXTRA_OPTIONS:-} -Dsimplestoragenh.autotest=true -Dsimplestoragenh.testHooks=true"
fi
REMOTE_STDIN="$fifo" REMOTE_LOG="$log" REMOTE_BUILD_TIMEOUT=$(( TIMEOUT + 3600 )) "$BR/bin/slot-build.sh" "$slot" runServer > /dev/null 2>&1 &
bpid=$!
alive() { kill -0 "$bpid" 2>/dev/null; }
finish() {
    if alive; then STATUS="$STATUS+killed"; kill -TERM "$bpid" 2>/dev/null; for _ in $(seq 1 60); do alive || break; sleep 1; done; fi
    wait "$bpid" 2>/dev/null; [ -z "$RC" ] && RC=$?
    exec 3>&-; rm -f "$fifo"
    if [ -n "${SCENARIOS:-}" ]; then
        printf 'status=%s\nboot_seconds=%s\nwall_seconds=%s\nexit=%s\nscenarios=%s\nlog=%s\n' "$STATUS" "$BOOT_S" "$(( $(date +%s) - T0 ))" "$RC" "$SCENARIOS" "$log" | tee "$done_f"
    else
        printf 'status=%s\nboot_seconds=%s\nwall_seconds=%s\nexit=%s\nlog=%s\n' "$STATUS" "$BOOT_S" "$(( $(date +%s) - T0 ))" "$RC" "$log" | tee "$done_f"
    fi
    [ "$STATUS" = ok ]
}
trap 'finish; exit $?' EXIT
trap 'STATUS=interrupted; exit 130' INT TERM HUP
until grep -q 'Done (' "$log" 2>/dev/null; do
    alive || { STATUS=died-during-boot; exit 1; }
    [ "$(date +%s)" -ge $(( T0 + TIMEOUT )) ] && { STATUS=boot-timeout; exit 1; }
    sleep 2
done
BOOT_S=$(( $(date +%s) - T0 )); echo "dev server up after ${BOOT_S}s"
IFS=';' read -r -a parts <<< "$CMDS"
for c in "${parts[@]}"; do
    c="$(echo "$c" | sed 's/^ *//; s/ *$//')"; [ -z "$c" ] && continue
    case "$c" in
        wait:*) sleep "${c#wait:}" ;;
        until:*) re="${c#until:}"; while alive && ! grep -aqE "$re" "$log"; do sleep 5; done ;;
        *) echo ">> $c"; printf '%s\n' "$c" >&3; sleep 2 ;;
    esac
done
SCENARIOS=""
if [ "$AUTOTEST" = 1 ]; then
    # in-game scenarios run at startup and stop the server themselves
    while alive && ! grep -aqE 'Scenarios: [0-9]+ passed, [0-9]+ failed' "$log"; do
        [ "$(date +%s)" -ge $(( T0 + TIMEOUT )) ] && break
        sleep 2
    done
    SCENARIOS="$(grep -aoE 'Scenarios: [0-9]+ passed, [0-9]+ failed' "$log" 2>/dev/null | tail -1)"
    PASSED="$(printf '%s' "$SCENARIOS" | sed -n 's/^Scenarios: \([0-9][0-9]*\) passed.*/\1/p')"
    if [ -z "$SCENARIOS" ]; then
        SCENARIOS="(none)"; STATUS=scenarios-missing
    elif echo "$SCENARIOS" | grep -qE ' 0 failed$' && [ "${PASSED:-0}" -gt 0 ]; then
        STATUS=ok
    else
        STATUS=scenarios-failed
    fi
    echo "scenarios: $SCENARIOS"
    grep -F '[scenario] FAIL' "$log" 2>/dev/null | tail -20
fi
printf 'stop\n' >&3
[ "$STATUS" = ok ] || STATUS=stop-timeout
for _ in $(seq 1 180); do alive || break; sleep 1; done
if ! alive; then wait "$bpid"; RC=$?; [ "$RC" = 0 ] && STATUS=ok || STATUS=gradle-failed; fi
