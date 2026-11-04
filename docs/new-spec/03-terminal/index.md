# 03-terminal: deviations in the new mod

What this mod (GTNH 2.9.0, Minecraft 1.7.10, Forge 10.13.4.1614) does differently from the original `03-terminal` area ([original-spec index](../../original-spec/03-terminal/index.md), 14 sheets). Nothing here is a decision unless the Decision column cites a D-row or record; everything else is **Proposed** and listed under "Proposed (not decided)".

## Sheets

No extra sheets: the single table below covers all 14 original sheets. Integration with NEI and Waila for the terminal GUI lives in [05-integration](../05-integration/index.md).

## How to read the table

| Column | Meaning |
|---|---|
| Original | The original-spec sheet (linked). |
| Here | `same` (behavior kept), `changed`, `dropped`, `deferred` (later than alpha), `new`. |
| What changes | Concrete: names, numbers with units, 1.7.10 classes. API names come from [API-MAP](../../original-spec/API-MAP.md); a 1.7.10 class not read in `00-pack-integration/` or API-MAP is marked "unverified". |
| Decision | A [decision-log](../../design/decision-log.md) D-row or a design record (Accepted), or `Proposed P03-n`. |
| Bugs | [BUGS.md](../../original-spec/BUGS.md) IDs the row owns. |

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [terminal-blocks](../../original-spec/03-terminal/terminal-blocks.md) | changed | Terminal and crafting terminal are ordinary 1.7.10 `Block` + `TileEntity` pairs, not GT MetaTileEntities and power-free. Recipe goes into `gt.recipe.assembler`; the storage terminal is tier LV (32 EU/t band). Reach stays 6 blocks (7 with the open-menu check); beacon scan stays every 40 ticks over a 17x17x17 box, but the gate that can never pass for one position hash is fixed. Registration is unconditional; `modules.terminal` gates behavior only. | D8, D9, D13, D14, D16, [0004](../../design/0004-energy-and-machine-model.md); scan fix Proposed P03-4 | TERM-04 (alias WIRE-04) |
| [storage-terminal](../../original-spec/03-terminal/storage-terminal.md) | changed | `Container` subclass; the 5x9 display "slots" stay `SlotStorage` records and the screen plus a `SlotAction` message re-implement shift-click and drag. Container data slots (`DataSlots`) have no 1.7.10 counterpart: the control mode, sort and search state go in a custom packet. Sort index read from NBT is range-checked, the stat tooltip guards a zero slot count, the wheel step is derived from the visible row count, not a hardcoded 5. Modes and sort bit layouts (`0x00-0x03` mode, `0x10` tall, `0x20` pull-from-inv; `0x00-0x05` type, `0x100` reverse, `0x200` ghost off) are unchanged. | Proposed P03-1, P03-5 | TERM-01, TERM-02, TERM-03 |
| [crafting-terminal](../../original-spec/03-terminal/crafting-terminal.md) | changed | Result slot (120, 129) and 3x3 grid keep their positions; GUI 194x256 px. The vanilla recipe book (`RecipeBookComponent`, `GhostSlots`, `PlaceRecipeHelper`) has no 1.7.10 counterpart (API-MAP): the in-GUI book and its width >= 379 px layout are dropped and NEI's overlay takes its job. Auto-fill reserves the stack per grid cell, so one item is never counted for two cells. | D11 (overlay); drop and fill Proposed P03-2 | TERM-05 |
| [filing-cabinet-gui](../../original-spec/03-terminal/filing-cabinet-gui.md) | same | 512 slots, 5 visible rows, 12x15 px scroller kept. Wheel scroll no longer divides by (rows - 5) when the cabinet has no hidden rows. Overlay on the cabinet GUI via `API.registerGuiOverlay`. | D11; fix Proposed P03-5 | TERM-06 |
| [inventory-configurator](../../original-spec/03-terminal/inventory-configurator.md) | same | Filter slot (80, 32) and six buttons kept; distance-squared < 256 range check stays server-side. The keep-last button gets its own narrator key (the original reuses "skip"). Item exists as a normal `Item`; recipe in `gt.recipe.assembler`. | D8, D16; key fix Proposed P03-5 | TERM-07 |
| [inventory-link](../../original-spec/03-terminal/inventory-link.md) | same | Channel list 114x100 px, 16 px rows kept. A typed channel name resolves to exactly one channel (exact match first, then a unique prefix; ambiguous input selects nothing). | Proposed P03-5 | TERM-08 |
| [level-emitter-gui](../../original-spec/03-terminal/level-emitter-gui.md) | changed | Phantom slot (43, 38) and amount buttons +-1/10/100/1000 kept; the SHIFT step table is made symmetric (+ and - use the same multiplier). The emitter may draw an optional 8 EU/t (ULV) while active, off by default (`euSink.enabled`). Phantom slot accepts an item by direct click or drag (no NEI ghost drag at alpha). | D7, D11; step table Proposed P03-5 | TERM-10 |
| [item-filter-gui](../../original-spec/03-terminal/item-filter-gui.md) | changed | 3x3 phantom grid at (62 + j*18, 17 + i*18) kept. Shift-click fills the first empty phantom slot not already holding that item, and never leaves a placeable item unplaced. The tag filter has no item tags in 1.7.10 (they are an `OreDictionary` name set): see P03-3. "Match components" becomes "match NBT" (stack tag equality). Menu opened through `IGuiHandler` (unverified). | D11 (alpha: no ghost drag); P03-3 | TERM-11 |
| [filters](../../original-spec/03-terminal/filters.md) | changed | `IFilter`, `MultiFilter` (priority sum mapped back through `Priority.fromSorting`) and the 9-slot `SimpleItemFilter` kept. Data-component equality becomes item + damage + `NBTTagCompound` equality; `TagFilter` matches `OreDictionary` names (unverified in the pack sources read so far). | P03-3 | none |
| [sorting](../../original-spec/03-terminal/sorting.md) | changed | NBT ordinals 0 AMOUNT .. 5 TYPE are unchanged so old saves load. TYPE order source is the 1.7.10 creative-tab item list (`CreativeTabs`); name key is `Item.itemRegistry` name. The lookup cache is synchronized (or rebuilt on a single thread), 300000 ms expiry kept. | Proposed P03-6 | TERM-09 |
| [terminal-stacks](../../original-spec/03-terminal/terminal-stacks.md) | changed | `StoredItemStack` keeps a `long` count and the `31 * itemHash + tagHash` int hash, where `tagHash` is the hash of the stack's `NBTTagCompound` (0 if none); `hashZero` flag kept. Display name via `getDisplayName`. | Proposed P03-7 | none |
| [inventory-access](../../original-spec/03-terminal/inventory-access.md) | changed | Resolution order is `ISidedInventory` -> `IInventory` -> AE2 (optional, off at alpha) -> `EMPTY`. The 10-tick network rescan cooldown stays. The multi-inventory change tracker rebuilds its `trackers`/`items` arrays whenever `connected` grows, not only when the tick changes. Offsets compaction (CORE-03) and negative filtered extract (CORE-04, keepLast) are fixed in the same stack code. Multithreaded tracking (parallel streams) is dropped in favor of the server thread; see P03-8. | D10; P03-8 | TERM-12, CORE-03, CORE-04 |
| [container-screens](../../original-spec/03-terminal/container-screens.md) | changed | `TSContainerScreen` becomes a `GuiContainer` subclass (1.7.10; `mouseScrolled` is handled in `handleMouseInput` via `Mouse.getEventDWheel`, unverified); `IScreen` is the mod's own interface. See "Re-implemented widget and screen behavior" below. | D11; P03-1 | none |
| [terminal-utils](../../original-spec/03-terminal/terminal-utils.md) | changed | `NumberFormatUtil` kept (base 1000, postfixes `KMGTPE`, 4 chars, `.#;0.#`, RoundingMode.DOWN). `ValueInput` becomes `NBTTagCompound`. `KeyUtil` checks both left and right Shift/Ctrl/Alt via `Keyboard.isKeyDown` (LWJGL2). `IAutoFillTerminal` and `ISearchHandler` contract is kept for the NEI plugin. `CraftingMatrix` wraps a 1.7.10 `InventoryCrafting`. Missing lang key (CORE-07/UI-03) is defined. | M03 exit gate; `CraftingMatrix` Proposed P03-7 | CORE-07 (alias UI-03), CORE-10 |
| [index](../../original-spec/03-terminal/index.md) | changed | Lang-key table is regenerated for the 1.7.10 `.lang` format; every narrator key is defined (`LangKeysTest`). Polymorphic terminal widget is not registered because Polymorph is not in the pack. | D16; ROADMAP M03 | TERM-01..TERM-12, CORE-08 (alias INT-08) |

