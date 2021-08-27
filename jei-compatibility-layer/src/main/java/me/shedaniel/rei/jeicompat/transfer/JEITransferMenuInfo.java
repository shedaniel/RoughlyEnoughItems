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

import com.google.common.base.Suppliers;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.api.common.transfer.info.MenuTransferException;
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class JEITransferMenuInfo<T extends AbstractContainerMenu> implements SimplePlayerInventoryMenuInfo<T, Display> {
    public static final String KEY = "REI-JEI-Transfer-Data";
    @Nullable
    protected Supplier<JEIRecipeTransferData<T>> data;
    @Nullable
    protected T menu;
    
    public JEIRecipeTransferData<T> getData(MenuSerializationContext<T, ?, Display> context) {
        menu = context.getMenu();
        JEIRecipeTransferData<T> transferData = data.get();
        menu = null;
        return transferData;
    }
    
    @Override
    public Iterable<SlotAccessor> getInventorySlots(MenuInfoContext<T, ?, Display> context) {
        return CollectionUtils.map(getData(context).getInventorySlots(), SlotAccessor::fromSlot);
    }
    
    @Override
    public Iterable<SlotAccessor> getInputSlots(MenuInfoContext<T, ?, Display> context) {
        return CollectionUtils.map(getData(context).getRecipeSlots(), SlotAccessor::fromSlot);
    }
    
    @Override
    public CompoundTag save(MenuSerializationContext<T, ?, Display> context, Display display) {
        CompoundTag tag = SimplePlayerInventoryMenuInfo.super.save(context, display);
        tag.put(KEY, getData(context).save(new CompoundTag()));
        return tag;
    }
    
    @Override
    public Display read(MenuSerializationContext<T, ?, Display> context, CompoundTag tag) {
        data = Suppliers.ofInstance(JEIRecipeTransferData.read(context.getMenu(), tag.getCompound(KEY)));
        return SimplePlayerInventoryMenuInfo.super.read(context, tag);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static class Client<T extends AbstractContainerMenu> extends JEITransferMenuInfo<T> {
        private final IRecipeTransferInfo<T> info;
        
        public Client(Function<T, JEIRecipeTransferData<T>> data, IRecipeTransferInfo<T> info) {
            this.info = info;
            this.data = Suppliers.memoize(() -> data.apply(Objects.requireNonNull(menu)));
        }
        
        @Override
        public void validate(MenuInfoContext<T, ?, Display> context) throws MenuTransferException {
            if (!info.canHandle(context.getMenu())) {
                throw MenuTransferException.createNotApplicable();
            }
        }
    }
}
