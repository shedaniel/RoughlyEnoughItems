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

package me.shedaniel.rei.jeicompat.wrap;

import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrap;
import static me.shedaniel.rei.jeicompat.JEIPluginDetector.wrapContext;

public enum JEIStackHelper implements IStackHelper {
    INSTANCE;
    
    @Override
    public boolean isEquivalent(@Nullable ItemStack lhs, @Nullable ItemStack rhs, @NotNull UidContext context) {
        if (context == UidContext.Ingredient) {
            return EntryStacks.equalsExact(wrap(lhs), wrap(rhs));
        }
        return EntryStacks.equalsFuzzy(wrap(lhs), wrap(rhs));
    }
    
    @Override
    @NotNull
    public String getUniqueIdentifierForStack(@NotNull ItemStack stack, @NotNull UidContext context) {
        return String.valueOf(ItemComparatorRegistry.getInstance().hashOf(wrapContext(context), stack));
    }
}
