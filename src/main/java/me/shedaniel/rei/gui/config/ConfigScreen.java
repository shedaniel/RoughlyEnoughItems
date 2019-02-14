package me.shedaniel.rei.gui.config;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.REIItemListOrdering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableTextComponent;

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
        entryListWidget.configAddEntry(new ConfigEntry(new TranslatableTextComponent("text.rei.side_searchbox"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setSideSearchField(!RoughlyEnoughItemsCore.getConfigHelper().sideSearchField());
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().sideSearchField());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TranslatableTextComponent("text.rei.enable_craftable_only"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setShowCraftableOnlyButton(!RoughlyEnoughItemsCore.getConfigHelper().showCraftableOnlyButton());
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().showCraftableOnlyButton());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TranslatableTextComponent("text.rei.list_ordering"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                int index = Arrays.asList(REIItemListOrdering.values()).indexOf(RoughlyEnoughItemsCore.getConfigHelper().getItemListOrdering()) + 1;
                if (index >= REIItemListOrdering.values().length) {
                    index = 0;
                    RoughlyEnoughItemsCore.getConfigHelper().setAscending(!RoughlyEnoughItemsCore.getConfigHelper().isAscending());
                }
                RoughlyEnoughItemsCore.getConfigHelper().setItemListOrdering(REIItemListOrdering.values()[index]);
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
                return I18n.translate("text.rei.list_ordering_button", I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().getItemListOrdering().getNameTranslationKey()), I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().isAscending() ? "ordering.rei.ascending" : "ordering.rei.descending"));
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TranslatableTextComponent("text.rei.mirror_rei"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setMirrorItemPanel(!RoughlyEnoughItemsCore.getConfigHelper().isMirrorItemPanel());
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().isMirrorItemPanel());
            }
        }));
        entryListWidget.configAddEntry(new ConfigEntry(new TranslatableTextComponent("text.rei.check_updates"), new ConfigEntry.ConfigEntryButtonProvider() {
            @Override
            public boolean onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setCheckUpdates(!RoughlyEnoughItemsCore.getConfigHelper().checkUpdates());
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
                return getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().checkUpdates());
            }
        }));
        addButton(new ButtonWidget(0, width / 2 - 100, height - 26, I18n.translate("gui.done")) {
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
    public GuiEventListener getFocused() {
        return entryListWidget;
    }
    
}
