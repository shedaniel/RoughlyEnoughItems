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

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.category.ButtonArea;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.InternalWidgets;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public class CompositeDisplayViewingScreen extends AbstractDisplayViewingScreen {
    private final List<Widget> widgets = Lists.newArrayList();
    private final List<Button> buttonList = Lists.newArrayList();
    private final List<DisplayRenderer> displayRenderers = Lists.newArrayList();
    public Rectangle scrollListBounds;
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
    
    public CompositeDisplayViewingScreen(Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap, @Nullable CategoryIdentifier<?> category) {
        super(categoryMap, category);
    }
    
    @Override
    public void recalculateCategoryPage() {
        super.recalculateCategoryPage();
        this.selectedRecipeIndex = 0;
    }
    
    @Override
    public void init() {
        super.init();
        this.children().clear();
        this.widgets.clear();
        this.buttonList.clear();
        this.displayRenderers.clear();
        int largestWidth = width - 100;
        int largestHeight = height - 40;
        DisplayCategory<Display> category = getCurrentCategory();
        DisplaySpec display = categoryMap.get(category).get(selectedRecipeIndex);
        int guiWidth = Mth.clamp(category.getDisplayWidth(display.provideInternalDisplay()) + 30, 0, largestWidth) + 100;
        int guiHeight = Mth.clamp(category.getDisplayHeight() + 40, 166, largestHeight);
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        this.initTabs(this.bounds.width);
        this.widgets.addAll(this.tabs.widgets());
        
        List<EntryIngredient> workstations = CategoryRegistry.getInstance().get(category.getCategoryIdentifier()).getWorkstations();
        if (!workstations.isEmpty()) {
            int ww = Mth.floor((bounds.width - 16) / 18f);
            int w = Math.min(ww, workstations.size());
            int h = Mth.ceil(workstations.size() / ((float) ww));
            int xx = bounds.x + 16;
            int yy = bounds.y + bounds.height + 2;
            widgets.add(Widgets.createCategoryBase(new Rectangle(xx - 5, bounds.y + bounds.height - 5, 10 + w * 16, 12 + h * 16)));
            widgets.add(Widgets.createSlotBase(new Rectangle(xx - 1, yy - 1, 2 + w * 16, 2 + h * 16)));
            int index = 0;
            for (EntryIngredient workingStation : workstations) {
                widgets.add(new DefaultDisplayViewingScreen.WorkstationSlotWidget(xx, yy, workingStation));
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
        
        Rectangle recipeBounds = new Rectangle(bounds.x + 100 + (guiWidth - 100) / 2 - category.getDisplayWidth(display.provideInternalDisplay()) / 2, bounds.y + bounds.height / 2 - category.getDisplayHeight() / 2, category.getDisplayWidth(display.provideInternalDisplay()), category.getDisplayHeight());
        List<Widget> setupDisplay;
        try {
            setupDisplay = getCurrentCategoryView(display.provideInternalDisplay()).setupDisplay(display.provideInternalDisplay(), recipeBounds);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            setupDisplay = new ArrayList<>();
            setupDisplay.add(Widgets.createRecipeBase(recipeBounds).color(0xFFBB0000));
            setupDisplay.add(Widgets.createLabel(new Point(recipeBounds.getCenterX(), recipeBounds.getCenterY() - 8), Component.literal("Failed to initiate setupDisplay")));
            setupDisplay.add(Widgets.createLabel(new Point(recipeBounds.getCenterX(), recipeBounds.getCenterY() + 1), Component.literal("Check console for error")));
        }
        setupTags(setupDisplay);
        transformFiltering(setupDisplay);
        transformIngredientNotice(setupDisplay, ingredientStackToNotice);
        transformResultNotice(setupDisplay, resultStackToNotice);
        for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets, EntryWidget.class::isInstance)) {
            widget.removeTagMatch = true;
        }
        this.widgets.addAll(setupDisplay);
        Optional<ButtonArea> supplier = CategoryRegistry.getInstance().get(category.getCategoryIdentifier()).getPlusButtonArea();
        if (supplier.isPresent() && supplier.get().get(recipeBounds) != null)
            this.widgets.add(Widgets.withTranslate(InternalWidgets.createAutoCraftingButtonWidget(recipeBounds, supplier.get().get(recipeBounds), Component.literal(supplier.get().getButtonText()), display::provideInternalDisplay, display::provideInternalDisplayIds, setupDisplay, category), 0, 0, 100));
        
        int index = 0;
        for (DisplaySpec recipeDisplay : categoryMap.get(category)) {
            int finalIndex = index;
            DisplayRenderer displayRenderer;
            displayRenderers.add(displayRenderer = getCurrentCategoryView(recipeDisplay.provideInternalDisplay()).getDisplayRenderer(recipeDisplay.provideInternalDisplay()));
            buttonList.add(Widgets.createButton(new Rectangle(bounds.x + 5, 0, displayRenderer.getWidth(), displayRenderer.getHeight()), Component.empty())
                    .onClick(button -> {
                        selectedRecipeIndex = finalIndex;
                        CompositeDisplayViewingScreen.this.init();
                    })
                    .containsMousePredicate((button, point) -> {
                        return (button.getBounds().contains(point) && scrollListBounds.contains(point)) || button.isFocused();
                    })
                    .onRender((matrices, button) -> button.setEnabled(selectedRecipeIndex != finalIndex)));
            index++;
        }
        
        this.widgets.add(Widgets.createClickableLabel(new Point(bounds.x + 4 + scrollListBounds.width / 2, bounds.y + 6), categories.get(selectedCategoryIndex).getTitle(), label -> {
            ViewSearchBuilder.builder().addAllCategories().open();
        }).tooltip(Component.translatable("text.rei.view_all_categories")).noShadow().color(0xFF404040, 0xFFBBBBBB).hoveredColor(0xFF0041FF, 0xFFFFBD4D));
        
        this.children().addAll(buttonList);
        this.children().addAll(widgets);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (scrolling.updateDraggingState(mouseX, mouseY, button)) {
            scrollBarAlpha = 1;
            return true;
        }
        if (ConfigObject.getInstance().getNextPageKeybind().matchesMouse(button)) {
            selectedRecipeIndex++;
            if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                selectedRecipeIndex = 0;
            init();
            return true;
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesMouse(button)) {
            selectedRecipeIndex--;
            if (selectedRecipeIndex < 0)
                selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
            init();
            return true;
        } else if (ConfigObject.getInstance().getPreviousScreenKeybind().matchesMouse(button)) {
            if (REIRuntimeImpl.getInstance().hasLastDisplayScreen()) {
                minecraft.setScreen(REIRuntimeImpl.getInstance().getLastDisplayScreen());
            } else {
                minecraft.setScreen(REIRuntime.getInstance().getPreviousScreen());
            }
            return true;
        }
        for (GuiEventListener entry : children())
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                setFocused(entry);
                if (button == 0)
                    setDragging(true);
                return true;
            }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (GuiEventListener listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        double height = scrolling.getMaxScrollHeight();
        if (scrollListBounds.contains(mouseX, mouseY) && height > scrollListBounds.height - 2) {
            scrolling.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
            if (scrollBarAlphaFuture == 0)
                scrollBarAlphaFuture = 1f;
            if (System.currentTimeMillis() - scrollBarAlphaFutureTime > 300f)
                scrollBarAlphaFutureTime = System.currentTimeMillis();
            return true;
        }
        REIRuntimeImpl.isWithinRecipeViewingScreen = true;
        for (GuiEventListener listener : children()) {
            if (listener.mouseScrolled(mouseX, mouseY, amount)) {
                REIRuntimeImpl.isWithinRecipeViewingScreen = false;
                return true;
            }
        }
        REIRuntimeImpl.isWithinRecipeViewingScreen = false;
        if (bounds.contains(PointHelper.ofMouse())) {
            if (amount < 0 && categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
                return true;
            } else if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (ConfigObject.getInstance().isCompositeScrollBarPermanent()) {
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
        renderBackground(matrices);
        int yOffset = 0;
        getOverlay().render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            widget.render(matrices, mouseX, mouseY, delta);
        }
        matrices.pushPose();
        ScissorsHandler.INSTANCE.scissor(scrolling.getBounds());
        for (Button button : buttonList) {
            button.getBounds().y = scrollListBounds.y + 1 + yOffset - scrolling.scrollAmountInt();
            if (button.getBounds().getMaxY() > scrollListBounds.getMinY() && button.getBounds().getMinY() < scrollListBounds.getMaxY()) {
                button.render(matrices, mouseX, mouseY, delta);
            }
            yOffset += button.getBounds().height;
        }
        for (int i = 0; i < buttonList.size(); i++) {
            if (buttonList.get(i).getBounds().getMaxY() > scrollListBounds.getMinY() && buttonList.get(i).getBounds().getMinY() < scrollListBounds.getMaxY()) {
                displayRenderers.get(i).setZ(1);
                displayRenderers.get(i).render(matrices, buttonList.get(i).getBounds(), mouseX, mouseY, delta);
                Optional.ofNullable(displayRenderers.get(i).getTooltip(TooltipContext.of(new Point(mouseX, mouseY)))).ifPresent(Tooltip::queue);
            }
        }
        scrolling.renderScrollBar(0, scrollBarAlpha, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
        ScissorsHandler.INSTANCE.removeLastScissor();
        matrices.popPose();
    }
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        for (GuiEventListener entry : children())
            if (entry.mouseReleased(double_1, double_2, int_1))
                return true;
        return super.mouseReleased(double_1, double_2, int_1);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (scrolling.mouseDragged(mouseX, mouseY, int_1, double_3, double_4)) {
            scrollBarAlphaFutureTime = System.currentTimeMillis();
            scrollBarAlphaFuture = 1f;
            return true;
        }
        for (GuiEventListener entry : children())
            if (entry.mouseDragged(mouseX, mouseY, int_1, double_3, double_4))
                return true;
        return super.mouseDragged(mouseX, mouseY, int_1, double_3, double_4);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258 && !minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            boolean boolean_1 = !hasShiftDown();
            if (!this.changeFocus(boolean_1))
                this.changeFocus(boolean_1);
            return true;
        }
        if (ConfigObject.getInstance().getNextPageKeybind().matchesKey(keyCode, scanCode)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex++;
                if (selectedRecipeIndex >= categoryMap.get(categories.get(selectedCategoryIndex)).size())
                    selectedRecipeIndex = 0;
                init();
                return true;
            }
            return false;
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(keyCode, scanCode)) {
            if (categoryMap.get(categories.get(selectedCategoryIndex)).size() > 1) {
                selectedRecipeIndex--;
                if (selectedRecipeIndex < 0)
                    selectedRecipeIndex = categoryMap.get(categories.get(selectedCategoryIndex)).size() - 1;
                init();
                return true;
            }
            return false;
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
}
