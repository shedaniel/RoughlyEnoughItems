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

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionResultHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Experimental library, scheduled to change if needed.
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FluidSupportProvider {
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    FluidSupportProvider INSTANCE = new FluidSupportProvider() {
        @Override
        public void registerProvider(@NotNull Provider provider) {
            getInstance().registerProvider(provider);
        }
        
        @Override
        public @NotNull Stream<EntryStack> itemToFluids(@NotNull EntryStack itemStack) {
            return getInstance().itemToFluids(itemStack);
        }
    };
    
    static FluidSupportProvider getInstance() {
        return Internals.getFluidSupportProvider();
    }
    
    /**
     * @deprecated Please switch to {@link FluidSupportProvider#registerProvider(Provider)}
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void registerFluidProvider(@NotNull FluidProvider provider) {
        registerProvider(itemStack -> {
            EntryStack stack = Objects.requireNonNull(provider.itemToFluid(itemStack));
            if (!stack.isEmpty())
                return InteractionResultHolder.success(Stream.of(stack));
            return InteractionResultHolder.pass(null);
        });
    }
    
    void registerProvider(@NotNull Provider provider);
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @NotNull
    default EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
        return EntryStack.empty();
    }
    
    @NotNull
    default EntryStack itemToFluid(@NotNull EntryStack itemStack) {
        return itemToFluids(itemStack).findFirst().orElse(EntryStack.empty());
    }
    
    @NotNull
    Stream<EntryStack> itemToFluids(@NotNull EntryStack itemStack);
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    interface FluidProvider {
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        @NotNull
        default EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
            return EntryStack.empty();
        }
        
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        @NotNull
        default EntryStack itemToFluid(@NotNull EntryStack itemStack) {
            return EntryStack.empty();
        }
    }
    
    @FunctionalInterface
    interface Provider {
        @NotNull
        InteractionResultHolder<@Nullable Stream<@NotNull EntryStack>> itemToFluid(@NotNull EntryStack itemStack);
    }
}
