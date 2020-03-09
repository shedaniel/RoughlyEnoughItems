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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Panel;
import me.shedaniel.rei.gui.config.RecipeBorderType;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.minecraft.client.gui.Element;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class PanelWidget extends Panel {
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private static final PanelWidget TEMP = new PanelWidget(new Rectangle());
    private Rectangle bounds;
    private int color = -1;
    private int innerColor = REIHelper.getInstance().isDarkThemeEnabled() ? -13750738 : -3750202;
    private int xTextureOffset = 0;
    private int yTextureOffset = RecipeBorderType.DEFAULT.getYOffset();
    @NotNull
    private Predicate<Panel> rendering = PanelWidget::isRendering;
    
    public static boolean isRendering(Panel panel) {
        return ConfigObject.getInstance().getRecipeScreenType() != RecipeScreenType.VILLAGER;
    }
    
    public PanelWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    public static void render(Rectangle bounds, int color) {
        TEMP.bounds = bounds;
        TEMP.color = color;
        TEMP.render(0, 0, 0);
    }
    
    @Override
    public int getInnerColor() {
        return innerColor;
    }
    
    @Override
    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
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
    public @NotNull Predicate<Panel> getRendering() {
        return rendering;
    }
    
    @Override
    public void setRendering(@NotNull Predicate<Panel> rendering) {
        this.rendering = Objects.requireNonNull(rendering);
    }
    
    @Override
    public me.shedaniel.math.api.Rectangle getBounds() {
        return new me.shedaniel.math.api.Rectangle(bounds);
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (!getRendering().test(this))
            return;
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        RenderSystem.color4f(red, green, blue, alpha);
        minecraft.getTextureManager().bindTexture(REIHelper.getInstance().isDarkThemeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int xTextureOffset = getXTextureOffset();
        int yTextureOffset = getYTextureOffset();
        
        //Four Corners
        this.blit(x, y, 106 + xTextureOffset, 124 + yTextureOffset, 4, 4);
        this.blit(x + width - 4, y, 252 + xTextureOffset, 124 + yTextureOffset, 4, 4);
        this.blit(x, y + height - 4, 106 + xTextureOffset, 186 + yTextureOffset, 4, 4);
        this.blit(x + width - 4, y + height - 4, 252 + xTextureOffset, 186 + yTextureOffset, 4, 4);
        
        //Sides
        for (int xx = 4; xx < width - 4; xx += 128) {
            int thisWidth = Math.min(128, width - 4 - xx);
            this.blit(x + xx, y, 110 + xTextureOffset, 124 + yTextureOffset, thisWidth, 4);
            this.blit(x + xx, y + height - 4, 110 + xTextureOffset, 186 + yTextureOffset, thisWidth, 4);
        }
        for (int yy = 4; yy < height - 4; yy += 50) {
            int thisHeight = Math.min(50, height - 4 - yy);
            this.blit(x, y + yy, 106 + xTextureOffset, 128 + yTextureOffset, 4, thisHeight);
            this.blit(x + width - 4, y + yy, 252 + xTextureOffset, 128 + yTextureOffset, 4, thisHeight);
        }
        fillGradient(x + 4, y + 4, x + width - 4, y + height - 4, getInnerColor(), getInnerColor());
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
}
