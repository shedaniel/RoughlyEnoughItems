/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.ScrollingContainer;
import me.shedaniel.rei.gui.widget.TabWidget;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.InternalWidgets;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public class VillagerRecipeViewingScreen extends Screen implements RecipeScreen {
    
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoryMap;
    private final List<RecipeCategory<?>> categories;
    private final List<Widget> widgets = Lists.newArrayList();
    private final List<Button> buttonList = Lists.newArrayList();
    private final List<RecipeEntry> recipeRenderers = Lists.newArrayList();
    private final List<TabWidget> tabs = Lists.newArrayList();
    public Rectangle bounds, scrollListBounds;
    private int tabsPerPage = 8;
    private int selectedCategoryIndex = 0;
    private int selectedRecipeIndex = 0;
    private final ScrollingContainer scrolling = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return new Rectangle(scrollListBounds.x + 1, scrollListBounds.y + 1, scrollListBounds.width - 2, scrollListBounds.height - 2);
        }
        
        @Override
        public int getMaxScrollHeight() {
            int i = 0;
            for (Button button : buttonList) {
                i += button.getBounds().height;
            }
            return i;
        }
    };
    private float scrollBarAlpha = 0;
    private float scrollBarAlphaFuture = 0;
    private long scrollBarAlphaFutureTime = -1;
    private int tabsPage = -1;
    private EntryStack ingredientStackToNotice = EntryStack.empty();
    private EntryStack resultStackToNotice = EntryStack.empty();
    
    public VillagerRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> categoryMap, @Nullable Identifier category) {
        super(NarratorManager.EMPTY);
        this.categoryMap = categoryMap;
        this.categories = Lists.newArrayList(categoryMap.keySet());
        if (category != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getIdentifier().equals(category)) {
                    this.selectedCategoryIndex = i;
                    break;
                }
            }
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void addIngredientStackToNotice(EntryStack stack) {
        ingredientStackToNotice = stack;
    }
    
    @Override
    public void addResultStackToNotice(EntryStack stack) {
        resultStackToNotice = stack;
    }
    
    @Override
    public Identifier getCurrentCategory() {
        return categories.get(selectedCategoryIndex).getIdentifier();
    }
    
    @Override
    public void recalculateCategoryPage() {
        this.tabsPage = -1;
    }
    
    @Override
    public void init() {
        super.init();
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        scrolling.draggingScrollBar = false;
        this.children.clear();
        this.widgets.clear();
        this.buttonList.clear();
        this.recipeRenderers.clear();
        this.tabs.clear();
        int largestWidth = width - 100;
        int largestHeight = height - 40;
        RecipeCategory<RecipeDisplay> category = (RecipeCategory<RecipeDisplay>) categories.get(selectedCategoryIndex);
        RecipeDisplay display = categoryMap.get(category).get(selectedRecipeIndex);
        int guiWidth = MathHelper.clamp(category.getDisplayWidth(display) + 30, 0, largestWidth) + 100;
        int guiHeight = MathHelper.clamp(category.getDisplayHeight() + 40, 166, largestHeight);
        this.tabsPerPage = Math.max(5, MathHelper.floor((guiWidth - 20d) / tabSize));
        if (this.tabsPage == -1) {
            this.tabsPage = selectedCategoryIndex / tabsPerPage;
        }
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        List<List<EntryStack>> workingStations = RecipeHelper.getInstance().getWorkingStations(category.getIdentifier());
        if (!workingStations.isEmpty()) {
            int ww = MathHelper.floor((bounds.width - 16) / 18f);
            int w = Math.min(ww, workingStations.size());
            int h = MathHelper.ceil(workingStations.size() / ((float) ww));
            int xx = bounds.x + 16;
            int yy = bounds.y + bounds.height + 2;
            widgets.add(Widgets.createCategoryBase(new Rectangle(xx - 5, bounds.y + bounds.height - 5, 10 + w * 16, 12 + h * 16)));
            widgets.add(Widgets.createSlotBase(new Rectangle(xx - 1, yy - 1, 2 + w * 16, 2 + h * 16)));
            int index = 0;
            List<Text> list = Collections.singletonList(new TranslatableText("text.rei.working_station").method_27692(Formatting.YELLOW));
            for (List<EntryStack> workingStation : workingStations) {
                widgets.add(new RecipeViewingScreen.WorkstationSlotWidget(xx, yy, CollectionUtils.map(workingStation, stack -> stack.copy().setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> list))));
                index++;
                xx += 16;
                if (index >= ww) {
                    index = 0;
                    xx = bounds.x + 16;
                    yy += 16;
                }
            }
        }
        
        this.widgets.add(Widgets.createCategoryBase(bounds));
        this.scrollListBounds = new Rectangle(bounds.x + 4, bounds.y + 17, 97 + 5, guiHeight - 17 - 7);
        this.widgets.add(Widgets.createSlotBase(scrollListBounds));
        
        Rectangle recipeBounds = new Rectangle(bounds.x + 100 + (guiWidth - 100) / 2 - category.getDisplayWidth(display) / 2, bounds.y + bounds.height / 2 - category.getDisplayHeight() / 2, category.getDisplayWidth(display), category.getDisplayHeight());
        List<Widget> setupDisplay = category.setupDisplay(display, recipeBounds);
        RecipeViewingScreen.transformIngredientNotice(setupDisplay, ingredientStackToNotice);
        RecipeViewingScreen.transformResultNotice(setupDisplay, resultStackToNotice);
        this.widgets.addAll(setupDisplay);
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getAutoCraftButtonArea(category);
        if (supplier.isPresent() && supplier.get().get(recipeBounds) != null)
            this.widgets.add(InternalWidgets.createAutoCraftingButtonWidget(recipeBounds, supplier.get().get(recipeBounds), new LiteralText(supplier.get().getButtonText()), () -> display, setupDisplay, category));
        
        int index = 0;
        for (RecipeDisplay recipeDisplay : categoryMap.get(category)) {
            int finalIndex = index;
            RecipeEntry recipeEntry;
            recipeRenderers.add(recipeEntry = category.getSimpleRenderer(recipeDisplay));
            buttonList.add(Widgets.createButton(new Rectangle(bounds.x + 5, 0, recipeEntry.getWidth(), recipeEntry.getHeight()), NarratorManager.EMPTY)
                    .onClick(button -> {
                        selectedRecipeIndex = finalIndex;
                        VillagerRecipeViewingScreen.this.init();
                    })
                    .containsMousePredicate((button, point) -> {
                        return (button.getBounds().contains(point) && scrollListBounds.contains(point)) || button.isFocused();
                    })
                    .onRender((matrices, button) -> button.setEnabled(selectedRecipeIndex != finalIndex)));
            index++;
        }
        int tabV = isCompactTabs ? 166 : 192;
        for (int i = 0; i < tabsPerPage; i++) {
            int j = i + tabsPage * tabsPerPage;
            if (categories.size() > j) {
                RecipeCategory<?> tabCategory = categories.get(j);
                TabWidget tab;
                tabs.add(tab = TabWidget.create(i, tabSize, bounds.x + bounds.width / 2 - Math.min(categories.size() - tabsPage * tabsPerPage, tabsPerPage) * tabSize / 2, bounds.y, 0, tabV, widget -> {
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    if (widget.selected)
                        return false;
                    ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, tabCategory.getIdentifier(), ingredientStackToNotice, resultStackToNotice);
                    return true;
                }));
                tab.setRenderer(tabCategory, tabCategory.getLogo(), tabCategory.getCategoryName(), j == selectedCategoryIndex);
            }
        }
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x + 2, bounds.y - 16, 10, 10), new TranslatableText("text.rei.left_arrow"))
                .onClick(button -> {
                    tabsPage--;
                    if (tabsPage < 0)
                        tabsPage = MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1;
                    VillagerRecipeViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        this.widgets.add(Widgets.createButton(new Rectangle(bounds.x + bounds.width - 12, bounds.y - 16, 10, 10), new TranslatableText("text.rei.right_arrow"))
                .onClick(button -> {
                    tabsPage++;
                    if (tabsPage > MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1)
                        tabsPage = 0;
                    VillagerRecipeViewingScreen.this.init();
                })
                .enabled(categories.size() > tabsPerPage));
        
        this.widgets.add(Widgets.createClickableLabel(new Point(bounds.x + 4 + scrollListBounds.width / 2, bounds.y + 6), new LiteralText(categories.get(selectedCategoryIndex).getCategoryName()), label -> {
            ClientHelper.getInstance().executeViewAllRecipesKeyBind();
        }).tooltipLine(I18n.translate("text.rei.view_all_categories")).noShadow().color(0xFF404040, 0xFFBBBBBB).hoveredColor(0xFF0041FF, 0xFFFFBD4D));
        
        this.children.addAll(buttonList);
        this.widgets.addAll(tabs);
        this.children.addAll(widgets);
        this.children.add(ScreenHelper.getLastOverlay(true, false));
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int int_1) {
        if (scrolling.updateDraggingState(mouseX, mouseY, int_1)) {
            scrollBarAlpha = 1;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, int_1);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        double height = scrolling.getMaxScrollHeight();
        if (scrollListBounds.contains(double_1, double_2) && height > scrollListBounds.height - 2) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            if (scrollBarAlphaFuture == 0)
                scrollBarAlphaFuture = 1f;
            if (System.currentTimeMillis() - scrollBarAlphaFutureTime > 300f)
                scrollBarAlphaFutureTime = System.currentTimeMillis();
            return true;
        }
        for (Element listener : children())
            if (listener.mouseScrolled(double_1, double_2, double_3))
                return true;
        if (bounds.contains(PointHelper.ofMouse())) {
            if (double_3 < 0 && categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
            } else if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (ConfigObject.getInstance().doesVillagerScreenHavePermanentScrollBar()) {
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            scrollBarAlphaFuture = 0;
            scrollBarAlpha = 1;
        } else if (scrollBarAlphaFutureTime > 0) {
            long l = System.currentTimeMillis() - scrollBarAlphaFutureTime;
            if (l > 300f) {
                if (scrollBarAlphaFutureTime == 0) {
                    scrollBarAlpha = scrollBarAlphaFuture;
                    scrollBarAlphaFutureTime = -1;
                } else if (l > 2000f && scrollBarAlphaFuture == 1) {
                    scrollBarAlphaFuture = 0;
                    scrollBarAlphaFutureTime = System.currentTimeMillis();
                } else
                    scrollBarAlpha = scrollBarAlphaFuture;
            } else {
                if (scrollBarAlphaFuture == 0)
                    scrollBarAlpha = Math.min(scrollBarAlpha, 1 - Math.min(1f, l / 300f));
                else if (scrollBarAlphaFuture == 1)
                    scrollBarAlpha = Math.max(Math.min(1f, l / 300f), scrollBarAlpha);
            }
        }
        scrolling.updatePosition(delta);
        this.fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
        int yOffset = 0;
        for (Widget widget : widgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
        ScreenHelper.getLastOverlay().render(matrices, mouseX, mouseY, delta);
        RenderSystem.pushMatrix();
        ScissorsHandler.INSTANCE.scissor(scrolling.getBounds());
        for (Button button : buttonList) {
            button.getBounds().y = scrollListBounds.y + 1 + yOffset - (int) scrolling.scrollAmount;
            if (button.getBounds().getMaxY() > scrollListBounds.getMinY() && button.getBounds().getMinY() < scrollListBounds.getMaxY()) {
                button.render(matrices, mouseX, mouseY, delta);
            }
            yOffset += button.getBounds().height;
        }
        for (int i = 0; i < buttonList.size(); i++) {
            if (buttonList.get(i).getBounds().getMaxY() > scrollListBounds.getMinY() && buttonList.get(i).getBounds().getMinY() < scrollListBounds.getMaxY()) {
                recipeRenderers.get(i).setZ(1);
                recipeRenderers.get(i).render(matrices, buttonList.get(i).getBounds(), mouseX, mouseY, delta);
                Optional.ofNullable(recipeRenderers.get(i).getTooltip(new Point(mouseX, mouseY))).ifPresent(Tooltip::queue);
            }
        }
        scrolling.renderScrollBar(0, scrollBarAlpha);
        ScissorsHandler.INSTANCE.removeLastScissor();
        RenderSystem.popMatrix();
        ScreenHelper.getLastOverlay().lateRender(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (scrolling.mouseDragged(mouseX, mouseY, int_1, double_3, double_4)) {
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            scrollBarAlphaFuture = 1f;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 258) {
            boolean boolean_1 = !hasShiftDown();
            if (!this.changeFocus(boolean_1))
                this.changeFocus(boolean_1);
            return true;
        }
        if (ConfigObject.getInstance().getNextPageKeybind().matchesKey(int_1, int_2)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
                return true;
            }
            return false;
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(int_1, int_2)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
            return false;
        }
        for (Element element : children())
            if (element.keyPressed(int_1, int_2, int_3))
                return true;
        if (int_1 == 256 || this.client.options.keyInventory.matchesKey(int_1, int_2)) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastHandledScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        if (int_1 == 259) {
            if (ScreenHelper.hasLastRecipeScreen())
                client.openScreen(ScreenHelper.getLastRecipeScreen());
            else
                client.openScreen(ScreenHelper.getLastHandledScreen());
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
}
