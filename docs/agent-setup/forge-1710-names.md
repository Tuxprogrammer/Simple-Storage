# Minecraft 1.7.10 name cheat sheet

Names below are the deobfuscated MCP names of the dev environment; check a class with `javap` on the dev jar when unsure, and every row here was a real compile error in this repo.

| Wrong (newer or guessed) | Right in 1.7.10 |
| --- | --- |
| `net.minecraft.util.ForgeDirection` or `codechicken...ForgeDirection` | `net.minecraftforge.common.util.ForgeDirection` (`getOrientation(int)`, `VALID_DIRECTIONS`, `offsetX/Y/Z`, `getOpposite()`) |
| `ForgeDirection.getOrientation(dx, dy, dz)` | loop `VALID_DIRECTIONS` comparing offsets |
| `entityItem.getStack()` | `entityItem.getEntityItem()` |
| `new AxisAlignedBB(...)` | `AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ)` |
| `ItemStack.splitStack(stack, n)` | `stack.splitStack(n)` (instance method) |
| `item.hasSubtypes()` | `item.getHasSubtypes()` |
| `world` field of a TileEntity | `worldObj` |
| `world.getBlockTileEntity(x,y,z)` | `world.getTileEntity(x,y,z)` |
| `BlockPos` | three ints x, y, z (pack to a long with a helper) |
| `BlockEntity`, `BlockState` | `TileEntity`, `Block` plus metadata 0-15 (`getBlockMetadata()`, `World.setBlockMetadataWithNotify(x,y,z,meta,3)`) |
| `Container` (inventory) | `net.minecraft.inventory.IInventory` / `ISidedInventory` |
| IInventory methods | exactly getSizeInventory, getStackInSlot, decrStackSize, getStackInSlotOnClosing(int), setInventorySlotContents, getInventoryName, hasCustomInventoryName, getInventoryStackLimit, markDirty, isUseableByPlayer, openInventory, closeInventory, isItemValidForSlot |
| `getAccessibleSlotsFromSide` / `canInsertItem` / `canExtractItem` | for ISidedInventory |
| `CompoundTag` | `NBTTagCompound` (`hasKey`, `getInteger`, `getTagList(key, 10)`, `tagList.getCompoundTagAt(i)`, `tagList.tagCount()`, `appendTag`) |
| `ItemStack.of(tag)` / `save(tag)` | `ItemStack.loadItemStackFromNBT(tag)` / `stack.writeToNBT(tag)` |
| registry names | `Item.itemRegistry.getNameForObject(item)`, `Block.blockRegistry.getNameForObject(block)`, `Block.getBlockFromName(String)` |
| `ResourceLocation`-based textures | `setBlockTextureName("simplestoragenh:name")` and `setBlockName` |
| `level.getGameTime()` | `world.getTotalWorldTime()` |
| `level.isClientSide` | `world.isRemote` |
| `Blocks.AIR` | `Blocks.air` |
| vanilla bootstrap in unit tests | `net.minecraft.init.Bootstrap.func_151354_b()` before touching Items or Blocks |
| `@SubscribeEvent` | `cpw.mods.fml.common.eventhandler.SubscribeEvent`, class and method must be public |
| `FMLCommonHandler` side check | `world.isRemote`, `FMLCommonHandler.instance().getSide()` |
| `mergeItemStack(...)` returns the leftover stack | returns boolean and reduces the stack in place (leftover = stack.stackSize > 0) |
| `ItemStack.copyItemStack()` / `ItemStack.areItemStackTagsEqual` as instance methods | `stack.copy()` and the STATIC `ItemStack.areItemStackTagsEqual(a, b)` |
| `net.minecraft.inventory.InventoryPlayer` | `net.minecraft.entity.player.InventoryPlayer` (`getCurrentItem()`, int field `currentItem`, `mainInventory`, `getItemStack()`/`setItemStack`) |
| `net.minecraft.util.IBlockAccess` | `net.minecraft.world.IBlockAccess` |
| `Block.onBlockPlacedBy(World,x,y,z,EntityLivingBase)` | `onBlockPlacedBy(World,int,int,int,EntityLivingBase,ItemStack)` (six parameters) |
| `Slot.slotIndex` | private: use `slot.getSlotIndex()` |
| `slot.markDirty()` | `slot.onSlotChanged()` |
| `Slot` constructor after other statements | `super(inventory, index, x, y)` must be the first statement: build the inventory view before constructing the slot |
| `TileEntity.worldObj` from another class | protected: use `getWorldObj()` |
| GuiScreen `initGui()`, `drawScreen`, `handleMouseInput`, `updateScreen` declared protected | all four are public |
| `keyTyped(char,int)`, `mouseClicked(int,int,int)`, `actionPerformed(GuiButton)` are protected |
| typed container in a GuiContainer subclass | `this.inventorySlots` is the plain Container: keep a typed field |
| `GuiButton.mousePressed(int,int,int)` | `mousePressed(Minecraft mc, int x, int y)` |
| `GuiTextField.mouseClicked(...)` returns boolean | returns void in 1.7.10 (use `isFocused()` afterwards) |
| `PacketBuffer.writeStringToBuffer` / `readStringFromBuffer(int)` | both throw IOException |
| `Bootstrap` unavailable in plain JUnit | tag the test `@Tag("ingame")` |

## Habits

- Build and test only on the build host (`tools/remote/work.sh build`), never on the laptop.
- Keep tile-entity NBT readers tolerant of missing keys.
- Keep pure logic free of Minecraft types.
- Every player-visible string is a lang key.
