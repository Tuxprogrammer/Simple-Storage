# storage · coverage and limits

Part of [storage](index.md).

## Coverage

- Source files read: 46 of 46 in scope (verified against the area file list — batches 02-b1…02-b7, re-read in full during the writing phase).
- Lang keys covered: every key used by in-scope classes is in the table below (exact text from `NeoForge/src/platform-shared/resources/assets/toms_storage/lang/en_us.json`).
- Textures/models referenced but not inspected: trim/cable/connector/emitter/hopper/crate/cabinet models and their client renderers (06-client-ui).
- Corrections made to earlier statements: `FilingCabinetContainer` lock semantics corrected after re-reading (lock cleared only when the whole cabinet is empty; `setItem` re-locks unconditionally — see `filing-cabinet.md`).

## Known limits

- `InventoryCableConnectorBlockEntity` (SH/block/entity/) is created by `InventoryCableConnectorBlock.newBlockEntity` but is **not** in the 46-file scope list; its beacon/link/NBT behavior is documented only indirectly (via `cable-connector.md`, 03-terminal `inventory-link.md`, 01-core `config.md`).
- `IInventoryConnector` (SH/block/entity/IInventoryConnector.java) — the interface implemented by connector/interface BEs — is out of scope; treated as a 01-core platform interface here.
- `BlockFilter`, `AbstractPainedBlockEntity`, `PlatformInventoryAccess`, `MultiblockInventoryAPI` are 01-core/05-integration — used, not documented here.
- Not run in a real pack: block shapes, model selection and render shapes read from code only.
- `MultiblockInventoryAPI` third-party implementations are unbounded (05-integration).

