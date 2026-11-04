# Platform layer

| # | Field | Contents |
| 1 | **Identity** | `Platform` (NF + Fabric variants), `GameObject`/`GameRegistry`/`GameRegistryBE`/`GameObjectBlockEntity` (NF + Fabric variants), `PlatformItem` (NF + Fabric), `PlatformBlockEntity` (NF + Fabric), `PlatformItemHandler` (NF, in `NeoForge/src/main/java/com/tom/storagemod/inventory/`). |
| 2 | **Inputs** | The mod event bus (registration), mod list (capability lookups for Curios/trinkets, Vivecraft, IRIS, polymorph), a block position + side (capability queries), a player + item predicate (extra-slot lookups). |
| 3 | **Numbers** | None of its own (it wraps registries and capabilities). The creative tab `toms_storage:tab` and attachment type `block_filter` are registered here (NF). |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | None. |
| 6 | **Data formats** | NF attachment type `block_filter` (serializable `BlockFilterAttachment`, registered in `Platform.ATTACHMENT_TYPES`) — the per-block-entity filter attachment used by `ServerEvents` and the connector (02-storage). |
| 7 | **Output** | Registration handles; capability providers; the `PlatformItemHandler` exposed by connector/interface/proxy block entities. |
| 8 | **Relationships** | `Content` (mod-lifecycle.md) uses `Platform.*` registries for every registration; `PlatformItemHandler` is the capability for `inventory_connector`, `inventory_interface`, `inv_proxy` (mod-lifecycle.md); `PlatformItem` is the base item class for many SH items (02-storage); `PlatformBlockEntity` is the base BE class (02-storage, 03-terminal); `WorldStates`/`InventoryChangeTracker` (this area) consume capabilities via `BlockCapabilityCache`. |
| 9 | **NeoForge dependencies** | Deferred registration (`DeferredRegister`, `DeferredHolder`); `NeoForgeRegistries.Keys.ATTACHMENT_TYPES` + `AttachmentType` (block entity attachments); `Capabilities.Item.BLOCK` + `BlockCapabilityCache`; `ResourceHandler<ItemResource>`/`DelegatingResourceHandler` (NeoForge transfer API — the item-handler abstraction); `VanillaContainerWrapper`; Curios `CuriosCapability.INVENTORY`; `EventHooks.onNeighborNotify`; `ItemAbilities.AXE_SCRAPE`; `RegistryFriendlyByteBuf` + `ConnectionType.NEOFORGE`. Fabric counterparts: direct `Registry.register`, `FabricBlockEntityTypeBuilder`, `FabricCreativeModeTab`, trinkets `TrinketsApi`, `UseBlockCallback`, `PacketContext.runWithContext`. |
| 10 | **Bugs** | None directly (see `inventory-access.md` for adapter bugs). |

## Behavior detail

### GameObject / GameRegistry (registration wrapper)

1. (both) `GameRegistry<T>` (NF) wraps a `DeferredRegister<T>` created for mod `toms_storage`. `register(name, supplier)` and `register(name, keyFunction)` return a `GameObject<I>` holding the `DeferredHolder`; the key-function overload pre-computes the `ResourceKey` (`toms_storage:<name>`) and passes it to the factory so the object knows its own id. `register(bus)` attaches the deferred register to the mod bus (called once from `Platform.register`).
2. (both) `GameRegistryBE.registerBE(name, factory, blocks...)` registers a `BlockEntityType` built from the factory and the associated block `GameObject`s. NF: `new BlockEntityType<>(factory, blocks)`. Fabric: `FabricBlockEntityTypeBuilder.create(factory, blocks).build()`; `GameObjectBlockEntity.addBlocks(...)` can extend the block list after creation (NF cannot).
3. (both) `GameObject.get()` (NF) dereferences the holder (lazy); (Fabric) returns the eagerly-registered value. `GameObject.getId()` returns the registry `Identifier`/`ResourceLocation`.

### PlatformItem (base item)

