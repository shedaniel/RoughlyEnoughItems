/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.Pair;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
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
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.RecipeDisplayExporter;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.toast.ExportRecipeIdentifierToast;
import me.shedaniel.rei.impl.client.gui.widget.*;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.PanelWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class DefaultDisplayViewingScreen extends AbstractDisplayViewingScreen {
    private static final int INNER_PADDING_Y = 36;
    private static final int OUTER_PADDING_TOP = 2;
    private static final int OUTER_PADDING_BOTTOM = 2;
    private static final int DISPLAY_GAP = 4;
    private final Map<Rectangle, Pair<DisplaySpec, List<Widget>>> recipeBounds = Maps.newHashMap();
    private List<Widget> widgets = Lists.newArrayList();
    public int page;
    @Nullable
    private Panel workingStationsBaseWidget;
    private Button recipeBack, recipeNext, categoryBack, categoryNext;
    private final int bestWidthDisplay;
    
    public DefaultDisplayViewingScreen(Map<DisplayCategory<?>, List<DisplaySpec>> categoriesMap, @Nullable CategoryIdentifier<?> category) {
        super(categoriesMap, category);
        this.bounds = new Rectangle(0, 0, 176, 150);
        //noinspection RedundantCast
        List<Integer> list = CollectionUtils.mapAndFilter(categoriesMap.entrySet(), Objects::nonNull, entry -> ((Optional<Integer>) CollectionUtils.<DisplaySpec, Integer>mapAndMax(entry.getValue(),
                display -> ((DisplayCategory<Display>) entry.getKey()).getDisplayWidth(display.provideInternalDisplay()), Comparator.naturalOrder())).orElse(null));
        list.sort(Comparator.naturalOrder());
        int mode = list.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(150);
        int median = list.size() % 2 == 0 ? (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2 : list.get(list.size() / 2);
        this.bestWidthDisplay = (int) Math.round((mode * 0.5 + median * 1.5) / 2.0);
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
        if (keyCode == 256) {
            Minecraft.getInstance().setScreen(REIRuntime.getInstance().getPreviousScreen());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void init() {
        super.init();
        this.children().clear();
        this.recipeBounds.clear();
        this.widgets.clear();
//        int maxWidthDisplay = CollectionUtils.<DisplaySpec, Integer>mapAndMax(getCurrentDisplayed(), display -> getCurrentCategory().getDisplayWidth(display.provideInternalDisplay()), Comparator.naturalOrder()).orElse(150);
//        int guiWidth = Math.max(maxWidthDisplay + 10 + 14 + 14, 190);
        int guiWidth = Math.max(bestWidthDisplay + 10 + 14 + 14, 190);
        this.tabs.initTabsSize(guiWidth);
        
        int topMargin = OUTER_PADDING_TOP + this.tabs.tabSize() - 2 + (categories.size() > this.tabs.tabsPerPage() ? 16 : 0);
        int bottomMargin = OUTER_PADDING_BOTTOM + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.CENTER ? 22 : 0);
        int largestHeight = Math.min(Math.max(height - topMargin - bottomMargin, 100), ConfigObject.getInstance().getMaxRecipesPageHeight());
        int maxHeight = Math.min(largestHeight, CollectionUtils.<DisplayCategory<?>, Integer>mapAndMax(categories,
                category -> INNER_PADDING_Y + (category.getDisplayHeight() + DISPLAY_GAP) * Math.max(1, Math.min(getRecipesPerPage(largestHeight, category) + 1, Math.max(categoryMap.get(category).size(), ConfigObject.getInstance().getMaxRecipePerPage()))), Comparator.naturalOrder()).orElse(66));
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, topMargin + (height - topMargin - bottomMargin) / 2 - maxHeight / 2, guiWidth, maxHeight);
        
        this.initTabs(guiWidth);
        this.widgets.addAll(this.tabs.widgets());
        
        this.page = Mth.clamp(page, 0, getCurrentTotalPages() - 1);
        this.widgets.add(categoryBack = Widgets.createButton(new Rectangle(bounds.getCenterX() - guiWidth / 2 + 5, bounds.getY() + 5, 12, 12), ImmutableTextComponent.EMPTY)
                .onClick(button -> previousCategory()).tooltipLine(new TranslatableComponent("text.rei.previous_category")));
        this.widgets.add(Widgets.createClickableLabel(new Point(bounds.getCenterX(), bounds.getY() + 7), getCurrentCategory().getTitle(), clickableLabelWidget -> {
            ViewSearchBuilder.builder().addAllCategories().open();
        }).tooltip(new TranslatableComponent("text.rei.view_all_categories")));
        this.widgets.add(categoryNext = Widgets.createButton(new Rectangle(bounds.getCenterX() + guiWidth / 2 - 17, bounds.getY() + 5, 12, 12), ImmutableTextComponent.EMPTY)
                .onClick(button -> nextCategory()).tooltipLine(new TranslatableComponent("text.rei.next_category")));
        this.categoryBack.setEnabled(categories.size() > 1);
        this.categoryNext.setEnabled(categories.size() > 1);
        this.widgets.add(Widgets.withTranslate(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Rectangle recipeBackBounds = recipeBack.getBounds();
            Rectangle recipeNextBounds = recipeNext.getBounds();
            Rectangle categoryBackBounds = categoryBack.getBounds();
            Rectangle categoryNextBounds = categoryNext.getBounds();
            matrices.pushPose();
            matrices.translate(0.5, 0.5, 0);
            RenderSystem.setShaderTexture(0, InternalTextures.ARROW_LEFT_TEXTURE);
            blit(matrices, recipeBackBounds.x + 2, recipeBackBounds.y + 2, 0, 0, 8, 8, 8, 8);
            blit(matrices, categoryBackBounds.x + 2, categoryBackBounds.y + 2, 0, 0, 8, 8, 8, 8);
            matrices.translate(-0.5, 0, 0);
            RenderSystem.setShaderTexture(0, InternalTextures.ARROW_RIGHT_TEXTURE);
            blit(matrices, recipeNextBounds.x + 2, recipeNextBounds.y + 2, 0, 0, 8, 8, 8, 8);
            blit(matrices, categoryNextBounds.x + 2, categoryNextBounds.y + 2, 0, 0, 8, 8, 8, 8);
            matrices.popPose();
        }), 0, 0, 1));
        
        this.widgets.add(recipeBack = Widgets.createButton(new Rectangle(bounds.getCenterX() - guiWidth / 2 + 5, bounds.getY() + 19, 12, 12), ImmutableTextComponent.EMPTY)
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
        this.widgets.add(recipeNext = Widgets.createButton(new Rectangle(bounds.getCenterX() + guiWidth / 2 - 17, bounds.getY() + 19, 12, 12), ImmutableTextComponent.EMPTY)
                .onClick(button -> {
                    page++;
                    if (page >= getCurrentTotalPages())
                        page = 0;
                    DefaultDisplayViewingScreen.this.init();
                }).tooltipLine(new TranslatableComponent("text.rei.next_page")));
        this.recipeBack.setEnabled(getCurrentTotalPages() > 1);
        this.recipeNext.setEnabled(getCurrentTotalPages() > 1);
        initDisplays();
        widgets = CollectionUtils.map(widgets, widget -> Widgets.withTranslate(widget, 0, 0, 10));
        widgets.add(Widgets.withTranslate(new PanelWidget(bounds), 0, 0, 5));
        widgets.add(Widgets.withTranslate(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            fill(matrices, bounds.getCenterX() - guiWidth / 2 + 17, bounds.y + 5, bounds.getCenterX() + guiWidth / 2 - 17, bounds.y + 17, darkStripesColor.value().getColor());
            fill(matrices, bounds.getCenterX() - guiWidth / 2 + 17, bounds.y + 19, bounds.getCenterX() + guiWidth / 2 - 17, bounds.y + 31, darkStripesColor.value().getColor());
        }), 0, 0, 6));
        initWorkstations(widgets);
        
        children().addAll(widgets);
    }
    
    private void initDisplays() {
        Optional<ButtonArea> plusButtonArea = CategoryRegistry.getInstance().get(getCurrentCategoryId()).getPlusButtonArea();
        int displayHeight = getCurrentCategory().getDisplayHeight();
        List<DisplaySpec> currentDisplayed = getCurrentDisplayed();
        for (int i = 0; i < currentDisplayed.size(); i++) {
            final DisplaySpec display = currentDisplayed.get(i);
            final Supplier<Display> displaySupplier = display::provideInternalDisplay;
            int displayWidth = getCurrentCategory().getDisplayWidth(displaySupplier.get());
            final Rectangle displayBounds = new Rectangle(getBounds().getCenterX() - displayWidth / 2, getBounds().getCenterY() + 16 - displayHeight * (getRecipesPerPage() + 1) / 2 - 2 * (getRecipesPerPage() + 1) + displayHeight * i + DISPLAY_GAP * i, displayWidth, displayHeight);
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
            transformFiltering(setupDisplay);
            transformIngredientNotice(setupDisplay, ingredientStackToNotice);
            transformResultNotice(setupDisplay, resultStackToNotice);
            unifyIngredients(setupDisplay);
            for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets, EntryWidget.class::isInstance)) {
                widget.removeTagMatch = true;
            }
            this.recipeBounds.put(displayBounds, Pair.of(display, setupDisplay));
            this.widgets.add(new DisplayCompositeWidget(display, setupDisplay, displayBounds));
            if (plusButtonArea.isPresent()) {
                this.widgets.add(Widgets.withTranslate(InternalWidgets.createAutoCraftingButtonWidget(displayBounds, plusButtonArea.get().get(displayBounds), new TextComponent(plusButtonArea.get().getButtonText()), displaySupplier, display::provideInternalDisplayIds, setupDisplay, getCurrentCategory()), 0, 0, 100));
            }
        }
    }
    
    private void initWorkstations(List<Widget> widgets) {
        workingStationsBaseWidget = null;
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(getCurrentCategoryId()).getWorkstations();
        if (!workstations.isEmpty()) {
            int hh = Mth.floor((bounds.height - 16) / 18f);
            int actualHeight = Math.min(hh, workstations.size());
            int innerWidth = Mth.ceil(workstations.size() / ((float) hh));
            int xx = bounds.x - (8 + innerWidth * 16) + 6;
            int yy = bounds.y + 16;
            widgets.add(workingStationsBaseWidget = Widgets.createCategoryBase(new Rectangle(xx - 5, yy - 5, 15 + innerWidth * 16, 10 + actualHeight * 16)));
            widgets.add(Widgets.createSlotBase(new Rectangle(xx - 1, yy - 1, innerWidth * 16 + 2, actualHeight * 16 + 2)));
            int index = 0;
            xx += (innerWidth - 1) * 16;
            for (EntryIngredient workingStation : workstations) {
                widgets.add(new WorkstationSlotWidget(xx, yy, workingStation));
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
    
    public List<Widget> widgets() {
        widgets.sort(Comparator.comparingDouble(Widget::getZRenderingPriority));
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
        return Mth.clamp(Mth.floor(((double) totalHeight - INNER_PADDING_Y) / ((double) height + DISPLAY_GAP)) - 1, 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, category.getMaximumDisplaysPerPage() - 1));
    }
    
    private final ValueAnimator<Color> darkStripesColor = ValueAnimator.ofColor()
            .withConvention(() -> Color.ofTransparent(REIRuntime.getInstance().isDarkThemeEnabled() ? 0xFF404040 : 0xFF9E9E9E), ValueAnimator.typicalTransitionTime());
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        darkStripesColor.update(delta);
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        getOverlay().render(matrices, mouseX, mouseY, delta);
        for (Widget widget : widgets()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            widget.render(matrices, mouseX, mouseY, delta);
        }
        {
            ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
            if (export.matchesCurrentKey() || export.matchesCurrentMouse()) {
                for (Rectangle bounds : Iterables.concat(recipeBounds.keySet(), Iterables.transform(getTabs(), TabWidget::getBounds))) {
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
    
    private Iterable<TabWidget> getTabs() {
        return Widgets.walk(widgets(), widget -> widget instanceof TabWidget);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesKey(keyCode, scanCode)) {
            if (checkExportDisplays()) return true;
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
    public boolean charTyped(char character, int modifiers) {
        for (GuiEventListener listener : children())
            if (listener.charTyped(character, modifiers))
                return true;
        return super.charTyped(character, modifiers);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (GuiEventListener entry : children())
            if (entry.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
                return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ModifierKeyCode export = ConfigObject.getInstance().getExportImageKeybind();
        if (export.matchesMouse(button)) {
            if (checkExportDisplays()) return true;
        }
        for (GuiEventListener entry : children())
            if (entry.mouseReleased(mouseX, mouseY, button))
                return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    private boolean checkExportDisplays() {
        for (Map.Entry<Rectangle, Pair<DisplaySpec, List<Widget>>> entry : recipeBounds.entrySet()) {
            Rectangle bounds = entry.getKey();
            if (bounds.contains(PointHelper.ofMouse())) {
                RecipeDisplayExporter.exportRecipeDisplay(bounds, entry.getValue().left(), entry.getValue().right(), true);
                return true;
            }
        }
        for (TabWidget tab : getTabs()) {
            Rectangle bounds = tab.getBounds();
            if (bounds.contains(PointHelper.ofMouse())) {
                minecraft.setScreen(new ConfirmScreen(confirmed -> {
                    if (confirmed) {
                        for (DisplaySpec spec : categoryMap.getOrDefault(tab.category, Collections.emptyList())) {
                            Display display = spec.provideInternalDisplay();
                            int displayWidth = getCurrentCategory().getDisplayWidth(display);
                            int displayHeight = getCurrentCategory().getDisplayHeight();
                            final Rectangle displayBounds = new Rectangle(0, 0, displayWidth, displayHeight);
                            List<Widget> setupDisplay;
                            try {
                                setupDisplay = getCurrentCategoryView(display).setupDisplay(display, displayBounds);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                                setupDisplay = new ArrayList<>();
                                setupDisplay.add(Widgets.createRecipeBase(displayBounds).color(0xFFBB0000));
                                setupDisplay.add(Widgets.createLabel(new Point(displayBounds.getCenterX(), displayBounds.getCenterY() - 8), new TextComponent("Failed to initiate setupDisplay")));
                                setupDisplay.add(Widgets.createLabel(new Point(displayBounds.getCenterX(), displayBounds.getCenterY() + 1), new TextComponent("Check console for error")));
                            }
                            setupTags(setupDisplay);
                            transformFiltering(setupDisplay);
                            transformIngredientNotice(setupDisplay, ingredientStackToNotice);
                            transformResultNotice(setupDisplay, resultStackToNotice);
                            unifyIngredients(setupDisplay);
                            for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets(), EntryWidget.class::isInstance)) {
                                widget.removeTagMatch = true;
                            }
                            
                            RecipeDisplayExporter.exportRecipeDisplay(displayBounds, spec, setupDisplay, false);
                        }
                        ExportRecipeIdentifierToast.addToast(I18n.get("msg.rei.exported_recipe"), I18n.get("msg.rei.exported_recipe.desc"));
                    }
                    minecraft.setScreen(null);
                }, new TranslatableComponent("text.rei.ask_to_export", tab.categoryName),
                        new TranslatableComponent("text.rei.ask_to_export.subtitle", categoryMap.getOrDefault(tab.category, Collections.emptyList()).size())));
            }
        }
        return false;
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
