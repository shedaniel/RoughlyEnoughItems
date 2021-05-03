/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.forge.api.PointHelper;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.jeicompat.wrap.JEIGuiHelper;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
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
        Widget widget = Widgets.createTexturedWidget(texture, 0, 0, u, v, width, height, textureWidth, textureHeight);
        return new IDrawableStatic() {
            @Override
            public void draw(@NotNull PoseStack matrixStack, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
                matrixStack.pushPose();
                matrixStack.translate(xOffset + paddingLeft, yOffset + paddingTop, 0);
                widget.render(matrixStack, PointHelper.getMouseX(), PointHelper.getMouseY(), 0);
                matrixStack.popPose();
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
        throw TODO();
    }
}
