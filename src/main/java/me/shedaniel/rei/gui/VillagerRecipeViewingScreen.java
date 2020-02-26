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
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApiStatus.Internal
public class VillagerRecipeViewingScreen extends Screen implements StackToNoticeScreen {
    
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoryMap;
    private final List<RecipeCategory<?>> categories;
    private final List<Widget> widgets;
    private final List<ButtonWidget> buttonWidgets;
    private final List<RecipeEntry> recipeRenderers;
    private final List<TabWidget> tabs;
    public Rectangle bounds, scrollListBounds;
    private int tabsPerPage = 8;
    private int selectedCategoryIndex, selectedRecipeIndex;
    private double scroll;
    private double target;
    private long start;
    private long duration;
    private float scrollBarAlpha;
    private float scrollBarAlphaFuture;
    private long scrollBarAlphaFutureTime = -1;
    private boolean draggingScrollBar;
    private int tabsPage;
    private EntryStack ingredientStackToNotice = EntryStack.empty();
    private EntryStack resultStackToNotice = EntryStack.empty();
    
    public VillagerRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        super(new LiteralText(""));
        this.widgets = Lists.newArrayList();
        this.categoryMap = Maps.newLinkedHashMap();
        this.selectedCategoryIndex = 0;
        this.selectedRecipeIndex = 0;
        this.scrollBarAlpha = 0;
        this.scrollBarAlphaFuture = 0;
        this.scroll = 0;
        this.draggingScrollBar = false;
        this.tabsPage = 0;
        this.categories = Lists.newArrayList();
        this.buttonWidgets = Lists.newArrayList();
        this.tabs = Lists.newArrayList();
        this.recipeRenderers = Lists.newArrayList();
        RecipeHelper.getInstance().getAllCategories().forEach(category -> {
            if (map.containsKey(category)) {
                categories.add(category);
                categoryMap.put(category, map.get(category));
            }
        });
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
    protected void init() {
        super.init();
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        this.draggingScrollBar = false;
        this.children.clear();
        this.widgets.clear();
        this.buttonWidgets.clear();
        this.recipeRenderers.clear();
        this.tabs.clear();
        int largestWidth = width - 100;
        int largestHeight = height - 40;
        RecipeCategory<RecipeDisplay> category = (RecipeCategory<RecipeDisplay>) categories.get(selectedCategoryIndex);
        RecipeDisplay display = categoryMap.get(category).get(selectedRecipeIndex);
        int guiWidth = MathHelper.clamp(category.getDisplayWidth(display) + 30, 0, largestWidth) + 100;
        int guiHeight = MathHelper.clamp(category.getDisplayHeight() + 40, 166, largestHeight);
        this.tabsPerPage = Math.max(5, MathHelper.floor((guiWidth - 20d) / tabSize));
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        
        List<List<EntryStack>> workingStations = RecipeHelper.getInstance().getWorkingStations(category.getIdentifier());
        if (!workingStations.isEmpty()) {
            int ww = MathHelper.floor((bounds.width - 16) / 18f);
            int w = Math.min(ww, workingStations.size());
            int h = MathHelper.ceil(workingStations.size() / ((float) ww));
            int xx = bounds.x + 16;
            int yy = bounds.y + bounds.height + 2;
            widgets.add(new CategoryBaseWidget(new Rectangle(xx - 5, bounds.y + bounds.height - 5, 10 + w * 16, 12 + h * 16)));
            widgets.add(new SlotBaseWidget(new Rectangle(xx - 1, yy - 1, 2 + w * 16, 2 + h * 16)));
            int index = 0;
            List<String> list = Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("text.rei.working_station"));
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
        
        this.widgets.add(new CategoryBaseWidget(bounds));
        this.scrollListBounds = new Rectangle(bounds.x + 4, bounds.y + 17, 97 + 5, guiHeight - 17 - 7);
        this.widgets.add(new SlotBaseWidget(scrollListBounds));
        
        Rectangle recipeBounds = new Rectangle(bounds.x + 100 + (guiWidth - 100) / 2 - category.getDisplayWidth(display) / 2, bounds.y + bounds.height / 2 - category.getDisplayHeight() / 2, category.getDisplayWidth(display), category.getDisplayHeight());
        List<Widget> setupDisplay = category.setupDisplay(() -> display, recipeBounds);
        RecipeViewingScreen.transformIngredientNotice(setupDisplay, ingredientStackToNotice);
        RecipeViewingScreen.transformResultNotice(setupDisplay, resultStackToNotice);
        this.widgets.addAll(setupDisplay);
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getAutoCraftButtonArea(category);
        if (supplier.isPresent() && supplier.get().get(recipeBounds) != null)
            this.widgets.add(new AutoCraftingButtonWidget(recipeBounds, supplier.get().get(recipeBounds), supplier.get().getButtonText(), () -> display, setupDisplay, category));
        
        int index = 0;
        for (RecipeDisplay recipeDisplay : categoryMap.get(category)) {
            int finalIndex = index;
            RecipeEntry recipeEntry;
            recipeRenderers.add(recipeEntry = category.getSimpleRenderer(recipeDisplay));
            buttonWidgets.add(new ButtonWidget(new Rectangle(bounds.x + 5, 0, recipeEntry.getWidth(), recipeEntry.getHeight()), NarratorManager.EMPTY) {
                @Override
                public void onPressed() {
                    selectedRecipeIndex = finalIndex;
                    VillagerRecipeViewingScreen.this.init();
                }
                
                @Override
                public boolean isHovered(int mouseX, int mouseY) {
                    return (isMouseOver(mouseX, mouseY) && scrollListBounds.contains(mouseX, mouseY)) || focused;
                }
                
                @Override
                protected int getTextureId(boolean boolean_1) {
                    enabled = selectedRecipeIndex != finalIndex;
                    return super.getTextureId(boolean_1);
                }
                
                @Override
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if ((isMouseOver(mouseX, mouseY) && scrollListBounds.contains(mouseX, mouseY)) && enabled && button == 0) {
                        minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        onPressed();
                        return true;
                    }
                    return false;
                }
            });
            index++;
        }
        int tabV = isCompactTabs ? 166 : 192;
        for (int i = 0; i < tabsPerPage; i++) {
            int j = i + tabsPage * tabsPerPage;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, tabSize, bounds.x + bounds.width / 2 - Math.min(categories.size() - tabsPage * tabsPerPage, tabsPerPage) * tabSize / 2, bounds.y, 0, tabV) {
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (containsMouse(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + tabsPage * tabsPerPage == selectedCategoryIndex)
                                return false;
                            selectedCategoryIndex = getId() + tabsPage * tabsPerPage;
                            scroll = 0;
                            selectedRecipeIndex = 0;
                            VillagerRecipeViewingScreen.this.init();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setRenderer(categories.get(j), categories.get(j).getLogo(), categories.get(j).getCategoryName(), tab.getId() + tabsPage * tabsPerPage == selectedCategoryIndex);
            }
        }
        ButtonWidget w, w2;
        this.widgets.add(w = ButtonWidget.create(new Rectangle(bounds.x + 2, bounds.y - 16, 10, 10), new TranslatableText("text.rei.left_arrow"), buttonWidget -> {
            tabsPage--;
            if (tabsPage < 0)
                tabsPage = MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1;
            VillagerRecipeViewingScreen.this.init();
        }));
        this.widgets.add(w2 = ButtonWidget.create(new Rectangle(bounds.x + bounds.width - 12, bounds.y - 16, 10, 10), new TranslatableText("text.rei.right_arrow"), buttonWidget -> {
            tabsPage++;
            if (tabsPage > MathHelper.ceil(categories.size() / (float) tabsPerPage) - 1)
                tabsPage = 0;
            VillagerRecipeViewingScreen.this.init();
        }));
        w.enabled = w2.enabled = categories.size() > tabsPerPage;
        
