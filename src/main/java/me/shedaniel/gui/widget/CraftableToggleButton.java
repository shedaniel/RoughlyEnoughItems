package me.shedaniel.gui.widget;

import me.shedaniel.Core;
import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;

public class CraftableToggleButton extends Control {
    
    private ItemRenderer itemRenderer;
    private final ItemStack itemStack;
    //protected float zLevel;
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");
    protected static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    public CraftableToggleButton(Rectangle rect) {
        super(rect);
        this.itemStack = new ItemStack(Blocks.CRAFTING_TABLE.asItem());
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    
    @Override
    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        Minecraft lvt_4_1_ = Minecraft.getInstance();
        FontRenderer lvt_5_1_ = lvt_4_1_.fontRenderer;
        lvt_4_1_.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int hoverState = (byte) 0;
        if (this.isEnabled()) {
            if (!this.isHighlighted())
                hoverState = (byte) 1;
            else
                hoverState = (byte) 2;
        }
        
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexturedModalRect(rect.x, rect.y, 0, 46 + hoverState * 20, rect.width / 2, rect.height, 0);
        this.drawTexturedModalRect(rect.x + rect.width / 2, rect.y, 200 - rect.width / 2, 46 + hoverState * 20, rect.width / 2, rect.height, 0);
        
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRenderer.zLevel = 0.0F;
        this.itemRenderer.renderItemAndEffectIntoGUI(itemStack, rect.x + 2, rect.y + 2);
        GlStateManager.disableLighting();
        this.itemRenderer.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        lvt_4_1_.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(rect.x, rect.y, (72 + (Core.runtimeConfig.craftableOnly ? 0 : 20)), 222, 20, 20, 100);
        GlStateManager.popMatrix();
        if (isHighlighted())
            drawTooltip();
    }
    
    private void drawTooltip() {
        Point mouse = REIRenderHelper.getMouseLoc();
        REIRenderHelper.addToolTip(Arrays.asList(I18n.format(Core.runtimeConfig.craftableOnly ? "text.rei.showing_craftable" : "text.rei.showing_all")), mouse.x, mouse.y);
    }
    
}