package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.REIItemListOrdering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigWidget extends Screen {
    
    private List<IWidget> widgets;
    private Screen parent;
    
    public ConfigWidget(Screen parent) {
        this.parent = parent;
        this.widgets = Lists.newArrayList();
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
        super.onInitialized();
        widgets.clear();
        Window window = MinecraftClient.getInstance().window;
        widgets.add(new ButtonWidget(window.getScaledWidth() / 2 - 20, 30, 40, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setSideSearchField(!RoughlyEnoughItemsCore.getConfigHelper().sideSearchField());
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                text = getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().sideSearchField());
                String t = I18n.translate("text.rei.side_searchbox");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
        widgets.add(new ButtonWidget(window.getScaledWidth() / 2 - 20, 60, 40, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setShowCraftableOnlyButton(!RoughlyEnoughItemsCore.getConfigHelper().showCraftableOnlyButton());
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                text = getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().showCraftableOnlyButton());
                String t = I18n.translate("text.rei.enable_craftable_only");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
        widgets.add(new ButtonWidget(window.getScaledWidth() / 2 - 90, 90, 150, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
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
                }
            }
            
            @Override
            public void draw(int int_1, int int_2, float float_1) {
                GuiLighting.disable();
                this.text = I18n.translate("text.rei.list_ordering_button", I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().getItemListOrdering().getNameTranslationKey()), I18n.translate(RoughlyEnoughItemsCore.getConfigHelper().isAscending() ? "ordering.rei.ascending" : "ordering.rei.descending"));
                String t = I18n.translate("text.rei.list_ordering") + ": ";
                drawString(MinecraftClient.getInstance().fontRenderer, t, parent.width / 2 - 95 - MinecraftClient.getInstance().fontRenderer.getStringWidth(t), 90 + 6, -1);
                super.draw(int_1, int_2, float_1);
            }
        });
        widgets.add(new ButtonWidget(window.getScaledWidth() / 2 - 20, 120, 40, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setMirrorItemPanel(!RoughlyEnoughItemsCore.getConfigHelper().isMirrorItemPanel());
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                text = getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().isMirrorItemPanel());
                String t = I18n.translate("text.rei.mirror_rei");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
        widgets.add(new ButtonWidget(window.getScaledWidth() / 2 - 20, 150, 40, 20, "") {
            @Override
            public void onPressed(int button, double mouseX, double mouseY) {
                if (button == 0)
                    RoughlyEnoughItemsCore.getConfigHelper().setCheckUpdates(!RoughlyEnoughItemsCore.getConfigHelper().checkUpdates());
                try {
                    RoughlyEnoughItemsCore.getConfigHelper().saveConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                text = getTrueFalseText(RoughlyEnoughItemsCore.getConfigHelper().checkUpdates());
                String t = I18n.translate("text.rei.check_updates");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%b", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        drawBackground(0);
        super.draw(int_1, int_2, float_1);
        widgets.forEach(widget -> {
            GuiLighting.disable();
            widget.draw(int_1, int_2, float_1);
        });
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public List<? extends GuiEventListener> getEntries() {
        return widgets;
    }
    
}
