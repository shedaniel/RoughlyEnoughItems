package me.shedaniel.rei.impl.client.gui.craftable;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import net.minecraft.client.Minecraft;

public class CraftableFilter {
    public static final CraftableFilter INSTANCE = new CraftableFilter();
    private boolean dirty = false;
    private LongSet invStacks = new LongOpenHashSet();
    
    public void markDirty() {
        dirty = true;
    }
    
    public boolean wasDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        
        return Minecraft.getInstance().player.containerMenu != null;
    }
    
    public void tick() {
        if (dirty) return;
        LongSet currentStacks = ClientHelperImpl.getInstance()._getInventoryItemsTypes();
        if (!currentStacks.equals(this.invStacks)) {
            invStacks = new LongOpenHashSet(currentStacks);
            markDirty();
        }
    }
    
    public boolean matches(EntryStack<?> stack, Iterable<SlotAccessor> inputSlots) {
        if (invStacks.contains(EntryStacks.hashExact(stack))) return true;
        if (stack.getType() != VanillaEntryTypes.ITEM) return false;
        for (SlotAccessor slot : inputSlots) {
            EntryStack<?> itemStack = EntryStacks.of(slot.getItemStack());
            if (!itemStack.isEmpty() && EntryStacks.equalsExact(itemStack, stack)) {
                return true;
            }
        }
        return false;
    }
}
