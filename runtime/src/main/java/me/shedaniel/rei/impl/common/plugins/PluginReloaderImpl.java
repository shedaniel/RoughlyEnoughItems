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

package me.shedaniel.rei.impl.common.plugins;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class PluginReloaderImpl<P extends REIPlugin<?>> {
    @Nullable
    private ReloadStage reloading = null;
    private List<ReloadStage> observedStages = new ArrayList<>();
    private final Stopwatch reloadStopwatch = Stopwatch.createUnstarted();
    private boolean forcedMainThread;
    private final Stopwatch forceMainThreadStopwatch = Stopwatch.createUnstarted();
    
    public boolean isReloading() {
        return reloading != null;
    }
    
    @FunctionalInterface
    private interface SectionPluginSink {
        void accept(boolean respectMainThread, Runnable task);
    }
    
    private static class SectionClosable implements Closeable {
        private ReloadStage stage;
        private MutablePair<Stopwatch, String> sectionData;
        
        public SectionClosable(ReloadStage stage, String section) {
            this.stage = stage;
            this.sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
            sectionData.setRight(section);
            InternalLogger.getInstance().trace("[" + stage + "] Reloading Section: \"%s\"", section);
            sectionData.getLeft().reset().start();
        }
        
        @Override
        public void close() {
            sectionData.getLeft().stop();
            String section = sectionData.getRight();
            InternalLogger.getInstance().trace("[" + stage + "] Reloading Section: \"%s\" done in %s", section, sectionData.getLeft().toString());
            sectionData.getLeft().reset();
        }
    }
    
    private SectionClosable section(ReloadStage stage, String section) {
        return new SectionClosable(stage, section);
    }
    
    private void pluginSection(ReloadStage stage, String sectionName, List<PluginManagerImpl.PluginWrapper<P>> list, @Nullable Reloadable<?> reloadable, BiConsumer<PluginManagerImpl.PluginWrapper<P>, SectionPluginSink> consumer) {
        for (PluginManagerImpl.PluginWrapper<P> wrapper : list) {
            try (SectionClosable section = section(stage, sectionName + wrapper.getPluginProviderName() + "/")) {
                consumer.accept(wrapper, (respectMainThread, runnable) -> {
                    if (!respectMainThread || reloadable == null || !wrapper.plugin().shouldBeForcefullyDoneOnMainThread(reloadable)) {
                        runnable.run();
                    } else {
                        try {
                            forcedMainThread = true;
                            forceMainThreadStopwatch.start();
                            InternalLogger.getInstance().warn("Forcing plugin " + wrapper.getPluginProviderName() + " to run on the main thread for " + sectionName + "! This is extremely dangerous, and have large performance implications.");
                            if (Platform.getEnvironment() == Env.CLIENT) {
                                EnvExecutor.runInEnv(Env.CLIENT, () -> () -> queueExecutionClient(runnable));
                            } else {
                                queueExecution(runnable);
                            }
                        } finally {
                            forceMainThreadStopwatch.stop();
                        }
                    }
                });
            } catch (Throwable throwable) {
                InternalLogger.getInstance().error(wrapper.getPluginProviderName() + " plugin failed to " + sectionName + "!", throwable);
            }
        }
    }
    
    private void queueExecution(Runnable runnable) {
        MinecraftServer server = GameInstance.getServer();
        if (server != null) {
            server.executeBlocking(runnable);
        }
    }
    
    private void queueExecutionClient(Runnable runnable) {
        Minecraft.getInstance().executeBlocking(runnable);
    }
    
    public void pre(PluginManager<P> manager, Class<P> pluginClass, List<PluginManagerImpl.PluginWrapper<P>> plugins, ReloadStage stage) {
        this.forcedMainThread = false;
        this.forceMainThreadStopwatch.reset();
        this.reloadStopwatch.reset().start();
        this.observedStages.clear();
        this.observedStages.add(stage);
        plugins = new ArrayList<>(plugins);
        plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper<P>::getPriority).reversed());
        Collections.reverse(plugins);
        try (SectionClosable preRegister = section(stage, "pre-register/");
             PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Pre Registration")) {
            pluginSection(stage, "pre-register/", plugins, null, (plugin, sink) -> {
                try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider(), plugin.plugin()))) {
                    sink.accept(false, () -> {
                        ((REIPlugin<P>) plugin.plugin()).preStage(manager, stage);
                    });
                }
            });
        } catch (Throwable throwable) {
            new RuntimeException("Failed to run pre registration").printStackTrace();
        }
        this.reloadStopwatch.stop();
    }
    
    public void post(PluginManager<P> manager, Class<P> pluginClass, List<PluginManagerImpl.PluginWrapper<P>> plugins, ReloadStage stage) {
        this.reloadStopwatch.start();
        plugins = new ArrayList<>(plugins);
        plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper<P>::getPriority).reversed());
        Collections.reverse(plugins);
        try (SectionClosable postRegister = section(stage, "post-register/");
             PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Post Registration")) {
            pluginSection(stage, "post-register/", plugins, null, (plugin, sink) -> {
                try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider(), plugin.plugin()))) {
                    sink.accept(false, () -> {
                        ((REIPlugin<P>) plugin.plugin()).postStage(manager, stage);
                    });
                }
            });
        } catch (Throwable throwable) {
            new RuntimeException("Failed to run post registration").printStackTrace();
        }
        this.reloadStopwatch.stop();
        InternalLogger.getInstance().info("Reloaded Plugin Manager [%s] with %d plugins in %s.", pluginClass.getSimpleName(), plugins.size(), reloadStopwatch);
        if (forcedMainThread) {
            InternalLogger.getInstance().warn("Forcing plugins to run on main thread took " + forceMainThreadStopwatch);
        }
    }
    
    public void startReload(PluginManager<P> manager, Class<P> pluginClass, List<PluginManagerImpl.PluginWrapper<P>> plugins, ReloadStage stage) {
        try {
            this.reloadStopwatch.start();
            reloading = stage;
            
            // Pre Reload
            try (SectionClosable startReloadAll = section(stage, "start-reload/");
                 PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Initialization")) {
                for (Reloadable<P> reloadable : manager.getReloadables()) {
                    Class<?> reloadableClass = reloadable.getClass();
                    try (SectionClosable startReload = section(stage, "start-reload/" + name(reloadableClass) + "/");
                         PerformanceLogger.Plugin.Inner inner = perfLogger.stage(name(reloadableClass))) {
                        reloadable.startReload(stage);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
            
            // Sort Plugins
            plugins = new ArrayList<>(plugins);
            plugins.sort(Comparator.comparingDouble(PluginManagerImpl.PluginWrapper<P>::getPriority).reversed());
            InternalLogger.getInstance().info("Reloading Plugin Manager [%s] stage [%s], registered %d plugins: %s", name(pluginClass), stage.toString(), plugins.size(), CollectionUtils.mapAndJoinToString(plugins, PluginManagerImpl.PluginWrapper::getPluginProviderName, ", "));
            Collections.reverse(plugins);
            
            // Reload
            for (Reloadable<P> reloadable : manager.getReloadables()) {
                Class<?> reloadableClass = reloadable.getClass();
                try (SectionClosable reloadablePlugin = section(stage, "reloadable-plugin/" + name(reloadableClass) + "/");
                     PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage(name(reloadableClass))) {
                    try (PerformanceLogger.Plugin.Inner inner = perfLogger.stage("reloadable-plugin/" + name(reloadableClass) + "/prompt-others-before")) {
                        for (Reloadable<P> listener : manager.getReloadables()) {
                            try {
                                listener.beforeReloadable(stage, reloadable);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                    
                    pluginSection(stage, "reloadable-plugin/" + name(reloadableClass) + "/", plugins, reloadable, (plugin, sink) -> {
                        try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider(), plugin.plugin()))) {
                            sink.accept(true, () -> {
                                for (Reloadable<P> listener : manager.getReloadables()) {
                                    try {
                                        listener.beforeReloadablePlugin(stage, reloadable, plugin.plugin());
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                }
                                
                                try {
                                    reloadable.acceptPlugin(plugin.plugin(), stage);
                                } finally {
                                    for (Reloadable<P> listener : manager.getReloadables()) {
                                        try {
                                            listener.afterReloadablePlugin(stage, reloadable, plugin.plugin());
                                        } catch (Throwable throwable) {
                                            throwable.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    });
                    
                    try (PerformanceLogger.Plugin.Inner inner = perfLogger.stage("reloadable-plugin/" + name(reloadableClass) + "/prompt-others-after")) {
                        for (Reloadable<P> listener : manager.getReloadables()) {
                            try {
                                listener.afterReloadable(stage, reloadable);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            // Post Reload
            try (SectionClosable endReloadAll = section(stage, "end-reload/");
                 PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Finalization")) {
                for (Reloadable<P> reloadable : manager.getReloadables()) {
                    Class<?> reloadableClass = reloadable.getClass();
                    try (SectionClosable endReload = section(stage, "end-reload/" + name(reloadableClass) + "/");
                         PerformanceLogger.Plugin.Inner inner = perfLogger.stage(name(reloadableClass))) {
                        reloadable.endReload(stage);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
            
            this.reloadStopwatch.stop();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            reloading = null;
        }
    }
    
    static String name(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty()) return clazz.getName();
        return simpleName;
    }
    
    public List<ReloadStage> getObservedStages() {
        return observedStages;
    }
}
