package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.audio.PositionedSoundInstance;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.TextComponent;

import java.awt.*;

public abstract class ConfigEntry extends EntryListWidget.Entry<ConfigEntry> {
    
    public static class ButtonConfigEntry extends ConfigEntry {
        private TextComponent nameComponent;
        private ConfigEntryButtonProvider buttonProvider;
        private ButtonWidget buttonWidget;
        
        public ButtonConfigEntry(TextComponent nameComponent, ConfigEntryButtonProvider buttonProvider) {
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
            if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - MinecraftClient.getInstance().textRenderer.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
                this.buttonWidget.text = buttonProvider.getText();
                this.buttonWidget.getBounds().setLocation(getX(), getY() + 2);
            } else {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(nameComponent.getFormattedText(), getX(), getY() + 5, 16777215);
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
    
    public static class CategoryTitleConfigEntry extends ConfigEntry {
        private TextComponent textComponent;
        
        public CategoryTitleConfigEntry(TextComponent nameComponent) {
            this.textComponent = nameComponent.setStyle(new Style().setBold(true));
        }
        
        @Override
        public void draw(int i, int i1, int i2, int i3, boolean b, float v) {
            Window window = MinecraftClient.getInstance().window;
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            textRenderer.draw(textComponent.getFormattedText(), (window.getScaledWidth() - textRenderer.getStringWidth(textComponent.getFormattedText())) / 2, getY() + 10, -1);
        }
    }
    
    public static class TextFieldConfigEntry extends ConfigEntry {
        private TextComponent nameComponent;
        private ConfigEntryTextFieldProvider textFieldProvider;
        private TextFieldWidget textFieldWidget;
        
        public TextFieldConfigEntry(TextComponent nameComponent, ConfigEntryTextFieldProvider textFieldProvider) {
            this.nameComponent = nameComponent;
            this.textFieldProvider = textFieldProvider;
            this.textFieldWidget = new TextFieldWidget(0, 0, 148, 18);
            this.textFieldWidget.setChangedListener(s -> textFieldProvider.onUpdateText(textFieldWidget, s));
            this.textFieldProvider.onInitWidget(textFieldWidget);
        }
        
        @Override
        public void draw(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
            Window window = MinecraftClient.getInstance().window;
            Point mouse = ClientHelper.getMouseLocation();
            if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - MinecraftClient.getInstance().textRenderer.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
                this.textFieldWidget.getBounds().setLocation(getX() + 1, getY() + 2);
            } else {
                MinecraftClient.getInstance().textRenderer.drawWithShadow(nameComponent.getFormattedText(), getX(), getY() + 5, 16777215);
                this.textFieldWidget.getBounds().setLocation(window.getScaledWidth() - 190 + 1, getY() + 2);
            }
            textFieldProvider.draw(textFieldWidget, mouse, delta);
        }
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            if (textFieldWidget.mouseClicked(double_1, double_2, int_1))
                return true;
            return false;
        }
        
        @Override
        public boolean charTyped(char char_1, int int_1) {
            if (textFieldWidget.charTyped(char_1, int_1))
                return true;
            return false;
        }
        
        @Override
        public boolean keyPressed(int int_1, int int_2, int int_3) {
            if (textFieldWidget.keyPressed(int_1, int_2, int_3))
                return true;
            return false;
        }
        
        interface ConfigEntryTextFieldProvider {
            
            public void onInitWidget(TextFieldWidget widget);
            
            public void onUpdateText(TextFieldWidget widget, String text);
            
            default public void draw(TextFieldWidget widget, Point mouse, float delta) {
                widget.draw(mouse.x, mouse.y, delta);
            }
            
        }
    }
    
}
