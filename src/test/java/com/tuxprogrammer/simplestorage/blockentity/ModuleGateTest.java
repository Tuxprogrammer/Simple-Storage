package com.tuxprogrammer.simplestorage.blockentity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.tuxprogrammer.simplestorage.config.ModulesConfig;

/**
 * Tests that the module flags in {@link ModulesConfig} gate only the
 * ticking/behavior of tile entities: with a flag off, the tile entity's
 * {@code isBehaviorActive()} is false and ticking does nothing, but the
 * flag never changes what is stored in the inventory itself.
 */
@Tag("ingame")
public class ModuleGateTest {

    @BeforeAll
    public static void registerVanillaContent() {
        Bootstrap.func_151354_b();
    }

    @Test
    public void storageFlagGatesTheCabinetAndTheCrate() {
        ModulesConfig.storage = false;
        try {
            assertFalse(new FilingCabinetTileEntity().isBehaviorActive());
            assertFalse(new OpenCrateTileEntity().isBehaviorActive());
        } finally {
            ModulesConfig.storage = true;
        }
        assertTrue(new FilingCabinetTileEntity().isBehaviorActive());
        assertTrue(new OpenCrateTileEntity().isBehaviorActive());
    }

    @Test
    public void networkFlagGatesConnectorInterfaceEmitterAndHopper() {
        ModulesConfig.network = false;
        try {
            assertFalse(new InventoryConnectorTileEntity().isBehaviorActive());
            assertFalse(new InventoryInterfaceTileEntity().isBehaviorActive());
            assertFalse(new LevelEmitterTileEntity().isBehaviorActive());
            assertFalse(new InventoryHopperTileEntity().isBehaviorActive());
        } finally {
            ModulesConfig.network = true;
        }
        assertTrue(new InventoryConnectorTileEntity().isBehaviorActive());
        assertTrue(new InventoryInterfaceTileEntity().isBehaviorActive());
        assertTrue(new LevelEmitterTileEntity().isBehaviorActive());
        assertTrue(new InventoryHopperTileEntity().isBehaviorActive());
    }

    /** With the terminal flag off, the terminal tile entities are inert; restored, they are active. */
    @Test
    public void terminalFlagGatesTheTerminalTileEntities() {
        ModulesConfig.terminal = false;
        try {
            assertFalse(new StorageTerminalTileEntity().isBehaviorActive());
            assertFalse(new CraftingTerminalTileEntity().isBehaviorActive());
            assertDoesNotThrow(() -> new StorageTerminalTileEntity().updateEntity());
            assertDoesNotThrow(() -> new CraftingTerminalTileEntity().updateEntity());
        } finally {
            ModulesConfig.terminal = true;
        }
        assertTrue(new StorageTerminalTileEntity().isBehaviorActive());
        assertTrue(new CraftingTerminalTileEntity().isBehaviorActive());
    }

    @Test
    public void inertTileEntityDoesNothingOnUpdate() {
        ModulesConfig.storage = false;
        ModulesConfig.network = false;
        try {
            assertDoesNotThrow(() -> new FilingCabinetTileEntity().updateEntity());
            assertDoesNotThrow(() -> new OpenCrateTileEntity().updateEntity());
            assertDoesNotThrow(() -> new InventoryConnectorTileEntity().updateEntity());
            assertDoesNotThrow(() -> new InventoryInterfaceTileEntity().updateEntity());
            assertDoesNotThrow(() -> new LevelEmitterTileEntity().updateEntity());
            assertDoesNotThrow(() -> new InventoryHopperTileEntity().updateEntity());
        } finally {
            ModulesConfig.storage = true;
            ModulesConfig.network = true;
        }
    }

    @Test
    public void flagsNeverChangeStorageContents() {
        FilingCabinetTileEntity cabinet = new FilingCabinetTileEntity();
        ItemStack sword = new ItemStack(Items.iron_sword);
        cabinet.setInventorySlotContents(3, sword);

        ModulesConfig.storage = false;
        try {
            assertSame(
                Items.iron_sword,
                cabinet.getStackInSlot(3)
                    .getItem());
            cabinet.setInventorySlotContents(3, new ItemStack(Items.iron_sword));
            assertTrue(
                cabinet.getStackInSlot(3)
                    .isItemEqual(sword));
        } finally {
            ModulesConfig.storage = true;
        }
        assertNotNull(cabinet.getStackInSlot(3));
        assertSame(
            Items.iron_sword,
            cabinet.getStackInSlot(3)
                .getItem());
    }
}
