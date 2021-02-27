/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.plugins.PluginManager;
import me.shedaniel.rei.api.plugins.REIPlugin;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.impl.registry.CategoryRegistryImpl;
import me.shedaniel.rei.impl.registry.DisplayRegistryImpl;
import me.shedaniel.rei.impl.subsets.SubsetsRegistryImpl;
import me.shedaniel.rei.impl.transfer.TransferHandlerRegistryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.ApiStatus;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class PluginManagerImpl implements PluginManager {
    private final List<Reloadable> reloadables = new ArrayList<>();
    private final Map<Class<? extends Reloadable>, Reloadable> cache = new ConcurrentHashMap<>();
    private boolean arePluginsLoading = false;
    
    public PluginManagerImpl() {
        registerReloadable(new ConfigManagerImpl());
        registerReloadable(new EntryTypeRegistryImpl());
        registerReloadable(new FluidSupportProviderImpl());
        registerReloadable(new CategoryRegistryImpl());
        registerReloadable(new DisplayRegistryImpl());
        registerReloadable(new ScreenRegistryImpl());
        registerReloadable(new EntryRegistryImpl());
        registerReloadable(new FavoriteEntryTypeRegistryImpl());
        registerReloadable(new SubsetsRegistryImpl());
        registerReloadable(new TransferHandlerRegistryImpl());
        registerReloadable(ScreenHelper::reload);
    }
    
    @Override
    public void registerReloadable(Reloadable reloadable) {
        this.reloadables.add(reloadable);
    }
    
    @Override
    public boolean arePluginsReloading() {
        return arePluginsLoading;
    }
    
    @Override
    public <T extends Reloadable> T get(Class<T> reloadableClass) {
        Reloadable reloadable = this.cache.get(reloadableClass);
        if (reloadable != null) return (T) reloadable;
        
        for (Reloadable r : reloadables) {
            if (reloadableClass.isInstance(r)) {
                this.cache.put(reloadableClass, r);
                return (T) r;
            }
        }
        throw new IllegalArgumentException("Unknown reloadable type! " + reloadableClass.getName());
    }
    
    @Override
    public List<Reloadable> getReloadables() {
        return Collections.unmodifiableList(reloadables);
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
    
    private void pluginSection(MutablePair<Stopwatch, String> sectionData, String sectionName, List<REIPlugin> list, Consumer<REIPlugin> consumer) {
        for (REIPlugin plugin : list) {
            try (SectionClosable section = section(sectionData, sectionName + " for " + plugin.getPluginName())) {
                consumer.accept(plugin);
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error(plugin.getPluginName() + " plugin failed to " + sectionName + "!", throwable);
            }
        }
    }
    
    public void tryResetData() {
        try {
            this.startReload();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        arePluginsLoading = false;
    }
    
    @Override
    public void startReload() {
        long startTime = Util.getMillis();
        MutablePair<Stopwatch, String> sectionData = new MutablePair<>(Stopwatch.createUnstarted(), "");
        
        try (SectionClosable startReload = section(sectionData, "start-reload")) {
            for (Reloadable reloadable : reloadables) {
                reloadable.startReload();
            }
        }
        
        arePluginsLoading = true;
        List<REIPlugin> plugins = new ArrayList<>(RoughlyEnoughItemsCore.getPlugins());
        plugins.sort(Comparator.comparingInt(REIPlugin::getPriority).reversed());
        RoughlyEnoughItemsCore.LOGGER.info("Reloading REI, registered %d plugins: %s", plugins.size(), CollectionUtils.mapAndJoinToString(plugins, REIPlugin::getPluginName, ", "));
        Collections.reverse(plugins);
        
        pluginSection(sectionData, "pre-register", plugins, REIPlugin::preRegister);
        for (Reloadable reloadable : getReloadables()) {
            Class<? extends Reloadable> reloadableClass = reloadable.getClass();
            pluginSection(sectionData, "reloadable-plugin-" + MoreObjects.firstNonNull(reloadableClass.getSimpleName(), reloadableClass.getName()), plugins, reloadable::acceptPlugin);
        }
        pluginSection(sectionData, "post-register", plugins, REIPlugin::postRegister);
        
        try (SectionClosable endReload = section(sectionData, "end-reload")) {
            for (Reloadable reloadable : reloadables) {
                reloadable.endReload();
            }
        }
        arePluginsLoading = false;
        try (SectionClosable refilter = section(sectionData, "entry-registry-refilter")) {
            EntryRegistry.getInstance().refilter();
        }
        
        long usedTime = Util.getMillis() - startTime;
        RoughlyEnoughItemsCore.LOGGER.info("Reloaded %d stack entries, %d displays, %d exclusion zones suppliers, %d overlay deciders, %d visibility predicates and %d categories (%s) in %dms.",
                EntryRegistry.getInstance().size(),
                DisplayRegistry.getInstance().getDisplayCount(),
                ScreenRegistry.getInstance().exclusionZones().getZonesCount(),
                ScreenRegistry.getInstance().getDeciders().size(),
                DisplayRegistry.getInstance().getVisibilityPredicates().size(),
                CategoryRegistry.getInstance().size(),
                CategoryRegistry.getInstance().stream()
                        .map(CategoryRegistry.CategoryConfiguration::getCategory)
                        .map(DisplayCategory::getTitle)
                        .map(Component::getString).collect(Collectors.joining(", ")),
                usedTime
        );
    }
}
