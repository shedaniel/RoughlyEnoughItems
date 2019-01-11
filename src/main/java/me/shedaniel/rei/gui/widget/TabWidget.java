package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.client.ClientHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TabWidget extends Drawable implements HighlightableWidget {
    
    private boolean shown = false, selected = false;
    private ItemStack item;
    private int id;
    private RecipeViewingWidget recipeViewingWidget;
    private String categoryName;
    private Rectangle bounds;
    private ItemRenderer itemRenderer;
    
    public TabWidget(int id, RecipeViewingWidget recipeViewingWidget, Rectangle bounds) {
        this.id = id;
        this.recipeViewingWidget = recipeViewingWidget;
        this.bounds = bounds;
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
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
    public List<IWidget> getListeners() {
        return Lists.newArrayList();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (shown) {
            int l = (int) this.bounds.getCenterX() - 8, i1 = (int) this.bounds.getCenterY() - 6;
            
            GuiLighting.disable();
            this.drawTexturedRect(bounds.x, bounds.y, selected ? 28 : 0, 158, 28, (selected ? 31 : 28));
            this.zOffset = 100.0F;
            this.itemRenderer.zOffset = 100.0F;
            GuiLighting.enableForItems();
            this.itemRenderer.renderItemAndGlowInGui(getItemStack(), l, i1);
            this.itemRenderer.renderItemOverlaysInGUI(MinecraftClient.getInstance().fontRenderer, getItemStack(), l, i1);
            GlStateManager.disableLighting();
            this.itemRenderer.zOffset = 0.0F;
            this.zOffset = 0.0F;
            if (isHighlighted(mouseX, mouseY))
                drawTooltip();
        }
    }
    
    private void drawTooltip() {
        recipeViewingWidget.getOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(categoryName)));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
