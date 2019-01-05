package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.Core;
import me.shedaniel.config.REIItemListOrdering;
import me.shedaniel.gui.widget.KeyBindButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

import java.io.IOException;
import java.util.Arrays;

public class ConfigGui extends Gui {
    
    private Gui parent;
    
    public ConfigGui(Gui parent) {
        this.parent = parent;
    }
    
    @Override
    protected void onInitialized() {
        addButton(new KeyBindButton(997, parent.width / 2 - 20, 30, 80, 20, Core.config.recipeKeyBind, key -> {
            Core.config.recipeKeyBind = key;
            ClientListener.recipeKeyBind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new KeyBindButton(998, parent.width / 2 - 20, 60, 80, 20, Core.config.usageKeyBind, key -> {
            Core.config.usageKeyBind = key;
            ClientListener.usageKeyBind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new KeyBindButton(999, parent.width / 2 - 20, 90, 80, 20, Core.config.hideKeyBind, key -> {
            Core.config.hideKeyBind = key;
            ClientListener.hideKeyBind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new ButtonWidget(1000, parent.width / 2 - 90, 120, 150, 20, "") {
            @Override
            public void onPressed(double double_1, double double_2) {
                Core.config.centreSearchBox = !Core.config.centreSearchBox;
                try {
                    Core.saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(int int_1, int int_2, float float_1) {
                this.text = I18n.translate("text.rei.centre_searchbox", Core.config.centreSearchBox ? "§a" : "§c", Core.config.centreSearchBox);
                super.draw(int_1, int_2, float_1);
                if (this.hovered)
                    drawSuggestion(int_1, int_2);
            }
            
            protected void drawSuggestion(int x, int y) {
                drawTooltip(Arrays.asList(I18n.translate("text.rei.centre_searchbox.tooltip").split("\n")), x, y);
            }
        });
        addButton(new ButtonWidget(1001, parent.width / 2 - 90, 150, 150, 20, "") {
            @Override
            public void onPressed(double double_1, double double_2) {
                int index = Arrays.asList(REIItemListOrdering.values()).indexOf(Core.config.itemListOrdering) + 1;
                if (index >= REIItemListOrdering.values().length) {
                    index = 0;
                    Core.config.isAscending = !Core.config.isAscending;
                }
                Core.config.itemListOrdering = REIItemListOrdering.values()[index];
                try {
                    Core.saveConfig();
                    REIRenderHelper.reiGui.updateView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    
            @Override
            public void draw(int int_1, int int_2, float float_1) {
                this.text = I18n.translate("text.rei.list_ordering_button", I18n.translate(Core.config.itemListOrdering.getNameTranslationKey()),
                        I18n.translate(Core.config.isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
                super.draw(int_1, int_2, float_1);
            }
        });
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        super.draw(mouseX, mouseY, partialTicks);
        String text = I18n.translate("key.rei.recipe") + ": ";
        drawString(MinecraftClient.getInstance().fontRenderer, text, parent.width / 2 - 25 - MinecraftClient.getInstance().fontRenderer.getStringWidth(text), 30 + 6, -1);
        text = I18n.translate("key.rei.use") + ": ";
        drawString(MinecraftClient.getInstance().fontRenderer, text, parent.width / 2 - 25 - MinecraftClient.getInstance().fontRenderer.getStringWidth(text), 60 + 6, -1);
        text = I18n.translate("key.rei.hide") + ": ";
        drawString(MinecraftClient.getInstance().fontRenderer, text, parent.width / 2 - 25 - MinecraftClient.getInstance().fontRenderer.getStringWidth(text), 90 + 6, -1);
        text = I18n.translate("text.rei.list_ordering") + ": ";
        drawString(MinecraftClient.getInstance().fontRenderer, text, parent.width / 2 - 95 - MinecraftClient.getInstance().fontRenderer.getStringWidth(text), 150 + 6, -1);
    }
    
    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 256 && this.doesEscapeKeyClose()) {
            this.close();
            if (parent != null)
                MinecraftClient.getInstance().openGui(parent);
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}
