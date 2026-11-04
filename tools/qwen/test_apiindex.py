"""Unit tests for apiindex.py, the javap-based public API index.

The build subcommand is not unit tested (it needs unzip and javap); the
pure parse, format and lookup functions are tested against a literal
sample of javap -public output.
"""

import os
import sys
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

import apiindex  # noqa: E402


SAMPLE = "\n".join([
    'Compiled from "ForgeDirection.java"',
    "public final class net.minecraftforge.common.util.ForgeDirection "
    "extends java.lang.Enum<net.minecraftforge.common.util.ForgeDirection> {",
    "    public static final net.minecraftforge.common.util.ForgeDirection "
    "DOWN;",
    "    public final int offsetX;",
    "    public static net.minecraftforge.common.util.ForgeDirection "
    "getOrientation(int);",
    "    public net.minecraftforge.common.util.ForgeDirection "
    "getOpposite();",
    "  }",
    "public class net.minecraft.entity.item.EntityItem "
    "extends net.minecraft.entity.Entity {",
    "    public net.minecraft.entity.item.EntityItem"
    "(net.minecraft.world.World);",
    "    public net.minecraft.item.ItemStack getEntityItem();",
    "    public void setEntityItem(net.minecraft.item.ItemStack);",
    "  }",
])


def parsed_rows():
    return apiindex.parse_javap(SAMPLE)


class ParseTests(unittest.TestCase):
    def test_parse_counts(self):
        rows = parsed_rows()
        self.assertEqual(len(rows), 9)
        by_kind = {"class": 0, "field": 0, "method": 0, "ctor": 0}
        for _class, kind, _sig in rows:
            by_kind[kind] += 1
        self.assertEqual(by_kind["class"], 2)
        self.assertEqual(by_kind["field"], 2)
        self.assertEqual(by_kind["method"], 4)
        self.assertEqual(by_kind["ctor"], 1)

    def test_parse_kinds(self):
        rows = parsed_rows()
        by_name = {}
        for _class, kind, sig in rows:
            by_name[apiindex.member_name(kind, sig)] = (kind, sig)
        self.assertEqual(by_name["getOrientation"][0], "method")
        self.assertEqual(by_name["offsetX"][0], "field")
        ctor = by_name["EntityItem"]
        self.assertEqual(ctor[0], "ctor")
        self.assertEqual(ctor[1],
                         "public net.minecraft.entity.item.EntityItem"
                         "(net.minecraft.world.World)")

    def test_parse_class_rows(self):
        rows = parsed_rows()
        classes = [sig for _c, kind, sig in rows if kind == "class"]
        self.assertEqual(len(classes), 2)
        self.assertIn("ForgeDirection", classes[0])
        self.assertNotIn(" {", classes[0])
        self.assertIn("EntityItem", classes[1])


class MemberNameTests(unittest.TestCase):
    def test_member_name(self):
        rows = parsed_rows()
        by_name = {}
        for _class, kind, sig in rows:
            by_name.setdefault(apiindex.member_name(kind, sig), sig)
        self.assertIn("getEntityItem",
                      by_name["getEntityItem"])
        self.assertIn("offsetX", by_name["offsetX"])
        class_sig = [sig for _c, kind, sig in rows if kind == "class"
                     and "ForgeDirection" in sig][0]
        self.assertEqual(apiindex.member_name("class", class_sig),
                         "ForgeDirection")


class SimpleNameTests(unittest.TestCase):
    def test_simple_name(self):
        self.assertEqual(apiindex.simple_name("net.minecraft.Foo"), "Foo")
        self.assertEqual(apiindex.simple_name("a.b.Outer$Inner"), "Inner")
        self.assertEqual(apiindex.simple_name("a.b.Outer$Inner$Deep"),
                         "Deep")


class FormatTests(unittest.TestCase):
    def test_format_parse_round_trip(self):
        row = ("net.minecraft.Foo", "method",
               "public int getValue()")
        line = apiindex.format_line(*row)
        self.assertEqual(apiindex.parse_line(line), row)

    def test_parse_line_rejects_bad_lines(self):
        self.assertIsNone(apiindex.parse_line("a\tb"))
        self.assertIsNone(apiindex.parse_line("a\tb\tc\td"))


class LookupTests(unittest.TestCase):
    def setUp(self):
        self.index = parsed_rows()

    def test_lookup_exact_first(self):
        results = apiindex.lookup(self.index, "getEntityItem")
        self.assertTrue(results)
        self.assertIn("getEntityItem", results[0])

    def test_lookup_prefix_and_contains(self):
        results = apiindex.lookup(self.index, "Entity")
        self.assertTrue(any("EntityItem" in line for line in results))

    def test_lookup_fuzzy(self):
        results = apiindex.lookup(self.index, "getEntityItm")
        self.assertTrue(any("getEntityItem" in line for line in results))

    def test_lookup_case_insensitive(self):
        results = apiindex.lookup(self.index, "getentityitem")
        self.assertTrue(any("getEntityItem" in line for line in results))

    def test_lookup_limit(self):
        self.assertEqual(len(apiindex.lookup(self.index, "Entity",
                                             limit=1)), 1)

    def test_lookup_none(self):
        self.assertEqual(apiindex.lookup(self.index, "zzzzzz"), [])


if __name__ == "__main__":
    unittest.main()
