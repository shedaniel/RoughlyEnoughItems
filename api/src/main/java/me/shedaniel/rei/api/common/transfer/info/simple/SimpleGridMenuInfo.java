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

package me.shedaniel.rei.api.common.transfer.info.simple;

import me.shedaniel.rei.api.common.display.SimpleMenuDisplay;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuTransferException;
import me.shedaniel.rei.api.common.transfer.info.stack.StackAccessor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A simple implementation of {@link MenuInfo} that provides {@link StackAccessor} by {@link net.minecraft.world.inventory.Slot}.
 * <p>
 * Designed to be used for {@link net.minecraft.world.inventory.RecipeBookMenu}, and expects a width and height for a grid for the input.
 * Requires the display to be a implementation of {@link SimpleMenuDisplay}, to provide the width and height of the display.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 */
public interface SimpleGridMenuInfo<T extends AbstractContainerMenu, D extends SimpleMenuDisplay> extends SimplePlayerInventoryMenuInfo<T, D> {
    default Iterable<StackAccessor> getInputStacks(MenuInfoContext<T, ?, D> context) {
        return IntStream.range(0, getCraftingWidth(context.getMenu()) * getCraftingHeight(context.getMenu()) + 1)
                .filter(value -> value != getCraftingResultSlotIndex(context.getMenu()))
                .mapToObj(value -> StackAccessor.fromSlot(context.getMenu().getSlot(value)))
                .collect(Collectors.toList());
    }
    
    int getCraftingResultSlotIndex(T menu);
    
    int getCraftingWidth(T menu);
    
    int getCraftingHeight(T menu);
    
    @Override
    default void validate(MenuInfoContext<T, ?, D> context) throws MenuTransferException {
        int width = getCraftingWidth(context.getMenu());
        int height = getCraftingHeight(context.getMenu());
        SimpleMenuDisplay display = context.getDisplay();
        if (display.getWidth() > width || display.getHeight() > height) {
            throw new MenuTransferException(new TranslatableComponent("error.rei.transfer.too_small", width, height));
        }
    }
}
