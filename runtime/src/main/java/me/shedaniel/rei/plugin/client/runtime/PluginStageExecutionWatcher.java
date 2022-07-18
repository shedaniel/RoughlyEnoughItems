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

package me.shedaniel.rei.plugin.client.runtime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.doubles.DoubleIntMutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleIntPair;
import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class PluginStageExecutionWatcher implements HintProvider {
    private final Map<PluginManager<?>, PluginManagerData> allStages = new HashMap<>();
    
    private static class PluginManagerData {
        private final PluginManager<?> manager;
        private final Map<ReloadStage, List<Reloadable<?>>> beganStages = new HashMap<>();
        private final Set<ReloadStage> finishedStages = new HashSet<>();
        
        public PluginManagerData(PluginManager<?> manager) {
            this.manager = manager;
        }
        
        private void clear() {
            beganStages.clear();
            finishedStages.clear();
        }
    }
    
    public <T extends REIPlugin<?>> Reloadable<? extends T> reloadable(PluginManager<?> manager) {
        return new Reloadable<>() {
            private PluginManagerData data() {
                return allStages.computeIfAbsent(manager, PluginManagerData::new);
            }
            
            @Override
            public void startReload() {
                for (ReloadStage stage : ReloadStage.values()) {
                    startReload(stage);
                }
            }
            
            @Override
            public void startReload(ReloadStage stage) {
                synchronized (allStages) {
                    if (manager == PluginManager.getInstance() && stage.ordinal() == 0) {
                        allStages.clear();
                    }
                    data().beganStages.put(stage, new ArrayList<>());
                }
            }
            
            @Override
            public void endReload() {
                for (ReloadStage stage : ReloadStage.values()) {
                    endReload(stage);
                }
            }
            
            @Override
            public void endReload(ReloadStage stage) {
                synchronized (allStages) {
                    data().finishedStages.add(stage);
                }
            }
            
            @Override
            public void beforeReloadable(ReloadStage stage, Reloadable<T> other) {
                synchronized (allStages) {
                    data().beganStages.get(stage).add(other);
                }
            }
        };
    }
    
    public Set<ReloadStage> notVisited() {
        synchronized (allStages) {
            Set<ReloadStage> notVisited = new HashSet<>(Arrays.asList(ReloadStage.values()));
            notVisited.removeIf(stage -> allStages.values().stream().allMatch(data -> data.finishedStages.contains(stage)));
            return notVisited;
        }
    }
    
    private int lastStep;
    private Double lastProgress;
    
    @Override
    public List<Component> provide() {
        List<ReloadStage> stages = Arrays.asList(ReloadStage.values());
        Set<ReloadStage> notVisited = notVisited();
        int allManagers = PluginManager.getActiveInstances().size();
        DoubleIntPair[] progresses = new DoubleIntPair[allManagers];
        Triple<PluginManager<?>, ReloadStage, Reloadable<?>> current = null;
        int i = 0;
        
        for (PluginManager<? extends REIPlugin<?>> manager : PluginManager.getActiveInstances()) {
            PluginManagerData data = allStages.get(manager);
            int index = i++;
            
            if (data == null) {
                progresses[index] = DoubleIntPair.of(0, 0);
                continue;
            }
            
            boolean allDone = data.finishedStages.size() == stages.size();
            if (allDone) {
                progresses[index] = DoubleIntPair.of(stages.size(), stages.size());
            } else {
                DoubleIntMutablePair pair = new DoubleIntMutablePair(0, 0);
                for (ReloadStage stage : stages) {
                    List<Reloadable<?>> reloadables = data.beganStages.get(stage);
                    pair.right(pair.rightInt() + 1);
                    
                    if (reloadables == null) {
                        continue;
                    }
                    
                    boolean finished = data.finishedStages.contains(stage);
                    
                    if (finished) {
                        pair.left(pair.leftDouble() + 1);
                    } else {
                        pair.left(pair.leftDouble() + (reloadables.size() / (double) manager.getReloadables().size()));
                        
                        if (!reloadables.isEmpty()) {
                            Reloadable<?> currentReloadable = Iterables.getLast(reloadables);
                            current = Triple.of(manager, stage, currentReloadable);
                        }
                    }
                }
                for (Map.Entry<ReloadStage, List<Reloadable<?>>> stageSetEntry : data.beganStages.entrySet()) {
                    ReloadStage stage = stageSetEntry.getKey();
                    
                }
                progresses[index] = pair;
            }
        }
        
        if (notVisited.isEmpty()) {
            lastProgress = null;
            return Collections.emptyList();
        } else {
            double total = 0;
            int j = 0;
            for (DoubleIntPair pair : progresses) {
                total += pair == null || pair.rightInt() == 0 ? 0 : pair.leftDouble() / pair.rightInt();
            }
            double average = total / progresses.length;
            lastProgress = average;
            String progress;
            String currentTask;
            if (current != null) {
                int managerIndex = PluginManager.getActiveInstances().indexOf(current.getLeft());
                lastStep = managerIndex + 1 + current.getMiddle().ordinal() * allManagers;
            }
            progress = String.format("Step %d/%d (%s%%):", lastStep, allManagers * stages.size(), Math.round(average * 100));
            if (current == null) {
                currentTask = "Waiting";
            } else {
                currentTask = getSimpleName(current.getRight().getClass());
            }
            return ImmutableList.of(Component.translatable("text.rei.not.fully.initialized"),
                    Component.literal(progress), Component.literal(currentTask));
        }
    }
    
    private static <P> String getSimpleName(Class<? extends P> clazz) {
        String name = clazz.getName();
        name = name.contains(".") ? StringUtils.substringAfterLast(name, ".") : name;
        name = name.replace("Impl", "");
        name = name.replace("$", ".");
        return name;
    }
    
    @Override
    @Nullable
    public Tooltip provideTooltip(Point mouse) {
        return Tooltip.create(mouse, Component.translatable("text.rei.not.fully.initialized.tooltip", notVisited().stream().map(Enum::name).collect(Collectors.joining(", "))));
    }
    
    @Override
    @Nullable
    public Double getProgress() {
        return lastProgress;
    }
    
    @Override
    public Color getColor() {
        return Color.ofTransparent(0x50ff1500);
    }
    
    @Override
    public List<HintButton> getButtons() {
        return Collections.emptyList();
    }
}
