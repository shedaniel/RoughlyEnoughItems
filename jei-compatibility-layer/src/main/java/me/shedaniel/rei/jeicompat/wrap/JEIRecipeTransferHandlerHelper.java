/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.jeicompat.wrap;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashSet;

public enum JEIRecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
    INSTANCE;
    
    @Override
    public IRecipeTransferError createInternalError() {
        return new JEIRecipeTransferError(IRecipeTransferError.Type.INTERNAL, Component.translatable("error.rei.internal.error", ""), new HashSet<>());
    }
    
    @Override
    public IRecipeTransferError createUserErrorWithTooltip(Component tooltipMessage) {
        return new JEIRecipeTransferError(IRecipeTransferError.Type.USER_FACING, tooltipMessage, new HashSet<>());
    }
    
    @Override
    public IRecipeTransferError createUserErrorForMissingSlots(Component tooltipMessage, Collection<IRecipeSlotView> missingItemSlots) {
        return new JEIRecipeTransferError(IRecipeTransferError.Type.USER_FACING, tooltipMessage, new HashSet<>(missingItemSlots));
    }
}
