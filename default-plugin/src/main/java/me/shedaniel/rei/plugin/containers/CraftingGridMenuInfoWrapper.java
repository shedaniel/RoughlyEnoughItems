/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.containers;

import me.shedaniel.rei.api.common.display.SimpleMenuDisplay;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;

public class CraftingGridMenuInfoWrapper<T extends RecipeBookMenu<?>, D extends SimpleMenuDisplay> implements SimpleGridMenuInfo<T, D> {
    private final Class<? extends RecipeBookMenu<?>> containerClass;
    
    public CraftingGridMenuInfoWrapper(Class<T> containerClass) {
        this.containerClass = containerClass;
    }
    
    @Override
    public Class<? extends T> getContainerClass() {
        return (Class<? extends T>) containerClass;
    }
    
    @Override
    public int getCraftingResultSlotIndex(T menu) {
        return menu.getResultSlotIndex();
    }
    
    @Override
    public int getCraftingWidth(T menu) {
        return menu.getGridWidth();
    }
    
    @Override
    public int getCraftingHeight(T menu) {
        return menu.getGridHeight();
    }
    
    @Override
    public void clearInputSlots(T menu) {
        menu.clearCraftingContent();
    }
    
    @Override
    public void populateRecipeFinder(T container, RecipeFinder finder) {
        container.fillCraftSlotsStackedContents(new net.minecraft.world.entity.player.StackedContents() {
            @Override
            public void accountSimpleStack(ItemStack stack) {
                finder.addNormalItem(stack);
            }
        });
    }
}
