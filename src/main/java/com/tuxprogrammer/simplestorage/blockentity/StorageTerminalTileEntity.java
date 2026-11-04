package com.tuxprogrammer.simplestorage.blockentity;
// Ported from Tom's Simple Storage (MIT):
// https://github.com/tom5454/Toms-Storage/blob/95bb61d/NeoForge/src/platform-shared/java/com/tom/storagemod/block/entity/StorageTerminalBlockEntity.java
// Avoids: TERM-04

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

import com.tuxprogrammer.simplestorage.core.Module;
import com.tuxprogrammer.simplestorage.inventory.StaggerGate;
import com.tuxprogrammer.simplestorage.terminal.TerminalModes;

/**
 * Tile entity backing the storage terminal block.
 *
 * <p>
 * Terminal behavior is <em>staggered</em>: each terminal rescans its
 * beacon only when {@link StaggerGate#isDue(long, int, int)} says it is
 * that tile's turn, so a dense terminal array does not hit the network on
 * the same tick. The gate hashes the block position, and the hash of a
 * particular position is {@link Integer#MIN_VALUE} (the original mod had a
 * gate that could never fire for that hash; ours cannot).
 *
 * <p>
 * Persistence is tolerant: any of the four keys may be missing from a
 * saved tag, in which case the field keeps its default.
 *
 * Side: server authoritative.
 */
public class StorageTerminalTileEntity extends GatedTileEntity {

    /**
     * How often (in ticks) a given terminal may rescan its beacon. TERM-04:
     * the stagger gate must also fire for the position hash
     * {@link Integer#MIN_VALUE}; {@link StaggerGate} already guarantees
     * that.
     */
    public static final int BEACON_SCAN_INTERVAL = 40;

    /**
     * The sort order the terminal presents items in.
     */
    private int sorting = TerminalModes.DEFAULT_SORTING;

    /**
     * The set of mode flags the terminal has enabled (stack mode, search
     * behavior, etc.).
     */
    private int modes = TerminalModes.DEFAULT_MODES;

    /**
     * The search type (by id, by name, ...).
     */
    private int searchType = TerminalModes.DEFAULT_SEARCH_TYPE;

    /**
     * The text of the last search the player ran.
     */
    private String lastSearch = "";

    /**
     * The beacon level this terminal currently receives, or -1 if not yet
     * scanned.
     */
    private int beaconLevel = -1;

    /**
     * @return the module this tile's behavior gate keys off of.
     */
    @Override
    protected Module module() {
        return Module.TERMINAL;
    }

    /**
     * Runs on the server every tick (when the gate is active). Rescans
     * the beacon on the stagger; otherwise does nothing.
     */
    @Override
    protected void serverTick() {
        if (worldObj == null) {
            return;
        }
        if (StaggerGate
            .isDue(worldObj.getTotalWorldTime(), Objects.hash(xCoord, yCoord, zCoord), BEACON_SCAN_INTERVAL)) {
            rescanBeacon();
        }
    }

    /**
     * Re-detect the wireless beacon level this terminal is tuned to.
     *
     * <p>
     * Today this only resets the cached level to -1 ("not scanned"); the
     * real scan (finding the beacon, measuring its level) belongs to the
     * wireless milestone.
     */
    private void rescanBeacon() {
        beaconLevel = -1;
    }

    /**
     * @return the sort order the terminal presents items in.
     */
    public int getSorting() {
        return sorting;
    }

    /**
     * Sets the sort order the terminal presents items in.
     *
     * @param sorting the new sort order.
     */
    public void setSorting(int sorting) {
        this.sorting = sorting;
        markDirty();
    }

    /**
     * @return the terminal's mode flags.
     */
    public int getModes() {
        return modes;
    }

    /**
     * Sets the terminal's mode flags.
     *
     * @param modes the new mode flags.
     */
    public void setModes(int modes) {
        this.modes = modes;
        markDirty();
    }

    /**
     * @return the terminal's search type.
     */
    public int getSearchType() {
        return searchType;
    }

    /**
     * Sets the terminal's search type.
     *
     * @param searchType the new search type.
     */
    public void setSearchType(int searchType) {
        this.searchType = searchType;
        markDirty();
    }

    /**
     * @return the text of the last search the player ran.
     */
    public String getLastSearch() {
        return lastSearch;
    }

    /**
     * Sets the text of the last search. Truncates to 256 characters and
     * maps {@code null} to the empty string.
     *
     * @param lastSearch the new search text; may be {@code null}.
     */
    public void setLastSearch(String lastSearch) {
        if (lastSearch == null) {
            lastSearch = "";
        }
        if (lastSearch.length() > 256) {
            lastSearch = lastSearch.substring(0, 256);
        }
        this.lastSearch = lastSearch;
        markDirty();
    }

    /**
     * @return the beacon level this terminal currently receives, or -1 if
     *         not yet scanned.
     */
    public int getBeaconLevel() {
        return beaconLevel;
    }

    /**
     * Builds a fresh backend over the inventories this terminal can reach
     * through cables. With no world yet (e.g. during block placement) this
     * returns a backend over an empty list.
     *
     * @return a backend over the reachable inventories.
     */
    public TerminalBackend backend() {
        List<IInventory> inventories = worldObj == null ? Collections.<IInventory>emptyList()
            : NetworkQuery.inventoriesFrom(worldObj, xCoord, yCoord, zCoord);
        return new TerminalBackend(inventories);
    }

    /**
     * Whether a player may open this terminal's GUI: it must still be the
     * tile at its position, and the player must be within 8 blocks.
     *
     * @param p the player asking.
     * @return true if {@code p} may interact with this terminal.
     */
    public boolean canInteractWith(EntityPlayer p) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && p.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64.0;
    }

    /**
     * Writes this terminal's persisted fields into {@code tag}. Missing
     * fields are written as their current values; the four keys are
     * "sorting", "modes", "searchType" and "lastSearch".
     *
     * @param tag the tag to write into; may be empty.
     */
    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("sorting", sorting);
        tag.setInteger("modes", modes);
        tag.setInteger("searchType", searchType);
        tag.setString("lastSearch", lastSearch);
    }

    /**
     * Restores the persisted fields from {@code tag}. Each key is read ONLY
     * when it is present, so a tag written by an older or partial save
     * leaves the corresponding field at its default; unknown keys are
     * ignored.
     *
     * @param tag the tag to read from.
     */
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("sorting")) {
            this.sorting = tag.getInteger("sorting");
        }
        if (tag.hasKey("modes")) {
            this.modes = tag.getInteger("modes");
        }
        if (tag.hasKey("searchType")) {
            this.searchType = tag.getInteger("searchType");
        }
        if (tag.hasKey("lastSearch")) {
            this.lastSearch = tag.getString("lastSearch");
        }
    }
}
