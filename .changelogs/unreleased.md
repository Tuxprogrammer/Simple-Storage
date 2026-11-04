# Unreleased

## Added
- Foundation: config file `simplestoragenh.cfg` with module flags, network, wireless, behavior and EU sink settings; GregTech and GTNHLib are required.
- Storage blocks: filing cabinet (512 single-item slots of one item type), open crate (the item entities in front of it as an inventory), inventory trim and painted trim, inventory cable and framed cable, cable connector and framed cable connector, inventory connector, inventory interface, inventory proxy, level emitter, basic inventory hopper, and the paint kit.
- Config: network and storage behavior is gated by the `modules.storage` and `modules.network` flags; the blocks always exist.
- Known limits: no GUIs, textures, recipes or paint rendering yet (later milestones).
- Storage terminal and crafting terminal: search, sort, filter and scroll the whole network; pull and push stacks with AE-like, RS-like or default controls; ghost mode and tall mode.
- Item filter and tag filter items, the filing cabinet screen and the level emitter screen.
- Known limits: the screens use plain panels and there are no recipes or textures yet.
