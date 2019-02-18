package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;


public class ConfigEntry extends GuiListExtended.IGuiListEntry<ConfigEntry> {
    
    private static final FontRenderer FONT_RENDERER = Minecraft.getInstance().fontRenderer;
    private ITextComponent nameComponent;
    private ConfigEntryButtonProvider buttonProvider;
    private ButtonWidget buttonWidget;
    
    public ConfigEntry(ITextComponent nameComponent, ConfigEntryButtonProvider buttonProvider) {
        this.nameComponent = nameComponent;
        this.buttonProvider = buttonProvider;
        this.buttonWidget = new ButtonWidget(0, 0, 150, 20, "") {
            @Override
            public boolean onMouseClick(int button, double mouseX, double mouseY) {
                if (getBounds().contains(mouseX, mouseY) && enabled)
                    if (buttonProvider.onPressed(button, mouseX, mouseY)) {
                        Minecraft.getInstance().getSoundHandler().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(nameComponent.getFormattedText(), window.getScaledWidth() - FONT_RENDERER.getStringWidth(nameComponent.getFormattedText()) - 40, getY() + 5, 16777215);
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
