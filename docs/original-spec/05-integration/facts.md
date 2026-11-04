# Integration · key facts

Part of [05-integration](index.md).

## Key facts

- **What is bundled vs optional (NeoForge jar at `95bb61d`):** REI `26.2.820`, Jade (curse.maven build `8326243`), The One Probe (curse.maven build `8887302`) are `implementation` dependencies — shipped inside the NeoForge jar. JEI `30.7.0.41` is compile-only API + `runtimeOnly` full jar (optional, not bundled). EMI `1.1.7+1.21` is only declared when the build runs with `-DuseLib`, and the EMI classes are additionally excluded from compilation (see INT-01) — EMI is effectively dead in the shipped NeoForge build. Polymorph is a dummy-compile stub by default (`curse.maven polymorph-388800:5995380` with `-DuseLib`).
- **Fabric:** JEI and REI are compile-only + runtime-only (optional); Jade (modrinth `26.2.0+fabric`) is `implementation` (bundled); the EMI dependency and the `emi` entrypoint are inconsistent (see INT-01/INT-02); Sophisticated Storage and Polymorph are dead on Fabric (see INT-08/INT-09).
- **Plugin discovery:** JEI (`@JeiPlugin`), REI (NeoForge `@REIPluginLoaderClient` loader class / Fabric `rei_client` entrypoint), EMI (`@EmiEntrypoint` and the `emi` entrypoint), Jade (`@WailaPlugin` and the `jade` entrypoint) are discovered by the viewer mod itself; TOP is wired through NeoForge inter-mod comms (`InterModComms.sendTo("theoneprobe", "getTheOneProbe", ...)`); ModMenu (`ModMenuApi`) and the Polymorph widget registry are wired explicitly.
- **Shared viewer contract:** all three viewers plug into `com.tom.storagemod.util.IAutoFillTerminal` (`SH/util/IAutoFillTerminal.java`, documented here because it is the integration seam): `updateSearch` is a static list of `ISearchHandler` (search-field sync), `sendMessage(CompoundTag)` carries the auto-fill request (`fill` key), `getStoredItems()` feeds the "missing" computation, `isSmartSearch()` gates the preview. The matching semantics (what counts as "missing") are identical in all three handlers: an input is satisfied if any alternative stack is in the player's inventory or in the terminal's stored items.
- **Build-script comment style matters:** `//*if(!useLib)*/exclude …` in the Gradle files is a *line* comment (the exclude never applies); `/*if(!useLib)*/exclude …` is a block comment (the exclude *always* applies). See INT-01.

## Related areas

- `03-terminal`: the viewers operate on the terminal screens/menus (`AbstractFilteredScreen`, `StorageTerminalMenu`, `CraftingTerminalMenu`, `PhantomSlot`, `FilterSlot`) and on the terminal's stored items (`TerminalItemStack`, `StoredItemStack`); the crafting result slot layout the JEI clickable area targets is documented there
- `06-client-ui`: the screens and widgets the viewers attach exclusion areas and ghost targets to; the lang keys the viewers use
- `02-storage`: the block-filter attachment (`BlockFilterAttachment`, `BlockFilter`) whose contents Jade and TOP read; the inventory hopper and inventory connector block entities
- `04-wireless`: the wireless terminal item is what the Jade/TOP hopper/connector info is usually about; the curios/trinkets belt data (wireless terminal in a belt) is that area's
- `01-core`: registration, config, network and the platform layer that hosts `ServerEvents` and the IMC
