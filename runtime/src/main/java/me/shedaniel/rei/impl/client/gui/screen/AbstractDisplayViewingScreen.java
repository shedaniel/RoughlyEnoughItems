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

package me.shedaniel.rei.impl.client.gui.screen;

import com.google.common.collect.Lists;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractDisplayViewingScreen extends Screen implements DisplayScreen {
    protected final Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap;
    protected final List<DisplayCategory<?>> categories;
    protected List<EntryStack<?>> ingredientStackToNotice = new ArrayList<>();
    protected List<EntryStack<?>> resultStackToNotice = new ArrayList<>();
    protected int selectedCategoryIndex = 0;
    protected int tabsPerPage;
    protected Rectangle bounds;
    
    protected AbstractDisplayViewingScreen(Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap, @Nullable CategoryIdentifier<?> category, int tabsPerPage) {
        super(NarratorChatListener.NO_TITLE);
        this.categoryMap = categoryMap;
        this.categories = Lists.newArrayList(categoryMap.keySet());
        this.tabsPerPage = tabsPerPage;
        if (category != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCategoryIdentifier().equals(category)) {
                    this.selectedCategoryIndex = i;
                    break;
                }
            }
        }
    }
    
    public List<GuiEventListener> _children() {
        return (List<GuiEventListener>) children();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void addIngredientToNotice(EntryStack<?> stack) {
        if (stack == null) return;
        this.ingredientStackToNotice.add(stack);
    }
    
    @Override
    public void addResultToNotice(EntryStack<?> stack) {
        if (stack == null) return;
        this.resultStackToNotice.add(stack);
    }
    
    @Override
    public List<EntryStack<?>> getIngredientsToNotice() {
        return Collections.unmodifiableList(ingredientStackToNotice);
    }
    
    @Override
    public List<EntryStack<?>> getResultsToNotice() {
        return Collections.unmodifiableList(resultStackToNotice);
    }
    
    @Override
    public DisplayCategory<Display> getCurrentCategory() {
        return (DisplayCategory<Display>) categories.get(selectedCategoryIndex);
    }
    
    protected DisplayCategoryView<Display> getCurrentCategoryView(Display display) {
        return CategoryRegistry.getInstance().get(categories.get(selectedCategoryIndex).getCategoryIdentifier().cast())
                .getView(display);
    }
    
    @Override
    public void previousCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex--;
        if (currentCategoryIndex < 0)
            currentCategoryIndex = categories.size() - 1;
        ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, categories.get(currentCategoryIndex).getCategoryIdentifier(), ingredientStackToNotice, resultStackToNotice);
    }
    
    @Override
    public void nextCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex++;
        if (currentCategoryIndex >= categories.size())
            currentCategoryIndex = 0;
        ClientHelperImpl.getInstance().openRecipeViewingScreen(categoryMap, categories.get(currentCategoryIndex).getCategoryIdentifier(), ingredientStackToNotice, resultStackToNotice);
    }
    
    protected void transformIngredientNotice(List<Widget> setupDisplay, List<EntryStack<?>> noticeStacks) {
        transformNotice(Slot.INPUT, setupDisplay, noticeStacks);
    }
    
    protected void transformResultNotice(List<Widget> setupDisplay, List<EntryStack<?>> noticeStacks) {
        transformNotice(Slot.OUTPUT, setupDisplay, noticeStacks);
    }
    
    private static void transformNotice(int marker, List<? extends GuiEventListener> setupDisplay, List<EntryStack<?>> noticeStacks) {
        if (noticeStacks.isEmpty())
            return;
        for (EntryWidget widget : Widgets.<EntryWidget>walk(setupDisplay, EntryWidget.class::isInstance)) {
            if (widget.getNoticeMark() == marker && widget.getEntries().size() > 1) {
                for (EntryStack<?> noticeStack : noticeStacks) {
                    EntryStack<?> stack = CollectionUtils.findFirstOrNullEqualsExact(widget.getEntries(), noticeStack);
                    if (stack != null) {
                        widget.clearStacks();
                        widget.entry(stack);
                        break;
                    }
                }
            }
        }
    }
    
    protected void transformFiltering(List<? extends GuiEventListener> setupDisplay) {
        for (EntryWidget widget : Widgets.<EntryWidget>walk(setupDisplay, EntryWidget.class::isInstance)) {
            if (widget.getEntries().size() > 1) {
                Collection<EntryStack<?>> refiltered = EntryRegistry.getInstance().refilterNew(false, widget.getEntries());
                if (!refiltered.isEmpty()) {
                    widget.clearStacks();
                    widget.entries(refiltered);
                }
            }
        }
    }
    
    protected void setupTags(List<Widget> widgets) {
        outer:
        for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets, EntryWidget.class::isInstance)) {
            widget.removeTagMatch = false;
            if (widget.getEntries().size() <= 1) continue;
            EntryType<?> type = null;
            for (EntryStack<?> entry : widget.getEntries()) {
                if (type == null) {
                    type = entry.getType();
                } else if (type != entry.getType()) {
                    continue outer;
                }
            }
            // TODO: Don't hardcode
            Stream<? extends TagKey<?>> collection;
            List<Holder<?>> objects;
            if (type == VanillaEntryTypes.ITEM) {
                collection = Registry.ITEM.getTagNames();
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<ItemStack>castValue().getItem().builtInRegistryHolder());
            } else if (type == VanillaEntryTypes.FLUID) {
                collection = Registry.FLUID.getTagNames();
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<FluidStack>castValue().getFluid().builtInRegistryHolder());
            } else continue;
            TagKey<?> firstOrNull = collection.filter(key ->
                            CollectionUtils.anyMatch(objects, holder -> ((Holder<Object>) holder).is((TagKey<Object>) key)))
                    .findAny().orElse(null);
            if (firstOrNull != null) {
                widget.tagMatch = firstOrNull.location();
            }
        }
    }
}
