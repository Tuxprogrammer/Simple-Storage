package com.tuxprogrammer.simplestorage.blockentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import com.tuxprogrammer.simplestorage.filter.FilterKey;
import com.tuxprogrammer.simplestorage.sort.SortKey;

/**
 * Derives identity and metadata keys for {@link ItemStack}s.
 * Side: server logic.
 */
public final class StackKeys {

    private StackKeys() {
        // no instances
    }

    /**
     * Builds a unique key for the given stack: registry name, damage, and NBT.
     * 
     * @param s the stack, may be null
     * @return a key of the form {@code registryName@damage#nbt}
     */
    public static String keyOf(ItemStack s) {
        String registryName = "";
        int damage = 0;
        NBTTagCompound tag = null;
        if (s != null) {
            registryName = Item.itemRegistry.getNameForObject(s.getItem());
            damage = s.getItemDamage();
            tag = s.getTagCompound();
        }
        String nbt = tag == null ? "" : tag.toString();
        return registryName + "@" + damage + "#" + nbt;
    }

    /**
     * Extracts the mod id from the stack's registry name.
     * 
     * @param s the stack, may be null
     * @return the part before ':' or "minecraft" when there is no ':'
     */
    public static String modId(ItemStack s) {
        String registryName = "";
        if (s != null) {
            registryName = Item.itemRegistry.getNameForObject(s.getItem());
        }
        int idx = registryName.indexOf(':');
        return idx < 0 ? "minecraft" : registryName.substring(0, idx);
    }

    /**
     * Builds a filter key for the given stack.
     * 
     * @param s the stack, may be null
     * @return a {@link FilterKey} with the registry name, damage, NBT digest, and ore names
     */
    public static FilterKey filterKeyOf(ItemStack s) {
        String registryName = "";
        int damage = 0;
        String nbtDigest = null;
        List<String> oreNames = new ArrayList<String>();
        if (s != null) {
            registryName = Item.itemRegistry.getNameForObject(s.getItem());
            damage = s.getItemDamage();
            NBTTagCompound tag = s.getTagCompound();
            nbtDigest = tag == null ? null : tag.toString();
            int[] oreIds = OreDictionary.getOreIDs(s);
            for (int oreId : oreIds) {
                oreNames.add(OreDictionary.getOreName(oreId));
            }
        }
        return new FilterKey(registryName, damage, nbtDigest, oreNames);
    }

    /**
     * Builds a sort key for the given stack.
     * 
     * @param s      the stack, may be null
     * @param amount the amount to report
     * @return a {@link SortKey} with name, mod id, item id, stack limit, and sort id
     */
    public static SortKey sortKeyOf(ItemStack s, long amount) {
        String name = s.getDisplayName();
        String modId = modId(s);
        int id = Item.getIdFromItem(s.getItem());
        int stackLimit = s.getMaxStackSize();
        String sortId = Item.itemRegistry.getNameForObject(s.getItem());
        return new SortKey(amount, name, modId, id, stackLimit, sortId);
    }
}
