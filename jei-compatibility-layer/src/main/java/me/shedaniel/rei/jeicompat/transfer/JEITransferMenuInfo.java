/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.jeicompat.transfer;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class JEITransferMenuInfo<T extends AbstractContainerMenu, R> implements SimplePlayerInventoryMenuInfo<T, Display> {
    public static final String KEY = "REI-JEI-Transfer-Data";
    @NotNull
    protected final Display display;
    protected final JEIRecipeTransferData<T, R> data;
    
    public JEITransferMenuInfo(Display display, @NotNull JEIRecipeTransferData<T, R> data) {
        this.display = display;
        this.data = data;
    }
    
    @Override
    public Iterable<SlotAccessor> getInventorySlots(MenuInfoContext<T, ?, Display> context) {
        return CollectionUtils.map(data.getInventorySlots(), SlotAccessor::fromSlot);
    }
    
    @Override
    public Iterable<SlotAccessor> getInputSlots(MenuInfoContext<T, ?, Display> context) {
        return CollectionUtils.map(data.getRecipeSlots(), SlotAccessor::fromSlot);
    }
    
    @Override
    public CompoundTag save(MenuSerializationContext<T, ?, Display> context, Display display) {
        CompoundTag tag = SimplePlayerInventoryMenuInfo.super.save(context, display);
        tag.put(KEY, data.save(new CompoundTag()));
        return tag;
    }
    
    @Override
    public Display getDisplay() {
        return display;
    }
}
