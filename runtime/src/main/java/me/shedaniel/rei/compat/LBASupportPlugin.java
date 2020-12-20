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

package me.shedaniel.rei.compat;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.entry.EntryStacks;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;

import java.util.stream.Stream;

public class LBASupportPlugin implements REIPluginV0 {
    @Override
    public ResourceLocation getPluginIdentifier() {
        return new ResourceLocation("roughlyenoughitems", "lba_support");
    }
    
    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        FluidSupportProvider.getInstance().registerProvider(entry -> {
            GroupedFluidInvView view = FluidAttributes.GROUPED_INV_VIEW.get(entry.getValue());
            if (view.getStoredFluids().size() > 0)
                return InteractionResultHolder.success(view.getStoredFluids().stream()
                        .filter(fluidKey -> !fluidKey.isEmpty() && fluidKey.getRawFluid() != null)
                        .map(fluidKey -> {
                            FluidAmount amount = view.getAmount_F(fluidKey);
                            return EntryStacks.of(fluidKey.getRawFluid(), Fraction.of(amount.whole, amount.numerator, amount.denominator));
                        }));
            return InteractionResultHolder.pass(Stream.empty());
        });
    }
}
