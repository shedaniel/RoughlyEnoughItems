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
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class EntryRendererManager<T extends EntryWidget> implements Iterable<T> {
    private final Int2ObjectMap<List<Object>> grouping = new Int2ObjectOpenHashMap<>();
    private final List<T> toRender = new ArrayList<>();
    
    public EntryRendererManager() {
    }
    
    public EntryRendererManager(Collection<? extends T> widgets) {
        addAll(widgets);
    }
    
    public void addAll(Collection<? extends T> widgets) {
        toRender.addAll(widgets);
    }
    
    public void add(T widget) {
        toRender.add(widget);
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        render(false, null, null, graphics, mouseX, mouseY, delta);
    }
    
    public void render(boolean debugTime, MutableInt size, MutableLong time, GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!toRender.isEmpty()) {
            renderEntries(debugTime, size, time, graphics, mouseX, mouseY, delta, toRender);
        }
    }
    
    public static <T extends EntryWidget> void renderEntries(boolean debugTime, MutableInt size, MutableLong time, GuiGraphics graphics, int mouseX, int mouseY, float delta, Iterable<T> entries) {
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
