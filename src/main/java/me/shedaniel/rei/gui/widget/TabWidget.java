/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class TabWidget extends HighlightableWidget {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    public boolean shown = false, selected = false;
    public Renderer renderer;
    public int id;
    public String categoryName;
    public Rectangle bounds;
    public RecipeCategory category;
    
    public TabWidget(int id, Rectangle bounds) {
        this.id = id;
        this.bounds = bounds;
    }
    
    public void setRenderer(RecipeCategory category, Renderer renderable, String categoryName, boolean selected) {
        if (renderable == null) {
            shown = false;
            this.renderer = null;
        } else {
            shown = true;
            this.renderer = renderable;
        }
        this.category = category;
        this.selected = selected;
        this.categoryName = categoryName;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isShown() {
        return shown;
    }
    
    public Renderer getRenderer() {
        return renderer;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (shown) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiLighting.disable();
            minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
            this.blit(bounds.x, bounds.y + 2, selected ? 28 : 0, 192, 28, (selected ? 30 : 27));
            renderer.setBlitOffset(100);
            renderer.render((int) bounds.getCenterX(), (int) bounds.getCenterY(), mouseX, mouseY, delta);
            if (isHighlighted(mouseX, mouseY))
                drawTooltip();
        }
    }
    
    private void drawTooltip() {
        if (this.minecraft.options.advancedItemTooltips)
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, "§8" + category.getIdentifier().toString(), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
        else
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
