#!/usr/bin/env bash
# api-index.sh: build the javap API index of the compile jars on the build host, then fetch it.
#
#   api-index.sh [--out FILE]   default FILE = $MAIN/reference/api/index.tsv
#
# The index is produced on the host by tools/qwen/apiindex.py (run against the host's JDK 8 javap over
# the minecraft, GregTech, NEI and ForgeMultipart compile jars) and copied to FILE. The log line is
# "api-index: N lines -> FILE". Settings come from the same .env as work.sh (REMOTE_HOST, REMOTE_ROOT).
set -euo pipefail

die() { echo "api-index: $*" >&2; exit 2; }

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

out="$MAIN/reference/api/index.tsv"
while [ $# -gt 0 ]; do
    case "$1" in
        --out) shift; [ $# -ge 1 ] || die "usage: $0 [--out FILE]"; out="$1" ;;
        --out=*) out="${1#--out=}" ;;
        *) die "unknown option: $1 (usage: $0 [--out FILE])" ;;
    esac; shift
done
mkdir -p "$(dirname "$out")"

# Upload the indexer, build the index on the host, fetch it, clean up.
scp -o BatchMode=yes "$HERE/tools/qwen/apiindex.py" "$HOST:$ROOT/tmp-apiindex.py" \
    || die "could not upload apiindex.py to $HOST:$ROOT"

"${SSH[@]}" bash -s "$ROOT" <<'REMOTE'
set -eu
ROOT="$1"
JAVAP="$ROOT/shared/jdk8/bin/javap"
declare -a jars=()
add_jar() {
    local label="$1" f
    f="$(find "$2" -name "$3" 2>/dev/null | sort -V | tail -1)"
    if [ -n "$f" ]; then jars+=("$f"); else echo "skip: $label" >&2; fi
}
add_jar MC  "$ROOT/wt" "recompiled_minecraft-1.7.10.jar"
add_jar GT  "$HOME/.gradle/caches/modules-2/files-2.1/com.github.GTNewHorizons/GT5-Unofficial" "*-dev.jar"
add_jar NEI "$HOME/.gradle/caches/modules-2/files-2.1/com.github.GTNewHorizons/NotEnoughItems" "*-dev.jar"
add_jar FMP "$HOME/.gradle/caches/modules-2/files-2.1/com.github.GTNewHorizons/ForgeMultipart" "*-dev.jar"
if [ "${#jars[@]}" -eq 0 ]; then echo "error: no minecraft jar" >&2; exit 3; fi
args=()
for j in "${jars[@]}"; do args+=(--jar "$j"); done
python3 "$ROOT/tmp-apiindex.py" build --javap "$JAVAP" --out "$ROOT/tmp-apiindex.tsv" "${args[@]}"
REMOTE

scp -o BatchMode=yes "$HOST:$ROOT/tmp-apiindex.tsv" "$out" || die "could not fetch the index from $HOST"
"${SSH[@]}" "rm -f '$ROOT/tmp-apiindex.py' '$ROOT/tmp-apiindex.tsv'"

lines="$(wc -l < "$out")"
echo "api-index: $lines lines -> $out"
