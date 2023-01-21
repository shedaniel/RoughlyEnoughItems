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

package me.shedaniel.rei.impl.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.value.IntValue;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class TabContainerWidget extends GuiComponent {
    private final Rectangle bounds = new Rectangle();
    private final List<Widget> widgets = new ArrayList<>();
    private final NumberAnimator<Double> scrollAnimator = ValueAnimator.ofDouble();
    private boolean isCompactTabs;
    private boolean isCompactTabButtons;
    private int tabSize;
    private int tabButtonsSize;
    private int tabsPerPage = 5;
    
    public TabContainerWidget() {
    }
    
    public void setBounds(Rectangle bounds) {
        this.bounds.setBounds(bounds);
    }
    
    public void updateScroll(List<DisplayCategory<?>> categories, int selectedCategory, long duration) {
        this.initTabsVariables();
        if (categories.size() <= tabsPerPage) {
            scrollAnimator.setAs(0d);
        } else if (selectedCategory < tabsPerPage / 2) {
            scrollAnimator.setTo(0d, duration);
        } else if (selectedCategory >= categories.size() - (int) Math.ceil(tabsPerPage / 2)) {
            scrollAnimator.setTo((categories.size() - tabsPerPage) * tabSize, duration);
        } else {
            scrollAnimator.setTo((selectedCategory - (tabsPerPage - 1) / 2.0) * tabSize, duration);
        }
    }
    
    private void initTabsVariables() {
        this.isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        this.isCompactTabButtons = ConfigObject.getInstance().isUsingCompactTabButtons();
        this.tabSize = isCompactTabs ? 24 : 28;
        this.tabButtonsSize = isCompactTabButtons ? 10 : 16;
    }
    
    public void initTabsSize(int width) {
        this.initTabsVariables();
        this.tabsPerPage = Mth.floor((width - 6) / tabSize);
        if (this.tabsPerPage % 2 == 0)
            this.tabsPerPage--;
    }
    
    public void init(Rectangle scissorsBounds, Rectangle bounds, List<DisplayCategory<?>> categories, IntValue categoryPages, IntValue selectedCategory, Runnable reInit) {
        this.setBounds(bounds);
        this.widgets.clear();
        
        initTabsSize(bounds.width);
        
        if (categoryPages.getAsInt() == -1) {
            categoryPages.accept(Math.max(0, selectedCategory.getAsInt() / tabsPerPage()));
        }
        
        if (categories.size() > tabsPerPage) {
            Button tabLeft, tabRight;
            this.widgets.add(tabLeft = Widgets.createButton(new Rectangle(bounds.x, bounds.getMaxY() - tabSize + 1 - tabButtonsSize, tabButtonsSize, tabButtonsSize), Component.empty())
                    .onClick(button -> {
                        int currentCategoryPage = selectedCategory.getAsInt() / tabsPerPage();
                        currentCategoryPage = Math.floorMod(currentCategoryPage - 1, categories.size() / tabsPerPage() + 1);
                        selectedCategory.accept(Mth.clamp(currentCategoryPage * tabsPerPage() + tabsPerPage() / 2,
                                tabsPerPage() / 2, categories.size() - (int) Math.ceil(tabsPerPage() / 2.0)));
                    })
                    .tooltipLine(Component.translatable("text.rei.previous_page")));
            this.widgets.add(tabRight = Widgets.createButton(new Rectangle(bounds.x + bounds.width - tabButtonsSize - (isCompactTabButtons ? 0 : 1), bounds.getMaxY() - tabSize + 1 - tabButtonsSize, tabButtonsSize, tabButtonsSize), Component.empty())
                    .onClick(button -> {
                        int currentCategoryPage = selectedCategory.getAsInt() / tabsPerPage();
                        currentCategoryPage = Math.floorMod(currentCategoryPage + 1, categories.size() / tabsPerPage() + 1);
                        selectedCategory.accept(Mth.clamp(currentCategoryPage * tabsPerPage() + tabsPerPage() / 2,
                                tabsPerPage() / 2, categories.size() - (int) Math.ceil(tabsPerPage() / 2.0)));
                    })
                    .tooltipLine(Component.translatable("text.rei.next_page")));
            
            this.widgets.add(Widgets.withTranslate(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
                Rectangle tabLeftBounds = tabLeft.getBounds();
                Rectangle tabRightBounds = tabRight.getBounds();
                if (isCompactTabButtons) {
                    matrices.pushPose();
                    matrices.translate(0, 0.5, 0);
                    RenderSystem.setShaderTexture(0, InternalTextures.ARROW_LEFT_SMALL_TEXTURE);
                    blit(matrices, tabLeftBounds.x + 2, tabLeftBounds.y + 2, 0, 0, 6, 6, 6, 6);
                    RenderSystem.setShaderTexture(0, InternalTextures.ARROW_RIGHT_SMALL_TEXTURE);
                    blit(matrices, tabRightBounds.x + 2, tabRightBounds.y + 2, 0, 0, 6, 6, 6, 6);
                    matrices.popPose();
                } else {
                    RenderSystem.setShaderTexture(0, InternalTextures.ARROW_LEFT_TEXTURE);
                    blit(matrices, tabLeftBounds.x + 4, tabLeftBounds.y + 4, 0, 0, 8, 8, 8, 8);
                    RenderSystem.setShaderTexture(0, InternalTextures.ARROW_RIGHT_TEXTURE);
                    blit(matrices, tabRightBounds.x + 4, tabRightBounds.y + 4, 0, 0, 8, 8, 8, 8);
                }
            }), 0, 0, 1));
        }
        
        this.widgets.add(new Widget() {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                scrollAnimator.update(delta);
                int absLeft = bounds.x + bounds.width / 2 - tabsPerPage() * tabSize / 2;
                int absRight = bounds.x + bounds.width / 2 + tabsPerPage() * tabSize / 2;
                int left;
                if (categories.size() > tabsPerPage()) {
                    left = bounds.x + bounds.width / 2 - Math.min(categories.size(), tabsPerPage()) * tabSize / 2 - (int) Math.round(scrollAnimator.doubleValue());
                    updateScroll(categories, selectedCategory.getAsInt(), 300);
                } else {
                    left = bounds.x + bounds.width / 2 - categories.size() * tabSize / 2;
                }
                for (TabWidget tab : Widgets.<TabWidget>walk(TabContainerWidget.this.widgets(), widget -> widget instanceof TabWidget)) {
                    tab.getBounds().x = left;
                    left += tabSize;
                    
                    if (tab.isSelected()) {
                        tab.opacity = 1;
                    } else if (tab.getBounds().getCenterX() <= absLeft) {
                        tab.opacity = 1 - (absLeft - tab.getBounds().getCenterX()) / 20f;
                        tab.opacity = (float) Math.pow(Mth.clamp(tab.opacity, 0, 1), 1.2);
                    } else if (tab.getBounds().getCenterX() >= absRight) {
                        tab.opacity = 1 - (tab.getBounds().getCenterX() - absRight) / 20f;
                        tab.opacity = (float) Math.pow(Mth.clamp(tab.opacity, 0, 1), 1.2);
                    } else {
                        tab.opacity = 1;
                    }
                    
                    if (tab.opacity < 0.1) {
                        tab.opacity = 0;
                    }
                    
                    if (tab.opacity == 0 || tab.getBounds().x > bounds.getMaxX() || tab.getBounds().getMaxX() < bounds.x) {
                        tab.getBounds().x = -1000;
                        tab.opacity = 0;
                    }
                }
            }
            
            @Override
            public double getZRenderingPriority() {
                return -1000;
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return List.of();
            }
            
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
                if (TabContainerWidget.this.bounds.contains(mouseX, mouseY) && categories.size() > 1) {
                    int currentCategoryIndex = selectedCategory.getAsInt();
                    if (amount > 0) {
                        currentCategoryIndex--;
                        if (currentCategoryIndex < 0)
                            currentCategoryIndex = categories.size() - 1;
                    } else {
                        currentCategoryIndex++;
                        if (currentCategoryIndex >= categories.size())
                            currentCategoryIndex = 0;
                    }
                    selectedCategory.accept(currentCategoryIndex);
                    
                    return true;
                }
                return false;
            }
        });
        
        for (int id = 0; id < categories.size(); id++) {
            int tabIndex = id;
            DisplayCategory<?> category = categories.get(tabIndex);
            TabWidget tab = TabWidget.create(id, tabSize, bounds.x + bounds.width / 2 - tabsPerPage * tabSize / 2, bounds.getMaxY(), 0, isCompactTabs ? 166 : 192, widget -> {
                Widgets.produceClickSound();
                if (tabIndex == selectedCategory.getAsInt())
                    return false;
                selectedCategory.accept(tabIndex);
                return true;
            });
            tab.setRenderer(category, category.getIcon(), category.getTitle(), tabIndex == selectedCategory.getAsInt());
            this.widgets.add(new DelegateWidget(tab) {
                @Override
                public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
                    try (CloseableScissors scissors = Widget.scissor(poseStack, new Rectangle(scissorsBounds.x, scissorsBounds.y, scissorsBounds.width, scissorsBounds.height + 4))) {
                        super.render(poseStack, mouseX, mouseY, delta);
                    }
                }
            });
        }
    }
    
    public List<Widget> widgets() {
        return this.widgets;
    }
    
    public int tabsPerPage() {
        return tabsPerPage;
    }
    
    public int tabButtonsSize() {
        return tabButtonsSize;
    }
    
    public int tabSize() {
        return tabSize;
    }
}
