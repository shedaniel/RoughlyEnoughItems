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
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.gui.config.RecipeBorderType;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

/**
 * @see me.shedaniel.rei.api.widgets.Widgets#createRecipeBase(me.shedaniel.math.Rectangle)
 * @see me.shedaniel.rei.api.widgets.Widgets#createRecipeBase(me.shedaniel.math.Rectangle, int)
 * @see me.shedaniel.rei.api.widgets.Widgets#createSlotBase(me.shedaniel.math.Rectangle)
 * @see me.shedaniel.rei.api.widgets.Widgets#createSlotBase(me.shedaniel.math.Rectangle, int)
 */
@ApiStatus.ScheduledForRemoval
@Deprecated
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
    
    protected boolean isRendering() {
        return ConfigObject.getInstance().getRecipeScreenType() != RecipeScreenType.VILLAGER;
    }
    
    protected int getInnerColor() {
        return REIHelper.getInstance().isDarkThemeEnabled() ? -13750738 : -3750202;
    }
    
    protected int getXTextureOffset() {
        return 0;
    }
    
    protected int getYTextureOffset() {
        return RecipeBorderType.DEFAULT.getYOffset();
    }
    
}
