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

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.DefaultPlugin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.stream.IntStream;

public class DefaultPluginImpl extends DefaultPlugin {
    @Override
    public void registerFluidSupport(FluidSupportProvider support) {
        super.registerFluidSupport(support);
        support.register(stack -> {
            ItemStack itemStack = stack.getValue();
            ResourceHandler<FluidResource> handler = ItemAccess.forStack(itemStack).getCapability(Capabilities.Fluid.ITEM);
            if (handler != null && handler.size() > 0) {
                return CompoundEventResult.interruptTrue(IntStream.range(0, handler.size())
                        .filter(i -> handler.getAmountAsInt(i) > 0)
                        .mapToObj(i -> handler.getResource(i).toStack(handler.getAmountAsInt(i)))
                        .map(neoforge -> dev.architectury.fluid.FluidStack.create(neoforge.getFluid(), neoforge.getAmount()))
                        .map(EntryStacks::of));
            }
            
            return CompoundEventResult.pass();
        });
    }
}
