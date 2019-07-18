/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class TabWidget extends WidgetWithBounds {
    
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final ResourceLocation CHEST_GUI_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
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
    public List<Widget> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (shown) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.disableStandardItemLighting();
            minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
            this.drawTexturedModalRect(bounds.x, bounds.y + 2, selected ? 28 : 0, 192, 28, (selected ? 30 : 27));
            renderer.setBlitOffset(100);
            renderer.render((int) bounds.getCenterX(), (int) bounds.getCenterY(), mouseX, mouseY, delta);
            if (containsMouse(mouseX, mouseY)) {
                drawTooltip();
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private void drawTooltip() {
        if (this.minecraft.gameSettings.advancedItemTooltips)
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ChatFormatting.DARK_GRAY.toString() + category.getIdentifier().toString(), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
        else
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
