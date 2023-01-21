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

package me.shedaniel.rei.impl.common.logging.performance;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PerformanceLoggerImpl implements PerformanceLogger {
    private final Map<String, PluginImpl> stages = Collections.synchronizedMap(Maps.newLinkedHashMap());
    
    @Override
    public Plugin stage(String stage) {
        PluginImpl plugin = stages.computeIfAbsent(stage, $ -> new PluginImpl());
        plugin.stopwatch.start();
        return plugin;
    }
    
    @Override
    public Map<String, Plugin> getStages() {
        return (Map<String, Plugin>) (Map<String, ? extends Plugin>) stages;
    }
    
    private static class PluginImpl implements Plugin {
        private final Stopwatch stopwatch = Stopwatch.createUnstarted();
        private long totalTime = 0;
        private Object2LongMap<Object> times = Object2LongMaps.synchronize(new Object2LongLinkedOpenHashMap<>());
        
        @Override
        public Inner stage(String stage) {
            Stopwatch s = Stopwatch.createStarted();
            return () -> {
                s.stop();
                times.put(stage, times.getOrDefault(stage, 0) + s.elapsed(TimeUnit.NANOSECONDS));
            };
        }
        
        @Override
        public Inner plugin(Pair<REIPluginProvider<?>, REIPlugin<?>> plugin) {
            Stopwatch s = Stopwatch.createStarted();
            return () -> {
                s.stop();
                times.put(plugin, times.getOrDefault(plugin, 0) + s.elapsed(TimeUnit.NANOSECONDS));
            };
        }
        
        @Override
        public void close() {
            stopwatch.stop();
            totalTime += stopwatch.elapsed(TimeUnit.NANOSECONDS);
            stopwatch.reset();
        }
        
        @Override
        public long totalNano() {
            return totalTime;
        }
        
        @Override
        public Map<Object, Long> times() {
            return times;
        }
    }
    
    @Override
    public void clear() {
        stages.clear();
    }
}
