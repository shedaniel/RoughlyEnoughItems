package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TabWidget extends Gui implements HighlightableWidget {
    
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
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
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
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
            
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.disableStandardItemLighting();
            Minecraft.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
            this.drawTexturedModalRect(bounds.x, bounds.y + 2, selected ? 28 : 0, 192, 28, (selected ? 30 : 27));
            this.zLevel = 100.0F;
            this.itemRenderer.zLevel = 100.0F;
            RenderHelper.enableStandardItemLighting();
            this.itemRenderer.renderItemAndEffectIntoGUI(getItemStack(), l, i1);
            this.itemRenderer.renderItemOverlays(Minecraft.getInstance().fontRenderer, getItemStack(), l, i1);
            GlStateManager.disableLighting();
            this.itemRenderer.zLevel = 0.0F;
            this.zLevel = 0.0F;
            if (isHighlighted(mouseX, mouseY))
                drawTooltip();
        }
    }
    
    private void drawTooltip() {
        GuiHelper.getOverlay(recipeViewingWidget.getParent().getContainerGui()).addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(categoryName)));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
