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

package me.shedaniel.rei.impl.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Panel;
import me.shedaniel.rei.gui.config.RecipeBorderType;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class PanelWidget extends Panel {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final ResourceLocation CHEST_GUI_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private static final PanelWidget TEMP = new PanelWidget(new Rectangle());
    private Rectangle bounds;
    private int color = -1;
    private int xTextureOffset = 0;
    private int yTextureOffset = RecipeBorderType.DEFAULT.getYOffset();
    @NotNull
    private Predicate<Panel> rendering = PanelWidget::isRendering;
    
    public static boolean isRendering(Panel panel) {
        return ConfigObject.getInstance().getRecipeScreenType() != RecipeScreenType.VILLAGER;
    }
    
    public PanelWidget(@NotNull Rectangle bounds) {
        this.bounds = Objects.requireNonNull(bounds);
    }
    
    public static void render(MatrixStack matrices, @NotNull Rectangle bounds, int color) {
        TEMP.bounds.setBounds(Objects.requireNonNull(bounds));
        TEMP.color = color;
        TEMP.render(matrices, 0, 0, 0);
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Override
    public int getInnerColor() {
        return REIHelper.getInstance().isDarkThemeEnabled() ? -13750738 : -3750202;
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    @Override
    public void setInnerColor(int innerColor) {}
    
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
    public @NotNull Predicate<Panel> getRendering() {
        return rendering;
    }
    
    @Override
    public void setRendering(@NotNull Predicate<Panel> rendering) {
        this.rendering = Objects.requireNonNull(rendering);
    }
    
    @NotNull
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!getRendering().test(this))
            return;
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        RenderSystem.color4f(red, green, blue, alpha);
        minecraft.getTextureManager().bind(REIHelper.getInstance().isDarkThemeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
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
        AbstractGui.innerBlit(matrix, x + 8, x + width - 8, y, y + 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (124 + yTextureOffset) / 256f, (132 + yTextureOffset) / 256f);
        AbstractGui.innerBlit(matrix, x + 8, x + width - 8, y + height - 8, y + height, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (182 + yTextureOffset) / 256f, (190 + yTextureOffset) / 256f);
        AbstractGui.innerBlit(matrix, x, x + 8, y + 8, y + height - 8, getZ(), (106 + xTextureOffset) / 256f, (114 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        AbstractGui.innerBlit(matrix, x + width - 8, x + width, y + 8, y + height - 8, getZ(), (248 + xTextureOffset) / 256f, (256 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        
        // Center
        AbstractGui.innerBlit(matrix, x + 8, x + width - 8, y + 8, y + height - 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.emptyList();
    }
}
