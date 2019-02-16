package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.client.ConfigHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.REIItemListOrdering;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import net.minecraft.client.Minecraft;
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
            GuiHelper.getLastOverlay().onInitialized();
            return true;
        } else {
            return super.keyPressed(int_1, int_2, int_3);
        }
    }
    
    @Override
    protected void initGui() {
        children.add(entryListWidget = new ConfigEntryListWidget(mc, width, height, 32, height - 32, 24));
        entryListWidget.configClearEntries();
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.side_searchbox"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ConfigHelper.getInstance().setSideSearchField(!ConfigHelper.getInstance().sideSearchField());
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ConfigHelper.getInstance().sideSearchField());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.enable_craftable_only"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ConfigHelper.getInstance().setShowCraftableOnlyButton(!ConfigHelper.getInstance().showCraftableOnlyButton());
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ConfigHelper.getInstance().showCraftableOnlyButton());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.list_ordering"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                int index = Arrays.asList(REIItemListOrdering.values()).indexOf(ConfigHelper.getInstance().getItemListOrdering()) + 1;
                if (index >= REIItemListOrdering.values().length) {
                    index = 0;
                    ConfigHelper.getInstance().setAscending(!ConfigHelper.getInstance().isAscending());
                }
                ConfigHelper.getInstance().setItemListOrdering(REIItemListOrdering.values()[index]);
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return I18n.format("text.rei.list_ordering_button", I18n.format(ConfigHelper.getInstance().getItemListOrdering().getNameTranslationKey()), I18n.format(ConfigHelper.getInstance().isAscending() ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.mirror_rei"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ConfigHelper.getInstance().setMirrorItemPanel(!ConfigHelper.getInstance().isMirrorItemPanel());
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ConfigHelper.getInstance().isMirrorItemPanel());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.check_updates"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ConfigHelper.getInstance().setCheckUpdates(!ConfigHelper.getInstance().checkUpdates());
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ConfigHelper.getInstance().checkUpdates());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TextComponentTranslation("text.rei.load_default_plugin"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    ConfigHelper.getInstance().setLoadingDefaultPlugin(!ConfigHelper.getInstance().isLoadingDefaultPlugin());
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            
            @Override
            public String getText() {
                return getTrueFalseText(ConfigHelper.getInstance().isLoadingDefaultPlugin());
            }
            
            @Override
            public void draw(ButtonWidget button, Point mouse, float delta) {
                button.draw(mouse.x, mouse.y, delta);
                if (button.getBounds().contains(mouse)) {
                    RenderHelper.disableStandardItemLighting();
                    drawHoveringText(Arrays.asList(I18n.format("text.rei.load_default_plugin.restart_tooltip").split("\n")), mouse.x, mouse.y);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }));
        addButton(new net.minecraft.client.gui.GuiButton(0, width / 2 - 100, height - 26, I18n.format("gui.done")) {
            @Override
            public void onClick(double double_1, double double_2) {
                try {
                    ConfigHelper.getInstance().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ConfigGui.this.mc.displayGuiScreen(parent);
                GuiHelper.getLastOverlay().onInitialized();
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