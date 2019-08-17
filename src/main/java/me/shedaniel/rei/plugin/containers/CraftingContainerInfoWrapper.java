/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.containers;

import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.RecipeFinder;
import net.minecraft.container.Container;
import net.minecraft.container.CraftingContainer;
import net.minecraft.item.ItemStack;

public class CraftingContainerInfoWrapper<T extends CraftingContainer<?>> implements ContainerInfo<T> {
    private Class<? extends CraftingContainer<?>> containerClass;
    
    public <T extends CraftingContainer<?>> CraftingContainerInfoWrapper(Class<T> containerClass) {
        this.containerClass = containerClass;
    }
    
    @Override
    public Class<? extends Container> getContainerClass() {
        return containerClass;
    }
    
    @Override
    public int getCraftingResultSlotIndex(T container) {
        return container.getCraftingResultSlotIndex();
    }
    
    @Override
    public int getCraftingWidth(T container) {
        return container.getCraftingWidth();
    }
    
    @Override
    public int getCraftingHeight(T container) {
        return container.getCraftingHeight();
    }
    
    @Override
    public void clearCraftingSlots(T container) {
        container.clearCraftingSlots();
    }
    
    @Override
    public void populateRecipeFinder(T container, RecipeFinder var1) {
        container.populateRecipeFinder(new net.minecraft.recipe.RecipeFinder() {
            @Override
            public void addNormalItem(ItemStack itemStack_1) {
                var1.addNormalItem(itemStack_1);
            }
        });
    }
}
