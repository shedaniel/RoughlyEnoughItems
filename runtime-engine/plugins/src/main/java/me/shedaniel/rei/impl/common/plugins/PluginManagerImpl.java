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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.impl.common.InternalLogger;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

@ApiStatus.Internal
public class PluginManagerImpl<P extends REIPlugin<?>> implements PluginManager<P>, PluginView<P> {
    private final List<Reloadable<P>> reloadables = new ArrayList<>();
    private final Map<Class<? extends Reloadable<P>>, Reloadable<? super P>> cache = new ConcurrentHashMap<>();
    private final Class<P> pluginClass;
    private final UnaryOperator<PluginView<P>> view;
    private final List<REIPluginProvider<P>> plugins = new ArrayList<>();
    private final PluginReloaderImpl<P> reloader = new PluginReloaderImpl<>();
    
    @SafeVarargs
    public PluginManagerImpl(Class<P> pluginClass, UnaryOperator<PluginView<P>> view, Class<? extends Reloadable<? extends P>>... reloadables) {
        this.pluginClass = pluginClass;
        this.view = view;
        for (Class<? extends Reloadable<? extends P>> reloadable : reloadables) {
            registerReloadable(reloadable);
        }
    }
    
    @Override
    public void registerReloadable(Reloadable<? extends P> reloadable) {
        this.reloadables.add((Reloadable<P>) reloadable);
        InternalLogger.getInstance().info("Registered reloadable into plugin manager []: " + reloadable.getClass().getName(), pluginClass.getSimpleName());
    }
    
    @Override
    public boolean isReloading() {
        return this.reloader.isReloading();
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
        InternalLogger.getInstance().info("Registered plugin provider %s for %s", plugin.getPluginProviderName(), PluginReloaderImpl.name(pluginClass));
    }
    
    @Override
    public void pre(ReloadStage stage) {
        this.reloader.pre(this, pluginClass, getPluginWrapped().toList(), stage);
    }
    
    @Override
    public void post(ReloadStage stage) {
        this.reloader.post(this, pluginClass, getPluginWrapped().toList(), stage);
    }
    
    @Override
    public void startReload(ReloadStage stage) {
        this.reloader.startReload(this, pluginClass, getPluginWrapped().toList(), stage);
    }
    
    @Override
    public List<REIPluginProvider<P>> getPluginProviders() {
        return Collections.unmodifiableList(plugins);
    }
    
    @Override
    public FluentIterable<P> getPlugins() {
        return FluentIterable.concat(Iterables.transform(plugins, REIPluginProvider::provide));
    }
    
    @SuppressWarnings({"RedundantTypeArguments", "UnstableApiUsage"})
    public FluentIterable<PluginWrapper<P>> getPluginWrapped() {
        return FluentIterable.<PluginWrapper<P>>concat(Iterables.<REIPluginProvider<P>, Iterable<PluginWrapper<P>>>transform(plugins, input -> Iterables.<P, PluginWrapper<P>>transform(input.provide(),
                plugin -> new PluginWrapper<>(plugin, input))));
    }
    
    @Override
    public List<ReloadStage> getObservedStages() {
        return Collections.unmodifiableList(reloader.getObservedStages());
    }
    
    public PluginReloaderImpl<P> getReloader() {
        return reloader;
    }
    
    record PluginWrapper<P extends REIPlugin<?>>(P plugin, REIPluginProvider<P> provider) {
        
        public double getPriority() {
            return plugin.getPriority();
        }
        
        public String getPluginProviderName() {
            return provider.getPluginProviderName();
        }
    }
}
