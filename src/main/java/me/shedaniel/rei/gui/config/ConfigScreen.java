package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.REIItemListOrdering;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.InputListener;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableTextComponent;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class ConfigScreen extends Screen {
    
    private Screen parent;
    private ConfigEntryListWidget entryListWidget;
    
    public ConfigScreen(Screen parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.doesEscapeKeyClose()) {
            MinecraftClient.getInstance().openScreen(parent);
            GuiHelper.getLastOverlay().onInitialized();
            return true;
        } else {
            return super.keyPressed(int_1, int_2, int_3);
        }
    }
    
    @Override
    protected void onInitialized() {
        listeners.add(entryListWidget = new ConfigEntryListWidget(client, width, height, 32, height - 32, 24));
        entryListWidget.configClearEntries();
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.appearance")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.side_search_box"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().sideSearchField = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().sideSearchField;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().sideSearchField);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.list_ordering"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                int index = Arrays.asList(REIItemListOrdering.values()).indexOf(RoughlyEnoughItemsCore.getConfigHelper().getConfig().itemListOrdering) + 1;
                if (index >= REIItemListOrdering.values().length) {
                    index = 0;
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().isAscending = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().isAscending;
                }
                RoughlyEnoughItemsCore.getConfigHelper().getConfig().itemListOrdering = REIItemListOrdering.values()[index];
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        
            @Override
            public String getText() {
                return I18n.translate("text.rei.config.list_ordering_button", I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().getConfig().itemListOrdering.getNameTranslationKey()), I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().getConfig().isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.mirror_rei"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().mirrorItemPanel = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().mirrorItemPanel;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().mirrorItemPanel);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.modules")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.enable_craftable_only"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().enableCraftableOnlyButton = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().enableCraftableOnlyButton;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().enableCraftableOnlyButton);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.load_default_plugin"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().loadDefaultPlugin = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().loadDefaultPlugin;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().loadDefaultPlugin);
            }
        
            @Override
            public void draw(me.shedaniel.rei.gui.widget.ButtonWidget button, Point mouse, float delta) {
                button.draw(mouse.x, mouse.y, delta);
                if (button.isHighlighted(mouse)) {
                    GuiLighting.disable();
                    drawTooltip(Arrays.asList(I18n.translate("text.rei.load_default_plugin.restart_tooltip").split("\n")), mouse.x, mouse.y);
                    GuiLighting.disable();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.advanced")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.check_updates"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().checkUpdates = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().checkUpdates;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().checkUpdates);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TranslatableTextComponent("text.rei.give_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().giveCommand);
                widget.setSuggestion(I18n.translate("text.rei.give_command.suggestion"));
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                RoughlyEnoughItemsCore.getConfigHelper().getConfig().giveCommand = text;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(TextFieldWidget widget, Point mouse, float delta) {
                widget.draw(mouse.x, mouse.y, delta);
                if (widget.isHighlighted(mouse)) {
                    GuiLighting.disable();
                    drawTooltip(Arrays.asList(I18n.translate("text.rei.give_command.tooltip").split("\n")), mouse.x, mouse.y);
                    GuiLighting.disable();
                }
            }
        }));
        addButton(new ButtonWidget(width / 2 - 100, height - 26, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double double_1, double double_2) {
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ConfigScreen.this.client.openScreen(parent);
                GuiHelper.getLastOverlay().onInitialized();
            }
        });
        super.onInitialized();
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%b", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        this.drawTextureBackground(0);
        this.entryListWidget.draw(int_1, int_2, float_1);
        this.drawStringCentered(this.fontRenderer, I18n.translate("text.rei.config"), this.width / 2, 16, 16777215);
        super.draw(int_1, int_2, float_1);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public InputListener getFocused() {
        return entryListWidget;
    }
    
}
