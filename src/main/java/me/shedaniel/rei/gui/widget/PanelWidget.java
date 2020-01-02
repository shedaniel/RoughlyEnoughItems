/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public class PanelWidget extends WidgetWithBounds {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private Rectangle bounds;
    private int color = -1;
    
    public PanelWidget(Rectangle bounds) {
        this.bounds = bounds;
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
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = ((color >> 0) & 0xFF) / 255f;
        float alpha = ((color >> 32) & 0xFF) / 255f;
        RenderSystem.color4f(red, green, blue, alpha);
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
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
        return ScreenHelper.isDarkModeEnabled() ? -13750738 : -3750202;
    }
    
    protected int getXTextureOffset() {
        return 0;
    }
    
    protected int getYTextureOffset() {
        return ConfigObject.getInstance().isUsingLightGrayRecipeBorder() ? 0 : 66;
    }
    
}
