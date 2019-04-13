package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class TabWidget extends HighlightableWidget {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    public boolean shown = false, selected = false;
    public ItemStack item;
    public int id;
    public RecipeViewingScreen recipeViewingWidget;
    public String categoryName;
    public Rectangle bounds;
    private ItemRenderer itemRenderer;
    
    public TabWidget(int id, RecipeViewingScreen recipeViewingWidget, Rectangle bounds) {
        this.id = id;
        this.recipeViewingWidget = recipeViewingWidget;
        this.bounds = bounds;
        this.itemRenderer = minecraft.getItemRenderer();
    }
    
    public void setItem(ItemStack item, String categoryName, boolean selected) {
        if (item == null) {
            shown = false;
            this.item = null;
        } else {
            shown = true;
            this.item = item;
        }
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
    
    public ItemStack getItemStack() {
        return item;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (shown) {
            int l = (int) this.bounds.getCenterX() - 8, i1 = (int) this.bounds.getCenterY() - 6;
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiLighting.disable();
            minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
            this.blit(bounds.x, bounds.y + 2, selected ? 28 : 0, 192, 28, (selected ? 30 : 27));
            this.blitOffset = 100;
            this.itemRenderer.zOffset = 100.0F;
            GuiLighting.enableForItems();
            this.itemRenderer.renderGuiItem(getItemStack(), l, i1);
            this.itemRenderer.renderGuiItemOverlay(minecraft.textRenderer, getItemStack(), l, i1);
            GlStateManager.disableLighting();
            this.itemRenderer.zOffset = 0.0F;
            this.blitOffset = 0;
            if (isHighlighted(mouseX, mouseY))
                drawTooltip();
        }
    }
    
    private void drawTooltip() {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
