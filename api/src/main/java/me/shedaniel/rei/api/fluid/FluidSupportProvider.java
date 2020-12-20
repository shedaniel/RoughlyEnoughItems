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

package me.shedaniel.rei.api.fluid;

import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Experimental library, scheduled to change if needed.
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FluidSupportProvider {
    static FluidSupportProvider getInstance() {
        return Internals.getFluidSupportProvider();
    }
    
    void registerProvider(@NotNull Provider provider);
    
    @NotNull
    Optional<Stream<EntryStack<FluidStack>>> itemToFluids(@NotNull EntryStack<? extends ItemStack> itemStack);
    
    @FunctionalInterface
    interface Provider {
        @NotNull
        InteractionResultHolder<@Nullable Stream<@NotNull EntryStack<FluidStack>>> itemToFluid(@NotNull EntryStack<? extends ItemStack> itemStack);
    }
}
