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

package me.shedaniel.rei.impl.client.gui.widget;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.architectury.registry.ReloadListenerRegistry;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CachedEntryListRender {
    public static final int RESOLUTION = 64;
    public static DynamicTexture cachedTexture;
    public static ResourceLocation cachedTextureLocation;
    public static Long2LongMap hash = new Long2LongOpenHashMap();
    
    public static class Sprite {
        public final float u0;
        public final float u1;
        public final float v0;
        public final float v1;
        
        public Sprite(float u0, float u1, float v0, float v1) {
            this.u0 = u0;
            this.u1 = u1;
            this.v0 = v0;
            this.v1 = v1;
        }
    }
    
    static {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, (barrier, resourceManager, preparationProfiler, reloadProfiler, preparationExecutor, reloadExecutor) -> {
            return barrier.wait(Unit.INSTANCE).thenRunAsync(CachedEntryListRender::refresh, reloadExecutor);
        });
    }
    
    public static void refresh() {
        RoughlyEnoughItemsCore.LOGGER.info("Refreshing cached entry list texture...");
        if (cachedTextureLocation != null) {
            Minecraft.getInstance().getTextureManager().release(cachedTextureLocation);
            cachedTextureLocation = null;
        }
        if (cachedTexture != null) {
            cachedTexture.close();
            cachedTexture = null;
        }
        hash = new Long2LongOpenHashMap();
    }
    
    @Nullable
    public static Sprite get(EntryStack<?> stack) {
        if (stack.getType() == VanillaEntryTypes.ITEM) {
            if (stack.getNullable(EntryStack.Settings.RENDERER) != null) {
                return null;
            }
            
            if (cachedTexture == null) {
                prepare();
            }
            
            long hashExact = EntryStacks.hashExact(stack);
            
            long hashOrDefault = hash.getOrDefault(hashExact, -1L);
            if (hashOrDefault != -1L) {
                // unpack
                int x = (int) (hashOrDefault >> 32);
                int y = (int) (hashOrDefault & 0xFFFFFFFFL);
                float width = cachedTexture.getPixels().getWidth();
                float height = cachedTexture.getPixels().getWidth();
                return new Sprite(x * RESOLUTION / width, (x + 1) * RESOLUTION / width, y * RESOLUTION / height, (y + 1) * RESOLUTION / height);
            }
        }
        
        return null;
    }
    
    private static void prepare() {
        int side = 4;
        List<EntryStack<?>> list = EntryRegistry.getInstance().getPreFilteredList();
        while (side * side < list.size()) {
            side++;
        }
        
        int width = side * RESOLUTION;
        int height = side * RESOLUTION;
        
        RoughlyEnoughItemsCore.LOGGER.info("Preparing cached texture with size %sx%s for %sx%s entries", width, height, side, side);
        
        hash = new Long2LongOpenHashMap(list.size() + 10);
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        TextureTarget target = new TextureTarget(width, height, true, false);
        target.bindWrite(true);
        Matrix4f projectionMatrix = Matrix4f.orthographic(0.0F, width, 0.0F, height, 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(projectionMatrix);
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        modelViewStack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();
        
        Lighting.setupFor3DItems();
        PoseStack matrices = new PoseStack();
        Rectangle bounds = new Rectangle();
        
        int index = 0;
        for (EntryStack<?> stack : list) {
            int x = index % side;
            int y = index / side;
            bounds.setBounds(x * RESOLUTION, y * RESOLUTION, RESOLUTION, RESOLUTION);
            ((EntryRenderer<Object>) stack.getDefinition().getRenderer()).render((EntryStack<Object>) stack, matrices, bounds, -1, -1, 0);
            hash.put(EntryStacks.hashExact(stack), pack(x, y));
            index++;
        }
        
        NativeImage nativeImage = new NativeImage(width, height, false);
        RenderSystem.bindTexture(target.getColorTextureId());
        nativeImage.downloadTexture(0, false);
        nativeImage.flipY();
        
        cachedTexture = new DynamicTexture(nativeImage);
        cachedTextureLocation = minecraft.getTextureManager().register("rei_cached_entries", cachedTexture);
        
        target.destroyBuffers();
        Minecraft.getInstance().levelRenderer.graphicsChanged();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    
    private static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
