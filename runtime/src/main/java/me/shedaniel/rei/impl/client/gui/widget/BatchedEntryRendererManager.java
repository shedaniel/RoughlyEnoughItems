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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BatchedEntryRendererManager {
    private boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
    private Int2ObjectMap<List<EntryWidget>> grouping = new Int2ObjectOpenHashMap<>();
    private List<EntryWidget> toRender = new ArrayList<>();
    
    public BatchedEntryRendererManager() {
    }
    
    public BatchedEntryRendererManager(Collection<? extends EntryWidget> widgets) {
        addAll(widgets);
    }
    
    public void addAll(Collection<? extends EntryWidget> widgets) {
        if (fastEntryRendering) {
            for (EntryWidget widget : widgets) {
                add(widget);
            }
        } else {
            toRender.addAll(widgets);
        }
    }
    
    public void add(EntryWidget widget) {
        if (fastEntryRendering) {
            EntryStack<?> currentEntry = widget.getCurrentEntry();
            EntryRenderer<?> renderer = currentEntry.getRenderer();
            if (renderer instanceof BatchedEntryRenderer) {
                int hash = ((BatchedEntryRenderer<Object>) renderer).getBatchIdentifier(currentEntry.cast(), widget.getBounds());
                List<EntryWidget> entries = grouping.get(hash);
                if (entries == null) {
                    grouping.put(hash, entries = new ArrayList<>());
                }
                entries.add(widget);
                return;
            }
        }
        toRender.add(widget);
    }
    
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        render(false, null, null, matrices, mouseX, mouseY, delta);
    }
    
    public void render(boolean debugTime, MutableInt size, MutableLong time, PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (fastEntryRendering) {
            for (List<EntryWidget> entries : grouping.values()) {
                renderEntries(debugTime, size, time, fastEntryRendering, matrices, mouseX, mouseY, delta, entries);
            }
        }
        if (!toRender.isEmpty()) {
            renderEntries(debugTime, size, time, fastEntryRendering, matrices, mouseX, mouseY, delta, toRender);
        }
    }
    
    
    public static <T extends EntryWidget> void renderEntries(boolean debugTime, MutableInt size, MutableLong time, boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta, Iterable<T> entries) {
        T firstWidget = Iterables.getFirst(entries, null);
        if (firstWidget == null) return;
        @SuppressWarnings("rawtypes")
        EntryStack first = firstWidget.getCurrentEntry();
        EntryRenderer<?> renderer = first.getRenderer();
        if (fastEntryRendering && renderer instanceof BatchedEntryRenderer) {
            BatchedEntryRenderer<?> firstRenderer = (BatchedEntryRenderer<?>) renderer;
            matrices = firstRenderer.batchModifyMatrices(matrices);
            firstRenderer.startBatch(first, matrices, delta);
            long l = debugTime ? System.nanoTime() : 0;
            MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
            for (T listEntry : entries) {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = listEntry.getCurrentEntry();
                currentEntry.setZ(100);
                listEntry.drawBackground(matrices, mouseX, mouseY, delta);
                firstRenderer.renderBase(currentEntry, matrices, immediate, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                if (debugTime && !currentEntry.isEmpty()) size.increment();
            }
            immediate.endBatch();
            for (T listEntry : entries) {
                @SuppressWarnings("rawtypes")
                EntryStack currentEntry = listEntry.getCurrentEntry();
                firstRenderer.renderOverlay(currentEntry, matrices, immediate, listEntry.getInnerBounds(), mouseX, mouseY, delta);
                if (listEntry.containsMouse(mouseX, mouseY)) {
                    listEntry.queueTooltip(matrices, mouseX, mouseY, delta);
                    listEntry.drawHighlighted(matrices, mouseX, mouseY, delta);
                }
            }
            immediate.endBatch();
            if (debugTime) time.add(System.nanoTime() - l);
            firstRenderer.endBatch(first, matrices, delta);
        } else {
            for (T entry : entries) {
                if (entry.getCurrentEntry().isEmpty())
                    continue;
                if (debugTime) {
                    size.increment();
                    long l = System.nanoTime();
                    entry.render(matrices, mouseX, mouseY, delta);
                    time.add(System.nanoTime() - l);
                } else entry.render(matrices, mouseX, mouseY, delta);
            }
        }
    }
}
