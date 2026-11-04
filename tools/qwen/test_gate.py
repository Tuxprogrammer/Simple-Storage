"""Unit tests for gate.py, the deterministic check gate.

PureFunctionTests calls the gate's helper functions directly.
GateLoopTests runs the whole gate as a subprocess, driving the worker
through fake_qwen.py, and checks the verdict line and the artefacts
written under the out directory.
"""

import hashlib
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import gate  # noqa: E402


class PureFunctionTests(unittest.TestCase):
    """Direct tests of the pure helper functions in gate.py."""

    def test_extract_error_lines_keeps_matching_lines(self):
        text = (
            "ok\n"
            "Foo.java:12: error: cannot find symbol\n"
            "noise\n"
            "FAILED  \n"
            "BUILD FAILED in 3s\n"
        )
        self.assertEqual(
            gate.extract_error_lines(text),
            ["Foo.java:12: error: cannot find symbol",
             "FAILED",
             "BUILD FAILED in 3s"],
        )

    def test_extract_error_lines_falls_back_to_last_lines(self):
        lines = ["one", "two", "three", "four", "five"]
        text = "\n".join(lines)
        self.assertEqual(gate.extract_error_lines(text, limit=30), lines)
        self.assertEqual(gate.extract_error_lines(text, limit=2),
                         ["four", "five"])

    def test_extract_error_lines_cuts_long_lines(self):
        text = "error: " + "x" * 395
        result = gate.extract_error_lines(text)
        self.assertEqual(len(result), 1)
        self.assertEqual(len(result[0]), 300)

    def test_extract_error_lines_limits_matched_lines(self):
        text = "error: a\nerror: b\nerror: c\nerror: d"
        self.assertEqual(
            gate.extract_error_lines(text, limit=2), ["error: a", "error: b"])

    def test_extract_keeps_javac_symbol_lines(self):
        text = (
            "A.java:5: error: cannot find symbol\n"
            "  symbol:   class Foo\n"
            "  location: class A\n"
            "noise\n"
        )
        self.assertEqual(
            gate.extract_error_lines(text),
            ["A.java:5: error: cannot find symbol",
             "  symbol:   class Foo",
             "  location: class A"],
        )

    def test_signature(self):
        self.assertEqual(gate.signature(["A.java:12: error: x"]),
                         gate.signature(["A.java:99: error: x"]))
        self.assertNotEqual(gate.signature(["A.java:12: error: x"]),
                            gate.signature(["A.java:12: error: y"]))
        self.assertEqual(gate.signature([]), hashlib.sha1(b"").hexdigest())
        self.assertEqual(gate.signature(["a   b"]), gate.signature(["a b"]))

    def test_files_in(self):
        lines = [
            "src/main/java/p/Foo.java:3: error: x",
            "/abs/prefix/src/test/java/p/BarTest.java:9: error: y",
            "src/main/java/p/Foo.java:7: error: z",
        ]
        self.assertEqual(
            gate.files_in(lines),
            ["src/main/java/p/Foo.java", "src/test/java/p/BarTest.java"],
        )

    def test_snapshot_and_restore(self):
        with tempfile.TemporaryDirectory() as tmp:
            os.makedirs(os.path.join(tmp, "tests", "sub"))
            for relative, content in [
                ("tests/a.txt", "1"),
                ("tests/sub/b.txt", "2"),
                ("other.txt", "3"),
            ]:
                with open(os.path.join(tmp, relative), "w") as handle:
                    handle.write(content)
            before = gate.snapshot(tmp, ["tests/**"])
            self.assertEqual(set(before),
                             {"tests/a.txt", "tests/sub/b.txt"})
            with open(os.path.join(tmp, "tests", "a.txt"), "w") as handle:
                handle.write("X")
            with open(os.path.join(tmp, "tests", "new.txt"), "w") as handle:
                handle.write("new")
            os.remove(os.path.join(tmp, "tests", "sub", "b.txt"))
            with open(os.path.join(tmp, "other.txt"), "w") as handle:
                handle.write("modified")
            fixed = gate.restore(tmp, before, ["tests/**"])
            self.assertEqual(
                fixed,
                ["tests/a.txt", "tests/new.txt", "tests/sub/b.txt"],
            )
            with open(os.path.join(tmp, "tests", "a.txt")) as handle:
                self.assertEqual(handle.read(), "1")
            self.assertFalse(os.path.exists(os.path.join(tmp, "tests",
                                                         "new.txt")))
            with open(os.path.join(tmp, "tests", "sub", "b.txt")) as handle:
                self.assertEqual(handle.read(), "2")
            with open(os.path.join(tmp, "other.txt")) as handle:
                self.assertEqual(handle.read(), "modified")
            self.assertEqual(gate.restore(tmp, before, ["tests/**"]), [])

    def test_build_fix_prompt(self):
        prompt = gate.build_fix_prompt(
            "BRIEF", 2, ["A.java:1: error: x"], ["src/A.java"], [])
        self.assertIn("BRIEF", prompt)
        self.assertIn("FIX ROUND 2", prompt)
        self.assertIn("- src/A.java", prompt)
        self.assertIn("A.java:1: error: x", prompt)
        self.assertNotIn("protected files", prompt)
        touched_prompt = gate.build_fix_prompt(
            "BRIEF", 2, ["A.java:1: error: x"], ["src/A.java"],
            ["tests/t.txt"])
        self.assertIn("protected files", touched_prompt)
        self.assertIn("tests/t.txt", touched_prompt)

    def test_parse_job_output(self):
        text = (
            "job fake-1: done\n"
            "\n"
            "fixed it\n"
            "second line\n"
            "\n"
            "[qwen worker fake-1 | no-think | mode=edit | turns=7 | "
            "out=2133 tok | 32s run, 0s queued | success]\n"
            + "=" * 60 + "\n"
        )
        result = gate.parse_job_output(text)
        self.assertEqual(result["state"], "done")
        self.assertEqual(result["turns"], 7)
        self.assertEqual(result["out_tokens"], 2133)
        self.assertEqual(result["secs"], 32)
        self.assertEqual(result["outcome"], "success")
        self.assertEqual(result["reply"], "fixed it\nsecond line")

    def test_parse_job_output_empty(self):
        self.assertEqual(gate.parse_job_output("")["state"], "died")

    def test_parse_job_outcome_error_max_turns(self):
        text = (
            "job fake-1: done\n"
            "\n"
            "reply\n"
            "\n"
            "[qwen worker fake-1 | no-think | mode=edit | turns=9 | "
            "out=50 tok | 10s run, 0s queued | error_max_turns]\n"
        )
        self.assertEqual(gate.parse_job_output(text)["outcome"],
                         "error_max_turns")


