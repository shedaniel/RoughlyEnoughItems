package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.Core;
import me.shedaniel.gui.widget.KeyBindButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;

import java.io.IOException;
import java.util.Arrays;

public class ConfigGui extends GuiScreen {
    
    private GuiScreen parent;
    
    public ConfigGui(GuiScreen parent) {
        this.parent = parent;
    }
    
    @Override
    protected void initGui() {
        addButton(new KeyBindButton(997, parent.width / 2 - 20, 30, 80, 20, Core.config.recipeKeyBind, key -> {
            Core.config.recipeKeyBind = key;
            ClientListener.recipeKeybind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new KeyBindButton(998, parent.width / 2 - 20, 60, 80, 20, Core.config.usageKeyBind, key -> {
            Core.config.usageKeyBind = key;
            ClientListener.useKeybind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new KeyBindButton(999, parent.width / 2 - 20, 90, 80, 20, Core.config.hideKeyBind, key -> {
            Core.config.hideKeyBind = key;
            ClientListener.hideKeybind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new GuiButton(1000, parent.width / 2 - 90, 120, 150, 20, "") {
            @Override
            public void onClick(double double_1, double double_2) {
                Core.config.centreSearchBox = !Core.config.centreSearchBox;
                try {
                    Core.saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        
            @Override
            public void render(int int_1, int int_2, float float_1) {
                this.displayString = I18n.format("text.rei.centre_searchbox", Core.config.centreSearchBox ? "§a" : "§c", Core.config.centreSearchBox);
                super.render(int_1, int_2, float_1);
                if (this.hovered)
                    drawSuggestion(int_1, int_2);
            }
        
            protected void drawSuggestion(int x, int y) {
                drawHoveringText(Arrays.asList(I18n.format("text.rei.centre_searchbox.tooltip").split("\n")), x, y);
            }
        });
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        String text = I18n.format("key.rei.recipe") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 40 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 30 + 6, -1);
        text = I18n.format("key.rei.use") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 40 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 60 + 6, -1);
        text = I18n.format("key.rei.hide") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 40 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 90 + 6, -1);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 256 && this.allowCloseWithEscape()) {
            this.close();
            if (parent != null)
                Minecraft.getInstance().displayGuiScreen(parent);
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