Bug placement summary (each in a Bugs cell above): TERM-01, 02, 03 storage-terminal; TERM-04 terminal-blocks; TERM-05 crafting-terminal; TERM-06 filing-cabinet-gui; TERM-07 inventory-configurator; TERM-08 inventory-link; TERM-09 sorting; TERM-10 level-emitter-gui; TERM-11 item-filter-gui; TERM-12 inventory-access; CORE-03/04 inventory-access; CORE-07/10 terminal-utils; CORE-08 index (not registered; INT-08 in [05-integration](../05-integration/index.md)).

## Re-implemented widget and screen behavior

1.7.10 lacks the NeoForge equivalent for each of these (API-MAP rows), so the mod writes them itself.

| Behavior | NeoForge piece | 1.7.10 plan | Evidence |
|---|---|---|---|
| Menu data sync (mode, sort, search) | `DataSlots` | custom packet fields | [API-MAP](../../original-spec/API-MAP.md) (container data slots row); item-filter-gui row 9 |
| Menu to screen binding | `RegisterMenuScreensEvent` | `IGuiHandler` (unverified) | API-MAP (menu registration row) |
| Recipe book in the crafting terminal | `RecipeBookComponent`, `GhostSlots`, `PlaceRecipeHelper` | none; NEI overlay replaces it | API-MAP; crafting-terminal row 9 |
| Nine-slice button/panel sprites | `WidgetSprites` | `Gui.drawModalRectWithCustomSize` | API-MAP |
| Text field | `EditBox` | `GuiTextField` (unverified) | API-MAP |
| Ghost-ingredient drag from a viewer | JEI `IGhostIngredientHandler` | beta candidate via `INEIGuiHandler` | D11 |
| Serialization | codecs, `ValueInput` | `NBTTagCompound` | terminal-utils row 9 |
| Keybind and tooltip hooks | `KeyMapping`, `ItemTooltipEvent` | `KeyBinding` plus client tick; `Item.addInformation` (API-MAP says `addTooltip`, unverified) | API-MAP |
| Wheel scroll, shift-click, drag on display slots | `AbstractContainerScreen` overrides | `GuiContainer.handleMouseInput`, `SlotAction` packet, server check on every action | storage-terminal sheet |

