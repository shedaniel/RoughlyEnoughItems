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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.TODO;

public class JEIDrawableBuilder implements IDrawableBuilder {
    private ResourceLocation texture;
    private int u;
    private int v;
    private int width;
    private int height;
    private int textureWidth = 256;
    private int textureHeight = 256;
    private int paddingTop;
    private int paddingBottom;
    private int paddingLeft;
    private int paddingRight;
    
    public JEIDrawableBuilder(ResourceLocation texture, int u, int v, int width, int height) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder setTextureSize(int width, int height) {
        this.textureWidth = width;
        this.textureHeight = height;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder addPadding(int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableBuilder trim(int trimTop, int trimBottom, int trimLeft, int trimRight) {
        this.u += trimLeft;
        this.v += trimTop;
        this.width -= trimLeft + trimRight;
        this.height -= trimTop + trimBottom;
        return this;
    }
    
    @Override
    @NotNull
    public IDrawableStatic build() {
        int actualWidth = width + paddingLeft + paddingRight;
        int actualHeight = height + paddingTop + paddingBottom;
        return new IDrawableStatic() {
            /**
             * The following method is licensed with The MIT License (MIT)
             * Copyright (c) 2014-2015 mezz
             * 
             * Full license text can be found in the https://github.com/mezz/JustEnoughItems/blob/1.17/LICENSE.txt
             */
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                RenderSystem.setShaderTexture(0, texture);
                Matrix4f matrix = matrixStack.last().pose();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                
                int xStart = xOffset + JEIDrawableBuilder.this.paddingLeft + maskLeft;
                int yStart = yOffset + JEIDrawableBuilder.this.paddingTop + maskTop;
                int u = JEIDrawableBuilder.this.u + maskLeft;
                int v = JEIDrawableBuilder.this.v + maskTop;
                int width = JEIDrawableBuilder.this.width - maskRight - maskLeft;
                int height = JEIDrawableBuilder.this.height - maskBottom - maskTop;
                float f = 1.0F / JEIDrawableBuilder.this.textureWidth;
                float f1 = 1.0F / JEIDrawableBuilder.this.textureHeight;
                
                float z = 0.0F;
                float xEnd = xStart + width;
                float yEnd = yStart + height;
                float uStart = u * f;
                float uEnd = (u + width) * f;
                float vStart = v * f1;
                float vEnd = (v + height) * f1;
                
                bufferBuilder.vertex(matrix, xStart, yEnd, z).uv(uStart, vEnd).endVertex();
                bufferBuilder.vertex(matrix, xEnd, yEnd, z).uv(uEnd, vEnd).endVertex();
                bufferBuilder.vertex(matrix, xEnd, yStart, z).uv(uEnd, vStart).endVertex();
                bufferBuilder.vertex(matrix, xStart, yStart, z).uv(uStart, vStart).endVertex();
                BufferUploader.drawWithShader(bufferBuilder.end());
            }
            
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset) {
                draw(matrixStack, xOffset, yOffset, 0, 0, 0, 0);
            }
            
            @Override
            public int getWidth() {
                return actualWidth;
            }
            
            @Override
            public int getHeight() {
                return actualHeight;
            }
        };
    }
    
    @Override
    @NotNull
    public IDrawableAnimated buildAnimated(int ticksPerCycle, @NotNull IDrawableAnimated.StartDirection startDirection, boolean inverted) {
        return JEIGuiHelper.INSTANCE.createAnimatedDrawable(build(), ticksPerCycle, startDirection, inverted);
    }
    
    @Override
    @NotNull
    public IDrawableAnimated buildAnimated(@NotNull ITickTimer tickTimer, @NotNull IDrawableAnimated.StartDirection startDirection) {
        return JEIGuiHelper.INSTANCE.createAnimatedDrawable(build(), tickTimer, startDirection);
    }
}
