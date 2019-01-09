package me.shedaniel.gui.widget;

import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class SmallButton extends Control {
    
    private String buttonText;
    private Function<Boolean, String> toolTipSupplier;
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    
    
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
        GuiContainer gui = REIRenderHelper.getOverlayedGui();
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
        gui.drawTexturedModalRect(rect.x, rect.y, 18 + 44, 222 + hoverState * 10, rect.width, rect.height);
        int lvt_7_1_ = 14737632;
        
        gui.drawCenteredString(lvt_5_1_, this.buttonText, rect.x + rect.width / 2, rect.y + (rect.height - 8) / 2, lvt_7_1_);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        String ttS = toolTipSupplier.apply(isEnabled());
        if (isHighlighted() && ttS != "") {
            List<String> toolTip = Arrays.asList(ttS.split("\n"));
            if (toolTip != null && toolTip.size() != 0)
                gui.drawHoveringText(toolTip, REIRenderHelper.getMouseLoc().x, REIRenderHelper.getMouseLoc().y);
        }
    }
    
}
