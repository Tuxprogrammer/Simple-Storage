# Per-item tier map (M05 input)

The per-item tier map [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §4 assigns to S2/M05. Every block and item the mod registers gets **exactly one tier** within the 0003 band — early game up to **HV** — gated by the one rule in the Gating notes below: for machine-recipe rows the final-recipe machine tier equals the item's tier (assembler at that tier) and the circuit of that tier is unobtainable before it; ULV crafting-table rows have no machine and no tier-gating input. Item inventory from 01-core `mod-lifecycle.md` row 6 (S1-verified registry names): 15 blocks + 7 items = **22 rows below**: 3 ULV crafting, 13 LV assembler, 1 MV assembler, 4 HV assembler (18 assembler rows plus 3 crafting rows plus the painted state). The network parts sit at LV, only the basic wireless terminal at MV ([D18](../../design/decision-log.md)) (`painted_trim` is the painted state of `trim` — same item, no separate recipe).

**Verified tier facts used here** ([gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md)): LV = 32 EU/t (cheapest machine, `V[1]`), MV = 128 EU/t (`V[2]`), HV = 512 EU/t (`V[3]`); every single-block GT machine exists at tiers 1–12 (LV–UMV); the assembler (`gt.recipe.assembler`) exists at all of them.

## The map

| Item (registry name) | Tier | Final recipe: machine + map | Key input that gates the tier | Notes |
|---|---|---|---|---|
| `open_crate` (Open Crate) | ULV | Crafting table (no machine; GT recipe type per [recipes-and-materials.md](recipes-and-materials.md) §crafting) | none — GT wood parts, obtainable from the start (band floor) | Virtual inventory of item entities in front of it; keep the upstream "creates item entities" warning (02-storage `open-crate.md`) |
| `trim` (Inventory Trim) | ULV | Crafting table | none — GT wood parts (band floor) | Gap-filler for connector scans; painted variant below |
| `painted_trim` (painted state of `trim`) | ULV | no separate recipe — painted in-game with the paint kit | same as `trim` | One block state, not a second item (02-storage `trims-paint.md`) |
| `paint_kit` (Paint Kit) | ULV | Crafting table (dyes + GT wood parts) | none — dyes and wood parts are start-obtainable (band floor) | Paints any paintable block to match surroundings (02-storage `trims-paint.md`) |
| `filing_cabinet` (Filing Cabinet) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` (`OrePrefixes.circuit.get(Materials.LV)`) + steel plates (LV-era GT material) | 512-slot single-item-type storage; the early-game "storage terminal" (02-storage `filing-cabinet.md`) |
| `storage_terminal` (Storage Terminal) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 | `circuitLV` + glass plates (LV-era GT material) | The basic terminal (0003 §3 early list); works on any `IInventory`/`ISidedInventory` — usable on a plain chest before the network exists (03-terminal `terminal-blocks.md`) |
| `item_filter` (Item Filter) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 | `circuitLV` + redstone alloy parts | NBT-matching filter item for hoppers/emitters (03-terminal `item-filter-gui.md`) |
| `tag_item_filter` (Tag Item Filter) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 | `circuitLV` + GT quartz parts (LV-era) | Tag-based filter (03-terminal `item-filter-gui.md`) |
| `inventory_configurator` (Inventory Configurator) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 | `circuitLV` + GT parts (band-floor tool; 0003 §3 early list) | Hand tool that binds to an inventory connector and edits priority/side/skip (03-terminal `inventory-configurator.md`); is reachable early per 0003 |
| `inventory_cable` (Inventory Cable) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT glass parts (LV-era) | Functional-node wiring; a cable network needs ≥1 connector (02-storage `cables.md`) |
| `inventory_cable_framed` (Framed Inventory Cable) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT glass frame parts (LV-era) | Aesthetic framed variant of the cable (02-storage `cables.md`) |
| `inventory_cable_connector` (Inventory Cable Connector) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT glass parts (LV-era) | Attaches an inventory face to the cable network; beacon-level link config (02-storage `cable-connector.md`) |
| `inventory_cable_connector_framed` (Framed Cable Connector) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT glass frame parts (LV-era) | Framed variant of the cable connector |
| `inventory_connector` (Inventory Connector) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT parts (LV-era) | Merges touching inventories; scans every 20 ticks, range 16 (02-storage `inventory-connector.md`) |
| `inventory_proxy` (Inventory Proxy) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT parts (LV-era) | Extends the face of the block it points at (02-storage `inventory-proxy.md`) |
| `inventory_interface` (Inventory Interface) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT glass parts (LV-era) | Pass-through view of a connector's network (02-storage `inventory-interface.md`) |
| `basic_inventory_hopper` (Basic Inventory Hopper) | LV | LV Assembler — `gt.recipe.assembler` @ tier 1 (32 EU/t) | `circuitLV` + GT hopper/piston parts (LV-era) | Filtered 1-item-per-cycle transfer in/out of the network; `basicHopperCooldown` config (02-storage `hopper.md`) |
| `wireless_terminal` (Wireless Terminal, basic) | MV | MV Assembler — `gt.recipe.assembler` @ tier 2 | `circuitMV` + GT quartz parts | Range-extending item (reach 16 blocks, `wirelessReach` config); stack size 1 (04-wireless `wireless-items.md`) |
| `crafting_terminal` (Crafting Terminal) | HV | HV Assembler — `gt.recipe.assembler` @ tier 3 (512 EU/t) | `circuitHV` + GT redstone/glass parts (HV-era) | Terminal + built-in crafting grid, auto-fill, recipe book (03-terminal `crafting-terminal.md`) |
| `adv_wireless_terminal` (Advanced Wireless Terminal) | HV | HV Assembler — `gt.recipe.assembler` @ tier 3 | `circuitHV` + GT quartz fibre parts (HV-era) | Binds to one terminal; range 64 / beacon-extended (04-wireless `wireless-items.md`); the top of the band |
| `level_emitter` (Level Emitter) | HV | HV Assembler — `gt.recipe.assembler` @ tier 3 | `circuitHV` + GT redstone parts (HV-era) | Redstone signal from network contents; **the 0004 optional EU sink target: 8 EU/t (ULV rate) while active, off by default** ([gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md) §EU model) |
| `polymorphic_item_filter` (Polymorphic Item Filter) | HV | HV Assembler — `gt.recipe.assembler` @ tier 3 | `circuitHV` + GT parts | Multi-item filter with component matching (03-terminal `item-filter-gui.md`) |

## Band check against 0003

- **Floor (reachable early):** crate, trims and paint kit are ULV crafting-table items; cabinet, storage terminal, filters, configurator and (D18) all network parts are LV (one LV machine and the LV circuit). ✔ matches 0003 §3 "LV or below".
- **Network parts LV, one MV item (D18):** cable, framed cable, connectors, inventory connector, interface, proxy and basic hopper are LV; the basic wireless terminal alone is MV; no network part above HV. ✔
- **Advanced at HV:** crafting terminal, advanced wireless terminal, level emitter, polymorphic filter — all HV, nothing above. ✔
- **Cap: HV = 512 V** in this GT build (verified cap, see [gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md) §Correction to 0003). The cap is enforced by the M05 dev self-check: any recipe whose output tier exceeds HV, or that uses an input/machine above the output's tier, fails the check (0003 §Consequences).

## Gating notes

- The **circuit** (`circuitLV`/`circuitMV`/`circuitHV`) is the tier gate: GT makes circuits in the Circuit Assembler (`gt.recipe.circuitassembler`, exists at tiers 1–14 — [recipes-and-materials.md](recipes-and-materials.md)), and the tier of the circuit is the tier of the machine that made it. A `circuitMV` cannot exist before the player has an MV circuit assembler, so the one MV row (basic wireless terminal) is not obtainable earlier; the 13 LV rows need only `circuitLV`.
- **Secondary materials** (steel plates and glass at LV, quartz parts at MV, gold- and silver-based parts at HV) are tier-appropriate GT materials obtainable by that tier (inferred from GT's ore progression; the verifiable gate is the circuit). No mod-own material appears (0003 §1).
- **Crafting-table rows** (ULV): the machine-voltage half of the gate is trivially met (no machine); the band floor is the gate. No ULV item uses a machine-tier input.
- **Nothing obtainable before its tier:** M05's dev self-check runs the rule per row — for a machine-recipe row, the final-recipe machine tier **equals** the item's tier (assembler at that tier) and at least one input (the circuit of that tier) is unobtainable before that tier; ULV crafting-table rows have no machine and no tier-gating input. No recipe may use a machine or input above the item's tier, and no output may exceed the HV cap (512V). The circuit column is that input for every LV/MV/HV row.

## Sources

- Item inventory: 01-core `mod-lifecycle.md` row 6 (S1, registry names in namespace `toms_storage`); per-item behavior sheets in areas `02-storage`, `03-terminal`, `04-wireless`.
- Tiers/rates/machines: [gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md) (GT5-Unofficial 5.09.54.265, GTValues.java#L92-L150, MTEBasicMachine.java#L747-L751, LoaderMetaTileEntities.java).
- Maps/builder/circuits: [recipes-and-materials.md](recipes-and-materials.md) (RecipeMaps.java#L1265, GTRecipeBuilder.java#L173/#L1201, GTModHandler.java#L662-L690).
- Band and rules: [0003](../../design/0003-gtnh-layer-and-tier-ceiling.md) §3-4; EU sink: [0004](../../design/0004-energy-and-machine-model.md) §3.
