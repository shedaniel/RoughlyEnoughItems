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

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.Util;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.ApiStatus;

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
        RoughlyEnoughItemsCore.LOGGER.info("Registered plugin provider %s for %s", plugin.getPluginProviderName(), pluginClass.getSimpleName());
    }
    
    @Override
    public List<REIPluginProvider<P>> getPluginProviders() {
        return Collections.unmodifiableList(plugins);
    }
    
    @Override
    public FluentIterable<P> getPlugins() {
        return FluentIterable.concat(Iterables.transform(plugins, REIPluginProvider::provide));
    }
    
    private static class SectionClosable implements Closeable {
        private MutablePair<Stopwatch, String> sectionData;
        
        public SectionClosable(MutablePair<Stopwatch, String> sectionData, String section) {
            this.sectionData = sectionData;
            sectionData.setRight(section);
            RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\"", section);
            sectionData.getLeft().reset().start();
        }
        
        @Override
        public void close() {
            sectionData.getLeft().stop();
            String section = sectionData.getRight();
            RoughlyEnoughItemsCore.LOGGER.debug("Reloading Section: \"%s\" done in %s", section, sectionData.getLeft().toString());
            sectionData.getLeft().reset();
        }
    }
    
    private SectionClosable section(MutablePair<Stopwatch, String> sectionData, String section) {
        return new SectionClosable(sectionData, section);
    }
    
    private void pluginSection(MutablePair<Stopwatch, String> sectionData, String sectionName, List<P> list, Consumer<P> consumer) {
        for (P plugin : list) {
            try (SectionClosable section = section(sectionData, sectionName + " for " + plugin.getPluginProviderName())) {
                consumer.accept(plugin);
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginProviderName() + " plugin failed to " + sectionName + "!", throwable);
            }
        }
    }
    
    @Override
    public void pre() {
        List<P> plugins = new ArrayList<>(getPlugins().toList());
        plugins.sort(Comparator.comparingDouble(P::getPriority).reversed());
        Collections.reverse(plugins);
        MutablePair<Stopwatch, String> sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
        pluginSection(sectionData, "pre-register", plugins, REIPlugin::preRegister);
    }
    
    @Override
    public void post() {
        List<P> plugins = new ArrayList<>(getPlugins().toList());
        plugins.sort(Comparator.comparingDouble(P::getPriority).reversed());
        Collections.reverse(plugins);
        MutablePair<Stopwatch, String> sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
        pluginSection(sectionData, "post-register", plugins, REIPlugin::postRegister);
    }
    
    @Override
    public void startReload(ReloadStage stage) {
        try {
            reloading = true;
            long startTime = Util.getMillis();
            MutablePair<Stopwatch, String> sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
            
            for (Reloadable<P> reloadable : reloadables) {
                Class<?> reloadableClass = reloadable.getClass();
                try (SectionClosable startReload = section(sectionData, "start-reload-" + MoreObjects.firstNonNull(reloadableClass.getSimpleName(), reloadableClass.getName()))) {
                    reloadable.startReload(stage);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            
            List<P> plugins = new ArrayList<>(getPlugins().toList());
            plugins.sort(Comparator.comparingDouble(P::getPriority).reversed());
            RoughlyEnoughItemsCore.LOGGER.info("Reloading Plugin Manager [%s] stage [%s], registered %d plugins: %s", pluginClass.getSimpleName(), stage.toString(), plugins.size(), CollectionUtils.mapAndJoinToString(plugins, REIPlugin::getPluginProviderName, ", "));
            Collections.reverse(plugins);
            
            for (Reloadable<P> reloadable : getReloadables()) {
                Class<?> reloadableClass = reloadable.getClass();
                pluginSection(sectionData, "reloadable-plugin-" + MoreObjects.firstNonNull(reloadableClass.getSimpleName(), reloadableClass.getName()), plugins, plugin -> reloadable.acceptPlugin(plugin, stage));
            }
            
            for (Reloadable<P> reloadable : reloadables) {
                Class<?> reloadableClass = reloadable.getClass();
                try (SectionClosable endReload = section(sectionData, "end-reload-" + MoreObjects.firstNonNull(reloadableClass.getSimpleName(), reloadableClass.getName()))) {
                    reloadable.endReload(stage);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
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
