package me.shedaniel.rei.gui.config;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ItemListOrdering;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableTextComponent;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigScreen extends Screen {
    
    private final List<QueuedTooltip> tooltipList;
    private Screen parent;
    private boolean initOverlay;
    private ConfigEntryListWidget entryListWidget;
    
    public ConfigScreen(Screen parent, boolean initOverlay) {
        this.parent = parent;
        this.initOverlay = initOverlay;
        this.tooltipList = Lists.newArrayList();
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256) {
            MinecraftClient.getInstance().openScreen(parent);
            if (initOverlay)
                ScreenHelper.getLastOverlay().onInitialized();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void onInitialized() {
        listeners.add(entryListWidget = new ConfigEntryListWidget(client, screenWidth, screenHeight, 32, screenHeight - 32, 24));
        entryListWidget.configClearEntries();
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.general")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.cheating"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                ClientHelper.setCheating(!ClientHelper.isCheating());
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ClientHelper.isCheating());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.appearance")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.side_search_box"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = !RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.list_ordering"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                int index = Arrays.asList(ItemListOrdering.values()).indexOf(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering) + 1;
                if (index >= ItemListOrdering.values().length) {
                    index = 0;
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending = !RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending;
                }
                RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering = ItemListOrdering.values()[index];
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return I18n.translate("text.rei.config.list_ordering_button", I18n.translate(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering.getNameTranslationKey()), I18n.translate(RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.mirror_rei"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = !RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.modules")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.enable_craftable_only"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = !RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.load_default_plugin"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin = !RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin);
            }
            
            @Override
            public void draw(me.shedaniel.rei.gui.widget.ButtonWidget button, Point mouse, float delta) {
                button.draw(mouse.x, mouse.y, delta);
                if (button.isHighlighted(mouse))
                    tooltipList.add(QueuedTooltip.create(I18n.translate("text.rei.config.load_default_plugin.restart_tooltip").split("\n")));
                
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.enable_util_buttons"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = !RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TranslatableTextComponent("text.rei.config.disable_recipe_book"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public void onPressed() {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = !RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TranslatableTextComponent("text.rei.config.advanced")));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TranslatableTextComponent("text.rei.give_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand);
                widget.setSuggestion(I18n.translate("text.rei.give_command.suggestion"));
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand = text;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(TextFieldWidget widget, Point mouse, float delta) {
                widget.draw(mouse.x, mouse.y, delta);
                if (widget.isHighlighted(mouse))
                    tooltipList.add(QueuedTooltip.create(I18n.translate("text.rei.give_command.tooltip").split("\n")));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TranslatableTextComponent("text.rei.gamemode_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand);
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().gamemodeCommand = text;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TranslatableTextComponent("text.rei.weather_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand);
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                RoughlyEnoughItemsCore.getConfigManager().getConfig().weatherCommand = text;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TranslatableTextComponent("text.rei.config.max_recipes_per_page"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(2);
                widget.setText(RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage + "");
                widget.stripInvaild = s -> {
                    StringBuilder stringBuilder_1 = new StringBuilder();
                    char[] var2 = s.toCharArray();
                    int var3 = var2.length;
                    
                    for(int var4 = 0; var4 < var3; ++var4) {
                        char char_1 = var2[var4];
                        if (Character.isDigit(char_1))
                            stringBuilder_1.append(char_1);
                    }
                    
                    return stringBuilder_1.toString();
                };
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                if (isInvaildNumber(text))
                    try {
                        RoughlyEnoughItemsCore.getConfigManager().getConfig().maxRecipePerPage = Integer.valueOf(text);
                        RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                    } catch (Exception e) {
                    }
            }
            
            @Override
            public void draw(TextFieldWidget widget, Point mouse, float delta) {
                widget.setEditableColor(isInvaildNumber(widget.getText()) ? -1 : Color.RED.getRGB());
                widget.draw(mouse.x, mouse.y, delta);
            }
            
            private boolean isInvaildNumber(String text) {
                try {
                    int page = Integer.valueOf(text);
                    return page >= 2 && page <= 99;
                } catch (Exception e) {
                }
                return false;
            }
        }));
        addButton(new ButtonWidget(screenWidth / 2 - 100, screenHeight - 26, I18n.translate("gui.done")) {
            @Override
            public void onPressed() {
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ConfigScreen.this.client.openScreen(parent);
                if (initOverlay)
                    ScreenHelper.getLastOverlay().onInitialized();
            }
        });
        addButton(new ButtonWidget(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel ? screenWidth - 55 : 10, screenHeight - 26, 45, 20, I18n.translate("text.rei.credits")) {
            @Override
            public void onPressed() {
                MinecraftClient.getInstance().openScreen(new CreditsScreen(ConfigScreen.this));
            }
        });
        super.onInitialized();
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%s", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton ? I18n.translate("text.rei.enabled") : I18n.translate("text.rei.disabled"));
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (entryListWidget.mouseScrolled(double_1, double_2, double_3))
            return true;
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        this.drawTextureBackground(0);
        this.entryListWidget.draw(int_1, int_2, float_1);
        this.drawStringCentered(this.fontRenderer, I18n.translate("text.rei.config"), this.screenWidth / 2, 16, 16777215);
        super.draw(int_1, int_2, float_1);
        GuiLighting.disable();
        tooltipList.forEach(queuedTooltip -> drawTooltip(queuedTooltip.getText(), queuedTooltip.getLocation().x, queuedTooltip.getLocation().y));
        tooltipList.clear();
        GuiLighting.disable();
    }
    
}
