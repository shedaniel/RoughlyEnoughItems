package me.shedaniel.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.gui.REIRenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.util.Identifier;

import java.awt.*;

/**
 * Created by James on 7/29/2018.
 */
public class Button extends Control {
    
    private String buttonText;
    protected static final Identifier BUTTON_TEXTURES = new Identifier("textures/gui/widgets.png");
    
    public Button(int x, int y, int width, int height, String buttonText) {
        super(x, y, width, height);
        this.buttonText = buttonText;
    }
    
    public Button(Rectangle rect, String buttonText) {
        super(rect);
        this.buttonText = buttonText;
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
        gui.drawTexturedRect(rect.x, rect.y, 0, 46 + hoverState * 20, rect.width / 2, rect.height);
        gui.drawTexturedRect(rect.x + rect.width / 2, rect.y, 200 - rect.width / 2, 46 + hoverState * 20, rect.width / 2, rect.height);
        int lvt_7_1_ = 14737632;
        
        gui.drawStringCentered(lvt_5_1_, this.buttonText, rect.x + rect.width / 2, rect.y + (rect.height - 8) / 2, lvt_7_1_);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
    
}
