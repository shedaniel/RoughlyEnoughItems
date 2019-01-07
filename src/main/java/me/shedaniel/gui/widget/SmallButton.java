package me.shedaniel.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class SmallButton extends Control {
    
    private String buttonText;
    private Function<Boolean, String> toolTipSupplier;
    protected static final Identifier BUTTON_TEXTURES = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    
    public SmallButton(int x, int y, int width, int height, String buttonText, Function<Boolean, String> toolTipSupplier) {
        super(x, y, width, height);
        this.buttonText = buttonText;
        this.toolTipSupplier = toolTipSupplier;
    }
    
    public SmallButton(Rectangle rect, String buttonText, Function<Boolean, String> toolTipSupplier) {
        super(rect);
        this.buttonText = buttonText;
        this.toolTipSupplier = toolTipSupplier;
    }
    
    public void setString(String text) {
        buttonText = text;
    }
    
    @Override
    public void draw() {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        ContainerGui gui = REIRenderHelper.getOverlayedGui();
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
        gui.drawTexturedRect(rect.x, rect.y, 18 + 44, 222 + hoverState * 10, rect.width, rect.height);
        int lvt_7_1_ = 14737632;
        
        gui.drawStringCentered(lvt_5_1_, this.buttonText, rect.x + rect.width / 2, rect.y + (rect.height - 8) / 2, lvt_7_1_);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        if (isHighlighted()) {
            List<String> toolTip = Arrays.asList(toolTipSupplier.apply(isEnabled()).split("\n"));
            if (toolTip != null && toolTip.size() != 0)
                gui.drawTooltip(toolTip, REIRenderHelper.getMouseLoc().x, REIRenderHelper.getMouseLoc().y);
        }
    }
    
}
