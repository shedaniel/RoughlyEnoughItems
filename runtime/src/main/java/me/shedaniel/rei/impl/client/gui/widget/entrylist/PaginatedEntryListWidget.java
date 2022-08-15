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

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.InternalTextures;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.changelog.ChangelogLoader;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.DefaultDisplayChoosePageWidget;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class PaginatedEntryListWidget extends CollapsingEntryListWidget {
    private Button leftButton, rightButton, changelogButton;
    private List<Widget> additionalWidgets;
    private List</*EntryStack<?> | EntryIngredient*/ Object> stacks = new ArrayList<>();
    protected List<EntryListStackEntry> entries = Collections.emptyList();
    private int page;
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    @Override
    protected void renderEntries(boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.leftButton.setEnabled(getTotalPages() > 1);
        this.rightButton.setEnabled(getTotalPages() > 1);
        
        if (ConfigObject.getInstance().doesCacheEntryRendering()) {
            for (EntryListStackEntry entry : entries) {
                CollapsedStack collapsedStack = entry.getCollapsedStack();
                if (collapsedStack != null && !collapsedStack.isExpanded()) {
                    continue;
                }
                
                if (entry.our == null) {
                    CachedEntryListRender.Sprite sprite = CachedEntryListRender.get(entry.getCurrentEntry());
                    if (sprite != null) {
                        CachingEntryRenderer renderer = new CachingEntryRenderer(sprite, this::getBlitOffset);
                        entry.our = ClientEntryStacks.setRenderer(entry.getCurrentEntry().copy().cast(), stack -> renderer);
                    }
                }
            }
        }
        
        BatchedEntryRendererManager manager = new BatchedEntryRendererManager();
        if (manager.isFastEntryRendering()) {
            for (EntryListStackEntry entry : entries) {
                CollapsedStack collapsedStack = entry.getCollapsedStack();
                if (collapsedStack != null && !collapsedStack.isExpanded()) {
                    manager.addSlow(entry);
                } else {
                    manager.add(entry);
                }
            }
        } else {
            manager.addAllSlow(entries);
        }
        manager.render(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta);
        
        for (Widget widget : additionalWidgets) {
            widget.render(matrices, mouseX, mouseY, delta);
        }
    }
    
    public int getTotalPages() {
        return Mth.ceil(stacks.size() / (float) entries.size());
    }
    
    @Override
    protected void updateEntries(int entrySize, boolean zoomed) {
        page = Math.max(page, 0);
        List<EntryListStackEntry> entries = Lists.newArrayList();
        int width = innerBounds.width / entrySize;
        int height = innerBounds.height / entrySize;
        for (int currentY = 0; currentY < height; currentY++) {
            for (int currentX = 0; currentX < width; currentX++) {
                int slotX = currentX * entrySize + innerBounds.x;
                int slotY = currentY * entrySize + innerBounds.y;
                if (notSteppingOnExclusionZones(slotX - 1, slotY - 1, entrySize, entrySize)) {
                    entries.add((EntryListStackEntry) new EntryListStackEntry(this, slotX, slotY, entrySize, zoomed).noBackground());
                }
            }
        }
        page = Math.max(Math.min(page, getTotalPages() - 1), 0);
        int skip = Math.max(0, page * entries.size());
        List</*EntryStack<?> | List<EntryStack<?>>*/ Object> subList = stacks.stream().skip(skip).limit(Math.max(0, entries.size() - Math.max(0, -page * entries.size()))).toList();
        Int2ObjectMap<CollapsedStack> indexedCollapsedStack = getCollapsedStackIndexed();
        for (int i = 0; i < subList.size(); i++) {
            /*EntryStack<?> | List<EntryStack<?>>*/
            Object stack = subList.get(i);
            EntryListStackEntry entry = entries.get(i + Math.max(0, -page * entries.size()));
            entry.clearStacks();
            
            if (stack instanceof EntryStack<?> entryStack) {
                entry.entry(entryStack);
            } else {
                entry.entries((List<EntryStack<?>>) stack);
            }
            
            CollapsedStack collapsedStack = indexedCollapsedStack.get(i + skip);
            if (collapsedStack != null && collapsedStack.getIngredient().size() > 1) {
                entry.collapsed(collapsedStack);
            } else {
                entry.collapsed(null);
            }
        }
        this.entries = entries;
    }
    
    @Override
    public List</*EntryStack<?> | List<EntryStack<?>>*/ Object> getStacks() {
        return stacks;
    }
    
    @Override
    public void setStacks(List</*EntryStack<?> | List<EntryStack<?>>*/ Object> stacks) {
        this.stacks = stacks;
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        return entries.stream().map(EntryWidget::getCurrentEntry);
    }
    
    @Override
    protected List<EntryListStackEntry> getEntryWidgets() {
        return entries;
    }
    
    @Override
    public List<? extends Widget> children() {
        return CollectionUtils.concatUnmodifiable(super.children(), additionalWidgets);
    }
    
    @Override
    public void init(ScreenOverlayImpl overlay) {
        Rectangle overlayBounds = overlay.getBounds();
        this.additionalWidgets = new ArrayList<>();
        this.leftButton = Widgets.createButton(new Rectangle(overlayBounds.x, overlayBounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.literal(""))
                .onClick(button -> {
                    setPage(getPage() - 1);
                    if (getPage() < 0)
                        setPage(getTotalPages() - 1);
                    updateEntriesPosition();
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLine(Component.translatable("text.rei.previous_page"))
                .focusable(false);
        this.additionalWidgets.add(leftButton);
        this.additionalWidgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            helper.setBlitOffset(helper.getBlitOffset() + 1);
            RenderSystem.setShaderTexture(0, InternalTextures.ARROW_LEFT_TEXTURE);
            Rectangle bounds = leftButton.getBounds();
            matrices.pushPose();
            blit(matrices, bounds.x + 4, bounds.y + 4, 0, 0, 8, 8, 8, 8);
            matrices.popPose();
            helper.setBlitOffset(helper.getBlitOffset() - 1);
        }));
        this.changelogButton = Widgets.createButton(new Rectangle(overlayBounds.getMaxX() - 18 - 18, overlayBounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.translatable(""))
                .onClick(button -> {
                    ChangelogLoader.show();
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLine(Component.translatable("text.rei.changelog.title"))
                .focusable(false);
        this.additionalWidgets.add(changelogButton);
        this.additionalWidgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            helper.setBlitOffset(helper.getBlitOffset() + 1);
            RenderSystem.setShaderTexture(0, InternalTextures.CHEST_GUI_TEXTURE);
            Rectangle bounds = changelogButton.getBounds();
            matrices.pushPose();
            matrices.translate(0.5f, 0, 0);
            helper.blit(matrices, bounds.x + 1, bounds.y + 2, !ChangelogLoader.hasVisited() ? 28 : 14, 0, 14, 14);
            matrices.popPose();
            helper.setBlitOffset(helper.getBlitOffset() - 1);
        }));
        this.rightButton = Widgets.createButton(new Rectangle(overlayBounds.getMaxX() - 18, overlayBounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 5, 16, 16), Component.literal(""))
                .onClick(button -> {
                    setPage(getPage() + 1);
                    if (getPage() >= getTotalPages())
                        setPage(0);
                    updateEntriesPosition();
                })
                .containsMousePredicate((button, point) -> button.getBounds().contains(point) && overlay.isNotInExclusionZones(point.x, point.y))
                .tooltipLine(Component.translatable("text.rei.next_page"))
                .focusable(false);
        this.additionalWidgets.add(rightButton);
        this.additionalWidgets.add(Widgets.createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            helper.setBlitOffset(helper.getBlitOffset() + 1);
            RenderSystem.setShaderTexture(0, InternalTextures.ARROW_RIGHT_TEXTURE);
            Rectangle bounds = rightButton.getBounds();
            matrices.pushPose();
            blit(matrices, bounds.x + 4, bounds.y + 4, 0, 0, 8, 8, 8, 8);
            matrices.popPose();
            helper.setBlitOffset(helper.getBlitOffset() - 1);
        }));
        this.additionalWidgets.add(Widgets.createClickableLabel(new Point(overlayBounds.x + ((overlayBounds.width - 18) / 2), overlayBounds.y + (ConfigObject.getInstance().getSearchFieldLocation() == SearchFieldLocation.TOP_SIDE ? 24 : 0) + 10), Component.empty(), label -> {
            if (!Screen.hasShiftDown()) {
                setPage(0);
                updateEntriesPosition();
            } else {
                ScreenOverlayImpl.getInstance().choosePageWidget = new DefaultDisplayChoosePageWidget(page -> {
                    setPage(page);
                    updateEntriesPosition();
                }, getPage(), getTotalPages());
            }
        }).tooltip(Component.translatable("text.rei.go_back_first_page"), Component.literal(" "), Component.translatable("text.rei.shift_click_to", Component.translatable("text.rei.choose_page")).withStyle(ChatFormatting.GRAY)).focusable(false).onRender((matrices, label) -> {
            label.setClickable(getTotalPages() > 1);
            label.setMessage(Component.literal(String.format("%s/%s", getPage() + 1, Math.max(getTotalPages(), 1))));
        }).rainbow(new Random().nextFloat() < 1.0E-4D || ClientHelperImpl.getInstance().isAprilFools.get()));
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (super.mouseScrolled(mouseX, mouseY, amount)) return true;
        if (!Screen.hasControlDown()) {
            if (amount > 0 && leftButton.isEnabled())
                leftButton.onClick();
            else if (amount < 0 && rightButton.isEnabled())
                rightButton.onClick();
            else
                return false;
            return true;
        }
        return false;
    }
}
