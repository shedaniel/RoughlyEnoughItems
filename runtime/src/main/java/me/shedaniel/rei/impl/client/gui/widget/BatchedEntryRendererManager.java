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

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BatchedEntryRendererManager<T extends EntryWidget> implements Iterable<T> {
    private final boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
    private final Int2ObjectMap<List<Object>> grouping = new Int2ObjectOpenHashMap<>();
    private final List<T> toRender = new ArrayList<>();
    
    public BatchedEntryRendererManager() {
    }
    
    public BatchedEntryRendererManager(Collection<? extends T> widgets) {
        addAll(widgets);
    }
    
    public boolean isFastEntryRendering() {
        return fastEntryRendering;
    }
    
    public void addAll(Collection<? extends T> widgets) {
        if (fastEntryRendering) {
            for (T widget : widgets) {
                add(widget);
            }
        } else {
            addAllSlow(widgets);
        }
    }
    
    public void add(T widget) {
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
                        return;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Adding entry");
                CrashReportUtils.renderer(report, currentEntry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        
        addSlow(widget);
    }
    
    public void addAllSlow(Collection<? extends T> widgets) {
        toRender.addAll(widgets);
    }
    
    public void addSlow(T widget) {
        toRender.add(widget);
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        render(false, null, null, graphics, mouseX, mouseY, delta);
    }
    
    public void render(boolean debugTime, MutableInt size, MutableLong time, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (fastEntryRendering) {
            for (List<Object> entries : grouping.values()) {
                Object[] extraData = new Object[entries.size() / 2];
                for (int i = 0; i < extraData.length; i++) {
                    extraData[i] = entries.get(i * 2 + 1);
                }
                renderBatched(debugTime, size, time, graphics, mouseX, mouseY, delta, () -> new AbstractIterator<T>() {
                    public int i = 0;
                    
                    @Override
                    protected T computeNext() {
                        if (i >= entries.size()) {
                            return endOfData();
                        }
                        T widget = (T) entries.get(i);
                        i += 2;
                        return widget;
                    }
                }, extraData);
            }
        }
        if (!toRender.isEmpty()) {
            renderSlow(debugTime, size, time, graphics, mouseX, mouseY, delta, toRender);
        }
    }
    
    public static <T extends EntryWidget> void renderEntries(boolean debugTime, MutableInt size, MutableLong time, boolean fastEntryRendering, GuiGraphics graphics, int mouseX, int mouseY, float delta, Collection<T> entries) {
        if (fastEntryRendering) {
            T firstWidget = Iterables.getFirst(entries, null);
            if (firstWidget == null) return;
            EntryRenderer<?> renderer = firstWidget.getCurrentEntry().getRenderer();
            if (renderer instanceof BatchedEntryRenderer) {
                BatchedEntryRenderer<?, Object> firstRenderer = (BatchedEntryRenderer<?, Object>) renderer;
                Object[] extraData = new Object[entries.size()];
                int i = 0;
                for (T entry : entries) {
                    EntryStack<?> currentEntry = entry.getCurrentEntry();
                    extraData[i++] = ((BatchedEntryRenderer<Object, Object>) currentEntry.getRenderer()).getExtraData(currentEntry.cast());
                }
                renderBatched(debugTime, size, time, graphics, mouseX, mouseY, delta, entries, extraData);
                return;
            }
        }
        renderSlow(debugTime, size, time, graphics, mouseX, mouseY, delta, entries);
    }
    
    private static <T extends EntryWidget> void renderBatched(boolean debugTime, MutableInt size, MutableLong time, GuiGraphics graphics, int mouseX, int mouseY, float delta, Iterable<T> entries, Object[] extraData) {
        T firstWidget = Iterables.getFirst(entries, null);
        if (firstWidget == null) return;
        @SuppressWarnings("rawtypes")
        EntryStack first = firstWidget.getCurrentEntry();
        EntryRenderer<?> renderer = first.getRenderer();
        BatchedEntryRenderer<?, Object> firstRenderer = (BatchedEntryRenderer<?, Object>) renderer;
        PoseStack newStack = firstRenderer.batchModifyMatrices(graphics.pose());
        graphics.pose().pushPose();
        graphics.pose().last().pose().set(newStack.last().pose());
        graphics.pose().last().normal().set(newStack.last().normal());
        long l = debugTime ? System.nanoTime() : 0;
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        int i = 0;
        for (T entry : entries) {
            try {
                entry.drawBackground(graphics, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry background");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        firstRenderer.startBatch(first, extraData[0], graphics, delta);
        for (T entry : entries) {
            try {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = entry.getCurrentEntry();
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, entry.getBounds().contains(mouseX, mouseY) ? 150 : 100);
                firstRenderer.renderBase(currentEntry, extraData[i++], graphics, immediate, entry.getInnerBounds(), mouseX, mouseY, delta);
                graphics.pose().popPose();
                if (debugTime && !currentEntry.isEmpty()) size.increment();
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry base");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        immediate.endBatch();
        firstRenderer.afterBase(first, extraData[0], graphics, delta);
        i = 0;
        for (T entry : entries) {
            try {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = entry.getCurrentEntry();
                firstRenderer.renderOverlay(currentEntry, extraData[i++], graphics, immediate, entry.getInnerBounds(), mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry base");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        immediate.endBatch();
        for (T entry : entries) {
            try {
                if (entry.containsMouse(mouseX, mouseY)) {
                    entry.queueTooltip(graphics, mouseX, mouseY, delta);
                    
                    if (entry.hasHighlight()) {
                        entry.drawHighlighted(graphics, mouseX, mouseY, delta);
                    }
                }
                entry.drawExtra(graphics, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry extra");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
        if (debugTime) time.add(System.nanoTime() - l);
        firstRenderer.endBatch(first, extraData[0], graphics, delta);
        graphics.pose().popPose();
    }
    
    public static <T extends EntryWidget> void renderSlow(boolean debugTime, MutableInt size, MutableLong time, GuiGraphics graphics, int mouseX, int mouseY, float delta, Iterable<T> entries) {
        for (T entry : entries) {
            if (entry.getCurrentEntry().isEmpty())
                continue;
            try {
                if (debugTime) {
                    size.increment();
                    long l = System.nanoTime();
                    entry.render(graphics, mouseX, mouseY, delta);
                    time.add(System.nanoTime() - l);
                } else entry.render(graphics, mouseX, mouseY, delta);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Rendering entry");
                CrashReportUtils.renderer(report, entry);
                CrashReportUtils.catchReport(report);
                return;
            }
        }
    }
    
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return Iterators.concat(toRender.iterator(), Iterators.concat(
                CollectionUtils.<List<Object>, Iterator<T>>map(grouping.values(), entries -> new AbstractIterator<>() {
                    public int i = 0;
                    
                    @Override
                    protected T computeNext() {
                        if (i >= entries.size()) {
                            return endOfData();
                        }
                        T widget = (T) entries.get(i);
                        i += 2;
                        return widget;
                    }
                }).iterator()
        ));
    }
}
