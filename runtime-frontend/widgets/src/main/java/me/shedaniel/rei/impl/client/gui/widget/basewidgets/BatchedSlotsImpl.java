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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.BatchedSlots;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.MultiBufferSource;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.Nullable;

import java.util.*;

final class BatchedSlotsImpl extends BatchedSlots implements ForwardingList<Slot> {
    private final boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
    private final Int2ObjectMap<List<Object>> grouping = new Int2ObjectOpenHashMap<>();
    private final List<Slot> toRender = new ArrayList<>();
    private final List<Slot> delegateList = new DelegatedSlotList();
    private int size;
    public boolean debug;
    public MutableInt debugSize = new MutableInt();
    public MutableLong debugTime = new MutableLong();
    
    @Override
    public boolean isBatched() {
        return fastEntryRendering;
    }
    
    @Override
    public List<Slot> delegate() {
        return delegateList;
    }
    
    @Override
    public void addAllUnbatched(Collection<? extends Slot> slots) {
        this.toRender.addAll(slots);
        this.size += slots.size();
    }
    
    @Override
    public void addDebugger(MutableInt size, MutableLong time) {
        this.debug = true;
        this.debugSize = size;
        this.debugTime = time;
    }
    
    @Override
    public void addUnbatched(Slot slot) {
        this.toRender.add(slot);
        this.size++;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (this.debug) {
            render(false, null, null, matrices, mouseX, mouseY, delta);
        } else {
            render(true, debugSize, debugTime, matrices, mouseX, mouseY, delta);
        }
    }
    
    public void render(boolean debugTime, MutableInt size, MutableLong time, PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (fastEntryRendering) {
            for (List<Object> entries : grouping.values()) {
                Object[] extraData = new Object[entries.size() / 2];
                for (int i = 0; i < extraData.length; i++) {
                    extraData[i] = entries.get(i * 2 + 1);
                }
                renderBatched(debugTime, size, time, matrices, mouseX, mouseY, delta, groupingAsList(entries), extraData);
            }
        }
        if (!toRender.isEmpty()) {
            renderSlow(debugTime, size, time, matrices, mouseX, mouseY, delta, toRender);
        }
    }
    
    public List<Slot> groupingsAsList() {
        return CollectionUtils.concatUnmodifiable((Iterable<List<Slot>>) () -> new AbstractIterator<>() {
            final Iterator<List<Object>> groups = grouping.values().iterator();
            
            @Nullable
            @Override
            protected List<Slot> computeNext() {
                if (groups.hasNext()) {
                    return groupingAsList(groups.next());
                }
                return endOfData();
            }
        });
    }
    
    public List<Slot> groupingAsList(List<Object> entries) {
        return new AbstractList<>() {
            @Override
            public Slot get(int index) {
                return (Slot) entries.get(index * 2);
            }
            
            @Override
            public Iterator<Slot> iterator() {
                return new AbstractIterator<>() {
                    public int i = 0;
                    
                    @Override
                    protected Slot computeNext() {
                        if (i >= entries.size()) {
                            return endOfData();
                        }
                        Slot widget = (Slot) entries.get(i);
                        i += 2;
                        return widget;
                    }
                };
            }
            
            @Override
            public int size() {
                return entries.size() / 2;
            }
        };
    }
    
    public static void renderEntries(boolean debugTime, MutableInt size, MutableLong time, boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta, Collection<? extends Slot> entries) {
        if (fastEntryRendering) {
            Slot firstWidget = Iterables.getFirst(entries, null);
            if (firstWidget == null) return;
            EntryRenderer<?> renderer = firstWidget.getCurrentEntry().getRenderer();
            if (renderer instanceof BatchedEntryRenderer) {
                BatchedEntryRenderer<?, Object> firstRenderer = (BatchedEntryRenderer<?, Object>) renderer;
                Object[] extraData = new Object[entries.size()];
                int i = 0;
                for (Slot entry : entries) {
                    EntryStack<?> currentEntry = entry.getCurrentEntry();
                    extraData[i++] = ((BatchedEntryRenderer<Object, Object>) currentEntry.getRenderer()).getExtraData(currentEntry.cast());
                }
                renderBatched(debugTime, size, time, matrices, mouseX, mouseY, delta, entries, extraData);
                return;
            }
        }
        renderSlow(debugTime, size, time, matrices, mouseX, mouseY, delta, entries);
    }
    
