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
import me.shedaniel.architectury.fluid.FluidStack;
import dev.architectury.utils.value.IntValue;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
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
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AbstractDisplayViewingScreen extends Screen implements DisplayScreen {
    protected final Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap;
    protected final List<DisplayCategory<?>> categories;
    protected final TabContainerWidget tabs = new TabContainerWidget();
    protected List<EntryStack<?>> ingredientStackToNotice = new ArrayList<>();
    protected List<EntryStack<?>> resultStackToNotice = new ArrayList<>();
    protected int selectedCategoryIndex = 0;
    protected int categoryPages = -1;
    protected Rectangle bounds;
    
    protected AbstractDisplayViewingScreen(Map<DisplayCategory<?>, List<DisplaySpec>> categoryMap, @Nullable CategoryIdentifier<?> category) {
        super(NarratorChatListener.NO_TITLE);
        this.categoryMap = categoryMap;
        this.categories = Lists.newArrayList(categoryMap.keySet());
        if (category != null) {
            selectCategory(category, false);
        }
    }
    
    protected void selectCategory(CategoryIdentifier<?> category) {
        selectCategory(category, true);
    }
    
    protected void selectCategory(CategoryIdentifier<?> category, boolean init) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getCategoryIdentifier().equals(category)) {
                this.selectedCategoryIndex = i;
                break;
            }
        }
        
        recalculateCategoryPage();
        this.tabs.updateScroll(categories, selectedCategoryIndex, !init ? 0 : 300);
        
        if (init) {
            init();
        }
    }
    
    @Override
    public void recalculateCategoryPage() {
        this.categoryPages = -1;
    }
    
    protected void initTabs() {
        this.tabs.init(new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28), categories, new IntValue() {
            @Override
            public void accept(int value) {
                AbstractDisplayViewingScreen.this.categoryPages = value;
            }
            
            @Override
            public int getAsInt() {
                return AbstractDisplayViewingScreen.this.categoryPages;
            }
        }, new IntValue() {
            @Override
            public void accept(int value) {
                AbstractDisplayViewingScreen.this.selectCategory(categories.get(value).getCategoryIdentifier());
            }
            
            @Override
            public int getAsInt() {
                return selectedCategoryIndex;
            }
        }, AbstractDisplayViewingScreen.this::init);
    }
    
    @Override
    public List<GuiEventListener> children() {
        List<? extends GuiEventListener> children = super.children();
        children.sort(Comparator.comparingDouble(value -> value instanceof Widget widget ? widget.getZRenderingPriority() : 0).reversed());
        return (List<GuiEventListener>) children;
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
        return CategoryRegistry.getInstance().get(display.getCategoryIdentifier().cast())
                .getView(display);
    }
    
    @Override
    public void previousCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex--;
        if (currentCategoryIndex < 0)
            currentCategoryIndex = categories.size() - 1;
        selectCategory(categories.get(currentCategoryIndex).getCategoryIdentifier());
    }
    
    @Override
    public void nextCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex++;
        if (currentCategoryIndex >= categories.size())
            currentCategoryIndex = 0;
        selectCategory(categories.get(currentCategoryIndex).getCategoryIdentifier());
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
        TagContainer tags = Minecraft.getInstance().getConnection().getTags();
        outer:
        for (EntryWidget widget : Widgets.<EntryWidget>walk(widgets, EntryWidget.class::isInstance)) {
            if (widget.getNoticeMark() != EntryWidget.INPUT) continue;
            addCyclingTooltip(widget);
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
            TagCollection<?> collection;
            List<Object> objects;
            if (type == VanillaEntryTypes.ITEM) {
                collection = tags.getItems();
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<ItemStack>castValue().getItem());
            } else if (type == VanillaEntryTypes.FLUID) {
                collection = tags.getFluids();
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<FluidStack>castValue().getFluid());
            } else continue;
            Map.Entry<ResourceLocation, ? extends Tag<?>> firstOrNull = CollectionUtils.findFirstOrNull(collection.getAllTags().entrySet(), entry -> entry.getValue().getValues().equals(objects));
            if (firstOrNull != null) {
                widget.tagMatch = firstOrNull.getKey();
            }
        }
    }
    
    private static final int MAX_WIDTH = 200;
    
    private void addCyclingTooltip(EntryWidget widget) {
        class TooltipProcessor implements UnaryOperator<Tooltip> {
            @Override
            public Tooltip apply(Tooltip tooltip) {
                if (widget.getEntries().size() > 1) {
                    for (Tooltip.Entry entry : tooltip.entries()) {
                        if (entry.isTooltipComponent() && entry.getAsTooltipComponent() instanceof TooltipProcessor) {
                            return tooltip;
                        }
                    }
                    
                    tooltip.add(new TranslatableComponent("text.rei.tag_accept", widget.tagMatch.toString())
                            .withStyle(ChatFormatting.GRAY));
                }
                return tooltip;
            }
        }
        
        widget.tooltipProcessor(new TooltipProcessor());
    }
    
    protected static ScreenOverlay getOverlay() {
        return REIRuntime.getInstance().getOverlay().orElseThrow(() -> new IllegalStateException("Overlay not initialized!"));
    }
    
    private boolean handleFocuses(int button) {
        if (button == 0) {
            setDragging(true);
        }
        handleFocuses();
        return true;
    }
    
    private boolean handleFocuses() {
        if (getFocused() instanceof ScreenOverlay || getFocused() == this) {
            setFocused(null);
        }
        
        return true;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || (getOverlay().mouseClicked(mouseX, mouseY, button) && handleFocuses(button));
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button) || (getOverlay().mouseReleased(mouseX, mouseY, button) && handleFocuses());
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || (getOverlay().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) && handleFocuses());
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount) || (getOverlay().mouseScrolled(mouseX, mouseY, amount) && handleFocuses());
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers) || (getOverlay().keyPressed(keyCode, scanCode, modifiers) && handleFocuses()))
            return true;
        if (ConfigObject.getInstance().getPreviousScreenKeybind().matchesKey(keyCode, scanCode)) {
            if (REIRuntimeImpl.getInstance().hasLastDisplayScreen()) {
                minecraft.setScreen(REIRuntimeImpl.getInstance().getLastDisplayScreen());
            } else {
                minecraft.setScreen(REIRuntime.getInstance().getPreviousScreen());
            }
            return true;
        }
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            Minecraft.getInstance().setScreen(REIRuntime.getInstance().getPreviousScreen());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers) || (getOverlay().keyReleased(keyCode, scanCode, modifiers) && handleFocuses());
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        return super.charTyped(character, modifiers) || (getOverlay().charTyped(character, modifiers) && handleFocuses());
    }
}
