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

package me.shedaniel.rei.fabric;

import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginDetectorImpl implements PluginDetector {
    private static <P extends REIPlugin<?>> void loadPlugin(Class<? extends P> pluginClass, Consumer<? super REIPluginProvider<P>> consumer) {
        Map<String, Env> entrypoints = new LinkedHashMap<>();
        entrypoints.put("rei_server", Env.SERVER);
        entrypoints.put("rei_common", null);
        entrypoints.put("rei", null);
        entrypoints.put("rei_client", Env.CLIENT);
        Set<String> deprecatedEntrypoints = new LinkedHashSet<>(Arrays.asList(
                "rei_containers",
                "rei_plugins",
                "rei_plugins_v0",
                "rei"
        ));
        List<Pair<EntrypointContainer<REIPluginProvider>, String>> containers = Stream.concat(entrypoints.entrySet().stream()
                                .filter(entry -> entry.getValue() == null || Platform.getEnvironment() == entry.getValue())
                                .map(Map.Entry::getKey)
                        , deprecatedEntrypoints.stream())
                .distinct()
                .flatMap(name -> FabricLoader.getInstance().getEntrypointContainers(name, REIPluginProvider.class)
                        .stream()
                        .map(container -> Pair.of(container, name)))
                .collect(Collectors.toList());
        
        out:
        for (Pair<EntrypointContainer<REIPluginProvider>, String> pair : containers) {
            EntrypointContainer<REIPluginProvider> container = pair.getLeft();
            String name = pair.getRight();
            try {
                if (deprecatedEntrypoints.contains(name)) {
                    RoughlyEnoughItemsState.LOGGER.warn("The entrypoint used by %s, \"%s\" is deprecated and will be removed in a future version of Roughly Enough Items. Please use \"rei_server\", \"rei_client\" or \"rei_common\" instead.", container.getProvider().getMetadata().getName(), name);
                }
                
                REIPluginProvider<P> plugin = container.getEntrypoint();
                if (pluginClass.isAssignableFrom(plugin.getPluginProviderClass())) {
                    consumer.accept(new REIPluginProvider<P>() {
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
                            return plugin.getPluginProviderName() + " [" + container.getProvider().getMetadata().getId() + "]";
                        }
                    });
                }
            } catch (Throwable t) {
                Throwable throwable = t;
                while (throwable != null) {
                    if (throwable.getMessage() != null && throwable.getMessage().contains("environment type SERVER") && !PrimitivePlatformAdapter.get().isClient()) {
                        RoughlyEnoughItemsState.LOGGER.warn("Rerached side issue when loading REI plugin by %s. Please use \"rei_server\", \"rei_client\" or \"rei_common\" instead.", container.getProvider().getMetadata().getName());
                        continue out;
                    }
                    throwable = throwable.getCause();
                }
                String error = "Could not create REI Plugin [" + getSimpleName(pluginClass) + "] due to errors, provided by '" + container.getProvider().getMetadata().getId() + "'!";
                new RuntimeException(error, t).printStackTrace();
                RoughlyEnoughItemsState.error(error);
            }
        }
    }
    
    private static <P> String getSimpleName(Class<? extends P> pluginClass) {
        String simpleName = pluginClass.getSimpleName();
        if (simpleName == null) return pluginClass.getName();
        return simpleName;
    }
    
    @Override
    public void detectServerPlugins() {
        loadPlugin(REIServerPlugin.class, ((PluginView<REIServerPlugin>) PluginManager.getServerInstance())::registerPlugin);
        try {
            PluginView.getServerInstance().registerPlugin((REIServerPlugin) Class.forName("me.shedaniel.rei.impl.common.compat.FabricFluidAPISupportPlugin").getConstructor().newInstance());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    @SuppressWarnings({"RedundantCast", "rawtypes"})
    @Override
    public void detectCommonPlugins() {
        loadPlugin((Class<? extends REIPlugin<?>>) (Class) REIPlugin.class, ((PluginView<REIPlugin<?>>) PluginManager.getInstance())::registerPlugin);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public Supplier<Runnable> detectClientPlugins() {
        return () -> () -> {
            loadPlugin(REIClientPlugin.class, ((PluginView<REIClientPlugin>) PluginManager.getClientInstance())::registerPlugin);
        };
    }
}
