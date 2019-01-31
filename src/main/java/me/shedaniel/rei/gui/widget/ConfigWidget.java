package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.REIItemListOrdering;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConfigWidget extends GuiScreen {
    
    private List<IWidget> widgets;
    private GuiScreen parent;
    
    public ConfigWidget(GuiScreen parent) {
        this.parent = parent;
        this.widgets = Lists.newArrayList();
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.allowCloseWithEscape()) {
            Minecraft.getInstance().displayGuiScreen(parent);
            return true;
        } else {
            return super.keyPressed(int_1, int_2, int_3);
        }
    }
    
    @Override
    protected void initGui() {
        super.initGui();
        widgets.clear();
        MainWindow window = Minecraft.getInstance().mainWindow;
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
                String t = I18n.format("text.rei.centre_searchbox");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawStringWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
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
                String t = I18n.format("text.rei.enable_craftable_only");
                int width = fontRenderer.getStringWidth(t);
                fontRenderer.drawStringWithShadow(t, this.x - width - 10, this.y + (this.height - 8) / 2, -1);
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
                RenderHelper.disableStandardItemLighting();
                this.text = I18n.format("text.rei.list_ordering_button", I18n.format(RoughlyEnoughItemsCore.getConfigHelper().getItemListOrdering().getNameTranslationKey()), I18n.format(RoughlyEnoughItemsCore.getConfigHelper().isAscending() ? "ordering.rei.ascending" : "ordering.rei.descending"));
                String t = I18n.format("text.rei.list_ordering") + ": ";
                drawString(Minecraft.getInstance().fontRenderer, t, parent.width / 2 - 95 - Minecraft.getInstance().fontRenderer.getStringWidth(t), 90 + 6, -1);
                super.draw(int_1, int_2, float_1);
            }
        });
    }
    
    private String getTrueFalseText(boolean showCraftableOnlyButton) {
        return String.format("%s%b", showCraftableOnlyButton ? "§a" : "§c", showCraftableOnlyButton);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        drawWorldBackground(0);
        super.render(int_1, int_2, float_1);
        widgets.forEach(widget -> {
            RenderHelper.disableStandardItemLighting();
            widget.draw(int_1, int_2, float_1);
        });
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
}
