#!/usr/bin/env bash
# remote.sh: run builds, dev-server checks, real-pack server runs and client screenshots on the
# shared build host, from the development machine, and fetch their logs back. See README.md.
#
#   remote.sh sync      <slot> [<rev>] [--bundle]   check out <rev> (default HEAD) in a remote slot
#   remote.sh build     <slot> [<rev>] [-- <gradle args...>]
#                                                      sync, then build (default: spotlessCheck build)
#   remote.sh devserver <slot> [<rev>] [-- <slot-devserver args...>]
#                                                      sync, then runServer to "Done", then stop
#   remote.sh devclient <slot> [<rev>] [-- <args>]
#                                                      syncs, runs the dev client under a virtual display until the main menu appears
#   remote.sh realpack  [--slot <slot>] [-- <run.sh args...>]
#                                                      boot the real-pack server (with the slot's jar)
#   remote.sh client    [-- <client-run.sh args...>]
#                                                      real-pack client under Xvfb, screenshot
#   remote.sh fetch     <remote path under REMOTE_ROOT>
#   remote.sh status                                our processes and the latest run markers
#   remote.sh install-tools                         copy tools/remote/server/ (scripts, README) to the root
#
# <rev> is anything `git rev-parse` understands here. It is resolved to a sha on this machine. If the
# sha is not on the public remote yet, or with --bundle, a git bundle of it is copied over instead.
# Settings come from the git-ignored .env at the main checkout's root (see .env.example): REMOTE_HOST (ssh
# target) and REMOTE_ROOT (the project's directory on that host) are required; the rest have defaults.
# Fetched files land in reference/remote/.
set -euo pipefail

die() { echo "remote: $*" >&2; exit 2; }

# Revs resolve in the checkout you run this from (a worktree works); the main checkout (not a
# worktree) holds .env and reference/.
HERE="$(git rev-parse --show-toplevel 2>/dev/null || git -C "$(dirname "$0")" rev-parse --show-toplevel)"
common="$(git -C "$HERE" rev-parse --path-format=absolute --git-common-dir)"
MAIN="$(dirname "$common")"
ENV_FILE="${REMOTE_ENV_FILE:-$MAIN/.env}"
read_env() { [ -f "$ENV_FILE" ] && sed -n "s/^$1=//p" "$ENV_FILE" | tail -1 | sed 's/^"\(.*\)"$/\1/'; }
HOST="${REMOTE_HOST:-$(read_env REMOTE_HOST)}"
ROOT="${REMOTE_ROOT:-$(read_env REMOTE_ROOT)}"
MOD_ID="${REMOTE_MOD_ID:-$(read_env REMOTE_MOD_ID)}"; MOD_ID="${MOD_ID:-examplemod}"
[ -n "$HOST" ] || die "set REMOTE_HOST in $ENV_FILE"
[ -n "$ROOT" ] || die "set REMOTE_ROOT in $ENV_FILE"
LOCAL="$MAIN/reference/remote"
SSH=(ssh -o BatchMode=yes -o ServerAliveInterval=30 "$HOST")

# Settings the server-side scripts read (see server/env.sh), forwarded from the local environment or .env.
FWD=""
for k in REMOTE_MOD_ID REMOTE_PORT_BASE REMOTE_DEV_PORT REMOTE_DEV_PORT_MIN REMOTE_DEV_PORT_MAX REMOTE_DISPLAY_MIN \
         REMOTE_DISPLAY_MAX REMOTE_REPO_URL REMOTE_PACK_LEVEL_TYPE REMOTE_BUILD_TIMEOUT; do
    v="${!k:-$(read_env "$k")}"
    [ -n "$v" ] && FWD="$FWD $k=$(printf '%q' "$v")"
done
# Run a command remotely with every argument quoted for the remote shell.
remote() { "${SSH[@]}" "env$FWD $(printf '%q ' "$@")"; }

# Pull a remote file or directory (under ROOT) into reference/remote/<dest>/.
pull() {
    local src="$1" dest="$LOCAL/$2"
    mkdir -p "$dest"
    rsync -a "$HOST:$src" "$dest/"
    echo "fetched: $dest/$(basename "$src")"
}

check_slot() { [[ "$1" =~ ^[A-Za-z0-9_.-]+$ ]] || die "bad slot name: $1"; }

do_sync() {
    local slot="$1" rev="${2:-HEAD}" force_bundle="${3:-0}" sha ref
    check_slot "$slot"
    sha="$(git -C "$HERE" rev-parse --verify "$rev^{commit}")" || die "unknown rev: $rev"
    # Use https when the public remote already has the commit, a bundle otherwise.
    if [ "$force_bundle" = 0 ] && git -C "$HERE" branch -r --contains "$sha" 2>/dev/null | grep -q '^ *origin/'; then
        remote "$ROOT/bin/slot-sync.sh" "$slot" "$sha"
    else
        # A bundle needs a ref; use a private one (not a branch) and drop it afterwards.
        ref="refs/remote-sh/$slot"
        local tmp; tmp="$(mktemp -d)"
        git -C "$HERE" update-ref "$ref" "$sha"
        git -C "$HERE" bundle create -q "$tmp/$slot.bundle" "$ref"
        git -C "$HERE" update-ref -d "$ref"
        remote mkdir -p "$ROOT/wt/.bundles"
        rsync -a "$tmp/$slot.bundle" "$HOST:$ROOT/wt/.bundles/"
        rm -rf "$tmp"
        remote "$ROOT/bin/slot-sync.sh" "$slot" "$sha" --bundle "$ROOT/wt/.bundles/$slot.bundle"
    fi
}