4. (both) `PlatformItem` adds an `onRightClick(player, stack, pos, hand)` hook returning `InteractionResult` (default `PASS`). NF: `onItemUseFirst` is `final` and delegates to `onRightClick` (item right-click while not on a block). Fabric: no `onItemUseFirst`; a `UseItemCallback` in the Fabric mod initializer calls `onRightClick` instead.
5. (both) SH items that need this hook extend `PlatformItem` (e.g. the filter items, 02-storage territory).

### PlatformBlockEntity (base block entity)

6. (both) `PlatformBlockEntity extends BlockEntity` implements `IValidInfo`: `isObjectValid()` = `!isRemoved()` (used as the validity predicate for capability caches, `inventory-access.md`). NF: `markCapsInvalid()` calls `level.invalidateCapabilities(worldPosition)` (NeoForge capability invalidation). Fabric: `markCapsInvalid()` is a no-op and the class additionally implements `TickerUtil.OnLoadListener` with an empty `onLoad()`.

### Platform (NF)

7. (both) Static registration handles: `ITEMS`, `BLOCKS`, `BLOCK_ENTITY`, `MENU_TYPE` (game registries), `TAB` (`toms_storage:tab` creative tab, icon = terminal, contents = the `tabItems` list), `ATTACHMENT_TYPES`, `DATA_COMPONENT_TYPES`. `STORAGE_MOD_TAB` is a `DeferredHolder<CreativeModeTab>`.
8. (both) `addItemToTab(item)`: appends to the creative tab list and returns the item (used by `Content.item(...)`).
9. (both) `register(bus)`: attaches all seven handles to the mod bus.
10. (both) `checkUse(world, hit, player, hand)`: posts a synthetic `PlayerInteractEvent.RightClickBlock`; returns the cancellation result if canceled, else null — callers (wireless items, 04-wireless) then call `state.useWithoutItem` and treat the whole thing as handled.
11. (both) `checkExtraSlots(player, predicate, def, map)`: NF — if Curios is loaded, queries `CuriosCapability.INVENTORY`, takes the first matching stack, applies `map`, else returns `def`. Fabric — same via trinkets (`TrinketsApi.getAttachment(player).equipped(is, false)`).
12. (both) `notifyBlocks(level, pos, direction)`: NF — runs `EventHooks.onNeighborNotify` with a single-direction set and returns false if the notify was canceled (lets other mods suppress re-scan triggers); Fabric — always true.
13. (both) `makeRegByteBuf(buffer, registryAccess)`: NF — `new RegistryFriendlyByteBuf(buffer, reg, ConnectionType.NEOFORGE)`; Fabric — two-arg constructor. Used by `TerminalSyncManager` (terminal-sync.md) to encode/decode item stacks in packets.
14. (both) `getIngredientItems(ingredient)`: NF — custom ingredients via `items()`, tag ingredients via `getValues()` (holders); Fabric — `items().toList()`.
15. (both) `getCloneItemStack(level, pos, player)`: delegates to `BlockState.getCloneItemStack` (right-click block item, e.g. for the inventory configurator).
16. (both) `canScrapeWithItem(item, player, hand)`: NF — `item.canPerformAction(ItemAbilities.AXE_SCRAPE)`; Fabric — `item.is(ItemTags.AXES)`.
17. (both) `runWithPacketContext(serverPlayer, runnable)`: NF — plain `runnable.run()`; Fabric — `PacketContext.runWithContext((PacketContextProvider) player, runnable)` so nested packet sends have a valid connection.

### PlatformItemHandler (NF)

18. (both) `PlatformItemHandler` extends `DelegatingResourceHandler<ItemResource>` and implements `IProxy`: constructed with an `IInventory` (SH interface, 03-terminal territory); delegates all resource-handler calls to `access.getInventoryAccess().getPlatformHandler()`; `getRootHandler(dejaVu)` delegates to the underlying access (proxy-chain resolution, `inventory-access.md`). Exposed as the block capability for `inventory_connector`, `inventory_interface`, `inv_proxy` (mod-lifecycle.md step 4).

## Loader differences (Fabric)

See steps 2, 3, 4, 6, 10–17 above; the Fabric `Platform` also registers the creative tab eagerly in a static initializer and has no attachment types.

## Deviations in the new mod

See the [`platform-layer` row of the new-spec 01-core deviation table](../../new-spec/01-core/deviations.md#deviation-table).
