"""Unit tests for namecheck.py, the known-bad-names pre-check.

Runs from the repo root with:
    python3 -m unittest tools/qwen/test_namecheck.py
"""

import contextlib
import io
import os
import re
import shutil
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import namecheck  # noqa: E402


class NamecheckTests(unittest.TestCase):

    def test_load_slips_skips_comments_and_blanks(self):
        tmp = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, tmp)
        slips_path = os.path.join(tmp, "slips.tsv")
        with open(slips_path, "w", encoding="utf-8") as handle:
            handle.write(
                "# a comment line\n"
                "\n"
                "no tab in this row\n"
                "\\bBlockPos\\b\tthree ints x, y, z\n"
                "\\bEnumFacing\b\tnet.minecraftforge.common.util.ForgeDirection\n"
            )
        slips = namecheck.load_slips(slips_path)
        self.assertEqual(len(slips), 2)

    def test_scan_text_reports_line_and_advice(self):
        slips = [(re.compile(r"\bBlockPos\b"), "use ints")]
        text = "int a;\nBlockPos p;\n"
        findings = namecheck.scan_text(text, slips, "A.java")
        self.assertEqual(findings,
                         ["error: A.java:2: BlockPos -> use ints"])

    def test_scan_text_skips_comment_lines(self):
        slips = [(re.compile(r"\bBlockPos\b"), "use ints")]
        text = "// BlockPos\n * BlockPos\n/* BlockPos */\n"
        self.assertEqual(namecheck.scan_text(text, slips, "A.java"), [])

    def test_scan_text_skips_trailing_comment(self):
        slips = [(re.compile(r"\bBlockPos\b"), "use ints")]
        self.assertEqual(
            namecheck.scan_text("int x; // BlockPos", slips, "A.java"), [])

    def test_scan_text_ignore_marker(self):
        slips = [(re.compile(r"\bBlockPos\b"), "use ints")]
        self.assertEqual(
            namecheck.scan_text("BlockPos p; // namecheck:ignore", slips,
                                "A.java"), [])

    def test_scan_tree_walks_src_only(self):
        tmp = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, tmp)
        os.makedirs(os.path.join(tmp, "src", "a"))
        os.makedirs(os.path.join(tmp, "docs"))
        with open(os.path.join(tmp, "src", "a", "B.java"), "w") as handle:
            handle.write("BlockPos p;\n")
        with open(os.path.join(tmp, "docs", "C.java"), "w") as handle:
            handle.write("BlockPos q;\n")
        slips = [(re.compile(r"\bBlockPos\b"), "use ints")]
        findings = namecheck.scan_tree(tmp, slips)
        self.assertEqual(findings,
                         ["error: src/a/B.java:1: BlockPos -> use ints"])

    def test_main_exit_codes(self):
        # bad tree: findings -> exit 1, first line starts with "error:"
        bad_root = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, bad_root)
        os.makedirs(os.path.join(bad_root, "src"))
        with open(os.path.join(bad_root, "src", "Bad.java"), "w") as handle:
            handle.write("BlockPos p;\n")
        slips_path = os.path.join(bad_root, "slips.tsv")
        with open(slips_path, "w", encoding="utf-8") as handle:
            handle.write("\\bBlockPos\\b\tuse ints\n")
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            code = namecheck.main(["--root", bad_root, "--slips", slips_path])
        out = buf.getvalue()
        self.assertEqual(code, 1)
        self.assertTrue(out.splitlines()[0].startswith("error:"))

        # clean tree: exit 0 and "namecheck: clean"
        clean_root = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, clean_root)
        os.makedirs(os.path.join(clean_root, "src"))
        with open(os.path.join(clean_root, "src", "Good.java"), "w") as handle:
            handle.write("int a;\n")
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            code = namecheck.main(["--root", clean_root, "--slips",
                                   slips_path])
        self.assertEqual(code, 0)
        self.assertIn("namecheck: clean", buf.getvalue())

        # missing slips file: exit 2
        missing = os.path.join(clean_root, "nope.tsv")
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            code = namecheck.main(["--root", clean_root, "--slips",
                                   missing])
        self.assertEqual(code, 2)

    def test_real_slips_file_loads(self):
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                            "..", "..", "docs", "agent-setup", "slips.tsv")
        slips = namecheck.load_slips(path)
        self.assertGreaterEqual(len(slips), 20)
        findings = namecheck.scan_text("EnumFacing f;", slips, "X.java")
        self.assertEqual(len(findings), 1)


if __name__ == "__main__":
    unittest.main()
