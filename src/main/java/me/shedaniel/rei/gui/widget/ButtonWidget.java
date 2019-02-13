package me.shedaniel.rei.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class ButtonWidget extends Drawable implements IWidget {
    
    protected static final Identifier WIDGET_TEX = new Identifier("textures/gui/widgets.png");
    public String text;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;
    private boolean pressed;
    private Rectangle bounds;
    
    public ButtonWidget(Rectangle rectangle, String text) {
        this.bounds = rectangle;
        this.enabled = true;
        this.visible = true;
        this.text = text;
    }
    
    public ButtonWidget(int x, int y, int width, int height, String text) {
        this(new Rectangle(x, y, width, height), text);
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    protected int getTextureId(boolean boolean_1) {
        int int_1 = 1;
        if (!this.enabled) {
            int_1 = 0;
        } else if (boolean_1) {
            int_1 = 2;
        }
        
        return int_1;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return new ArrayList<>();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
            MinecraftClient minecraftClient_1 = MinecraftClient.getInstance();
            FontRenderer fontRenderer_1 = minecraftClient_1.fontRenderer;
            minecraftClient_1.getTextureManager().bindTexture(WIDGET_TEX);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = bounds.contains(mouseX, mouseY);
            int textureOffset = this.getTextureId(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            //Four Corners
            this.drawTexturedRect(x, y, 0, 46 + textureOffset * 20, 4, 4);
            this.drawTexturedRect(x + width - 4, y, 196, 46 + textureOffset * 20, 4, 4);
            this.drawTexturedRect(x, y + height - 4, 0, 62 + textureOffset * 20, 4, 4);
            this.drawTexturedRect(x + width - 4, y + height - 4, 196, 62 + textureOffset * 20, 4, 4);
            
            //Sides
            this.drawTexturedRect(x + 4, y, 4, 46 + textureOffset * 20, width - 8, 4);
            this.drawTexturedRect(x + 4, y + height - 4, 4, 62 + textureOffset * 20, width - 8, 4);
            
            for(int i = y + 4; i < y + height - 4; i += 4) {
                this.drawTexturedRect(x, i, 0, 50 + textureOffset * 20, width / 2, MathHelper.clamp(y + height - 4 - i, 0, 4));
                this.drawTexturedRect(x + width / 2, i, 200 - width / 2, 50 + textureOffset * 20, width / 2, MathHelper.clamp(y + height - 4 - i, 0, 4));
            }
            
            int colour = 14737632;
            if (!this.enabled) {
                colour = 10526880;
            } else if (this.hovered) {
                colour = 16777120;
            }
            
            this.drawStringCentered(fontRenderer_1, this.text, x + width / 2, y + (height - 8) / 2, colour);
        }
    }
    
    @Override
    public boolean onMouseClick(int button, double mouseX, double mouseY) {
        if (bounds.contains(mouseX, mouseY) && enabled) {
            MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onPressed(button, mouseX, mouseY);
            return true;
        }
        return false;
    }
    
    public abstract void onPressed(int button, double mouseX, double mouseY);
    
}
