package me.shedaniel.gui.widget;

import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Tab extends Control {
    
    private boolean shown = false, selected = false;
    private ItemStack item;
    private int id, guiLeft;
    private String categoryName;
    
    public Tab(int id, int guiLeft, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.id = id;
        this.guiLeft = guiLeft;
        itemRender = Minecraft.getInstance().getItemRenderer();
    }
    
    public void moveTo(int guiLeft, int x, int y) {
        this.rect = new Rectangle(x, y, rect.width, rect.height);
        this.guiLeft = guiLeft;
    }
    
    public int getId() {
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Tab) {
            Tab anotherTab = (Tab) o;
            return anotherTab.id == this.id;
        }
        return false;
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
    
    public boolean isShown() {
        return shown;
    }
    
    public ItemStack getItemStack() {
        return item;
    }
    
    protected float zLevel;
    protected ItemRenderer itemRender;
    
    @Override
    public void draw() {
    
    }
    
    public void drawTab() {
        if (shown) {
            int l = this.guiLeft + 176 - 28 * (6 - id) - 4;
            int i1 = this.rect.y + 8;
            
            GlStateManager.disableLighting();
            this.drawTexturedModalRect(rect.x, rect.y - (selected ? 0 : 2), 28, (selected ? 32 : 0), 28, (selected ? 32 : 31));
            this.zLevel = 100.0F;
            this.itemRender.zLevel = 100.0F;
            RenderHelper.enableGUIStandardItemLighting();
            this.itemRender.renderItemAndEffectIntoGUI(getItemStack(), l, i1);
            this.itemRender.renderItemOverlays(Minecraft.getInstance().fontRenderer, getItemStack(), l, i1);
            GlStateManager.disableLighting();
            this.itemRender.zLevel = 0.0F;
            this.zLevel = 0.0F;
            if (isHighlighted())
                drawTooltip();
        }
    }
    
    private void drawTooltip() {
        List<String> toolTip = new ArrayList<>();
        toolTip.add(categoryName);
        Point mouse = REIRenderHelper.getMouseLoc();
        REIRenderHelper.addToolTip(toolTip, mouse.x, mouse.y);
    }
    
}
