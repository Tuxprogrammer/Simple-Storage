# Machine model: what "machine" means in 1.7.10 GTNH for a storage mod

Verified 2026-11-03 from GT5-Unofficial source at tag `5.09.54.265` (git-ignored `reference/src/GT5`) and the shipped pack's mod list ([shipped-mods.md](shipped-mods.md), S3). This sheet settles the mechanics behind [0004](../../design/0004-energy-and-machine-model.md).

## The two shapes a block can have in 1.7.10 GTNH

1. **GT MetaTileEntity (MTE) machine.** GT's machine framework: a single GT machine block hosts every MTE via a registry; each MTE is a Java class registered at pre-init (`gregtech/loaders/preload/LoaderMetaTileEntities.java` builds 465 single-block machines via `MTEBasicMachineWithRecipeBuilder`), rendered with GT's texture/overlay system, with GT/ModularUI GUIs, and an energy model (tier rate `V[tier] × amps`, see [gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md)). The key fact for us: **`IMetaTileEntity extends ISidedInventory, IFluidTank, IFluidHandler`** (IMetaTileEntity.java#L50, `gregtech/api/interfaces/metatileentity/IMetaTileEntity.java`) — every GT machine exposes standard Forge inventory/fluid access, and `IMetaTileEntity#getInventoryHandler()` (IMetaTileEntity.java#L607) exposes a ModularUI item handler.
2. **Ordinary 1.7.10 block + TileEntity.** A vanilla `Block` registered with `GameRegistry.registerBlock`, its own `TileEntity` (via `Block#registerTileEntity`), its own `Container` + `GuiContainer`, and inventory access through the standard Forge interfaces (`IInventory`, `ISidedInventory`). This is the shape of the pack's storage neighbors (below).

## The faithful-power-free default (0004) and its in-pack precedent

[0004](../../design/0004-energy-and-machine-model.md) §1-2: our blocks are **ordinary 1.7.10 blocks with their own simple GUIs — not MetaTileEntities, not GT's GUI framework, no MetaTileEntity IDs, no EU at alpha.**

Precedent in the shipped pack (S3's MOD-INDEX, [shipped-mods.md](shipped-mods.md)): the entire storage-neighbor ecosystem is shape (2), not shape (1):

| Pack mod (S3-verified version) | Shape |
|---|---|
| ironchest 6.1.13 | ordinary blocks + tile entities, no EU |
| StorageDrawers 2.2.31-GTNH | ordinary blocks + tile entities, no EU |
| EnderStorage 1.8.9 | ordinary blocks + tile entities (ender chest), no EU |
| EnderIO 2.10.51 / BuildCraft 7.1.65 chests & silos | ordinary tile entities for storage; EU only in their *machines* |
| AE2 rv3-beta-1095-GTNH | AE2's own tile-entity framework (its own MTE-like system), power-free storage with ME drives |

So "power-free utility block with its own GUI" is exactly how the pack's non-mechanical storage looks. The GT-MTE shape is reserved for things that consume EU to do processing. **Decision (D9):** all our blocks are shape (2); the optional EU sink ([gtnh-tiers-and-energy.md](gtnh-tiers-and-energy.md) §EU model) is a drain on a plain tile entity, not a conversion to an MTE.

## Block-entity / tile-entity conventions (1.7.10)

- Register the block in pre-init; `Block#registerTileEntity(block, TEClass)`; the TE persists via `writeToNBT`/`readFromNBT` and drops its contents on break where appropriate (upstream's filing cabinet keeps its lock NBT — 02-storage `filing-cabinet.md`).
- GUI: one `Container` (server-authoritative slots/data) + one `GuiContainer` per menu type (8 menu types upstream — 01-core `mod-lifecycle.md` row 30); client screen registration happens in the client pre-init.
- Server/client safety (house rule 6): TE tick logic is server-only; the client TE mirrors only what the container/data slots sync. Our `DataSlots`-style sync (01-core `util-data-slots.md`) maps to 1.7.10 `Container` data slots.
- GTNHLib 0.11.54 (tag `0.11.54`, git-ignored `reference/src/GTNHLib`): provides the pack's **blockstate registry** (`com.gtnewhorizon.gtnhlib.blockstate.*`) for property-based blocks — the 1.7.10-friendly way to register blocks with properties (our cable/connector/trim state variants) without per-state block classes; and the annotation config system (below).
- **Config (01-core `config.md` upstream → 1.7.10):** GTNHLib's `@Config` annotation (`com.gtnewhorizon.gtnhlib.config.Config`, GTNHLib src/main/java/com/gtnewhorizon/gtnhlib/config/Config.java#L14) with `modid`, `category`, `configSubDirectory`, `filename`, `categoryCulling`, and `@Config.Sync` for client-visible values (Config.java#L251); `ConfigurationManager` parses fields into `config/simplestoragenh.cfg` and the pack's automatic config screen renders it (GTNHLib `SimpleGuiFactory`, S3 [integration-opportunities.md](integration-opportunities.md) §Config conventions). Per [config-safe-registration](../../../.claude/skills/config-safe-registration/SKILL.md): register everything, gate behavior; the EU-sink flag changes behavior only.

## Capability system basics for the inventory access the terminals need

1.7.10 Forge's capability system (deobfuscated Forge 10.13.4.1614 API; the universal jar ships SRG names — the dev build deobfuscates them):

- `net.minecraftforge.common.capabilities.Capability<T>` + `CapabilityManager.INSTANCE.register(...)`; providers implement `ICapabilityProvider` (`hasCapability`, `getCapability`) — the standard 1.7.10 mechanism for "does this block expose an X".
- **For inventory access specifically, the pack's convention is simpler and is what our terminals should target:**
  - `net.minecraft.inventory.IInventory` (vanilla) — chests, vanilla hoppers, most storage neighbors.
  - `net.minecraftforge.items.IInventory`/`ISidedInventory` (Forge) — sided access; **every GT MTE implements `ISidedInventory`** (IMetaTileEntity.java#L50), so one code path reads both GT machines and ordinary blocks.
  - AE2: ME inventories are reached through AE2's own grid/storage API (AE2 `IStorageChannel`/`IGridNode` — the ME interface precedent S3 recorded in [integration-opportunities.md](integration-opportunities.md) §AE2) — for M05's AE2 compat, behind `Loader.isModLoaded("appeng")`.
  - GT's `gregtech.api.interfaces.IItemContainer` (IItemContainer.java#L10) is an **item-registration helper** (ItemList-style stack factory), *not* an inventory-access interface — do not confuse the two.
- **Decision (D10):** the port's `PlatformInventoryAccess` (01-core `platform-layer.md`) resolves, in order: `ISidedInventory` (sided) → `IInventory` (vanilla) → AE2 storage (optional compat) → `EMPTY`. Our own blocks implement `ISidedInventory` so they are readable by the rest of the pack (hoppers, AE2 buses, GT) without extra glue.

## Sources

- GT5-Unofficial `5.09.54.265`: `gregtech/api/interfaces/metatileentity/IMetaTileEntity.java#L50,#L607`, `gregtech/api/interfaces/IItemContainer.java#L10`, `gregtech/loaders/preload/LoaderMetaTileEntities.java`, `gregtech/api/metatileentity/implementations/MTEBasicMachineWithRecipe.java`.
- GTNHLib `0.11.54`: `src/main/java/com/gtnewhorizon/gtnhlib/config/Config.java#L14,#L251`, `.../config/ConfigurationManager.java`, `.../config/SimpleGuiFactory.java`, `.../blockstate/`.
- Forge `10.13.4.1614` (pack jar `reference/gtnh-pack/forge-1.7.10-10.13.4.1614-1.7.10-universal.jar`; capability classes present under SRG names).
- S3: [shipped-mods.md](shipped-mods.md), [integration-opportunities.md](integration-opportunities.md).
