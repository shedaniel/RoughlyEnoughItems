/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class RecipeBaseWidget extends WidgetWithBounds {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private Rectangle bounds;
    
    public RecipeBaseWidget(Rectangle bounds) {
        this.bounds = bounds;
    }
    
    public int getBlitOffset() {
        return this.blitOffset;
    }
    
    public void setBlitOffset(int offset) {
        this.blitOffset = offset;
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
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (!isRendering())
            return;
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GuiLighting.disable();
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int textureOffset = getTextureOffset();
        
        //Four Corners
        this.blit(x, y, 106, 124 + textureOffset, 4, 4);
        this.blit(x + width - 4, y, 252, 124 + textureOffset, 4, 4);
        this.blit(x, y + height - 4, 106, 186 + textureOffset, 4, 4);
        this.blit(x + width - 4, y + height - 4, 252, 186 + textureOffset, 4, 4);
        
        //Sides
        for(int xx = 4; xx < width - 4; xx += 128) {
            int thisWidth = Math.min(128, width - 4 - xx);
            this.blit(x + xx, y, 110, 124 + textureOffset, thisWidth, 4);
            this.blit(x + xx, y + height - 4, 110, 186 + textureOffset, thisWidth, 4);
        }
        for(int yy = 4; yy < height - 4; yy += 50) {
            int thisHeight = Math.min(50, height - 4 - yy);
            this.blit(x, y + yy, 106, 128 + textureOffset, 4, thisHeight);
            this.blit(x + width - 4, y + yy, 252, 128 + textureOffset, 4, thisHeight);
        }
        fillGradient(x + 4, y + 4, x + width - 4, y + height - 4, getInnerColor(), getInnerColor());
    }
    
    protected boolean isRendering() {
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().screenType != RecipeScreenType.VILLAGER;
    }
    
    protected int getInnerColor() {
        return ScreenHelper.isDarkModeEnabled() ? 0xFF2E2E2E : -3750202;
    }
    
    protected int getTextureOffset() {
        return RoughlyEnoughItemsCore.getConfigManager().getConfig().lightGrayRecipeBorder ? 0 : 66;
    }
    
    
}
