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

import me.shedaniel.rei.api.server.ContainerInfo;
import me.shedaniel.rei.api.server.RecipeFinder;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class CraftingContainerInfoWrapper<T extends RecipeBookMenu<?>> implements ContainerInfo<T> {
    private Class<? extends RecipeBookMenu<?>> containerClass;
    
    public CraftingContainerInfoWrapper(Class<T> containerClass) {
        this.containerClass = containerClass;
    }
    
    public static <R extends RecipeBookMenu<?>> ContainerInfo<R> create(Class<R> containerClass) {
        return new CraftingContainerInfoWrapper<>(containerClass);
    }
    
    @Override
    public Class<? extends AbstractContainerMenu> getContainerClass() {
        return containerClass;
    }
    
    @Override
    public int getCraftingResultSlotIndex(T container) {
        return container.getResultSlotIndex();
    }
    
    @Override
    public int getCraftingWidth(T container) {
        return container.getGridWidth();
    }
    
    @Override
    public int getCraftingHeight(T container) {
        return container.getGridHeight();
    }
    
    @Override
    public void clearCraftingSlots(T container) {
        container.clearCraftingContent();
    }
    
    @Override
    public void populateRecipeFinder(T container, RecipeFinder var1) {
        container.fillCraftSlotsStackedContents(new net.minecraft.world.entity.player.StackedContents() {
            @Override
            public void accountSimpleStack(ItemStack itemStack_1) {
                var1.addNormalItem(itemStack_1);
            }
        });
    }
}
