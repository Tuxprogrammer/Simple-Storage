# Build-host toolchain for one project. Source me. Everything points inside this directory (REMOTE_ROOT on the
# development machine); nothing here depends on another project's directories.
BR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export BR
# Optional per-host settings (ranges, mod id); remote.sh also forwards REMOTE_* values from the local .env.
[ -f "$BR/remote.env" ] && . "$BR/remote.env"
# Ranges on a shared build host: pick ones nobody else uses, and set them in .env.
: "${REMOTE_MOD_ID:=examplemod}"                  # jar name prefix, e.g. examplemod-1.0.0.jar
: "${REMOTE_PORT_BASE:=26000}"                    # real-pack server (instance 0); instance N uses BASE+20+N
: "${REMOTE_DEV_PORT_MIN:=26010}" "${REMOTE_DEV_PORT_MAX:=26019}"   # dev servers (runServer)
: "${REMOTE_DISPLAY_MIN:=100}" "${REMOTE_DISPLAY_MAX:=109}"        # Xvfb displays for clients
export REMOTE_MOD_ID REMOTE_PORT_BASE REMOTE_DEV_PORT_MIN REMOTE_DEV_PORT_MAX REMOTE_DISPLAY_MIN REMOTE_DISPLAY_MAX
export JAVA_HOME="$BR/shared/jdk21"      # runs Gradle (needs 17+); the buildscript toolchains jdk8 to compile
export GRADLE_USER_HOME="$BR/gradle"     # its gradle.properties lists these JDKs and disables auto-download
XV="$BR/shared/xvfb"                     # optional: an unpacked Xvfb userland when the host lacks one
export PATH="$JAVA_HOME/bin:$XV/usr/bin:$XV/usr/bin/X11:$BR/bin:$PATH"
export LD_LIBRARY_PATH="$XV/usr/lib/x86_64-linux-gnu:$XV/usr/lib:${LD_LIBRARY_PATH:-}"
export XKB_DIR=/usr/share/X11/xkb
export REMOTE_JDK8="$BR/shared/jdk8" REMOTE_JDK17="$BR/shared/jdk17" REMOTE_JDK21="$BR/shared/jdk21"
# Some 1.7.10 mods and tools look under ${user.home}/.gradle (MCP mappings) or write ${user.home}/.java;
# point user.home at a shim whose .gradle links to $GRADLE_USER_HOME.
export JAVA_TOOL_OPTIONS="-Duser.home=$BR/shared/home ${JAVA_EXTRA_OPTIONS:-}"

# remote_xvfb N: start Xvfb on :N (inside REMOTE_DISPLAY_MIN..MAX), pid in $BR/logs/xvfb-N.pid.
remote_xvfb() {
    local n="$1"
    if ! [[ "$n" =~ ^[0-9]+$ ]] || [ "$n" -lt "$REMOTE_DISPLAY_MIN" ] || [ "$n" -gt "$REMOTE_DISPLAY_MAX" ]; then
        echo "remote_xvfb: display must be $REMOTE_DISPLAY_MIN-$REMOTE_DISPLAY_MAX, got '$n'" >&2; return 2
    fi
    if [ -e "/tmp/.X11-unix/X$n" ] || [ -e "/tmp/.X$n-lock" ]; then echo "remote_xvfb: :$n already in use" >&2; return 3; fi
    mkdir -p "$BR/logs"
    nohup "$(command -v Xvfb)" ":$n" -screen 0 1600x900x24 -xkbdir "$XKB_DIR" -nolisten tcp \
        +extension RANDR +extension GLX > "$BR/logs/xvfb-$n.log" 2>&1 &
    echo $! > "$BR/logs/xvfb-$n.pid"
    sleep 2
    if kill -0 "$(cat "$BR/logs/xvfb-$n.pid")" 2>/dev/null; then echo "Xvfb :$n up (pid $(cat "$BR/logs/xvfb-$n.pid"))"; else echo "Xvfb :$n failed, see $BR/logs/xvfb-$n.log" >&2; return 1; fi
}
# remote_xvfb_stop N: stop only the Xvfb we started on :N (by its pid file).
remote_xvfb_stop() {
    local f="$BR/logs/xvfb-$1.pid"
    [ -f "$f" ] || { echo "no pid file $f" >&2; return 1; }
    kill "$(cat "$f")" 2>/dev/null; rm -f "$f"
}
