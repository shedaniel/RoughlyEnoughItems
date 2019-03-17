package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;

public abstract class ConfigEntry extends GuiListExtended.IGuiListEntry<ConfigEntry> {
    
    public static class ButtonConfigEntry extends ConfigEntry {
        private ITextComponent nameComponent;
        private ConfigEntryButtonProvider buttonProvider;
        private ButtonWidget buttonWidget;
        
        public ButtonConfigEntry(ITextComponent nameComponent, ConfigEntryButtonProvider buttonProvider) {
            this.nameComponent = nameComponent;
            this.buttonProvider = buttonProvider;
            this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "") {
                @Override
                public boolean onMouseClick(int button, double mouseX, double mouseY) {
                    if (getBounds().contains(mouseX, mouseY) && enabled)
                        if (buttonProvider.onPressed(button, mouseX, mouseY)) {
                            Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            return true;
                        }
                    return false;
                }
                
                @Override
                public void onPressed(int button, double mouseX, double mouseY) {}
            };
        }
        
        @Override
        public void drawEntry(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
            MainWindow window = Minecraft.getInstance().mainWindow;
            Point mouse = ClientHelper.getMouseLocation();
            if (Minecraft.getInstance().fontRenderer.getBidiFlag()) {
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - Minecraft.getInstance().fontRenderer.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
                this.buttonWidget.text = buttonProvider.getText();
                this.buttonWidget.getBounds().setLocation(getX(), getY() + 2);
            } else {
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(nameComponent.getFormattedText(), getX(), getY() + 5, 16777215);
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
        private ITextComponent textComponent;
        
        public CategoryTitleConfigEntry(ITextComponent nameComponent) {
            this.textComponent = nameComponent.applyTextStyle(TextFormatting.BOLD);
        }
        
        @Override
        public void drawEntry(int i, int i1, int i2, int i3, boolean b, float v) {
            MainWindow window = Minecraft.getInstance().mainWindow;
            FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
            fontRenderer.drawString(textComponent.getFormattedText(), (window.getScaledWidth() - fontRenderer.getStringWidth(textComponent.getFormattedText())) / 2, getY() + 10, -1);
        }
    }
    
    public static class TextFieldConfigEntry extends ConfigEntry {
        private ITextComponent nameComponent;
        private ConfigEntryTextFieldProvider textFieldProvider;
        private TextFieldWidget textFieldWidget;
        
        public TextFieldConfigEntry(ITextComponent nameComponent, ConfigEntryTextFieldProvider textFieldProvider) {
            this.nameComponent = nameComponent;
            this.textFieldProvider = textFieldProvider;
            this.textFieldWidget = new TextFieldWidget(0, 0, 148, 18);
            this.textFieldWidget.setChangedListener(s -> textFieldProvider.onUpdateText(textFieldWidget, s));
            this.textFieldProvider.onInitWidget(textFieldWidget);
        }
        
        @Override
        public void drawEntry(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
            MainWindow window = Minecraft.getInstance().mainWindow;
            Point mouse = ClientHelper.getMouseLocation();
            if (Minecraft.getInstance().fontRenderer.getBidiFlag()) {
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - Minecraft.getInstance().fontRenderer.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
                this.textFieldWidget.getBounds().setLocation(getX() + 1, getY() + 2);
            } else {
                Minecraft.getInstance().fontRenderer.drawStringWithShadow(nameComponent.getFormattedText(), getX(), getY() + 5, 16777215);
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
