package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.Core;
import me.shedaniel.config.REIItemListOrdering;
import me.shedaniel.gui.widget.KeyBindButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

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
            ClientListener.recipeKeyBind.setKey(key);
            try {
                Core.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        addButton(new KeyBindButton(998, parent.width / 2 - 20, 60, 80, 20, Core.config.usageKeyBind, key -> {
            Core.config.usageKeyBind = key;
            ClientListener.useKeyBind.setKey(key);
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
        addButton(new GuiButton(1001, parent.width / 2 - 90, 150, 150, 20, "") {
            @Override
            public void onClick(double double_1, double double_2) {
                Core.config.enableCraftableOnlyButton = !Core.config.enableCraftableOnlyButton;
                try {
                    Core.saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void render(int int_1, int int_2, float float_1) {
                RenderHelper.disableStandardItemLighting();
                this.displayString = I18n.format("text.rei.enable_craftable_only.button", Core.config.enableCraftableOnlyButton ? "§a" : "§c", Core.config.enableCraftableOnlyButton);
                super.render(int_1, int_2, float_1);
                if (this.hovered)
                    drawSuggestion(int_1, int_2);
            }
            
            protected void drawSuggestion(int x, int y) {
                drawHoveringText(Arrays.asList(I18n.format("text.rei.enable_craftable_only.tooltip").split("\n")), x, y);
            }
        });
        addButton(new GuiButton(1002, parent.width / 2 - 90, 180, 150, 20, "") {
            @Override
            public void onClick(double double_1, double double_2) {
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
            public void render(int int_1, int int_2, float float_1) {
                RenderHelper.disableStandardItemLighting();
                this.displayString = I18n.format("text.rei.list_ordering_button", I18n.format(Core.config.itemListOrdering.getNameTranslationKey()),
                        I18n.format(Core.config.isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
                super.render(int_1, int_2, float_1);
            }
        });
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.render(mouseX, mouseY, partialTicks);
        RenderHelper.disableStandardItemLighting();
        String text = I18n.format("key.rei.recipe") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 25 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 30 + 6, -1);
        text = I18n.format("key.rei.use") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 25 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 60 + 6, -1);
        text = I18n.format("key.rei.hide") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 25 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 90 + 6, -1);
        text = I18n.format("text.rei.list_ordering") + ": ";
        drawString(Minecraft.getInstance().fontRenderer, text, parent.width / 2 - 95 - Minecraft.getInstance().fontRenderer.getStringWidth(text), 180 + 6, -1);
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
