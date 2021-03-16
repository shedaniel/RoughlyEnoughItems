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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.gui.widgets.Slot;
import me.shedaniel.rei.api.gui.widgets.Widget;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.ClientHelperImpl;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class AbstractRecipeViewingScreen extends Screen implements RecipeScreen {
    protected final Map<DisplayCategory<?>, List<Display>> categoryMap;
    protected final List<DisplayCategory<?>> categories;
    protected EntryStack<?> ingredientStackToNotice = EntryStack.empty();
    protected EntryStack<?> resultStackToNotice = EntryStack.empty();
    protected int selectedCategoryIndex = 0;
    protected int tabsPerPage;
    protected Rectangle bounds;
    
    protected AbstractRecipeViewingScreen(Map<DisplayCategory<?>, List<Display>> categoryMap, @Nullable ResourceLocation category, int tabsPerPage) {
        super(NarratorChatListener.NO_TITLE);
        this.categoryMap = categoryMap;
        this.categories = Lists.newArrayList(categoryMap.keySet());
        this.tabsPerPage = tabsPerPage;
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
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void addIngredientStackToNotice(EntryStack<?> stack) {
        this.ingredientStackToNotice = stack;
    }
    
    @Override
    public void addResultStackToNotice(EntryStack<?> stack) {
        this.resultStackToNotice = stack;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public ResourceLocation getCurrentCategory() {
        return getSelectedCategory().getIdentifier();
    }
    
    public DisplayCategory<Display> getSelectedCategory() {
        return (DisplayCategory<Display>) categories.get(selectedCategoryIndex);
    }
    
    protected void previousCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex--;
        if (currentCategoryIndex < 0)
            currentCategoryIndex = categories.size() - 1;
        ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, categories.get(currentCategoryIndex).getIdentifier(), ingredientStackToNotice, resultStackToNotice);
    }
    
    protected void nextCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex++;
        if (currentCategoryIndex >= categories.size())
            currentCategoryIndex = 0;
        ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, categories.get(currentCategoryIndex).getIdentifier(), ingredientStackToNotice, resultStackToNotice);
    }
    
    protected void transformIngredientNotice(List<Widget> setupDisplay, EntryStack<?> noticeStack) {
        transformNotice(Slot.INPUT, setupDisplay, noticeStack);
    }
    
    protected void transformResultNotice(List<Widget> setupDisplay, EntryStack<?> noticeStack) {
        transformNotice(Slot.OUTPUT, setupDisplay, noticeStack);
    }
    
    private static void transformNotice(int marker, List<? extends GuiEventListener> setupDisplay, EntryStack<?> noticeStack) {
        if (noticeStack.isEmpty())
            return;
        for (GuiEventListener widget : setupDisplay) {
            if (widget instanceof EntryWidget) {
                EntryWidget entry = (EntryWidget) widget;
                if (entry.getNoticeMark() == marker && entry.getEntries().size() > 1) {
                    EntryStack<?> stack = CollectionUtils.findFirstOrNullEqualsEntryIgnoreAmount(entry.getEntries(), noticeStack);
                    if (stack != null) {
                        entry.clearStacks();
                        entry.entry(stack);
                    }
                }
            } else if (widget instanceof ContainerEventHandler) {
                transformNotice(marker, ((ContainerEventHandler) widget).children(), noticeStack);
            }
        }
    }
}
