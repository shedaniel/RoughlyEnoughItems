package me.shedaniel.rei.gui.config;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ItemListOrdering;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigScreen extends GuiScreen {
    
    private final List<QueuedTooltip> tooltipList;
    private GuiScreen parent;
    private boolean initOverlay;
    private ConfigEntryListWidget entryListWidget;
    
    public ConfigScreen(GuiScreen parent, boolean initOverlay) {
        this.parent = parent;
        this.initOverlay = initOverlay;
        this.tooltipList = Lists.newArrayList();
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.allowCloseWithEscape()) {
            Minecraft.getInstance().displayGuiScreen(parent);
            if (initOverlay)
                ScreenHelper.getLastOverlay().onInitialized();
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
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField = !RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().sideSearchField);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.list_ordering"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
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
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return I18n.format("text.rei.config.list_ordering_button", I18n.format(RoughlyEnoughItemsCore.getConfigManager().getConfig().itemListOrdering.getNameTranslationKey()), I18n.format(RoughlyEnoughItemsCore.getConfigManager().getConfig().isAscending ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.mirror_rei"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel = !RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().mirrorItemPanel);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.modules")));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.enable_craftable_only"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton = !RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().enableCraftableOnlyButton);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.load_default_plugin"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin = !RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().loadDefaultPlugin);
            }
            
            @Override
            public void draw(me.shedaniel.rei.gui.widget.ButtonWidget button, Point mouse, float delta) {
                button.draw(mouse.x, mouse.y, delta);
                if (button.isHighlighted(mouse))
                    tooltipList.add(QueuedTooltip.create(I18n.format("text.rei.config.load_default_plugin.restart_tooltip").split("\n")));
                
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.disable_credits_button"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().disableCreditsButton = !RoughlyEnoughItemsCore.getConfigManager().getConfig().disableCreditsButton;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().disableCreditsButton);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.enable_util_buttons"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons = !RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().showUtilsButtons);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.ButtonConfigEntry(new TextComponentTranslation("text.rei.config.disable_recipe_book"), new ConfigEntry.ButtonConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook = !RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook;
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigManager().getConfig().disableRecipeBook);
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.CategoryTitleConfigEntry(new TextComponentTranslation("text.rei.config.advanced")));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.give_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
            @Override
            public void onInitWidget(TextFieldWidget widget) {
                widget.setMaxLength(99999);
                widget.setText(RoughlyEnoughItemsCore.getConfigManager().getConfig().giveCommand);
                widget.setSuggestion(I18n.format("text.rei.give_command.suggestion"));
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
                    tooltipList.add(QueuedTooltip.create(I18n.format("text.rei.give_command.tooltip").split("\n")));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.gamemode_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.weather_command"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
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
        entryListWidget.configAddEntry(new ConfigEntry.TextFieldConfigEntry(new TextComponentTranslation("text.rei.config.max_recipes_per_page"), new ConfigEntry.TextFieldConfigEntry.ConfigEntryTextFieldProvider() {
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
        addButton(new GuiButton(0, width / 2 - 100, height - 26, I18n.format("gui.done")) {
            @Override
            public void onClick(double double_1, double double_2) {
                try {
                    RoughlyEnoughItemsCore.getConfigManager().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ConfigScreen.this.mc.displayGuiScreen(parent);
                if (initOverlay)
                    ScreenHelper.getLastOverlay().onInitialized();
            }
        });
        super.initGui();
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%s", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton ? I18n.format("text.rei.enabled") : I18n.format("text.rei.disabled"));
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.drawWorldBackground(0);
        this.entryListWidget.drawScreen(int_1, int_2, float_1);
        this.drawCenteredString(this.fontRenderer, I18n.format("text.rei.config"), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
        RenderHelper.disableStandardItemLighting();
        tooltipList.forEach(queuedTooltip -> drawHoveringText(queuedTooltip.getText(), queuedTooltip.getLocation().x, queuedTooltip.getLocation().y));
        tooltipList.clear();
        RenderHelper.disableStandardItemLighting();
    }
    
    @Nullable
    @Override
    public IGuiEventListener getFocused() {
        return entryListWidget;
    }
}
