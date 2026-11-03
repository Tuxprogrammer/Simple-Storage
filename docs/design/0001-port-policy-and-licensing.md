# 0001 · Port policy and licensing

- Status: Accepted
- Gate: none (foundation)
- Amends: none
- Date: 2026-11-02

## Context

Simple Storage backports Tom's Simple Storage Mod to Minecraft 1.7.10 for GT New Horizons. Upstream is **MIT** (Copyright (c) 2020 tom5454) on every branch. The baseline is branch `26.2` at commit `95bb61d` (mod version 2.11.3, Minecraft 26.2, NeoForge 26.2.0.90) of [tom5454/Toms-Storage](https://github.com/tom5454/Toms-Storage). Older branches exist too (`1.15`, `1.16`, `1.17`, `1.18`, `1.19`, `1.20.x`, `1.21.x`, `26.1`, `master`), all under the same MIT license.

The owner's standing rule for ported code is that a port does not come in until it meets the quality bar of native code. Cloning for speed is how bad code ships. The goal is "faithful to the functionality, not to the bugs".

## Decision

1. **The baseline is upstream `26.2` at `95bb61d`.** The behavior spec ([docs/original-spec/](../original-spec/README.md)) documents that commit. Later upstream commits come in only through a deliberate re-pin: update the local `26.2` branch, diff it against the pin, and update the spec and this record's commit.
2. **The NeoForge side is the port baseline.** The shared code in `NeoForge/src/platform-shared/` is what the port ports from, with the `NeoForge/` sources as the loader context. The `Fabric/` side is a reference: where a fact differs by loader, the spec sheet says which loader it came from.
3. **Upstream code may be ported, under the ported-code gate** (`CONTRIBUTING.md` § Ported code). A port is a rewrite that starts from Tom's logic:
   - it follows our package layout, naming and idioms, and the Java 8 API;
   - it carries Javadoc and `// Spec:` comments as native code does;
   - unit tests cover the spec's numbers;
   - every upstream bug in the ported logic is fixed, with a regression test per bug-register ID.

   It is never a copy made for speed.
4. **Provenance on every ported file.** A header comment names the upstream file at the pinned commit (`Ported from Tom's Simple Storage (MIT): <permalink>`), then one `Avoids: <ID>` line per bug the port fixes.
5. **The notice.** Tom's copyright line is kept in [THIRD-PARTY-NOTICES.md](../../THIRD-PARTY-NOTICES.md), which names the upstream repository, the pinned commit and the license, and in the jar's copy of it. The `LICENSE` file is ours: MIT, sole copyright Spencer Callicott 2026 (owner decision 2026-11-02), with the upstream credit in `mcmod.info` `credits` only.
6. **Upstream textures and models are reused.** They are MIT under the repository license and may be carried over, each with a provenance line in its folder's README and a THIRD-PARTY-NOTICES entry. Any image the port needs that upstream lacks is made anew. Our lang file is our own (American English, our lang keys); upstream's non-English translations are carried over only where S1 finds their keys still resolve.
7. **Older upstream branches are references, not baselines.** `1.15` and `1.16` above all are much closer to 1.7.10's APIs (the old container and slot classes, the old network and registry mechanics). Their code may be read, and ported under the same gate, wherever it solves a 1.7.10 problem that `26.2` solves with newer APIs. Cite it with a permalink pinned to its commit.
8. **Our own work is MIT**, copyright Spencer Callicott 2026. Other third-party material follows `CONTRIBUTING.md` § Assets: CC0, CC-BY, MIT and LGPL are acceptable, each with its license and a notices entry. Nothing NC, ND, All Rights Reserved or unlicensed is acceptable. GPL code is not copied in while the mod is MIT.

## Consequences

- Builders may read upstream code while building; clean-room separation is not required. The spec is still the behavior contract: tests check the spec's numbers, and the bug register says what must not be reproduced.
- `PortedCodeGateTest` (roadmap M01) checks the header, the notice, the Javadoc, the test class and the bug IDs on every ported file, so CI fails a port that misses any of them.
- Deviations from upstream behavior are recorded in [docs/new-spec/](../new-spec/README.md) and in design records.

## Alternatives considered

- **Spec-first clean room.** The license does not require it, and rebuilding MIT logic blind costs time without a quality gain.
- **Re-licensing to AGPL or GPL.** Upstream is MIT and our work starts as a port of it, so a copyleft license buys nothing up front; it stays open as a later option if an integration truly needs copyleft pack-mod code.
- **Straight port, refactor later.** Rejected under the owner's rule: ported code is not committed until it meets the bar.

## Still open

None.
