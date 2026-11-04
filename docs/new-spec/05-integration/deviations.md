# 05-integration · deviations

Part of [05-integration](index.md).

## Sheets

No extra sheets; the tables below cover everything. The terminal-side widget work is in [03-terminal](../03-terminal/index.md).

## How to read the table

| Column | Meaning |
|---|---|
| Original | The original-spec sheet (linked). |
| Here | `same`, `changed`, `not ported` (by decision; the upstream code remains at the 26.2 branch), `deferred`, `new`. |
| What changes | Concrete names and 1.7.10 classes; a fact about a mod not read in `00-pack-integration/` or API-MAP is marked "unverified". |
| Decision | D-row or record (Accepted), or `Proposed P05-n`. |
| Bugs | [BUGS.md](../../original-spec/BUGS.md) IDs. |

## Deviation table

| Original | Here | What changes | Decision | Bugs |
|---|---|---|---|---|
| [jei](../../original-spec/05-integration/jei.md) | changed | JEI is not in the pack; the viewer is NEI. Alpha plugin `compat/nei/NEISimpleStorageConfig implements IConfigureNEI` (found by NEI's ASM scan: name starts with `NEI`, ends with `Config`): an `ICraftingHandler` for the assembler recipes, an `IUsageHandler` for network inventory contents, `API.registerGuiOverlay` on terminal, cabinet and filter GUIs, and an `INEIGuiHandler` fast transfer. The JEI clickable area and its wrong rectangle (83, 35 + rows*18, 28, 23 px) are not carried over; the overlay button is placed over the crafting result slot (120, 129). 16x16 px ghost targets and `ISearchHandler` search sync are beta candidates. Auto-fill keeps the contract `IAutoFillTerminal` (`CompoundTag{fill: recipeId}` becomes an `NBTTagCompound` field in the packet). | D11, recipe-viewer.md; ghost drag and search sync Proposed P05-1 | INT-10 |
| [rei](../../original-spec/05-integration/rei.md) | not ported | REI has no 1.7.10 counterpart (none). Its missing-slot colors `0x40ff0000` / `0x67aaaa00` are replaced by NEI's own overlay highlight (API-MAP, recipe-viewer.md). The guessed missing-slot index remap is not ported; the overlay computes missing slots from the real grid index. | D11 | INT-05 |
| [emi](../../original-spec/05-integration/emi.md) | not ported | EMI has no 1.7.10 counterpart (none). The upstream EMI code is dead (never compiled, declared entrypoint with no class, "craft" never sends the fill packet), so nothing is lost. Ghost fill `0x8822BB33` and missing fill `0x44FF0000` are not used. INT-04 is not a bug: slot 0 is the crafting result slot in `CraftingTerminalMenu`, so excluding it is correct, and the NEI overlay keeps that exclusion. | D11 | INT-01, INT-02, INT-03, INT-04 (not a bug), INT-05 |
| [jade](../../original-spec/05-integration/jade.md) | changed | Jade maps to Waila 1.19.34 `IWailaBlock`, registered with `FMLInterModComms.sendMessage("Waila", "register", "<fqcn>")` (GT precedent `gregtech.crossmod.waila.Waila`). Providers for inventory_connector (`free/all/blocks`), inventory_config (`bf/skip/pr/filter`) and inventory_hopper (`filter/enabled`) become tile-entity-specific providers (filing cabinet, terminal slot stats, level emitter, cable/connector, hopper, configurator), registered per class, not for every block; the NBT keys stay so the data shape is identical. Everything guarded by `Loader.isModLoaded("Waila")`. | recipe-viewer.md ("What M06 builds"), ROADMAP M06; provider split Proposed P05-2 | INT-06 |
| [jade](../../original-spec/05-integration/jade.md) (extra providers) | new | Upstream Jade has three components (inventory connector usage, block-filter config, hopper filter). M06 lists six Waila providers: filing cabinet contents, terminal stats, level emitter, network size, hopper filter, configurator state, so three are new here. | Proposed P05-5 | none |
| [other-compat](../../original-spec/05-integration/other-compat.md) | mixed | See per-integration map below. | see map | INT-07, INT-08, INT-09 |
| [index](../../original-spec/05-integration/index.md) | changed | Shared viewer contract (`IAutoFillTerminal`: `updateSearch`, `sendMessage`, `getStoredItems`, `isSmartSearch`) is implemented by the crafting terminal and consumed by the NEI plugin. Module `modules.integration` flags gate behavior only. | D16, ROADMAP M03 exit gate | INT-01..INT-10 |

## Per-integration map

| Upstream integration | Maps to | Evidence | Decision |
|---|---|---|---|
| JEI | NEI plugin (`IConfigureNEI`) | recipe-viewer.md; API-MAP; NEI 2.8.171-GTNH ClientHandler ASM scan | D11 |
| REI | none | no 1.7.10 mod in the pack (API-MAP) | D11 |
| EMI | none | upstream code is dead (INT-01/02/03); no 1.7.10 counterpart | D11 |
| Jade | Waila `IWailaBlock` providers | recipe-viewer.md; Waila 1.19.34 `IWailaRegistrar` | D12 context, ROADMAP M06 |
| The One Probe (TOP) | none | no 1.7.10 TOP in the pack read; zero-total progress (INT-07) not ported | Proposed P05-3 |
| Polymorph | none | not in the pack; widget was never registered upstream (INT-08, alias CORE-08) | ROADMAP M03 exit gate |
| Sophisticated Storage | none | not in the pack; dead on Fabric upstream (INT-09) | Proposed P05-3 |
| ModMenu (Fabric) | none; config screen from GTNHLib `@Config` | GTNHLib 0.11.54; D16 | D16 |
| MultiblockInventoryAPI / `VanillaMultiblockInventories.checkChest` | double-chest handling by the D10 chain (`ISidedInventory` -> `IInventory`); no extra API | D10 | D10 |
| ServerEvents (`BreakBlockEvent` drops filter, `LeftClickBlock` -> `ILeftClickListener`) | Forge `BlockEvent.BreakEvent` / `PlayerInteractEvent` on `MinecraftForge.EVENT_BUS` (unverified) | API-MAP | Proposed P05-4 |
| Terminal-contents recipe category | NEI `IUsageHandler`, not REI/EMI-derived | D11 | D11 |
| AE2 storage access | optional, off at alpha | D10; AE2 rv3-beta-1095-GTNH | D10 |
| GT recipe display | none of ours: GT's own `gregtech/nei/GTNEIDefaultHandler.java` renders every `gt.recipe.assembler` recipe (GT5-Unofficial 5.09.54.265) | D8, D11 | D8, D11 |
