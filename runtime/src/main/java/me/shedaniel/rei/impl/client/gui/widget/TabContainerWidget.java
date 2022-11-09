/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.value.IntValue;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.CloseableScissors;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class TabContainerWidget extends GuiComponent {
    private final Rectangle bounds = new Rectangle();
    private final List<Widget> widgets = new ArrayList<>();
    private final NumberAnimator<Double> scrollAnimator = ValueAnimator.ofDouble();
    private int tabsPerPage = 5;
    
    public TabContainerWidget() {
    }
    
    public void setBounds(Rectangle bounds) {
        this.bounds.setBounds(bounds);
    }
    
    public void updateScroll(List<DisplayCategory<?>> categories, int selectedCategory, long duration) {
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        
        double curr = scrollAnimator.doubleValue() % (tabSize * categories.size());
        double newValue1 = selectedCategory * tabSize + Math.floor(scrollAnimator.doubleValue() / (tabSize * categories.size())) * (tabSize * categories.size());
        double newValue2 = newValue1 - (tabSize * categories.size());
        double newValue3 = newValue1 + (tabSize * categories.size());
        
        Stream.of(newValue1, newValue2, newValue3).min(Comparator.comparingDouble(value -> Math.abs(value - scrollAnimator.doubleValue()))).ifPresent(newValue -> {
            scrollAnimator.setTo(newValue, duration);
        });
    }
    
    public void init(Rectangle bounds, List<DisplayCategory<?>> categories, IntValue categoryPages, IntValue selectedCategory, Runnable reInit) {
        this.setBounds(bounds);
        this.widgets.clear();
        
        boolean isCompactTabs = ConfigObject.getInstance().isUsingCompactTabs();
        int tabSize = isCompactTabs ? 24 : 28;
        this.tabsPerPage = Mth.floor((this.bounds.getWidth() - 10) / tabSize);
        
        if (categoryPages.getAsInt() == -1) {
            categoryPages.accept(Math.max(0, selectedCategory.getAsInt() / tabsPerPage()));
        }
        
        this.widgets.add(new Widget() {
            @Override
            public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
                scrollAnimator.update(delta);
                int absLeft = bounds.x + bounds.width / 2 - tabsPerPage() * tabSize / 2;
                int left;
                if (categories.size() > tabsPerPage()) {
                    left = bounds.x + bounds.width / 2 - tabSize / 2 - (int) Math.round(scrollAnimator.doubleValue());
                    updateScroll(categories, selectedCategory.getAsInt(), 300);
                } else {
                    left = bounds.x + bounds.width / 2 - categories.size() * tabSize / 2;
                }
                int passed = 0;
                for (TabWidget tab : Widgets.<TabWidget>walk(TabContainerWidget.this.widgets(), widget -> widget instanceof TabWidget)) {
                    if (categories.size() <= tabsPerPage()) {
                        tab.getBounds().x = left;
                        left += tabSize;
                    } else {
                        if (left > bounds.getMaxX()) {
                            while (left > bounds.getMaxX()) {
                                left -= tabSize * categories.size();
                            }
                            tab.getBounds().x = left;
                            left += tabSize;
                        } else if (left + tabSize < bounds.x) {
                            while (left + tabSize < bounds.x) {
                                left += tabSize * categories.size();
                            }
                            tab.getBounds().x = left;
                            left += tabSize;
                        } else {
                            tab.getBounds().x = left;
                            left += tabSize;
                        }
                    }
                    passed++;
                    
                    if (tab.getBounds().x > bounds.getMaxX() || tab.getBounds().getMaxX() < bounds.x) {
                        tab.getBounds().x = -1000;
                    }
                    
                    if (tab.getBounds().getCenterX() <= absLeft + 20) {
                        tab.opacity = 1 - (absLeft + 20 - tab.getBounds().getCenterX()) / 20f;
                        tab.opacity = (float) Math.pow(Mth.clamp(tab.opacity, 0, 1), 0.9);
                    } else if (tab.getBounds().getCenterX() >= absLeft + tabsPerPage() * tabSize - 20) {
                        tab.opacity = 1 - (tab.getBounds().getCenterX() - (absLeft + tabsPerPage() * tabSize - 20)) / 20f;
                        tab.opacity = (float) Math.pow(Mth.clamp(tab.opacity, 0, 1), 0.9);
                    } else {
                        tab.opacity = 1;
                    }
                    
                    if (tab.opacity < 0.1) {
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
                    try (CloseableScissors scissors = Widget.scissor(poseStack, new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height + 4))) {
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
}
