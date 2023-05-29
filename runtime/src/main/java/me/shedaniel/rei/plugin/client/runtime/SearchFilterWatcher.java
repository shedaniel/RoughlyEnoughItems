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

package me.shedaniel.rei.plugin.client.runtime;

import com.google.common.collect.ImmutableList;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListSearchManager;
import me.shedaniel.rei.impl.client.search.AsyncSearchManager;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SearchFilterWatcher implements HintProvider {
    private Double lastProcess;
    
    @Override
    public List<Component> provide() {
        lastProcess = null;
        try {
            AsyncSearchManager searchManager = EntryListSearchManager.INSTANCE.getSearchManager();
            if (searchManager.executor != null) {
                if (searchManager.executor.future().isDone()) return Collections.emptyList();
                if (searchManager.executor.future().isCancelled()) return Collections.emptyList();
                if (searchManager.executor.future().isCompletedExceptionally()) return Collections.emptyList();
                AsyncSearchManager.Steps steps = searchManager.executor.steps();
                if (steps.startTime == 0 || steps.totalPartitions == 0) return Collections.emptyList();
                if (Util.getEpochMillis() - steps.startTime < 1000) return Collections.emptyList();
                lastProcess = steps.partitionsDone.get() / (double) steps.totalPartitions;
                return ImmutableList.of(Component.translatable("text.rei.searching"),
                        Component.translatable("text.rei.searching.step", Math.round(lastProcess * 100)));
            }
        } catch (NullPointerException ignored) {
        }
        return Collections.emptyList();
    }
    
    @Override
    @Nullable
    public Tooltip provideTooltip(Point mouse) {
        return null;
    }
    
    @Override
    @Nullable
    public Double getProgress() {
        return lastProcess;
    }
    
    @Override
    public Color getColor() {
        return Color.ofTransparent(0x5003fc24);
    }
    
    @Override
    public List<HintButton> getButtons() {
        return Collections.emptyList();
    }
}
