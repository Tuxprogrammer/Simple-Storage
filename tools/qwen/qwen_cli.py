"""Command line front end for the qwen-agents server script.

Adds a ``start`` subcommand that the server script does not provide, and
delegates ``wait`` to the server script run as a child process.
"""

import importlib.util
import os
import sys


def server_path() -> str:
    """Return the path of the server script."""
    path = os.environ.get("QWEN_AGENTS_SERVER")
    if path:
        return path
    return os.path.expanduser("~/.claude/local-plugins/qwen-agents/server/qwen_agents.py")


def load_server():
    """Import the server script by path and return its module."""
    path = server_path()
    spec = importlib.util.spec_from_file_location("qwen_agents", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def cmd_start(argv: list[str]) -> int:
    """Handle the ``start`` subcommand; return the process exit code."""
    prompt_file = None
    cwd = os.getcwd()
    mode = "edit"
    thinking = False
    i = 0
    while i < len(argv):
        flag = argv[i]
        if flag == "--prompt-file":
            i += 1
            if i >= len(argv):
                print("start: missing value for --prompt-file", file=sys.stderr)
                return 2
            prompt_file = argv[i]
        elif flag == "--cwd":
            i += 1
            if i >= len(argv):
                print("start: missing value for --cwd", file=sys.stderr)
                return 2
            cwd = argv[i]
        elif flag == "--mode":
            i += 1
            if i >= len(argv):
                print("start: missing value for --mode", file=sys.stderr)
                return 2
            mode = argv[i]
        elif flag in ("--max-turns", "--timeout-s", "--max-output-tokens"):
            i += 1
            if i >= len(argv):
                print("start: missing value for %s" % flag, file=sys.stderr)
                return 2
            try:
                value = int(argv[i])
            except ValueError:
                print("start: not an integer: %s" % argv[i], file=sys.stderr)
                return 2
            if flag == "--max-turns":
                max_turns = value
            elif flag == "--timeout-s":
                timeout_s = value
            else:
                max_output_tokens = value
        elif flag == "--thinking":
            thinking = True
        else:
            print("start: unknown flag: %s" % flag, file=sys.stderr)
            return 2
        i += 1
    if prompt_file is None:
        print("start: --prompt-file is required", file=sys.stderr)
        return 2
    try:
        module = load_server()
    except Exception as exc:
        print("start: %s" % exc, file=sys.stderr)
        return 1
    a = {
        "prompt": None,
        "cwd": cwd,
        "mode": mode,
        "max_turns": getattr(module, "DEFAULT_TURNS", 0),
        "timeout_s": getattr(module, "DEFAULT_TIMEOUT_S", 0),
        "max_output_tokens": getattr(module, "MAX_OUT", 0),
        "thinking": thinking,
    }
    try:
        with open(prompt_file, "r", encoding="utf-8") as f:
            a["prompt"] = f.read()
        job_id = module.start_job(a)
    except Exception as exc:
        print("start: %s" % exc, file=sys.stderr)
        return 1
    print(job_id)
    return 0


def cmd_wait(argv: list[str]) -> int:
    """Handle the ``wait`` subcommand; return the process exit code."""
    if not argv:
        print("wait: job id required", file=sys.stderr)
        return 2
    os.execv(sys.executable, [sys.executable, server_path(), "wait", *argv])
    return 1


def main(argv: list[str]) -> int:
    """Dispatch to the named subcommand; return the process exit code."""
    if not argv:
        print("usage: qwen_cli.py start --prompt-file F | wait <job id> ...", file=sys.stderr)
        return 2
    if argv[0] == "start":
        return cmd_start(argv[1:])
    if argv[0] == "wait":
        return cmd_wait(argv[1:])
    print("usage: qwen_cli.py start --prompt-file F | wait <job id> ...", file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
