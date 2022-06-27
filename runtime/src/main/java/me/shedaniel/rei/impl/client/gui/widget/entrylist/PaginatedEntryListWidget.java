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
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.common.entry.type.collapsed.CollapsedStack;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PaginatedEntryListWidget extends CollapsingEntryListWidget {
    private List</*EntryStack<?> | EntryIngredient*/ Object> stacks = new ArrayList<>();
    protected List<EntryListStackEntry> entries = Collections.emptyList();
    private int page;
    
    @Override
    public int getPage() {
        return page;
    }
    
    @Override
    public void setPage(int page) {
        this.page = page;
    }
    
    @Override
    protected void renderEntries(boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta) {
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
    }
    
    @Override
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
            
            entry.collapsed(indexedCollapsedStack.get(i + skip));
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
}
