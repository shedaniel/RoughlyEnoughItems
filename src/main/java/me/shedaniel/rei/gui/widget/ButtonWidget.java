/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class ButtonWidget extends WidgetWithBounds {
    
    public static final ResourceLocation BUTTON_LOCATION = new ResourceLocation("roughlyenoughitems", "textures/gui/button.png");
    public static final ResourceLocation BUTTON_LOCATION_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/button_dark.png");
    public String text;
    public boolean enabled;
    private Rectangle bounds;
    
    public ButtonWidget(Rectangle rectangle, ITextComponent text) {
        this(rectangle, text.getFormattedText());
    }
    
    public ButtonWidget(Rectangle rectangle, String text) {
        this.bounds = rectangle;
        this.enabled = true;
        this.text = text;
    }
    
    public ButtonWidget(int x, int y, int width, int height, String text) {
        this(new Rectangle(x, y, width, height), text);
    }
    
    public ButtonWidget(int x, int y, int width, int height, ITextComponent text) {
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
    public void render(int mouseX, int mouseY, float delta) {
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        minecraft.getTextureManager().bindTexture(ScreenHelper.isDarkModeEnabled() ? BUTTON_LOCATION_DARK : BUTTON_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int textureOffset = this.getTextureId(isHovered(mouseX, mouseY));
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //Four Corners
        drawTexturedModalRect(x, y, 0, textureOffset * 80, 4, 4);
        drawTexturedModalRect(x + width - 4, y, 252, textureOffset * 80, 4, 4);
        drawTexturedModalRect(x, y + height - 4, 0, textureOffset * 80 + 76, 4, 4);
        drawTexturedModalRect(x + width - 4, y + height - 4, 252, textureOffset * 80 + 76, 4, 4);
        
        //Sides
        drawTexturedModalRect(x + 4, y, 4, textureOffset * 80, MathHelper.ceil((width - 8) / 2f), 4);
        drawTexturedModalRect(x + 4, y + height - 4, 4, textureOffset * 80 + 76, MathHelper.ceil((width - 8) / 2f), 4);
        drawTexturedModalRect(x + 4 + MathHelper.ceil((width - 8) / 2f), y + height - 4, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80 + 76, MathHelper.floor((width - 8) / 2f), 4);
        drawTexturedModalRect(x + 4 + MathHelper.ceil((width - 8) / 2f), y, 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80, MathHelper.floor((width - 8) / 2f), 4);
        for(int i = y + 4; i < y + height - 4; i += 76) {
            drawTexturedModalRect(x, i, 0, 4 + textureOffset * 80, MathHelper.ceil(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76));
            drawTexturedModalRect(x + MathHelper.ceil(width / 2f), i, 256 - MathHelper.floor(width / 2f), 4 + textureOffset * 80, MathHelper.floor(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76));
        }
        
        int colour = 14737632;
        if (!this.enabled) {
            colour = 10526880;
        } else if (isHovered(mouseX, mouseY)) {
            colour = 16777120;
        }
        
        this.drawCenteredString(font, text, x + width / 2, y + (height - 8) / 2, colour);
        
        if (getTooltips().isPresent())
            if (containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
    }
    
    public boolean isHovered(int mouseX, int mouseY) {
        return containsMouse(mouseX, mouseY);
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (bounds.contains(mouseX, mouseY) && enabled && button == 0) {
            minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onPressed();
            return true;
        }
        return false;
    }
    
    public abstract void onPressed();
    
    public Optional<String> getTooltips() {
        return Optional.empty();
    }
    
}