## Contradictions with other docs

| Where | Quote | Conflicts with |
|---|---|---|
| [OPEN-QUESTIONS.md:20](../OPEN-QUESTIONS.md) (Q1) | tier table "shifted one tier up vs classic GT5" | [decision-log.md:23](../../design/decision-log.md) (D15) "matches classic GT5 (no shift)"; this area follows D15 |
| [API-MAP.md:59](../../original-spec/API-MAP.md) | "Forge capability system (`ICapabilityProvider`/`hasCapability`)" for inventory access | D10 (`ISidedInventory` chain); Forge 10.13.4.1614 capability system unverified, D10 wins |
| [API-MAP.md:34](../../original-spec/API-MAP.md) | tooltip hook `Item.addTooltip` | 1.7.10 name is believed to be `Item.addInformation`; unverified |
| [API-MAP.md:37](../../original-spec/API-MAP.md) | `IBlockColor`/`BlockColorMap` registration | not a 1.7.10 API name as far as read; unverified |
| [original-spec/03-terminal/index.md:171](../../original-spec/03-terminal/index.md) | "None yet (port notes go to the new-spec 03-terminal area)." | not the literal "None yet." body; left as is (see rule note in the report) |

## Proposed (not decided)

| Id | Question | Recommendation | Settled by |
|---|---|---|---|
| P03-1 | How does the terminal GUI base handle display slots, scroll and the data packet? | One `GuiContainer` subclass (`TSContainerScreen`) with a `SlotAction` packet, server validates every action | M03 |
| P03-2 | Keep any in-GUI recipe book in the crafting terminal? | Drop it; rely on the NEI overlay (D11); keep per-cell auto-fill reservation (TERM-05) | Owner, M03 |
| P03-3 | The tag filter and item tags: what replaces them in 1.7.10? | `OreDictionary` name filter with the same add/remove list UI; "match components" becomes "match NBT" | Owner, M03 |
| P03-4 | Fix the unsatisfiable beacon scan gate (TERM-04/WIRE-04) in the terminal or the wireless lane? | Fix once in shared code in M03 and let M04 reuse it | M03 and M04 |
| P03-5 | Fix the small UI bugs (TERM-01, 03, 06, 07, 08, 10, 11) as written in the table? | Yes: fix each with a `doesNotReproduce<ID>_` test (ROADMAP M03 exit gate already requires the tests) | M03 |
| P03-6 | Source of the TYPE sort order in 1.7.10 | Creative-tab list order; names from `Item.itemRegistry`; keep the ordinal 5 for old saves | M03 |
| P03-7 | Hash and `CraftingMatrix` on 1.7.10 stacks | NBT-based hash; `CraftingMatrix` over `InventoryCrafting` | M03 |
| P03-8 | Keep multithreaded change tracking (TERM-12)? | Server thread only until a profile shows a need; fix the stale array either way | M03 |
