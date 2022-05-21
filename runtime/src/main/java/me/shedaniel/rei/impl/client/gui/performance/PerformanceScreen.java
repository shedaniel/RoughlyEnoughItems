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

package me.shedaniel.rei.impl.client.gui.performance;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.impl.client.gui.performance.entry.PerformanceEntryImpl;
import me.shedaniel.rei.impl.client.gui.performance.entry.SubCategoryListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.*;

@Environment(EnvType.CLIENT)
public class PerformanceScreen extends Screen {
    private Screen parent;
    
    public PerformanceScreen(Screen parent) {
        super(Component.translatable("text.rei.performance"));
        this.parent = parent;
    }
    
    private PerformanceEntryListWidget list;
    
    /*
     * Copyright (C) 2008 The Guava Authors
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
     * in compliance with the License. You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software distributed under the License
     * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
     * or implied. See the License for the specific language governing permissions and limitations under
     * the License.
     */
    public static FormattedCharSequence formatTime(long time, boolean total) {
        TimeUnit unit = chooseUnit(time);
        double value = (double) time / NANOSECONDS.convert(1, unit);
        return Component.literal(String.format(Locale.ROOT, "%.4g", value) + " " + abbreviate(unit))
                .withStyle(style -> style.withColor(TextColor.fromRgb(chooseColor(MILLISECONDS.convert(time, NANOSECONDS), total))))
                .getVisualOrderText();
    }
    
    private static int chooseColor(long time, boolean total) {
        if (time > (total ? 2500 : 1000)) {
            return 0xff5555;
        } else if (time > (total ? 700 : 300)) {
            return 0xffa600;
        } else if (time > (total ? 200 : 100)) {
            return 0xfff017;
        }
        return 0x12ff22;
    }
    
    private static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }
    
    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }
    
    @Override
    public void init() {
        {
            Component backText = Component.literal("↩ ").append(Component.translatable("gui.back"));
            addRenderableWidget(new Button(4, 4, Minecraft.getInstance().font.width(backText) + 10, 20, backText, button -> {
                minecraft.setScreen(parent);
                this.parent = null;
            }));
        }
        list = new PerformanceEntryListWidget();
        long[] totalTime = {0};
        RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.getStages().forEach((stage, inner) -> {
            List<PerformanceEntryImpl> entries = new ArrayList<>();
            inner.times().forEach((obj, time) -> {
                entries.add(new PerformanceEntryImpl(Component.literal(obj instanceof Pair ? ((Pair<REIPluginProvider<?>, REIPlugin<?>>) obj).getFirst().getPluginProviderName() : Objects.toString(obj)), time));
            });
            Collection<Long> values = inner.times().values();
            long separateTime;
            synchronized (inner.times()) {
                separateTime = values.stream().collect(Collectors.summarizingLong(value -> value)).getSum();
            }
            if ((inner.totalNano() - separateTime) > 1000000) {
                entries.add(new PerformanceEntryImpl(Component.literal("Miscellaneous Operations"), inner.totalNano() - separateTime));
            }
            totalTime[0] += Math.max(inner.totalNano(), separateTime);
            entries.sort(Comparator.<PerformanceEntryImpl>comparingLong(value -> value.time).reversed());
            list.addItem(new SubCategoryListEntry(Component.literal(stage), (List<PerformanceScreen.PerformanceEntry>) (List<? extends PerformanceScreen.PerformanceEntry>) entries, Math.max(inner.totalNano(), separateTime), false));
        });
        list.children().add(0, new PerformanceEntryImpl(Component.literal("Total Load Time"), totalTime[0]));
        addWidget(list);
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        renderDirtBackground(0);
        list.render(poses, mouseX, mouseY, delta);
        this.font.drawShadow(poses, this.title.getVisualOrderText(), this.width / 2.0F - this.font.width(this.title) / 2.0F, 12.0F, -1);
        super.render(poses, mouseX, mouseY, delta);
    }
    
    public static abstract class PerformanceEntry extends DynamicElementListWidget.ElementEntry<PerformanceEntry> {
    }
    
    private class PerformanceEntryListWidget extends DynamicElementListWidget<PerformanceEntry> {
        public PerformanceEntryListWidget() {super(PerformanceScreen.this.minecraft, PerformanceScreen.this.width, PerformanceScreen.this.height, 30, PerformanceScreen.this.height, GuiComponent.BACKGROUND_LOCATION);}
        
        @Override
        public int getItemWidth() {
            return width;
        }
        
        @Override
        public int addItem(PerformanceEntry item) {
            return super.addItem(item);
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 6;
        }
    }
}