    private static void renderBatched(boolean debugTime, MutableInt size, MutableLong time, PoseStack matrices, int mouseX, int mouseY, float delta, Iterable<? extends Slot> entries, Object[] extraData) {
        Slot firstWidget = Iterables.getFirst(entries, null);
        if (firstWidget == null) return;
        @SuppressWarnings("rawtypes")
        EntryStack first = firstWidget.getCurrentEntry();
        EntryRenderer<?> renderer = first.getRenderer();
        BatchedEntryRenderer<?, Object> firstRenderer = (BatchedEntryRenderer<?, Object>) renderer;
        matrices = firstRenderer.batchModifyMatrices(matrices);
        long l = debugTime ? System.nanoTime() : 0;
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        int i = 0;
        for (Slot entry : entries) {
            try {
                entry.drawBackground(matrices, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry background");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        firstRenderer.startBatch(first, extraData[0], matrices, delta);
        for (Slot entry : entries) {
            try {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = entry.getCurrentEntry();
                currentEntry.setZ(100);
                firstRenderer.renderBase(currentEntry, extraData[i++], matrices, immediate, entry.getInnerBounds(), mouseX, mouseY, delta);
                if (debugTime && !currentEntry.isEmpty()) size.increment();
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry base");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        immediate.endBatch();
        firstRenderer.afterBase(first, extraData[0], matrices, delta);
        i = 0;
        for (Slot entry : entries) {
            try {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = entry.getCurrentEntry();
                firstRenderer.renderOverlay(currentEntry, extraData[i++], matrices, immediate, entry.getInnerBounds(), mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry base");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        immediate.endBatch();
        for (Slot entry : entries) {
            try {
                if (entry.containsMouse(mouseX, mouseY)) {
                    Tooltip tooltip = entry.getCurrentTooltip(TooltipContext.of(new Point(mouseX, mouseY)));
                    if (tooltip != null) {
                        tooltip.queue();
                    }
                    
                    if (entry.isHighlightEnabled()) {
                        entry.drawHighlighted(matrices, mouseX, mouseY, delta);
                    }
                }
                
                entry.drawExtra(matrices, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry extra");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        if (debugTime) time.add(System.nanoTime() - l);
        firstRenderer.endBatch(first, extraData[0], matrices, delta);
    }
    
    public static void renderSlow(boolean debugTime, MutableInt size, MutableLong time, PoseStack matrices, int mouseX, int mouseY, float delta, Iterable<? extends Slot> entries) {
        for (Slot entry : entries) {
            if (entry.getCurrentEntry().isEmpty())
                continue;
            try {
                if (debugTime) {
                    size.increment();
                    long l = System.nanoTime();
                    entry.render(matrices, mouseX, mouseY, delta);
                    time.add(System.nanoTime() - l);
                } else entry.render(matrices, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return null;
    }
    
    private class DelegatedSlotList extends AbstractList<Slot> {
        private final List<Slot> unmodifiable = CollectionUtils.concatUnmodifiable(toRender,
                groupingsAsList());
        
        @Override
        public void add(int index, Slot element) {
            add(element);
        }
        
        @Override
        public int size() {
            return size;
        }
        
        @Override
        public boolean add(Slot widget) {
            if (fastEntryRendering) {
                EntryStack<?> currentEntry = widget.getCurrentEntry();
                try {
                    EntryRenderer<?> renderer = currentEntry.getRenderer();
                    if (renderer instanceof BatchedEntryRenderer) {
                        BatchedEntryRenderer<Object, Object> batchedRenderer = (BatchedEntryRenderer<Object, Object>) renderer;
                        EntryStack<Object> cast = currentEntry.cast();
                        if (batchedRenderer.isBatched(cast)) {
                            Object extraData = batchedRenderer.getExtraData(cast);
                            int hash = batchedRenderer.getBatchIdentifier(cast, widget.getBounds(), extraData)
                                       ^ widget.getCurrentEntry().getType().hashCode();
                            List<Object> entries = grouping.get(hash);
                            if (entries == null) {
                                grouping.put(hash, entries = new ArrayList<>());
                            }
                            entries.add(widget);
                            entries.add(extraData);
                            size++;
                            return true;
                        }
                    }
                } catch (Throwable throwable) {
                    CrashReport report = CrashReportUtils.essential(throwable, "Adding entry");
                    CrashReportUtils.renderer(report, currentEntry);
                    CrashReportUtils.catchReport(report);
                    return false;
                }
            }
            
            addUnbatched(widget);
            return true;
        }
        
        @Override
        public Slot get(int index) {
            return unmodifiable.get(index);
        }
        
        @Override
        public boolean addAll(Collection<? extends Slot> widgets) {
            if (fastEntryRendering) {
                for (Slot widget : widgets) {
                    add(widget);
                }
            } else {
                addAllUnbatched(widgets);
            }
            
            return true;
        }
    }
}
