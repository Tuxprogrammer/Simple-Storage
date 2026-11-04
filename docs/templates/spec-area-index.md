# NN · Area name

<!-- Two layouts. Part 1: an original-spec area. Part 2: a new-spec area. In both, index.md is a map of AT MOST 40 LINES (check with wc -l); tables and long sections live in sibling files. Write "none" rather than omitting a section. -->

## Part 1 · Original-spec area

An area is `index.md` plus `facts.md`, `coverage.md`, `bugs.md` and, optionally, `lang.md`, next to the sheets.

### index.md (at most 40 lines)

```markdown
# NN · Area name

What this area covers, in a few sentences, and which version of the original mod it documents.

## Contents

- `[sheet-a.md](sheet-a.md)`: one line on what it documents
- `[sheet-b.md](sheet-b.md)`: ...

| File | What it holds |
|---|---|
| [facts.md](facts.md) | Key facts and related areas |
| [coverage.md](coverage.md) | Files read, assets and lang keys covered, known limits |
| [bugs.md](bugs.md) | Suspected bugs, N rows |
| [lang.md](lang.md) | Lang keys (optional) |
```

### facts.md

Key facts (things an implementer must know before reading any sheet; optional) and Related areas (`NN-other-area`: why it is related).

### coverage.md

- Source files read: N of M in scope
- Textures / sounds / lang keys covered: N of M each
- Corrections made to earlier statements, with dates
- Known limits: what could not be verified, and why (could not run it, could not hear the audio, dead code paths)

### bugs.md

| ID | Title | Location | What looks wrong | Severity |
|---|---|---|---|---|
| AREA-01 | | `path/Class.java:123` | | crash / gameplay / performance / visual / cosmetic / not a bug |

<!-- Merged and deduplicated into BUGS.md. Suspected, not confirmed; no fixes designed here. -->

### lang.md (optional)

Keys used by the in-scope classes, with exact text and source file. Omit if the area has none.

## Part 2 · New-spec area

An area is `index.md` plus `deviations.md` and `proposals.md` (plus any area-specific files, such as `config.md`).

### index.md (at most 40 lines)

```markdown
# NN · Area name

What changes in the new mod, in a few lines; link the original-spec area.

Status: draft. Implementation: none yet (roadmap MNN).

| File | What it holds |
|---|---|
| [deviations.md](deviations.md) | Sheets, how to read the table, the deviation table (N rows) |
| [proposals.md](proposals.md) | Contradictions with other docs; proposed, not decided (N rows) |
```

### deviations.md

`# Area · deviations`, then `Part of [Area](index.md).`, then Sheets (if any), How to read the table, `## Deviation table` (keep the heading so `#deviation-table` links work) and any other tables that are neither contradictions nor proposals.

### proposals.md

`# Area · contradictions and proposals`, then the same back-link, then Contradictions with other docs and `## Proposed (not decided)`. Every proposal also appears in [OPEN-QUESTIONS.md](../new-spec/OPEN-QUESTIONS.md).
