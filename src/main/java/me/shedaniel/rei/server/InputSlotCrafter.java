/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.server;

import com.google.common.collect.Lists;
import net.minecraft.container.CraftingContainer;
import net.minecraft.container.CraftingTableContainer;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InputSlotCrafter<C extends Inventory> {
    
    protected final RecipeFinder recipeFinder = new RecipeFinder();
    protected CraftingContainer<C> craftingContainer;
    protected PlayerInventory inventory;
    
    private InputSlotCrafter(CraftingContainer<C> craftingContainer_1) {
        this.craftingContainer = craftingContainer_1;
    }
    
    public static <C extends Inventory> void start(CraftingContainer<C> craftingContainer_1, ServerPlayerEntity player, Map<Integer, List<ItemStack>> map, boolean hasShift) {
        new InputSlotCrafter<C>(craftingContainer_1).fillInputSlots(player, map, hasShift);
    }
    
    private void fillInputSlots(ServerPlayerEntity player, Map<Integer, List<ItemStack>> map, boolean hasShift) {
        /*
         * Steps:
         * Return the already placed items on the grid
         * Check if the player have the enough resource to even craft one
         * Calculate how many items the player is going to craft
         * Move the best suited items for the player to use
         * Send container updates to the player
         * Profit??
         */
        this.inventory = player.inventory;
        if (this.canReturnInputs() || player.isCreative()) {
            // Return the already placed items on the grid
            this.returnInputs();
            
            // Check if the player have the enough resource to even craft one
            if (!isPossibleToCraft(map)) {
                craftingContainer.sendContentUpdates();
                player.inventory.markDirty();
                throw new NullPointerException();
            }
            
            // Calculate how many items the player is going to craft
            int amountCrafting = hasShift ? 0 : 1;
            if (hasShift) {
            
            }
            
            // TODO: Rewrite most parts of this
            //            map.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).forEach(entry -> {
            //                int id = entry.getKey().intValue();
            //                List<ItemStack> possibleStacks = entry.getValue();
            //                boolean done = false;
            //                for (ItemStack possibleStack : possibleStacks) {
            //                    int requiredCount = possibleStack.getCount();
            //                    int invCount = 0;
            //                    for (ItemStack stack : inventory.main) {
            //                        if (ItemStack.areItemsEqualIgnoreDamage(possibleStack, stack))
            //                            invCount += stack.getCount();
            //                    }
            //                    if (invCount >= requiredCount) {
            //                        for (ItemStack stack : inventory.main) {
            //                            if (ItemStack.areItemsEqualIgnoreDamage(possibleStack, stack)) {
            //                                Slot containerSlot = craftingContainer.getSlot(id + craftingContainer.getCraftingResultSlotIndex() + 1);
            //                                while (containerSlot.getStack().getCount() < requiredCount && !stack.isEmpty()) {
            //                                    if (containerSlot.getStack().isEmpty()) {
            //                                        containerSlot.setStack(new ItemStack(stack.getItem(), 1));
            //                                    } else {
            //                                        containerSlot.getStack().setCount(containerSlot.getStack().getCount() + 1);
            //                                    }
            //                                    stack.setCount(stack.getCount() - 1);
            //                                }
            //                                if (containerSlot.getStack().getCount() >= requiredCount)
            //                                    break;
            //                            }
            //                        }
            //                        break;
            //                    }
            //                }
            //            });
            
            craftingContainer.sendContentUpdates();
            player.inventory.markDirty();
        }
    }
    
    private boolean isPossibleToCraft(Map<Integer, List<ItemStack>> map) {
        // Create a clone of player's inventory, and count
        DefaultedList<ItemStack> copyMain = DefaultedList.create();
        for (ItemStack stack : inventory.main) {
            copyMain.add(stack.copy());
        }
        for (Map.Entry<Integer, List<ItemStack>> entry : map.entrySet()) {
            List<ItemStack> possibleStacks = entry.getValue();
            boolean done = possibleStacks.isEmpty();
            for (ItemStack possibleStack : possibleStacks) {
                if (!done) {
                    int invRequiredCount = possibleStack.getCount();
                    for (ItemStack stack : copyMain) {
                        if (ItemStack.areItemsEqualIgnoreDamage(possibleStack, stack)) {
                            while (invRequiredCount > 0 && !stack.isEmpty()) {
                                invRequiredCount--;
                                stack.decrement(1);
                            }
                        }
                    }
                    if (invRequiredCount <= 0) {
                        done = true;
                    }
                }
            }
            if (!done)
                return false;
        }
        return true;
    }
    
    protected void returnInputs() {
        for (int int_1 = 0; int_1 < this.craftingContainer.getCraftingWidth() * this.craftingContainer.getCraftingHeight() + 1; ++int_1) {
            if (int_1 != this.craftingContainer.getCraftingResultSlotIndex() || !(this.craftingContainer instanceof CraftingTableContainer) && !(this.craftingContainer instanceof PlayerContainer)) {
                this.returnSlot(int_1);
            }
        }
        
        this.craftingContainer.clearCraftingSlots();
    }
    
    protected void returnSlot(int int_1) {
        ItemStack itemStack_1 = this.craftingContainer.getSlot(int_1).getStack();
        if (!itemStack_1.isEmpty()) {
            for (; itemStack_1.getCount() > 0; this.craftingContainer.getSlot(int_1).takeStack(1)) {
                int int_2 = this.inventory.getOccupiedSlotWithRoomForStack(itemStack_1);
                if (int_2 == -1) {
                    int_2 = this.inventory.getEmptySlot();
                }
                
                ItemStack itemStack_2 = itemStack_1.copy();
                itemStack_2.setCount(1);
                if (!this.inventory.insertStack(int_2, itemStack_2)) {
                    throw new IllegalStateException("Can't find any space for item in the inventory");
                }
            }
            
        }
    }
    
    private boolean canReturnInputs() {
        List<ItemStack> list_1 = Lists.newArrayList();
        int int_1 = this.getFreeInventorySlots();
        
        for (int int_2 = 0; int_2 < this.craftingContainer.getCraftingWidth() * this.craftingContainer.getCraftingHeight() + 1; ++int_2) {
            if (int_2 != this.craftingContainer.getCraftingResultSlotIndex()) {
                ItemStack itemStack_1 = this.craftingContainer.getSlot(int_2).getStack().copy();
                if (!itemStack_1.isEmpty()) {
                    int int_3 = this.inventory.getOccupiedSlotWithRoomForStack(itemStack_1);
                    if (int_3 == -1 && list_1.size() <= int_1) {
                        Iterator var6 = list_1.iterator();
                        
                        while (var6.hasNext()) {
                            ItemStack itemStack_2 = (ItemStack) var6.next();
                            if (itemStack_2.isItemEqualIgnoreDamage(itemStack_1) && itemStack_2.getCount() != itemStack_2.getMaxCount() && itemStack_2.getCount() + itemStack_1.getCount() <= itemStack_2.getMaxCount()) {
                                itemStack_2.increment(itemStack_1.getCount());
                                itemStack_1.setCount(0);
                                break;
                            }
                        }
                        
                        if (!itemStack_1.isEmpty()) {
                            if (list_1.size() >= int_1) {
                                return false;
                            }
                            
                            list_1.add(itemStack_1);
                        }
                    } else if (int_3 == -1) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    private int getFreeInventorySlots() {
        int int_1 = 0;
        Iterator var2 = this.inventory.main.iterator();
        while (var2.hasNext()) {
            ItemStack itemStack_1 = (ItemStack) var2.next();
            if (itemStack_1.isEmpty()) {
                ++int_1;
            }
        }
        return int_1;
    }
    
}
