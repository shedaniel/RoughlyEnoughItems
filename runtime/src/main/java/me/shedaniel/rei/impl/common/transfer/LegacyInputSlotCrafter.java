/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.common.transfer;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class LegacyInputSlotCrafter<T extends AbstractContainerMenu, C extends Container, D extends Display> extends InputSlotCrafter<T, C> implements MenuInfoContext<T, ServerPlayer, D> {
    protected CategoryIdentifier<D> category;
    protected MenuInfo<T, D> menuInfo;
    
    protected LegacyInputSlotCrafter(CategoryIdentifier<D> category, T container) {
        super(container);
        this.category = category;
    }
    
    public void setMenuInfo(MenuInfo<T, D> menuInfo) {
        this.menuInfo = menuInfo;
    }
    
    public static <T extends AbstractContainerMenu, C extends Container, D extends Display> LegacyInputSlotCrafter<T, C, D> start(CategoryIdentifier<D> category, T menu, ServerPlayer player, CompoundTag display, boolean hasShift) {
        LegacyInputSlotCrafter<T, C, D> crafter = new LegacyInputSlotCrafter<>(category, menu);
        MenuInfo<T, D> menuInfo = Objects.requireNonNull(MenuInfoRegistry.getInstance().get(category, menu, crafter, display), "Container Info does not exist on the server!");
        crafter.setMenuInfo(menuInfo);
        crafter.fillInputSlots(player, hasShift);
        return crafter;
    }
    
    @Override
    protected Iterable<SlotAccessor> getInputSlots() {
        return this.menuInfo.getInputSlots(this);
    }
    
    @Override
    protected Iterable<SlotAccessor> getInventorySlots() {
        return this.menuInfo.getInventorySlots(this);
    }
    
    @Override
    protected List<InputIngredient<ItemStack>> getInputs() {
        return this.menuInfo.getInputsIndexed(this, true);
    }
    
    @Override
    protected void populateRecipeFinder(RecipeFinder recipeFinder) {
        this.menuInfo.getRecipeFinderPopulator().populate(this, recipeFinder);
    }
    
    @Override
    protected void markDirty() {
        this.menuInfo.markDirty(this);
    }
    
    @Override
    protected void cleanInputs() {
        this.menuInfo.getInputCleanHandler().clean(this);
    }
    
    @Override
    public T getMenu() {
        return container;
    }
    
    @Override
    public ServerPlayer getPlayerEntity() {
        return player;
    }
    
    @Override
    public D getDisplay() {
        return menuInfo.getDisplay();
    }
    
    @Override
    public CategoryIdentifier<D> getCategoryIdentifier() {
        return category;
    }
}
