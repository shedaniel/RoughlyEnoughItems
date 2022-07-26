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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.config.RecipeBorderType;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class PanelWidget extends Panel {
    private static final PanelWidget TEMP = new PanelWidget(new Rectangle());
    private Rectangle bounds;
    private int color = -1;
    private int xTextureOffset = 0;
    private int yTextureOffset = RecipeBorderType.DEFAULT.getYOffset();
    private Predicate<Panel> rendering = Predicates.alwaysTrue();
    private final NumberAnimator<Float> darkBackgroundAlpha = ValueAnimator.ofFloat()
            .withConvention(() -> REIRuntime.getInstance().isDarkThemeEnabled() ? 1.0F : 0.0F, ValueAnimator.typicalTransitionTime())
            .asFloat();
    
    public static boolean isRendering(Panel panel) {
        return ConfigObject.getInstance().getRecipeScreenType() != DisplayScreenType.COMPOSITE;
    }
    
    public PanelWidget(Rectangle bounds) {
        this.bounds = Objects.requireNonNull(bounds);
    }
    
    @Override
    public int getXTextureOffset() {
        return xTextureOffset;
    }
    
    @Override
    public void setXTextureOffset(int xTextureOffset) {
        this.xTextureOffset = xTextureOffset;
    }
    
    @Override
    public int getYTextureOffset() {
        return yTextureOffset;
    }
    
    @Override
    public void setYTextureOffset(int yTextureOffset) {
        this.yTextureOffset = yTextureOffset;
    }
    
    @Override
    public int getColor() {
        return color;
    }
    
    @Override
    public void setColor(int color) {
        this.color = color;
    }
    
    @Override
    public Predicate<Panel> getRendering() {
        return rendering;
    }
    
    @Override
    public void setRendering(Predicate<Panel> rendering) {
        this.rendering = Objects.requireNonNull(rendering);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.darkBackgroundAlpha.update(delta);
        if (!getRendering().test(this))
            return;
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        renderBackground(matrices, x, y, width, height, false, alpha, red, green, blue);
        if (darkBackgroundAlpha.value() * alpha > 0.0F) {
            renderBackground(matrices, x, y, width, height, true, this.darkBackgroundAlpha.value() * alpha, red, green, blue);
        }
    }
    
    public void renderBackground(PoseStack matrices, int x, int y, int width, int height, boolean dark, float alpha, float red, float green, float blue) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShaderTexture(0, dark ? InternalTextures.CHEST_GUI_TEXTURE_DARK : InternalTextures.CHEST_GUI_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        int xTextureOffset = getXTextureOffset();
        int yTextureOffset = getYTextureOffset();
        
        // 9 Patch Texture
        
        // Four Corners
        this.blit(matrices, x, y, 106 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(matrices, x + width - 8, y, 248 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(matrices, x, y + height - 8, 106 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        this.blit(matrices, x + width - 8, y + height - 8, 248 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        
        Matrix4f matrix = matrices.last().pose();
        // Sides
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y, y + 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (124 + yTextureOffset) / 256f, (132 + yTextureOffset) / 256f);
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y + height - 8, y + height, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (182 + yTextureOffset) / 256f, (190 + yTextureOffset) / 256f);
        GuiComponent.innerBlit(matrix, x, x + 8, y + 8, y + height - 8, getZ(), (106 + xTextureOffset) / 256f, (114 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        GuiComponent.innerBlit(matrix, x + width - 8, x + width, y + 8, y + height - 8, getZ(), (248 + xTextureOffset) / 256f, (256 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        
        // Center
        GuiComponent.innerBlit(matrix, x + 8, x + width - 8, y + 8, y + height - 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
}
