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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Experimental library, scheduled to change if needed.
 */
@ApiStatus.Experimental
@OnlyIn(Dist.CLIENT)
public interface FluidSupportProvider {
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    FluidSupportProvider INSTANCE = new FluidSupportProvider() {
        @Override
        public void registerFluidProvider(@NotNull FluidProvider provider) {
            getInstance().registerFluidProvider(provider);
        }
        
        @Override
        public @NotNull EntryStack itemToFluid(@NotNull EntryStack itemStack) {
            return getInstance().itemToFluid(itemStack);
        }
    };
    
    static FluidSupportProvider getInstance() {
        return Internals.getFluidSupportProvider();
    }
    
    void registerFluidProvider(@NotNull FluidProvider provider);
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @NotNull
    default EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
        return EntryStack.empty();
    }
    
    @NotNull
    EntryStack itemToFluid(@NotNull EntryStack itemStack);
    
    interface FluidProvider {
        @Deprecated
        @ApiStatus.ScheduledForRemoval
        @NotNull
        default EntryStack fluidToItem(@NotNull EntryStack fluidStack) {
            return EntryStack.empty();
        }
        
        @NotNull
        default EntryStack itemToFluid(@NotNull EntryStack itemStack) {
            return EntryStack.empty();
        }
    }
}
