package me.shedaniel.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.Core;
import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;

public class CraftableToggleButton extends Control {
    
    private ItemRenderer itemRenderer;
    private final ItemStack itemStack;
    //protected float zLevel;
    protected static final Identifier BUTTON_TEXTURES = new Identifier("textures/gui/widgets.png");
    protected static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    public CraftableToggleButton(Rectangle rect) {
        super(rect);
        this.itemStack = new ItemStack(Blocks.CRAFTING_TABLE.getItem());
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }
    
    @Override
    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        MinecraftClient lvt_4_1_ = MinecraftClient.getInstance();
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
        GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcBlendFactor.ONE, GlStateManager.DstBlendFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexturedRect(rect.x, rect.y, 0, 46 + hoverState * 20, rect.width / 2, rect.height, 0);
        this.drawTexturedRect(rect.x + rect.width / 2, rect.y, 200 - rect.width / 2, 46 + hoverState * 20, rect.width / 2, rect.height, 0);
        
        GuiLighting.enableForItems();
        //this.zLevel = 100.0F;
        //this.itemRenderer.zOffset = 100.0F;
        this.itemRenderer.zOffset = 0.0F;
        this.itemRenderer.renderItemAndGlowInGui(itemStack, rect.x + 2, rect.y + 2);
        GlStateManager.disableLighting();
        //this.zLevel = 0.0F;
        this.itemRenderer.zOffset = 0.0F;
        GuiLighting.disable();
        lvt_4_1_.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedRect(rect.x, rect.y, (72 + (Core.runtimeConfig.craftableOnly ? 0 : 20)), 222, 20, 20, 100);
        GlStateManager.popMatrix();
        if (isHighlighted())
            drawTooltip();
    }
    
    private void drawTooltip() {
        Point mouse = REIRenderHelper.getMouseLoc();
        REIRenderHelper.addToolTip(Arrays.asList(I18n.translate(Core.runtimeConfig.craftableOnly ? "text.rei.showing_craftable" : "text.rei.showing_all")), mouse.x, mouse.y);
    }
    
}