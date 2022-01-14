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

import lombok.experimental.ExtensionMethod;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@ExtensionMethod(JEIPluginDetector.class)
public enum JEISubtypeManager implements ISubtypeManager {
    INSTANCE;
    
    @Override
    @Nullable
    public String getSubtypeInfo(ItemStack itemStack, UidContext context) {
        if (ItemComparatorRegistry.getInstance().containsComparator(itemStack.getItem())) {
            return String.valueOf(ItemComparatorRegistry.getInstance().hashOf(context.unwrapContext(), itemStack));
        }
        return null;
    }
    
    @Override
    @Nullable
    public String getSubtypeInfo(FluidStack fluidStack, UidContext context) {
        if (FluidComparatorRegistry.getInstance().containsComparator(fluidStack.getFluid())) {
            return String.valueOf(FluidComparatorRegistry.getInstance().hashOf(context.unwrapContext(), FluidStackHooksForge.fromForge(fluidStack)));
        }
        return null;
    }
}
