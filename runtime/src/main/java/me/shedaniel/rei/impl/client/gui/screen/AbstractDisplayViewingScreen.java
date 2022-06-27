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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
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
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
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
        return CategoryRegistry.getInstance().get(display.getCategoryIdentifier().cast())
                .getView(display);
    }
    
    @Override
    public void previousCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex--;
        if (currentCategoryIndex < 0)
            currentCategoryIndex = categories.size() - 1;
        ClientHelperImpl.getInstance().openDisplayViewingScreen(categoryMap, categories.get(currentCategoryIndex).getCategoryIdentifier(), ingredientStackToNotice, resultStackToNotice);
    }
    
    @Override
    public void nextCategory() {
        int currentCategoryIndex = selectedCategoryIndex;
        currentCategoryIndex++;
        if (currentCategoryIndex >= categories.size())
            currentCategoryIndex = 0;
        ClientHelperImpl.getInstance().openDisplayViewingScreen(categoryMap, categories.get(currentCategoryIndex).getCategoryIdentifier(), ingredientStackToNotice, resultStackToNotice);
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
            Registry<?> registry;
            List<Holder<?>> objects;
            if (type == VanillaEntryTypes.ITEM) {
                registry = Registry.ITEM;
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<ItemStack>castValue().getItem().builtInRegistryHolder());
            } else if (type == VanillaEntryTypes.FLUID) {
                registry = Registry.FLUID;
                objects = CollectionUtils.map(widget.getEntries(), stack -> stack.<FluidStack>castValue().getFluid().builtInRegistryHolder());
            } else continue;
            Stream<? extends TagKey<?>> collection = registry.getTags()
                    .filter(pair -> pair.getSecond().size() == objects.size())
                    .map(Pair::getFirst);
            TagKey<?> firstOrNull = CollectionUtils.findFirstOrNull(collection::iterator,
                    key -> CollectionUtils.allMatch(objects, holder -> ((Holder<Object>) holder).is((TagKey<Object>) key)));
            if (firstOrNull != null) {
                widget.tagMatch = firstOrNull.location();
            }
        }
    }
    
    private static final int MAX_WIDTH = 200;
    
    private void addCyclingTooltip(EntryWidget widget) {
        class TooltipProcessor implements UnaryOperator<Tooltip>, TooltipComponent, ClientTooltipComponent {
            @Override
            public Tooltip apply(Tooltip tooltip) {
                if (widget.getEntries().size() > 1) {
                    for (Tooltip.Entry entry : tooltip.entries()) {
                        if (entry.isTooltipComponent() && entry.getAsTooltipComponent() instanceof TooltipProcessor) {
                            return tooltip;
                        }
                    }
                    
                    tooltip.add(this);
                }
                return tooltip;
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            
            @Override
            public boolean equals(Object obj) {
                return obj instanceof TooltipProcessor;
            }
            
            @Override
            public int getHeight() {
                int entrySize = EntryListWidget.entrySize();
                int w = Math.max(1, MAX_WIDTH / entrySize);
                int height = Math.min(6, Mth.ceil(widget.getEntries().size() / (float) w)) * entrySize + 2;
                height += 12;
                if (widget.tagMatch != null) height += 12;
                return height;
            }
            
            @Override
            public int getWidth(Font font) {
                int entrySize = EntryListWidget.entrySize();
                int w = Math.max(1, MAX_WIDTH / entrySize);
                int size = widget.getEntries().size();
                int width = Math.min(size, w) * entrySize;
                width = Math.max(width, font.width(Component.translatable("text.rei.accepts")));
                if (widget.tagMatch != null) width = Math.max(width, font.width(Component.translatable("text.rei.tag_accept", widget.tagMatch.toString())));
                return width;
            }
            
            @Override
            public void renderImage(Font font, int x, int y, PoseStack poses, ItemRenderer renderer, int z) {
                int entrySize = EntryListWidget.entrySize();
                int w = Math.max(1, MAX_WIDTH / entrySize);
                int i = 0;
                poses.pushPose();
                poses.translate(0, 0, z + 50);
                for (EntryStack<?> entry : widget.getEntries()) {
                    int x1 = x + (i % w) * entrySize;
                    int y1 = y + 13 + (i / w) * entrySize;
                    i++;
                    if (i / w > 5) {
                        MultiBufferSource.BufferSource source = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                        Component text = Component.literal("+" + (widget.getEntries().size() - w * 6 + 1)).withStyle(ChatFormatting.GRAY);
                        font.drawInBatch(text, x1 + entrySize / 2 - font.width(text) / 2, y1 + entrySize / 2 - 1, -1, true, poses.last().pose(), source, false, 0, 15728880);
                        source.endBatch();
                        break;
                    } else {
                        entry.render(poses, new Rectangle(x1, y1, entrySize, entrySize), -1000, -1000, 0);
                    }
                }
                poses.popPose();
            }
            
            @Override
            public void renderText(Font font, int x, int y, Matrix4f pose, MultiBufferSource.BufferSource buffers) {
                font.drawInBatch(Component.translatable("text.rei.accepts").withStyle(ChatFormatting.GRAY),
                        x, y + 2, -1, true, pose, buffers, false, 0, 15728880);
                
                if (widget.tagMatch != null) {
                    int entrySize = EntryListWidget.entrySize();
                    int w = Math.max(1, MAX_WIDTH / entrySize);
                    font.drawInBatch(Component.translatable("text.rei.tag_accept", widget.tagMatch.toString())
                                    .withStyle(ChatFormatting.GRAY),
                            x, y + 16 + Math.min(6, Mth.ceil(widget.getEntries().size() / (float) w)) * entrySize,
                            -1, true, pose, buffers, false, 0, 15728880);
                }
            }
        }
        
        widget.tooltipProcessor(new TooltipProcessor());
    }
    
    private static ScreenOverlay getOverlay() {
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
        return super.mouseClicked(mouseX, mouseY, button) || (getOverlay().mouseClicked(mouseX, mouseY, button) && handleFocuses());
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
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return super.keyReleased(keyCode, scanCode, modifiers) || (getOverlay().keyReleased(keyCode, scanCode, modifiers) && handleFocuses());
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        return super.charTyped(character, modifiers) || (getOverlay().charTyped(character, modifiers) && handleFocuses());
    }
}
