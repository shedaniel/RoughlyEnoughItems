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

package me.shedaniel.rei.impl;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class SimpleFluidRenderer {
    private static final Map<Fluid, FluidRenderingData> FLUID_DATA = new HashMap<>();
    
    private SimpleFluidRenderer() {}
    
    @Nullable
    public static FluidRenderingData fromFluid(Fluid fluid) {
        return FLUID_DATA.computeIfAbsent(fluid, FluidRenderingDataImpl::from);
    }
    
    public interface FluidRenderingData {
        Sprite getSprite();
        
        int getColor();
    }
    
    public static final class FluidRenderingDataImpl implements FluidRenderingData {
        private final Sprite sprite;
        private final int color;
        
        public FluidRenderingDataImpl(Sprite sprite, int color) {
            this.sprite = sprite;
            this.color = color;
        }
        
        public static FluidRenderingData from(Fluid fluid) {
            FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
            if (fluidRenderHandler == null)
                return null;
            Sprite[] sprites = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().world == null ? null : BlockPos.ORIGIN, fluid.getDefaultState());
            int color = -1;
            if (MinecraftClient.getInstance().world != null)
                color = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.getDefaultState());
            return new FluidRenderingDataImpl(sprites[0], color);
        }
        
        @Override
        public Sprite getSprite() {
            return sprite;
        }
        
        @Override
        public int getColor() {
            return color;
        }
    }
}
