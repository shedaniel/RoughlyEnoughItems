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

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.gui.config.RecipeBorderType;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

public class PanelWidget extends WidgetWithBounds {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private static final PanelWidget TEMP = new PanelWidget(new Rectangle());
    private Rectangle bounds;
    private int color = -1;
    
    public PanelWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    public static void render(Rectangle bounds, int color) {
        TEMP.bounds = bounds;
        TEMP.color = color;
        TEMP.render();
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    public void render() {
        render(0, 0, 0);
    }
    
    public int getColor() {
        return color;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (!isRendering())
            return;
        float alpha = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;
        RenderSystem.color4f(red, green, blue, alpha);
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int xTextureOffset = getXTextureOffset();
        int yTextureOffset = getYTextureOffset();
        
        // 9 Patch Texture
        
        // Four Corners
        this.blit(x, y, 106 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(x + width - 8, y, 248 + xTextureOffset, 124 + yTextureOffset, 8, 8);
        this.blit(x, y + height - 8, 106 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        this.blit(x + width - 8, y + height - 8, 248 + xTextureOffset, 182 + yTextureOffset, 8, 8);
        
        // Sides
        DrawableHelper.innerBlit(x + 8, x + width - 8, y, y + 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (124 + yTextureOffset) / 256f, (132 + yTextureOffset) / 256f);
        DrawableHelper.innerBlit(x + 8, x + width - 8, y + height - 8, y + height, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (182 + yTextureOffset) / 256f, (190 + yTextureOffset) / 256f);
        DrawableHelper.innerBlit(x, x + 8, y + 8, y + height - 8, getZ(), (106 + xTextureOffset) / 256f, (114 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        DrawableHelper.innerBlit(x + width - 8, x + width, y + 8, y + height - 8, getZ(), (248 + xTextureOffset) / 256f, (256 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
        
        // Center
        DrawableHelper.innerBlit(x + 8, x + width - 8, y + 8, y + height - 8, getZ(), (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);
    }
    
    protected boolean isRendering() {
        return ConfigObject.getInstance().getRecipeScreenType() != RecipeScreenType.VILLAGER;
    }
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    protected int getInnerColor() {
        return ScreenHelper.isDarkModeEnabled() ? -13750738 : -3750202;
    }
    
    protected int getXTextureOffset() {
        return 0;
    }
    
    protected int getYTextureOffset() {
        return RecipeBorderType.DEFAULT.getYOffset();
    }
    
}
