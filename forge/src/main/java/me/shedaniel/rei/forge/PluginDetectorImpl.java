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

package me.shedaniel.rei.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import dev.architectury.platform.forge.EventBuses;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.init.PluginDetector;
import me.shedaniel.rei.plugin.client.forge.DefaultClientPluginImpl;
import me.shedaniel.rei.plugin.client.runtime.DefaultClientRuntimePlugin;
import me.shedaniel.rei.plugin.common.forge.DefaultPluginImpl;
import me.shedaniel.rei.plugin.common.runtime.DefaultRuntimePlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class PluginDetectorImpl implements PluginDetector {
    private static <P extends me.shedaniel.rei.api.common.plugins.REIPlugin<?>> REIPluginProvider<P> wrapPlugin(List<String> modId, REIPluginProvider<P> plugin) {
        return new REIPluginProvider<P>() {
            final String nameSuffix = " [" + String.join(", ", modId) + "]";
            
            @Override
            public Collection<P> provide() {
                return plugin.provide();
            }
            
            @Override
            public Class<P> getPluginProviderClass() {
                return plugin.getPluginProviderClass();
            }
            
            @Override
            public String getPluginProviderName() {
                return plugin.getPluginProviderName() + nameSuffix;
            }
        };
    }
    
    private static final Supplier<List<Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>>>> loaderProvided =
            Suppliers.memoize(() -> getPluginsLoader(REIPluginLoader.class));
    private static final Supplier<List<Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>>>> loaderProvidedCommon =
            Suppliers.memoize(() -> getPluginsLoader(REIPluginLoaderCommon.class));
    private static final Supplier<List<Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>>>> loaderProvidedDist;
    
    static {
        Supplier<List<Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>>>> dist;
        if (FMLEnvironment.dist == Dist.CLIENT) {
            dist = Suppliers.memoize(() -> getPluginsLoader(REIPluginLoaderClient.class));
        } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            dist = Suppliers.memoize(() -> getPluginsLoader(REIPluginLoaderDedicatedServer.class));
        } else throw new IllegalStateException("Unknown environment: " + FMLEnvironment.dist);
        loaderProvidedDist = dist;
    }
    
    @NotNull
    private static <T, P extends me.shedaniel.rei.api.common.plugins.REIPlugin<?>> List<Map.Entry<REIPluginProvider<P>, List<String>>> getPluginsLoader(Class<T> annotation) {
        List<Map.Entry<REIPluginProvider<P>, List<String>>> list = new ArrayList<>();
        AnnotationUtils.<T, REIPluginProvider<P>>scanAnnotation(annotation, REIPluginProvider.class::isAssignableFrom, (modId, provider, clazz) -> {
            try {
                list.add(new AbstractMap.SimpleEntry<>(provider.get(), modId));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return list;
    }
    
    @Override
    public void detectServerPlugins() {
        PluginView.getServerInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultPluginImpl()));
        PluginView.getServerInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultRuntimePlugin()));
        
        // Legacy Annotation
        AnnotationUtils.<REIPlugin, REIServerPlugin>scanAnnotation(REIPlugin.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
        
        // Common plugins
        AnnotationUtils.<REIPluginCommon, REIServerPlugin>scanAnnotation(REIPluginCommon.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
        
        // Dist plugins
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            AnnotationUtils.<REIPluginDedicatedServer, REIServerPlugin>scanAnnotation(REIPluginDedicatedServer.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
        } else {
            AnnotationUtils.<REIPluginClient, REIServerPlugin>scanAnnotation(REIPluginClient.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
        }
        
        // Loaders
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvided.get()) {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIServerPlugin.class)));
        }
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedCommon.get()) {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIServerPlugin.class)));
        }
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedDist.get()) {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIServerPlugin.class)));
        }
    }
    
    @Override
    public void detectCommonPlugins() {
        EventBuses.registerModEventBus("roughlyenoughitems", FMLJavaModLoadingContext.get().getModEventBus());
        
        // Legacy Annotation
        AnnotationUtils.<REIPlugin, me.shedaniel.rei.api.common.plugins.REIPlugin<?>>scanAnnotation(REIPlugin.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
        
        // Common plugins
        AnnotationUtils.<REIPluginCommon, me.shedaniel.rei.api.common.plugins.REIPlugin<?>>scanAnnotation(REIPluginCommon.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
        });
        
        // Dist plugins
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AnnotationUtils.<REIPluginClient, me.shedaniel.rei.api.common.plugins.REIPlugin>scanAnnotation(REIPluginClient.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
        } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            AnnotationUtils.<REIPluginDedicatedServer, me.shedaniel.rei.api.common.plugins.REIPlugin>scanAnnotation(REIPluginDedicatedServer.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
        }
        
        // Loaders
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvided.get()) {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(entry.getValue(), entry.getKey()));
        }
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedCommon.get()) {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(entry.getValue(), entry.getKey()));
        }
        for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedDist.get()) {
            ((PluginView) PluginManager.getInstance()).registerPlugin(wrapPlugin(entry.getValue(), entry.getKey()));
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public Supplier<Runnable> detectClientPlugins() {
        return () -> () -> {
            PluginView.getClientInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultClientPluginImpl()));
            PluginView.getClientInstance().registerPlugin(wrapPlugin(Collections.singletonList("roughlyenoughitems"), new DefaultClientRuntimePlugin()));
            
            // Legacy Annotation
            AnnotationUtils.<REIPlugin, REIClientPlugin>scanAnnotation(REIPlugin.class, REIClientPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
            
            // Common plugins
            AnnotationUtils.<REIPluginCommon, REIClientPlugin>scanAnnotation(REIPluginCommon.class, REIClientPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
            
            // Dist plugins
            AnnotationUtils.<REIPluginClient, REIClientPlugin>scanAnnotation(REIPluginClient.class, REIClientPlugin.class::isAssignableFrom, (modId, plugin, clazz) -> {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(modId, plugin.get()));
            });
            
            // Loaders
            for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvided.get()) {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIClientPlugin.class)));
            }
            for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedCommon.get()) {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIClientPlugin.class)));
            }
            for (Map.Entry<REIPluginProvider<me.shedaniel.rei.api.common.plugins.REIPlugin<?>>, List<String>> entry : loaderProvidedDist.get()) {
                ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(wrapPlugin(entry.getValue(), wrapAndFilter(entry.getKey(), REIClientPlugin.class)));
            }
        };
    }
    
    private static <P extends me.shedaniel.rei.api.common.plugins.REIPlugin<P>> REIPluginProvider<P> wrapAndFilter(REIPluginProvider<?> provider, Class<P> clazz) {
        return new REIPluginProvider<>() {
            @Override
            public Collection<P> provide() {
                return new AbstractCollection<>() {
                    @Override
                    public Iterator<P> iterator() {
                        return Iterables.filter(provider.provide(), clazz).iterator();
                    }
                    
                    @Override
                    public int size() {
                        return Iterators.size(iterator());
                    }
                };
            }
            
            @Override
            public Class<P> getPluginProviderClass() {
                return clazz;
            }
            
            @Override
            public String getPluginProviderName() {
                return provider.getPluginProviderName();
            }
        };
    }
}
