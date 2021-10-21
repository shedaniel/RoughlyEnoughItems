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

package me.shedaniel.rei.impl.common.plugins;

import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.mojang.datafixers.util.Pair;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.GameInstance;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.common.logging.performance.PerformanceLogger;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class PluginManagerImpl<P extends REIPlugin<?>> implements PluginManager<P>, PluginView<P> {
    private final List<Reloadable<P>> reloadables = new ArrayList<>();
    private final Map<Class<? extends Reloadable<P>>, Reloadable<? super P>> cache = new ConcurrentHashMap<>();
    private final Class<P> pluginClass;
    private final UnaryOperator<PluginView<P>> view;
    private boolean reloading = false;
    private final List<REIPluginProvider<P>> plugins = new ArrayList<>();
    private final LongConsumer reloadDoneListener;
    
    @SafeVarargs
    public PluginManagerImpl(Class<P> pluginClass, UnaryOperator<PluginView<P>> view, LongConsumer reloadDoneListener, Reloadable<? extends P>... reloadables) {
        this.pluginClass = pluginClass;
        this.view = view;
        this.reloadDoneListener = reloadDoneListener;
        for (Reloadable<? extends P> reloadable : reloadables) {
            registerReloadable(reloadable);
        }
    }
    
    @Override
    public void registerReloadable(Reloadable<? extends P> reloadable) {
        this.reloadables.add((Reloadable<P>) reloadable);
    }
    
    @Override
    public boolean isReloading() {
        return reloading;
    }
    
    @Override
    public <T extends Reloadable<? super P>> T get(Class<T> reloadableClass) {
        Reloadable<? super P> reloadable = this.cache.get(reloadableClass);
        if (reloadable != null) return (T) reloadable;
        
        for (Reloadable<P> r : reloadables) {
            if (reloadableClass.isInstance(r)) {
                this.cache.put((Class<? extends Reloadable<P>>) reloadableClass, r);
                return (T) r;
            }
        }
        throw new IllegalArgumentException("Unknown reloadable type! " + reloadableClass.getName());
    }
    
    @Override
    public List<Reloadable<P>> getReloadables() {
        return Collections.unmodifiableList(reloadables);
    }
    
    @Override
    public PluginView<P> view() {
        return view.apply(this);
    }
    
    @Override
    public void registerPlugin(REIPluginProvider<? extends P> plugin) {
        plugins.add((REIPluginProvider<P>) plugin);
        RoughlyEnoughItemsCore.LOGGER.info("Registered plugin provider %s for %s", plugin.getPluginProviderName(), name(pluginClass));
    }
    
    @Override
    public List<REIPluginProvider<P>> getPluginProviders() {
        return Collections.unmodifiableList(plugins);
    }
    
    @Override
    public FluentIterable<P> getPlugins() {
        return FluentIterable.concat(Iterables.transform(plugins, REIPluginProvider::provide));
    }
    
    private static class PluginWrapper<P extends REIPlugin<?>> {
        private final P plugin;
        private final REIPluginProvider<P> provider;
        
        public PluginWrapper(P plugin, REIPluginProvider<P> provider) {
            this.plugin = plugin;
            this.provider = provider;
        }
        
        public double getPriority() {
            return plugin.getPriority();
        }
        
        public String getPluginProviderName() {
            return provider.getPluginProviderName();
        }
    }
    
    @SuppressWarnings("RedundantTypeArguments")
    public FluentIterable<PluginWrapper<P>> getPluginWrapped() {
        return FluentIterable.<PluginWrapper<P>>concat(Iterables.<REIPluginProvider<P>, Iterable<PluginWrapper<P>>>transform(plugins, input -> Iterables.<P, PluginWrapper<P>>transform(input.provide(),
                plugin -> new PluginWrapper(plugin, input))));
    }
    
    private static class SectionClosable implements Closeable {
        private ReloadStage stage;
        private MutablePair<Stopwatch, String> sectionData;
        
        public SectionClosable(ReloadStage stage, String section) {
            this.stage = stage;
            this.sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
            sectionData.setRight(section);
            RoughlyEnoughItemsCore.LOGGER.debug("[" + stage + "] Reloading Section: \"%s\"", section);
            sectionData.getLeft().reset().start();
        }
        
        @Override
        public void close() {
            sectionData.getLeft().stop();
            String section = sectionData.getRight();
            RoughlyEnoughItemsCore.LOGGER.debug("[" + stage + "] Reloading Section: \"%s\" done in %s", section, sectionData.getLeft().toString());
            sectionData.getLeft().reset();
        }
    }
    
    private SectionClosable section(ReloadStage stage, String section) {
        return new SectionClosable(stage, section);
    }
    
    private void pluginSection(ReloadStage stage, String sectionName, List<PluginWrapper<P>> list, @Nullable Reloadable<?> reloadable, Consumer<PluginWrapper<P>> consumer) {
        for (PluginWrapper<P> wrapper : list) {
            try (SectionClosable section = section(stage, sectionName + wrapper.getPluginProviderName() + "/")) {
                if (reloadable == null || !wrapper.plugin.shouldBeForcefullyDoneOnMainThread(reloadable)) {
                    consumer.accept(wrapper);
                } else if (Platform.getEnvironment() == Env.CLIENT) {
                    EnvExecutor.runInEnv(Env.CLIENT, () -> () -> queueExecutionClient(() -> consumer.accept(wrapper)));
                } else {
                    queueExecution(() -> consumer.accept(wrapper));
                }
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error(wrapper.getPluginProviderName() + " plugin failed to " + sectionName + "!", throwable);
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
    
    @Override
    public void pre(ReloadStage stage) {
        List<PluginWrapper<P>> plugins = new ArrayList<>(getPluginWrapped().toList());
        plugins.sort(Comparator.comparingDouble(PluginWrapper<P>::getPriority).reversed());
        Collections.reverse(plugins);
        try (SectionClosable preRegister = section(stage, "pre-register/");
             PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Pre Registration")) {
            pluginSection(stage, "pre-register/", plugins, null, plugin -> {
                try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider, plugin.plugin))) {
                    plugin.plugin.preRegister();
                    ((REIPlugin<P>) plugin.plugin).preStage(this, stage);
                }
            });
        }
    }
    
    @Override
    public void post(ReloadStage stage) {
        List<PluginWrapper<P>> plugins = new ArrayList<>(getPluginWrapped().toList());
        plugins.sort(Comparator.comparingDouble(PluginWrapper<P>::getPriority).reversed());
        Collections.reverse(plugins);
        try (SectionClosable postRegister = section(stage, "post-register/");
             PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Post Registration")) {
            pluginSection(stage, "post-register/", plugins, null, plugin -> {
                try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider, plugin.plugin))) {
                    plugin.plugin.postRegister();
                    ((REIPlugin<P>) plugin.plugin).postStage(this, stage);
                }
            });
        }
    }
    
    private static String name(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        if (simpleName.isEmpty()) return clazz.getName();
        return simpleName;
    }
    
    @Override
    public void startReload(ReloadStage stage) {
        try {
            reloading = true;
            long startTime = Util.getMillis();
            
            try (SectionClosable startReloadAll = section(stage, "start-reload/");
                 PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Initialization")) {
                for (Reloadable<P> reloadable : reloadables) {
                    Class<?> reloadableClass = reloadable.getClass();
                    try (SectionClosable startReload = section(stage, "start-reload/" + name(reloadableClass) + "/");
                         PerformanceLogger.Plugin.Inner inner = perfLogger.stage(name(reloadableClass))) {
                        reloadable.startReload(stage);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
            
            List<PluginWrapper<P>> plugins = new ArrayList<>(getPluginWrapped().toList());
            plugins.sort(Comparator.comparingDouble(PluginWrapper<P>::getPriority).reversed());
            RoughlyEnoughItemsCore.LOGGER.info("Reloading Plugin Manager [%s] stage [%s], registered %d plugins: %s", name(pluginClass), stage.toString(), plugins.size(), CollectionUtils.mapAndJoinToString(plugins, PluginWrapper::getPluginProviderName, ", "));
            Collections.reverse(plugins);
            
            for (Reloadable<P> reloadable : getReloadables()) {
                Class<?> reloadableClass = reloadable.getClass();
                try (SectionClosable reloadablePlugin = section(stage, "reloadable-plugin/" + name(reloadableClass) + "/");
                     PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage(name(reloadableClass))) {
                    pluginSection(stage, "reloadable-plugin/" + name(reloadableClass) + "/", plugins, reloadable, plugin -> {
                        try (PerformanceLogger.Plugin.Inner inner = perfLogger.plugin(new Pair<>(plugin.provider, plugin.plugin))) {
                            reloadable.acceptPlugin(plugin.plugin, stage);
                        }
                    });
                }
            }
            
            try (SectionClosable endReloadAll = section(stage, "end-reload/");
                 PerformanceLogger.Plugin perfLogger = RoughlyEnoughItemsCore.PERFORMANCE_LOGGER.stage("Reload Finalization")) {
                for (Reloadable<P> reloadable : reloadables) {
                    Class<?> reloadableClass = reloadable.getClass();
                    try (SectionClosable endReload = section(stage, "end-reload/" + name(reloadableClass) + "/");
                         PerformanceLogger.Plugin.Inner inner = perfLogger.stage(name(reloadableClass))) {
                        reloadable.endReload(stage);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
            
            long usedTime = Util.getMillis() - startTime;
            reloadDoneListener.accept(usedTime);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            reloading = false;
        }
    }
}
