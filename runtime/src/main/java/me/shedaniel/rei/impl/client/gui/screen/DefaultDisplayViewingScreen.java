/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.ImmutableTextComponent;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.RecipeDisplayExporter;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.DefaultDisplayChoosePageWidget;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.client.gui.widget.TabWidget;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.PanelWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
public class DefaultDisplayViewingScreen extends AbstractDisplayViewingScreen {
    public static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private final List<Widget> preWidgets = Lists.newArrayList();
    private final List<Widget> widgets = Lists.newArrayList();
    private final Map<Rectangle, List<Widget>> recipeBounds = Maps.newHashMap();
    private final List<TabWidget> tabs = Lists.newArrayList();
    public int page;
    public int categoryPages = -1;
    @Nullable
    private Panel workingStationsBaseWidget;
    private Button recipeBack, recipeNext, categoryBack, categoryNext;
    
    public DefaultDisplayViewingScreen(Map<DisplayCategory<?>, List<DisplaySpec>> categoriesMap, @Nullable CategoryIdentifier<?> category) {
        super(categoriesMap, category, 5);
        this.bounds = new Rectangle(0, 0, 176, 150);
    }
    
    @Override
    public void recalculateCategoryPage() {
        this.categoryPages = -1;
    }
    
    @Nullable
    public Panel getWorkingStationsBaseWidget() {
        return workingStationsBaseWidget;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258 && !minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            boolean next = !hasShiftDown();
            if (!this.changeFocus(next))
                this.changeFocus(next);
            return true;
        } else if (ConfigObject.getInstance().getNextPageKeybind().matchesKey(keyCode, scanCode)) {
            if (recipeNext.isEnabled())
                recipeNext.onClick();
            return recipeNext.isEnabled();
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(keyCode, scanCode)) {
            if (recipeBack.isEnabled())
                recipeBack.onClick();
            return recipeBack.isEnabled();
        }
        for (GuiEventListener element : children())
            if (element.keyPressed(keyCode, scanCode, modifiers))
                return true;
        if (keyCode == 256 || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            Minecraft.getInstance().setScreen(REIRuntime.getInstance().getPreviousScreen());
            return true;
        }
        if (ConfigObject.getInstance().getPreviousScreenKeybind().matchesKey(keyCode, scanCode)) {
            if (REIRuntimeImpl.getInstance().hasLastDisplayScreen()) {
                minecraft.setScreen(REIRuntimeImpl.getInstance().getLastDisplayScreen());
            } else {
                minecraft.setScreen(REIRuntime.getInstance().getPreviousScreen());
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void init() {
        super.init();
        this.children().clear();
        this.recipeBounds.clear();
        this.tabs.clear();
        this.preWidgets.clear();
        this.widgets.clear();
        int largestHeight = Math.min(Math.max(height - 34 - 30, 100), ConfigObject.getInstance().getMaxRecipesPageHeight());
        int maxWidthDisplay = CollectionUtils.<DisplaySpec, Integer>mapAndMax(getCurrentDisplayed(), display -> getCurrentCategory().getDisplayWidth(display.provideInternalDisplay()), Comparator.naturalOrder()).orElse(150);
        int maxHeight = Math.min(largestHeight, CollectionUtils.<DisplayCategory<?>, Integer>mapAndMax(categories,
                category -> (category.getDisplayHeight() + 4) * Math.max(1, Math.min(getRecipesPerPage(largestHeight, category) + 1, Math.max(categoryMap.get(category).size(), ConfigObject.getInstance().getMaxRecipePerPage()))) + 36, Comparator.naturalOrder()).orElse(66));
        int totalDisplayHeight = (getCurrentCategory().getDisplayHeight() + 4) * Math.max(1, getRecipesPerPage(maxHeight, getCurrentCategory()) + 1) + 36;
        int guiWidth = Math.max(maxWidthDisplay + 10, 190);
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - maxHeight / 2, guiWidth, maxHeight);
        if (ConfigObject.getInstance().isSubsetsEnabled()) {
            this.bounds.setLocation(this.bounds.getX(), this.bounds.getY() + 15);
            this.bounds.setSize(this.bounds.getWidth(), this.bounds.getHeight() - 10);
        }
        
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        this.tabsPerPage = Math.max(5, Mth.floor((guiWidth - 20d) / tabSize));
        if (this.categoryPages == -1) {
            this.categoryPages = Math.max(0, selectedCategoryIndex / tabsPerPage);
        }
        
        this.page = Mth.clamp(page, 0, getCurrentTotalPages() - 1);
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x, bounds.y - 16, 10, 10), new TranslatableComponent("text.rei.left_arrow"))
                .onClick(button -> {
                    categoryPages--;
                    if (categoryPages < 0)
                        categoryPages = Mth.ceil(categories.size() / (float) tabsPerPage) - 1;
                    DefaultDisplayViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x + bounds.width - 10, bounds.y - 16, 10, 10), new TranslatableComponent("text.rei.right_arrow"))
                .onClick(button -> {
                    categoryPages++;
                    if (categoryPages > Mth.ceil(categories.size() / (float) tabsPerPage) - 1)
                        categoryPages = 0;
                    DefaultDisplayViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        this.widgets.add(categoryBack = Widgets.createButton(new Rectangle(bounds.getX() + 5, bounds.getY() + 5, 12, 12), new TranslatableComponent("text.rei.left_arrow"))
                .onClick(button -> previousCategory()).tooltipLine(new TranslatableComponent("text.rei.previous_category")));
        this.widgets.add(Widgets.createClickableLabel(new Point(bounds.getCenterX(), bounds.getY() + 7), getCurrentCategory().getTitle(), clickableLabelWidget -> {
            ViewSearchBuilder.builder().addAllCategories().open();
        }).tooltip(new TranslatableComponent("text.rei.view_all_categories")));
        this.widgets.add(categoryNext = Widgets.createButton(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 5, 12, 12), new TranslatableComponent("text.rei.right_arrow"))
                .onClick(button -> nextCategory()).tooltipLine(new TranslatableComponent("text.rei.next_category")));
        this.categoryBack.setEnabled(categories.size() > 1);
        this.categoryNext.setEnabled(categories.size() > 1);
        
        this.widgets.add(recipeBack = Widgets.createButton(new Rectangle(bounds.getX() + 5, bounds.getY() + 19, 12, 12), new TranslatableComponent("text.rei.left_arrow"))
                .onClick(button -> {
                    page--;
                    if (page < 0)
                        page = getCurrentTotalPages() - 1;
                    DefaultDisplayViewingScreen.this.init();
                }).tooltipLine(new TranslatableComponent("text.rei.previous_page")));
        this.widgets.add(Widgets.createClickableLabel(new Point(bounds.getCenterX(), bounds.getY() + 21), NarratorChatListener.NO_TITLE, label -> {
            if (!Screen.hasShiftDown()) {
                page = 0;
                DefaultDisplayViewingScreen.this.init();
            } else {
                ScreenOverlayImpl.getInstance().choosePageWidget = new DefaultDisplayChoosePageWidget(page -> {
                    DefaultDisplayViewingScreen.this.page = page;
                    DefaultDisplayViewingScreen.this.init();
                }, page, getCurrentTotalPages());
            }
        }).onRender((matrices, label) -> {
            label.setMessage(new ImmutableTextComponent(String.format("%d/%d", page + 1, getCurrentTotalPages())));
            label.setClickable(getCurrentTotalPages() > 1);
        }).tooltipFunction(label -> label.isClickable() ? new Component[]{new TranslatableComponent("text.rei.go_back_first_page"), new TextComponent(" "), new TranslatableComponent("text.rei.shift_click_to", new TranslatableComponent("text.rei.choose_page")).withStyle(ChatFormatting.GRAY)} : null));
        this.widgets.add(recipeNext = Widgets.createButton(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 19, 12, 12), new TranslatableComponent("text.rei.right_arrow"))
                .onClick(button -> {
                    page++;
                    if (page >= getCurrentTotalPages())
                        page = 0;
                    DefaultDisplayViewingScreen.this.init();
                }).tooltipLine(new TranslatableComponent("text.rei.next_page")));
        this.recipeBack.setEnabled(getCurrentTotalPages() > 1);
        this.recipeNext.setEnabled(getCurrentTotalPages() > 1);
        for (int id = 0; id < tabsPerPage; id++) {
            int tabIndex = id + categoryPages * tabsPerPage;
            if (categories.size() > tabIndex) {
                TabWidget tab;
                tabs.add(tab = TabWidget.create(id, tabSize, bounds.x + bounds.width / 2 - Math.min(categories.size() - categoryPages * tabsPerPage, tabsPerPage) * tabSize / 2, bounds.y, 0, isCompactTabs ? 166 : 192, widget -> {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    if (widget.getId() + categoryPages * tabsPerPage == selectedCategoryIndex)
                        return false;
                    ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, categories.get(widget.getId() + categoryPages * tabsPerPage).getCategoryIdentifier(), ingredientStackToNotice, resultStackToNotice);
                    return true;
                }));
                tab.setRenderer(categories.get(tabIndex), categories.get(tabIndex).getIcon(), categories.get(tabIndex).getTitle(), tab.getId() + categoryPages * tabsPerPage == selectedCategoryIndex);
            }
        }
        initDisplays();
        initWorkstations(preWidgets);
        
        _children().addAll(tabs);
        _children().addAll(widgets);
        _children().addAll(preWidgets);
    }
    
    private void initDisplays() {
        Optional<ButtonArea> plusButtonArea = CategoryRegistry.getInstance().get(getCurrentCategoryId()).getPlusButtonArea();
        int displayHeight = getCurrentCategory().getDisplayHeight();
        List<DisplaySpec> currentDisplayed = getCurrentDisplayed();
        for (int i = 0; i < currentDisplayed.size(); i++) {
            final DisplaySpec display = currentDisplayed.get(i);
            final Supplier<Display> displaySupplier = display::provideInternalDisplay;
            int displayWidth = getCurrentCategory().getDisplayWidth(displaySupplier.get());
            final Rectangle displayBounds = new Rectangle(getBounds().getCenterX() - displayWidth / 2, getBounds().getCenterY() + 16 - displayHeight * (getRecipesPerPage() + 1) / 2 - 2 * (getRecipesPerPage() + 1) + displayHeight * i + 4 * i, displayWidth, displayHeight);
            List<Widget> setupDisplay;
            try {
                setupDisplay = getCurrentCategoryView(display.provideInternalDisplay()).setupDisplay(display.provideInternalDisplay(), displayBounds);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                setupDisplay = new ArrayList<>();
                setupDisplay.add(Widgets.createRecipeBase(displayBounds).color(0xFFBB0000));
                setupDisplay.add(Widgets.createLabel(new Point(displayBounds.getCenterX(), displayBounds.getCenterY() - 8), new TextComponent("Failed to initiate setupDisplay")));
                setupDisplay.add(Widgets.createLabel(new Point(displayBounds.getCenterX(), displayBounds.getCenterY() + 1), new TextComponent("Check console for error")));
            }
            setupTags(setupDisplay);
            transformIngredientNotice(setupDisplay, ingredientStackToNotice);
            transformResultNotice(setupDisplay, resultStackToNotice);
            for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets, EntryWidget.class::isInstance)) {
                widget.removeTagMatch = true;
            }
            this.recipeBounds.put(displayBounds, setupDisplay);
            this.widgets.addAll(setupDisplay);
            if (plusButtonArea.isPresent() && plusButtonArea.get().get(displayBounds) != null) {
                this.widgets.add(InternalWidgets.createAutoCraftingButtonWidget(displayBounds, plusButtonArea.get().get(displayBounds), new TextComponent(plusButtonArea.get().getButtonText()), displaySupplier, display::provideInternalDisplayIds, setupDisplay, getCurrentCategory()));
            }
        }
    }
    
    private void initWorkstations(List<Widget> preWidgets) {
        workingStationsBaseWidget = null;
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(getCurrentCategoryId()).getWorkstations();
        if (!workstations.isEmpty()) {
            int hh = Mth.floor((bounds.height - 16) / 18f);
            int actualHeight = Math.min(hh, workstations.size());
            int innerWidth = Mth.ceil(workstations.size() / ((float) hh));
            int xx = bounds.x - (8 + innerWidth * 16) + 6;
            int yy = bounds.y + 16;
            preWidgets.add(workingStationsBaseWidget = Widgets.createCategoryBase(new Rectangle(xx - 5, yy - 5, 15 + innerWidth * 16, 10 + actualHeight * 16)));
            preWidgets.add(Widgets.createSlotBase(new Rectangle(xx - 1, yy - 1, innerWidth * 16 + 2, actualHeight * 16 + 2)));
            int index = 0;
            xx += (innerWidth - 1) * 16;
            for (EntryIngredient workingStation : workstations) {
                preWidgets.add(new WorkstationSlotWidget(xx, yy, workingStation));
                index++;
                yy += 16;
                if (index >= hh) {
                    index = 0;
                    yy = bounds.y + 16;
                    xx -= 16;
                }
            }
        }
    }
    
    public List<Widget> getWidgets() {
        return widgets;
    }
    
    public List<DisplaySpec> getCurrentDisplayed() {
        List<DisplaySpec> list = Lists.newArrayList();
        int recipesPerPage = getRecipesPerPage();
        List<DisplaySpec> displays = categoryMap.get(getCurrentCategory());
        for (int i = 0; i <= recipesPerPage; i++) {
            if (page * (recipesPerPage + 1) + i < displays.size()) {
                list.add(displays.get(page * (recipesPerPage + 1) + i));
            }
        }
        return list;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    private int getRecipesPerPage() {
        return getRecipesPerPage(this.bounds.height, getCurrentCategory());
    }
    
    private static int getRecipesPerPage(int totalHeight, DisplayCategory<?> category) {
        if (category.getFixedDisplaysPerPage() > 0)
            return category.getFixedDisplaysPerPage() - 1;
        int height = category.getDisplayHeight();
        return Mth.clamp(Mth.floor(((double) totalHeight - 36) / ((double) height + 4)) - 1, 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, category.getMaximumDisplaysPerPage() - 1));
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        for (Widget widget : preWidgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
        PanelWidget.render(matrices, bounds, -1);
        if (REIRuntime.getInstance().isDarkThemeEnabled()) {
            fill(matrices, bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
            fill(matrices, bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 30, 0xFF404040);
        } else {
            fill(matrices, bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
            fill(matrices, bounds.x + 17, bounds.y + 19, bounds.x + bounds.width - 17, bounds.y + 31, 0xFF9E9E9E);
        }
        for (TabWidget tab : tabs) {
            if (!tab.isSelected())
                tab.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (TabWidget tab : tabs) {
            if (tab.isSelected())
                tab.render(matrices, mouseX, mouseY, delta);
        }
        {
            ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
            if (export.matchesCurrentKey() || export.matchesCurrentMouse()) {
                for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                    Rectangle bounds = entry.getKey();
                    setBlitOffset(470);
                    if (bounds.contains(mouseX, mouseY)) {
                        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744822402, 1744822402);
                        Component text = new TranslatableComponent("text.rei.release_export", export.getLocalizedName().plainCopy().getString());
                        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                        matrices.pushPose();
                        matrices.translate(0.0D, 0.0D, 480);
                        Matrix4f matrix4f = matrices.last().pose();
                        font.drawInBatch(text.getVisualOrderText(), bounds.getCenterX() - font.width(text) / 2f, bounds.getCenterY() - 4.5f, 0xff000000, false, matrix4f, immediate, false, 0, 15728880);
                        immediate.endBatch();
                        matrices.popPose();
                    } else {
                        fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 1744830463, 1744830463);
                    }
                    setBlitOffset(0);
                }
            }
        }
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesKey(keyCode, scanCode)) {
            for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                Rectangle bounds = entry.getKey();
                if (bounds.contains(PointHelper.ofMouse())) {
                    RecipeDisplayExporter.exportRecipeDisplay(bounds, entry.getValue());
                    break;
                }
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    public int getCurrentTotalPages() {
        return getTotalPages(selectedCategoryIndex);
    }
    
    public int getTotalPages(int categoryIndex) {
        return Mth.ceil(categoryMap.get(categories.get(categoryIndex)).size() / (double) (getRecipesPerPage() + 1));
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (GuiEventListener listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        for (GuiEventListener entry : children())
            if (entry.mouseDragged(double_1, double_2, int_1, double_3, double_4))
                return true;
        return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesMouse(button)) {
            for (Map.Entry<Rectangle, List<Widget>> entry : recipeBounds.entrySet()) {
                Rectangle bounds = entry.getKey();
                if (bounds.contains(PointHelper.ofMouse())) {
                    RecipeDisplayExporter.exportRecipeDisplay(bounds, entry.getValue());
                    break;
                }
            }
        }
        for (GuiEventListener entry : children())
            if (entry.mouseReleased(mouseX, mouseY, button))
                return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        REIRuntimeImpl.isWithinRecipeViewingScreen = true;
        for (GuiEventListener listener : children()) {
            if (listener.mouseScrolled(mouseX, mouseY, amount)) {
                REIRuntimeImpl.isWithinRecipeViewingScreen = false;
                return true;
            }
        }
        REIRuntimeImpl.isWithinRecipeViewingScreen = false;
        if (getBounds().contains(PointHelper.ofMouse())) {
            if (amount > 0 && recipeBack.isEnabled())
                recipeBack.onClick();
            else if (amount < 0 && recipeNext.isEnabled())
                recipeNext.onClick();
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(PointHelper.ofMouse())) {
            if (amount > 0 && categoryBack.isEnabled())
                categoryBack.onClick();
            else if (amount < 0 && categoryNext.isEnabled())
                categoryNext.onClick();
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ConfigObject.getInstance().getNextPageKeybind().matchesMouse(button)) {
            if (recipeNext.isEnabled())
                recipeNext.onClick();
            return recipeNext.isEnabled();
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesMouse(button)) {
            if (recipeBack.isEnabled())
                recipeBack.onClick();
            return recipeBack.isEnabled();
        } else if (ConfigObject.getInstance().getPreviousScreenKeybind().matchesMouse(button)) {
            if (REIRuntimeImpl.getInstance().hasLastDisplayScreen()) {
                minecraft.setScreen(REIRuntimeImpl.getInstance().getLastDisplayScreen());
            } else {
                minecraft.setScreen(REIRuntime.getInstance().getPreviousScreen());
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    public static class WorkstationSlotWidget extends EntryWidget {
        public WorkstationSlotWidget(int x, int y, EntryIngredient widgets) {
            super(new Point(x, y));
            entries(widgets);
            noBackground();
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return getInnerBounds().contains(mouseX, mouseY);
        }
    }
    
}
