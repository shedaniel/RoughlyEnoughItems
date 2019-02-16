package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TextComponent;

import java.awt.*;

public class ConfigEntry extends EntryListWidget.Entry<ConfigEntry> {
    
    private static final FontRenderer FONT_RENDERER = MinecraftClient.getInstance().fontRenderer;
    private TextComponent nameComponent;
    private ConfigEntryButtonProvider buttonProvider;
    private ButtonWidget buttonWidget;
    
    public ConfigEntry(TextComponent nameComponent, ConfigEntryButtonProvider buttonProvider) {
        this.nameComponent = nameComponent;
        this.buttonProvider = buttonProvider;
        this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "") {
            @Override
            public boolean onMouseClick(int button, double mouseX, double mouseY) {
                if (getBounds().contains(mouseX, mouseY) && enabled)
                    if (buttonProvider.onPressed(button, mouseX, mouseY)) {
                        MinecraftClient.getInstance().getSoundLoader().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        return true;
                    }
                return false;
            }
            
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {}
        };
    }
    
    @Override
    public void draw(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
        Window window = MinecraftClient.getInstance().window;
        Point mouse = ClientHelper.getMouseLocation();
        if (MinecraftClient.getInstance().fontRenderer.isRightToLeft()) {
            MinecraftClient.getInstance().fontRenderer.drawWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - FONT_RENDERER.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
            this.buttonWidget.text = buttonProvider.getText();
            this.buttonWidget.getBounds().setLocation(10, getY() + 2);
        } else {
            MinecraftClient.getInstance().fontRenderer.drawWithShadow(nameComponent.getFormattedText(), 10.0F, getY() + 5, 16777215);
            this.buttonWidget.text = buttonProvider.getText();
            this.buttonWidget.getBounds().setLocation(window.getScaledWidth() - 190, getY() + 2);
        }
        buttonProvider.draw(buttonWidget, mouse, delta);
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (buttonWidget.mouseClicked(double_1, double_2, int_1))
            return true;
        return false;
    }
    
    interface ConfigEntryButtonProvider {
        
        public boolean onPressed(int button, double mouseX, double mouseY);
        
        public String getText();
        
        default public void draw(ButtonWidget button, Point mouse, float delta) {
            button.draw(mouse.x, mouse.y, delta);
        }
        
    }
    
}
