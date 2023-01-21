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

package me.shedaniel.rei.plugin.common.forge;

import com.google.common.base.Predicates;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.DefaultPlugin;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.stream.IntStream;

public class DefaultPluginImpl extends DefaultPlugin {
    @Override
    public void registerFluidSupport(FluidSupportProvider support) {
        super.registerFluidSupport(support);
        support.register(stack -> {
            ItemStack itemStack = stack.getValue();
            LazyOptional<IFluidHandlerItem> handlerOptional = FluidUtil.getFluidHandler(itemStack);
            if (handlerOptional.isPresent()) {
                IFluidHandlerItem handler = handlerOptional.orElse(null);
                if (handler.getTanks() > 0) {
                    return CompoundEventResult.interruptTrue(IntStream.range(0, handler.getTanks())
                            .mapToObj(handler::getFluidInTank)
                            .filter(Predicates.not(FluidStack::isEmpty))
                            .map(FluidStackHooksForge::fromForge)
                            .map(EntryStacks::of));
                }
            }
            
            return CompoundEventResult.pass();
        });
    }
}
