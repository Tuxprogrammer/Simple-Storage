# Mod API and polymorph hooks

| # | Field | Contents |
|---|---|---|
| 1 | **Identity** | `EventHandler<T>` (SH), `MultiblockInventoryAPI` (+ inner `MultiblockDetector`) (SH), `PolymorphHelper` (SH), `PolymorphTerminalWidget` (SH, client). |
| 2 | **Inputs** | `EventHandler`: an event class and an invoker factory. `MultiblockInventoryAPI`: registered `MultiblockDetector` lambdas. `PolymorphHelper`: a player, recipe type, crafting input, level. `PolymorphTerminalWidget`: a `CraftingTerminalScreen`. |
| 3 | **Numbers** | None. |
| 4 | **Behavior** | Ordered steps in [Behavior detail](#behavior-detail). |
| 5 | **Determinism** | Handler invocation order = registration order (list order). |
| 6 | **Data formats** | None. |
| 7 | **Output** | The combined multiblock-detection invoker; polymorph recipe lookup; (dead on NF) the polymorph recipe widget in the crafting terminal screen. |
| 8 | **Relationships** | `MultiblockInventoryAPI.EVENT.invoker()` is called by `InventoryConnectorBlockEntity` (02-storage, line 113) and `BlockFilter` (03-terminal, line 186) to discover extra multiblock inventory positions during a connectivity scan. Registered in `StorageMod.setup` (mod-lifecycle.md): `VanillaMultiblockInventories::checkChest` always, `SophisticatedDoubleBlocks::checkGroup` when sophisticated storage is loaded (NF). `PolymorphHelper` is used by the crafting terminal menu (03-terminal) when the polymorph mod is present. `PolymorphTerminalWidget` is client UI (06-client-ui territory) but is *dead code* on NF — see `CORE-08`. |
| 9 | **NeoForge dependencies** | None directly (plain Java; the polymorph classes depend on the optional `polymorph` mod's API — `PolymorphApi`, `PolymorphWidgets`, `PlayerRecipesWidget`). |
| 10 | **Bugs** | `CORE-08` (polymorph widget never registered on NF). |

## Behavior detail

1. (both) `EventHandler<T>(eventType, invokerFactory)`: keeps a list of handlers and a cached combined invoker built by `invokerFactory(handlers.toArray(...))`. `register(listener)`: null check, synchronized add, rebuilds the invoker. `invoker()`: the cached combined invoker. Rebuilds are O(n) per registration (fine — a handful of registrations at startup).
2. (both) `MultiblockInventoryAPI.EVENT`: an `EventHandler<MultiblockDetector>` whose invoker is `(level, pos, state, extra) →` call every registered detector's `detectMultiblocks(level, pos, state, extra)`. `MultiblockDetector` is a functional interface: given a level, position and block state, it reports additional block positions that belong to the same multiblock inventory (via the `extra` consumer) — e.g. a double chest reports its partner half, a sophisticated storage group reports its members.
3. (both) `PolymorphHelper.getRecipe(player, crafting, input, level)`: asks the polymorph mod's recipe manager for the player's current recipe (polymorph lets players switch crafting recipes per player); returns `Optional<RecipeHolder<CraftingRecipe>>`.
4. (client) `PolymorphTerminalWidget`: a `PlayerRecipesWidget` (polymorph API) for the crafting terminal screen, bound to the screen's crafting result slot. `initChildWidgets()` first sends an inventory button click (containerId, button 1) to the server to request recipes, then builds the child widgets. `selectRecipe` is currently a no-op beyond the superclass (its body is commented out). `render` calls `resetWidgetOffsets()` before delegating. `register()` (static) adds a widget factory for `CraftingTerminalScreen` to `PolymorphWidgets` — but the only call site (`StorageMod.doClientStuff`, line 69) is commented out, so on NF the widget is never created (*dead code*, `CORE-08`; `StorageMod.polymorph` is set at line 56 and consumed nowhere else).

## Deviations in the new mod

See the [`util-api` row of the new-spec 01-core deviation table](../../new-spec/01-core/index.md#deviation-table).