# Split "<slot> [<rev>] [-- args...]" into SLOT, REV, BUNDLE and the array REST.
parse_slot_rev() {
    SLOT="${1:-}"; [ -n "$SLOT" ] || die "missing <slot>"; shift
    REV=HEAD BUNDLE=0 REST=()
    while [ $# -gt 0 ]; do
        case "$1" in
            --) shift; REST=("$@"); break ;;
            --bundle) BUNDLE=1; shift ;;
            *) REV="$1"; shift ;;
        esac
    done
}

# The release jar a slot's last build made, as a path on the build server.
slot_jar() {
    local slot="$1" jar
    check_slot "$slot"
    jar="$("${SSH[@]}" "ls -t $(printf '%q' "$ROOT/wt/$slot/build/libs")/$MOD_ID-*.jar" 2>/dev/null \
        | grep -v -E -- '-(dev|sources|api)\.jar$' | head -1)" || true
    [ -n "$jar" ] || die "no built jar in slot $slot (run build first)"
    echo "$jar"
}

# Print the remote output and fetch the path printed after "<key>=" in it.
fetch_from_output() {
    local out="$1" key="$2" dest="$3" path
    path="$(sed -n "s/^$key=//p" "$out" | tail -1)"
    [ -n "$path" ] && pull "$path" "$dest"
}

cmd="${1:-}"; [ $# -gt 0 ] && shift
out="$(mktemp)"; trap 'rm -f "$out"' EXIT
case "$cmd" in
    sync)
        parse_slot_rev "$@"
        do_sync "$SLOT" "$REV" "$BUNDLE"
        ;;
    build)
        parse_slot_rev "$@"
        do_sync "$SLOT" "$REV" "$BUNDLE"
        rc=0; remote "$ROOT/bin/slot-build.sh" "$SLOT" "${REST[@]}" | tee "$out" || rc=$?
        log="$(sed -n 's/^exit=[0-9]* log=//p' "$out" | tail -1)"
        [ -n "$log" ] && pull "$log" "builds"
        exit "$rc"
        ;;
    devserver)
        parse_slot_rev "$@"
        do_sync "$SLOT" "$REV" "$BUNDLE"
        rc=0; remote "$ROOT/bin/slot-devserver.sh" "$SLOT" "${REST[@]}" | tee "$out" || rc=$?
        fetch_from_output "$out" log "devserver" || true
        exit "$rc"
        ;;
    devclient)
        parse_slot_rev "$@"
        do_sync "$SLOT" "$REV" "$BUNDLE"
        rc=0; remote "$ROOT/bin/slot-devclient.sh" "$SLOT" "${REST[@]}" | tee "$out" || rc=$?
        fetch_from_output "$out" log "devclient" || true
        exit "$rc"
        ;;
    realpack)
        SLOT="" REST=()
        while [ $# -gt 0 ]; do
            case "$1" in
                --slot) SLOT="$2"; shift 2 ;;
                --) shift; REST=("$@"); break ;;
                *) die "realpack: unknown argument $1 (run.sh arguments go after --)" ;;
            esac
        done
        MOD=()
        if [ -n "$SLOT" ]; then jar="$(slot_jar "$SLOT")" || exit 2; MOD=(--mod "$jar"); fi
        rc=0; remote "$ROOT/realpack/run.sh" "${MOD[@]}" "${REST[@]}" | tee "$out" || rc=$?
        fetch_from_output "$out" run_dir "realpack" || true
        exit "$rc"
        ;;
    client)
        [ "${1:-}" = -- ] && shift
        rc=0; remote "$ROOT/realpack/client-run.sh" "$@" | tee "$out" || rc=$?
        fetch_from_output "$out" run_dir "client" || true
        exit "$rc"
        ;;
    install-tools)
        # The remote scripts live in git (tools/remote/server/); this puts them in place. It only adds or
        # replaces those files: JDKs, the pack, worktrees and logs stay as they are.
        # Tracked files only, so caches and stray local files never reach the server.
        git -C "$HERE/tools/remote/server" ls-files -z \
            | rsync -rltp --checksum --itemize-changes --from0 --files-from=- \
                "$HERE/tools/remote/server/" "$HOST:$ROOT/"
        # rsync --checksum skips files whose content is unchanged, so the executable bit from git may
        # never be applied: enforce it explicitly after the copy.
        "${SSH[@]}" "chmod +x $ROOT/bin/*.sh $ROOT/realpack/*.sh $ROOT/env.sh && echo 'install-tools: scripts are executable'"
        ;;
    fetch)
        p="${1:?usage: remote.sh fetch <path under REMOTE_ROOT>}"
        case "$p" in /*|*..*) die "fetch takes a relative path without ..";; esac
        pull "$ROOT/$p" "fetched"
        ;;
    status)
        "${SSH[@]}" bash -s -- "$(printf '%q' "$ROOT")" <<'EOS'
root="$1"
ps -eo pid,etime,rss,args | grep -F -- "$root" | grep -v -e 'grep -F' -e 'bash -s' || echo "nothing running under the project root"
for f in "$root"/realpack/run.done "$root"/realpack/instances/*/run.done "$root"/logs/devserver-*.done; do
    [ -f "$f" ] && { echo "== $f"; cat "$f"; }
done
true
EOS
        ;;
    ""|-h|--help|help)
        sed -n "2,21p" "$0" | sed 's/^# \{0,1\}//'
        ;;
    *)
        die "unknown command: $cmd (try --help)"
        ;;
esac
