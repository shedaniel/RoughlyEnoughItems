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
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.BatchedEntryRendererManager;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginatedEntryListWidget extends EntryListWidget {
    private List<EntryStack<?>> stacks = new ArrayList<>();
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
                if (entry.our == null) {
                    CachedEntryListRender.Sprite sprite = CachedEntryListRender.get(entry.getCurrentEntry());
                    if (sprite != null) {
                        entry.our = ClientEntryStacks.setRenderer(entry.getCurrentEntry().copy().cast(), stack -> new EntryRenderer<Object>() {
                            @Override
                            public void render(EntryStack<Object> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                                RenderSystem.setShaderTexture(0, CachedEntryListRender.cachedTextureLocation);
                                innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getBlitOffset(), sprite.u0, sprite.u1, sprite.v0, sprite.v1);
                            }
                            
                            @Override
                            @Nullable
                            public Tooltip getTooltip(EntryStack<Object> entry, TooltipContext context) {
                                return stack.getDefinition().getRenderer().getTooltip(entry.cast(), context);
                            }
                        });
                    }
                }
            }
            
            BatchedEntryRendererManager.renderSlow(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta, entries);
        } else {
            new BatchedEntryRendererManager(entries).render(debugger.debugTime, debugger.size, debugger.time, matrices, mouseX, mouseY, delta);
        }
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
        List<EntryStack<?>> subList = stacks.stream().skip(Math.max(0, page * entries.size())).limit(Math.max(0, entries.size() - Math.max(0, -page * entries.size()))).collect(Collectors.toList());
        for (int i = 0; i < subList.size(); i++) {
            EntryStack<?> stack = subList.get(i);
            entries.get(i + Math.max(0, -page * entries.size())).clearStacks().entry(stack);
        }
        this.entries = entries;
    }
    
    @Override
    public List<EntryStack<?>> getStacks() {
        return stacks;
    }
    
    @Override
    public void setStacks(List<EntryStack<?>> stacks) {
        this.stacks = stacks;
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        return entries.stream().map(EntryWidget::getCurrentEntry);
    }
}