class GateLoopTests(unittest.TestCase):
    """Run gate.py as a subprocess against the fake_qwen.py worker."""

    CHECK = 'grep -q ok target.txt || { echo "Foo.java:12: error: target says $(cat target.txt 2>/dev/null)"; exit 1; }'

    def run_gate(self, script, check, extra=None, files=None):
        tmp = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, tmp, True)
        work = os.path.join(tmp, "work")
        os.makedirs(work)
        out = os.path.join(tmp, "out")
        script_path = os.path.join(tmp, "script.json")
        with open(script_path, "w") as handle:
            json.dump(script, handle)
        brief = os.path.join(tmp, "brief.md")
        with open(brief, "w") as handle:
            handle.write("BRIEF TEXT")
        state_path = os.path.join(tmp, "state.json")
        env = dict(os.environ,
                   QWEN_AGENTS_CLI=os.path.join(HERE, "fake_qwen.py"),
                   FAKE_QWEN_SCRIPT=script_path,
                   FAKE_QWEN_STATE=state_path)
        if files:
            for relative, content in files.items():
                target = os.path.join(work, relative)
                parent = os.path.dirname(target)
                if parent:
                    os.makedirs(parent, exist_ok=True)
                with open(target, "w") as handle:
                    handle.write(content)
        proc = subprocess.run(
            [sys.executable, os.path.join(HERE, "gate.py"),
             "--brief", brief,
             "--check", check,
             "--cwd", work,
             "--out", out] + (extra or []),
            capture_output=True, text=True, timeout=120, env=env,
        )
        return proc, out, work, state_path

    def last_stdout_line(self, proc):
        return proc.stdout.strip().splitlines()[-1]

    def read_jsonl(self, out):
        with open(os.path.join(out, "rounds.jsonl")) as handle:
            return [json.loads(line) for line in handle if line.strip()]

    def test_pass_in_round_one(self):
        proc, out, _work, _state = self.run_gate(
            [{"write": {"target.txt": "ok"}}], self.CHECK)
        self.assertEqual(proc.returncode, 0)
        self.assertTrue(self.last_stdout_line(proc).startswith("PASS rounds=1"))
        records = self.read_jsonl(out)
        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["check_exit"], 0)

    def test_fix_in_round_two(self):
        proc, out, _work, _state = self.run_gate(
            [{"write": {"target.txt": "bad"}},
             {"write": {"target.txt": "ok"}}],
            self.CHECK,
        )
        self.assertEqual(proc.returncode, 0)
        self.assertTrue(self.last_stdout_line(proc).startswith("PASS rounds=2"))
        with open(os.path.join(out, "round-2.prompt.md")) as handle:
            prompt = handle.read()
        self.assertIn("FIX ROUND 2", prompt)
        self.assertIn("target says bad", prompt)
        with open(os.path.join(out, "round-1.prompt.md")) as handle:
            self.assertEqual(handle.read(), "BRIEF TEXT")
        records = self.read_jsonl(out)
        self.assertEqual(len(records), 2)
        self.assertEqual(records[0]["check_exit"], 1)
        self.assertEqual(records[0]["error_count"], 1)

    def test_repeat_stops(self):
        proc, _out, _work, _state = self.run_gate(
            [{"write": {"target.txt": "bad"}}],
            self.CHECK,
            extra=["--max-rounds", "5"],
        )
        self.assertEqual(proc.returncode, 1)
        self.assertTrue(
            self.last_stdout_line(proc).startswith("STUCK(repeat) rounds=2"))

    def test_max_rounds(self):
        proc, out, _work, _state = self.run_gate(
            [{"write": {"target.txt": "bad1"}},
             {"write": {"target.txt": "bad2"}},
             {"write": {"target.txt": "bad3"}}],
            self.CHECK,
        )
        self.assertEqual(proc.returncode, 1)
        self.assertTrue(self.last_stdout_line(proc).startswith(
            "STUCK(max-rounds) rounds=3"))
        with open(os.path.join(out, "result.json")) as handle:
            result = json.load(handle)
        self.assertEqual(result["status"], "STUCK")
        self.assertEqual(result["reason"], "max-rounds")
        self.assertEqual(result["rounds"], 3)

    def test_protected_files_are_restored(self):
        proc, _out, work, _state = self.run_gate(
            [{"write": {"target.txt": "bad", "tests/t.txt": "v1"}},
             {"write": {"target.txt": "ok", "tests/t.txt": "v2"}}],
            self.CHECK,
            extra=["--protect", "tests/**"],
            files={"tests/t.txt": "v0"},
        )
        self.assertEqual(proc.returncode, 0)
        with open(os.path.join(work, "tests", "t.txt")) as handle:
            self.assertEqual(handle.read(), "v1")
        records = self.read_jsonl(_out)
        self.assertEqual(records[-1]["touched"], ["tests/t.txt"])

    def test_allow_protected_edits(self):
        proc, _out, work, _state = self.run_gate(
            [{"write": {"target.txt": "bad", "tests/t.txt": "v1"}},
             {"write": {"target.txt": "ok", "tests/t.txt": "v2"}}],
            self.CHECK,
            extra=["--protect", "tests/**", "--allow-protected-edits"],
            files={"tests/t.txt": "v0"},
        )
        self.assertEqual(proc.returncode, 0)
        with open(os.path.join(work, "tests", "t.txt")) as handle:
            self.assertEqual(handle.read(), "v2")

    def test_job_failure_stops(self):
        proc, _out, _work, _state = self.run_gate(
            [{"state": "failed"}], self.CHECK)
        self.assertEqual(proc.returncode, 1)
        self.assertTrue(self.last_stdout_line(proc).startswith(
            "STUCK(job-failed) rounds=1"))

    def test_error_outcome_still_checks(self):
        proc, _out, _work, _state = self.run_gate(
            [{"outcome": "error_max_turns", "write": {"target.txt": "ok"}}],
            self.CHECK,
        )
        self.assertEqual(proc.returncode, 0)
        self.assertTrue(self.last_stdout_line(proc).startswith("PASS rounds=1"))

    def test_thinking_from_round(self):
        proc, _out, _work, state_path = self.run_gate(
            [{"write": {"target.txt": "bad"}},
             {"write": {"target.txt": "ok"}}],
            self.CHECK,
            extra=["--thinking-from-round", "2"],
        )
        self.assertEqual(proc.returncode, 0)
        self.assertTrue(self.last_stdout_line(proc).startswith("PASS"))
        with open(state_path) as handle:
            state = json.load(handle)
        self.assertFalse(state["jobs"][0]["thinking"])
        self.assertTrue(state["jobs"][1]["thinking"])

    def test_missing_brief_is_usage_error(self):
        proc, _out, _work, _state = self.run_gate([], "true",
                                                 files=None,
                                                 extra=None)
        # brief.md always exists in the helper; run the gate directly here
        # with a nonexistent brief path.
        with tempfile.TemporaryDirectory() as tmp:
            work = os.path.join(tmp, "work")
            os.makedirs(work)
            env = dict(os.environ,
                       QWEN_AGENTS_CLI=os.path.join(HERE, "fake_qwen.py"),
                       FAKE_QWEN_SCRIPT=os.path.join(tmp, "script.json"),
                       FAKE_QWEN_STATE=os.path.join(tmp, "state.json"))
            proc = subprocess.run(
                [sys.executable, os.path.join(HERE, "gate.py"),
                 "--brief", "/nonexistent/file",
                 "--check", "true",
                 "--cwd", work,
                 "--out", os.path.join(tmp, "out")],
                capture_output=True, text=True, timeout=120, env=env,
            )
            self.assertEqual(proc.returncode, 2)


if __name__ == "__main__":
    unittest.main()
