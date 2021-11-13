/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PluginStageExecutionWatcher implements HintProvider {
    private final Map<PluginManager<?>, Set<ReloadStage>> allStages = new HashMap<>();
    
    public <T extends REIPlugin<?>> Reloadable<? extends T> reloadable(PluginManager<?> manager) {
        return new Reloadable<>() {
            @Override
            public void startReload() {
                for (ReloadStage stage : ReloadStage.values()) {
                    startReload(stage);
                }
            }
            
            @Override
            public void startReload(ReloadStage stage) {
                synchronized (allStages) {
                    Set<ReloadStage> stages = allStages.computeIfAbsent(manager, $ -> new HashSet<>());
                    if (stage.ordinal() == 0) stages.clear();
                    stages.add(stage);
                }
            }
        };
    }
    
    public Set<ReloadStage> notVisited() {
        synchronized (allStages) {
            Set<ReloadStage> notVisited = new HashSet<>(Arrays.asList(ReloadStage.values()));
            notVisited.removeIf(stage -> allStages.values().stream().allMatch(stages -> stages.contains(stage)));
            return notVisited;
        }
    }
    
    @Override
    public List<Component> provide() {
        Set<ReloadStage> notVisited = notVisited();
        if (notVisited.isEmpty()) {
            return Collections.emptyList();
        } else {
            return ImmutableList.of(new TranslatableComponent("text.rei.not.fully.initialized"));
        }
    }
    
    @Override
    @Nullable
    public Tooltip provideTooltip(Point mouse) {
        return Tooltip.create(mouse, new TranslatableComponent("text.rei.not.fully.initialized.tooltip", notVisited().stream().map(Enum::name).collect(Collectors.joining(", "))));
    }
    
    @Override
    public Color getColor() {
        return Color.ofTransparent(0x50ff1500);
    }
}
