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

package me.shedaniel.rei.fabric;

import com.google.common.collect.Iterables;
import me.shedaniel.rei.RoughlyEnoughItemsInitializer;
import me.shedaniel.rei.RoughlyEnoughItemsState;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.Collection;
import java.util.function.Consumer;

public class PluginDetectorImpl {
    private static <P extends REIPlugin<?>> void loadPlugin(Class<? extends P> pluginClass, Consumer<? super REIPluginProvider<P>> consumer) {
        out:
        for (EntrypointContainer<REIPluginProvider> container : Iterables.concat(
                FabricLoader.getInstance().getEntrypointContainers("rei_containers", REIPluginProvider.class),
                FabricLoader.getInstance().getEntrypointContainers("rei_server", REIPluginProvider.class),
                FabricLoader.getInstance().getEntrypointContainers("rei", REIPluginProvider.class),
                FabricLoader.getInstance().getEntrypointContainers("rei_common", REIPluginProvider.class),
                FabricLoader.getInstance().getEntrypointContainers("rei_plugins", REIPluginProvider.class),
                FabricLoader.getInstance().getEntrypointContainers("rei_plugins_v0", REIPluginProvider.class)
        )) {
            try {
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
                    if (throwable.getMessage().contains("environment type SERVER") && !RoughlyEnoughItemsInitializer.isClient()) continue out;
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
    
    public static void detectServerPlugins() {
        loadPlugin(REIServerPlugin.class, ((PluginView<REIServerPlugin>) PluginManager.getServerInstance())::registerPlugin);
        if (FabricLoader.getInstance().isModLoaded("libblockattributes-fluids")) {
            try {
                PluginView.getServerInstance().registerPlugin((REIServerPlugin) Class.forName("me.shedaniel.rei.impl.common.compat.LBASupportPlugin").getConstructor().newInstance());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    
    @SuppressWarnings({"RedundantCast", "rawtypes"})
    public static void detectCommonPlugins() {
        loadPlugin((Class<? extends REIPlugin<?>>) (Class) REIPlugin.class, ((PluginView<REIPlugin<?>>) PluginManager.getInstance())::registerPlugin);
    }
    
    @Environment(EnvType.CLIENT)
    public static void detectClientPlugins() {
        loadPlugin(REIClientPlugin.class, ((PluginView<REIClientPlugin>) PluginManager.getClientInstance())::registerPlugin);
    }
}
