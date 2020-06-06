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
