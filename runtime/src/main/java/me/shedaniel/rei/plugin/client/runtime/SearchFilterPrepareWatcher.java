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
import me.shedaniel.rei.impl.client.search.argument.Argument;
import me.shedaniel.rei.impl.client.search.argument.ArgumentCache;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SearchFilterPrepareWatcher implements HintProvider {
    private Double lastProcess;
    
    @Override
    public List<Component> provide() {
        lastProcess = null;
        try {
            ArgumentCache cache = Argument.cache;
            if (cache.currentStep != null && cache.prepareStacks != null && cache.prepareStacks.size() > 100
                    && cache.prepareStart != null) {
                if (Util.getEpochMillis() - cache.prepareStart < 100) return Collections.emptyList();
                int prepareStageCurrent = cache.currentStep.step;
                int prepareStageTotal = cache.currentStep.totalSteps;
                ArgumentCache.CurrentStep.Step currentStage = ArrayUtils.get(cache.currentStep.steps, prepareStageCurrent - 1);
                int currentStageCurrent = currentStage == null ? 0 : currentStage.stacks;
                int currentStageTotal = currentStage == null ? 0 : currentStage.totalStacks;
                double prepareStageProgress = prepareStageTotal == 0 ? 0 : prepareStageCurrent / (double) prepareStageTotal;
                double currentStageProgress = currentStageTotal == 0 ? 0 : currentStageCurrent / (double) currentStageTotal;
                double lastProcess = prepareStageTotal == 0 ? 0 : Math.max(0, prepareStageProgress - (1 - currentStageProgress) / prepareStageTotal);
                if (lastProcess < 0.05 || lastProcess > 0.95) return Collections.emptyList();
                this.lastProcess = lastProcess;
                return ImmutableList.of(Component.translatable("text.rei.caching.search"),
                        Component.translatable("text.rei.caching.search.step", prepareStageCurrent, prepareStageTotal, Math.round(lastProcess * 100)));
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
        return Color.ofTransparent(0xffde38ff);
    }
    
    @Override
    public List<HintButton> getButtons() {
        return Collections.emptyList();
    }
}
