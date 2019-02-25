package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.REIItemListOrdering;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

public class ConfigGui extends GuiScreen {
    
    private GuiScreen parent;
    private ConfigEntryListWidget entryListWidget;
    
    public ConfigGui(GuiScreen parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.allowCloseWithEscape()) {
            Minecraft.getInstance().displayGuiScreen(parent);
            GuiHelper.getLastOverlay().init();
            return true;
        } else {
            return super.keyPressed(int_1, int_2, int_3);
        }
    }
    
    @Override
    protected void initGui() {
        children.add(entryListWidget = new ConfigEntryListWidget(mc, width, height, 32, height - 32, 24));
        entryListWidget.configClearEntries();
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.general")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.cheating"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ClientHelper.setCheating(!ClientHelper.isCheating());
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ClientHelper.isCheating());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.appearance")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.side_search_box"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.list_ordering"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
                return I18n.format("text.rei.config.list_ordering_button", I18n.format(RoughlyEnoughItemsCore.getConfigHelper().getConfig().itemListOrdering.getNameTranslationKey()), I18n.format(RoughlyEnoughItemsCore.getConfigHelper().getConfig().isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.mirror_rei"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.modules")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.enable_craftable_only"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.load_default_plugin"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
                    RenderHelper.disableStandardItemLighting();
                    drawHoveringText(Arrays.asList(I18n.format("text.rei.config.load_default_plugin.restart_tooltip").split("\n")), mouse.x, mouse.y);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.disable_credits_button"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().disableCreditsButton = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().disableCreditsButton;
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().disableCreditsButton);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.enable_util_buttons"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().getConfig().showUtilsButtons = !RoughlyEnoughItemsCore.getConfigHelper().getConfig().showUtilsButtons;
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().showUtilsButtons);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.advanced")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.check_updates"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.give_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().giveCommand);
                widget.setSuggestion(I18n.format("text.rei.give_command.suggestion"));
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
                    RenderHelper.disableStandardItemLighting();
                    drawHoveringText(Arrays.asList(I18n.format("text.rei.give_command.tooltip").split("\n")), mouse.x, mouse.y);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.gamemode_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().gamemodeCommand);
                widget.setSuggestion(I18n.format("text.rei.give_command.suggestion"));
            }
            
            @Override
            public void onUpdateText(TextFieldWidget button, String text) {
                RoughlyEnoughItemsCore.getConfigHelper().getConfig().gamemodeCommand = text;
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.config.max_recipes_per_page"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(2);
                widget.setText(RoughlyEnoughItemsCore.getConfigHelper().getConfig().maxRecipePerPage + "");
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
                        RoughlyEnoughItemsCore.getConfigHelper().getConfig().maxRecipePerPage = Integer.valueOf(text);
                        RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
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
        addButton(new GuiButton(-1, width / 2 - 100, height - 26, I18n.format("gui.done")) {
            @Override
            public void onClick(double double_1, double double_2) {
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ConfigGui.this.mc.displayGuiScreen(parent);
                GuiHelper.getLastOverlay().init();
            }
        });
        super.initGui();
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%b", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.drawBackground(0);
        this.entryListWidget.drawScreen(int_1, int_2, float_1);
        this.drawCenteredString(this.fontRenderer, I18n.format("text.rei.config"), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public IGuiEventListener getFocused() {
        return entryListWidget;
    }
    
}