        this.widgets.add(new ClickableLabelWidget(new Point(bounds.x + 4 + scrollListBounds.width / 2, bounds.y + 6), categories.get(selectedCategoryIndex).getCategoryName()) {
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.getInstance().executeViewAllRecipesKeyBind();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.view_all_categories"));
            }
        });
        
        this.children.addAll(buttonWidgets);
        this.widgets.addAll(tabs);
        this.children.addAll(widgets);
        this.children.add(ScreenHelper.getLastOverlay(true, false));
        ScreenHelper.getLastOverlay().init();
    }
    
    private double getMaxScroll() {
        return Math.max(0, this.getMaxScrollPosition() - (scrollListBounds.height - 2));
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int int_1) {
        double height = getMaxScrollPosition();
        int actualHeight = scrollListBounds.height - 2;
        if (height > actualHeight && scrollBarAlpha > 0 && mouseY >= scrollListBounds.y + 1 && mouseY <= scrollListBounds.getMaxY() - 1) {
            double scrollbarPositionMinX = scrollListBounds.getMaxX() - 6;
            if (mouseX >= scrollbarPositionMinX & mouseX <= scrollbarPositionMinX + 8) {
                this.draggingScrollBar = true;
                scrollBarAlpha = 1;
                return false;
            }
        }
        this.draggingScrollBar = false;
        return super.mouseClicked(mouseX, mouseY, int_1);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for (Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    public void offset(double value, boolean animated) {
        scrollTo(target + value, animated);
    }
    
    public void scrollTo(double value, boolean animated) {
        scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
    }
    
    public void scrollTo(double value, boolean animated, long duration) {
        target = ClothConfigInitializer.clamp(value, getMaxScroll());
        
        if (animated) {
            start = System.currentTimeMillis();
            this.duration = duration;
        } else
            scroll = target;
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        double height = CollectionUtils.sumInt(buttonWidgets, b -> b.getBounds().getHeight());
        if (scrollListBounds.contains(double_1, double_2) && height > scrollListBounds.height - 2) {
            offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
            if (scrollBarAlphaFuture == 0)
                scrollBarAlphaFuture = 1f;
            if (System.currentTimeMillis() - scrollBarAlphaFutureTime > 300f)
                scrollBarAlphaFutureTime = System.currentTimeMillis();
            return true;
        }
        for (Element listener : children())
            if (listener.mouseScrolled(double_1, double_2, double_3))
                return true;
        if (bounds.contains(PointHelper.fromMouse())) {
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
    
    private double getMaxScrollPosition() {
        return CollectionUtils.sumInt(buttonWidgets, b -> b.getBounds().getHeight());
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
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
        updatePosition(delta);
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int yOffset = 0;
        for (Widget widget : widgets) {
            widget.render(mouseX, mouseY, delta);
        }
        ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
        RenderSystem.pushMatrix();
        ScissorsHandler.INSTANCE.scissor(new Rectangle(0, scrollListBounds.y + 1, width, scrollListBounds.height - 2));
        for (ButtonWidget buttonWidget : buttonWidgets) {
            buttonWidget.getBounds().y = scrollListBounds.y + 1 + yOffset - (int) scroll;
            if (buttonWidget.getBounds().getMaxY() > scrollListBounds.getMinY() && buttonWidget.getBounds().getMinY() < scrollListBounds.getMaxY()) {
                buttonWidget.render(mouseX, mouseY, delta);
            }
            yOffset += buttonWidget.getBounds().height;
        }
        for (int i = 0; i < buttonWidgets.size(); i++) {
            if (buttonWidgets.get(i).getBounds().getMaxY() > scrollListBounds.getMinY() && buttonWidgets.get(i).getBounds().getMinY() < scrollListBounds.getMaxY()) {
                recipeRenderers.get(i).setZ(1);
                recipeRenderers.get(i).render(buttonWidgets.get(i).getBounds(), mouseX, mouseY, delta);
                ScreenHelper.getLastOverlay().addTooltip(recipeRenderers.get(i).getTooltip(mouseX, mouseY));
            }
        }
        double maxScroll = getMaxScrollPosition();
        if (maxScroll > scrollListBounds.height - 2) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            int height = (int) (((scrollListBounds.height - 2) * (scrollListBounds.height - 2)) / this.getMaxScrollPosition());
            height = MathHelper.clamp(height, 32, scrollListBounds.height - 2 - 8);
            height -= Math.min((scroll < 0 ? (int) -scroll : scroll > getMaxScroll() ? (int) scroll - getMaxScroll() : 0), height * .95);
            height = Math.max(10, height);
            int minY = (int) Math.min(Math.max((int) scroll * (scrollListBounds.height - 2 - height) / getMaxScroll() + scrollListBounds.y + 1, scrollListBounds.y + 1), scrollListBounds.getMaxY() - 1 - height);
            int scrollbarPositionMinX = scrollListBounds.getMaxX() - 6, scrollbarPositionMaxX = scrollListBounds.getMaxX() - 1;
            boolean hovered = (new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height)).contains(PointHelper.fromMouse());
            float bottomC = (hovered ? .67f : .5f) * (ScreenHelper.isDarkModeEnabled() ? 0.8f : 1f);
            float topC = (hovered ? .87f : .67f) * (ScreenHelper.isDarkModeEnabled() ? 0.8f : 1f);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            RenderSystem.shadeModel(7425);
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height, 800).color(bottomC, bottomC, bottomC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY + height, 800).color(bottomC, bottomC, bottomC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX, minY, 800).color(bottomC, bottomC, bottomC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMinX, minY, 800).color(bottomC, bottomC, bottomC, scrollBarAlpha).next();
            tessellator.draw();
            buffer.begin(7, VertexFormats.POSITION_COLOR);
            buffer.vertex(scrollbarPositionMinX, minY + height - 1, 800).color(topC, topC, topC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX - 1, minY + height - 1, 800).color(topC, topC, topC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMaxX - 1, minY, 800).color(topC, topC, topC, scrollBarAlpha).next();
            buffer.vertex(scrollbarPositionMinX, minY, 800).color(topC, topC, topC, scrollBarAlpha).next();
            tessellator.draw();
            RenderSystem.shadeModel(7424);
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableTexture();
        }
        ScissorsHandler.INSTANCE.removeLastScissor();
        RenderSystem.popMatrix();
        ScreenHelper.getLastOverlay().lateRender(mouseX, mouseY, delta);
    }
    
    private void updatePosition(float delta) {
        double[] target = new double[]{this.target};
        this.scroll = ClothConfigInitializer.handleScrollingPosition(target, this.scroll, this.getMaxScroll(), delta, this.start, this.duration);
        this.target = target[0];
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int int_1, double double_3, double double_4) {
        if (int_1 == 0 && scrollBarAlpha > 0 && draggingScrollBar) {
            double height = CollectionUtils.sumInt(buttonWidgets, b -> b.getBounds().getHeight());
            int actualHeight = scrollListBounds.height - 2;
            if (height > actualHeight && mouseY >= scrollListBounds.y + 1 && mouseY <= scrollListBounds.getMaxY() - 1) {
                int int_3 = MathHelper.clamp((int) ((actualHeight * actualHeight) / height), 32, actualHeight - 8);
                double double_6 = Math.max(1.0D, Math.max(1d, height) / (double) (actualHeight - int_3));
                scrollBarAlphaFutureTime = System.currentTimeMillis();
                scrollBarAlphaFuture = 1f;
                scroll = target = MathHelper.clamp(scroll + double_4 * double_6, 0, height - scrollListBounds.height + 2);
                return true;
            }
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
        if (int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        if (int_1 == 259) {
            if (ScreenHelper.hasLastRecipeScreen())
                minecraft.openScreen(ScreenHelper.getLastRecipeScreen());
            else
                minecraft.openScreen(ScreenHelper.getLastContainerScreen());
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
}
