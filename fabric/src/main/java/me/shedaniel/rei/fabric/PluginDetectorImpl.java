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

package me.shedaniel.rei.fabric;

import com.google.common.collect.Iterables;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

public class PluginDetectorImpl {
    public static void detectServerPlugins() {
        for (REIServerPlugin plugin : Iterables.concat(
                FabricLoader.getInstance().getEntrypoints("rei_containers", REIServerPlugin.class),
                FabricLoader.getInstance().getEntrypoints("rei_server", REIServerPlugin.class)
        )) {
            try {
                PluginView.getServerInstance().registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        }
        if (FabricLoader.getInstance().isModLoaded("libblockattributes-fluids")) {
            try {
                PluginView.getServerInstance().registerPlugin((REIServerPlugin) Class.forName("me.shedaniel.rei.compat.LBASupportPlugin").getConstructor().newInstance());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    
    public static void detectCommonPlugins() {
        for (REIPlugin<?> plugin : Iterables.concat(
                FabricLoader.getInstance().getEntrypoints("rei_common", REIPlugin.class)
        )) {
            try {
                PluginView.getInstance().registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static void detectClientPlugins() {
        for (REIClientPlugin plugin : Iterables.concat(
                FabricLoader.getInstance().getEntrypoints("rei", REIClientPlugin.class),
                FabricLoader.getInstance().getEntrypoints("rei_plugins", REIClientPlugin.class),
                FabricLoader.getInstance().getEntrypoints("rei_plugins_v0", REIClientPlugin.class)
        )) {
            try {
                PluginView.getClientInstance().registerPlugin(plugin);
            } catch (Exception e) {
                e.printStackTrace();
                RoughlyEnoughItemsCore.LOGGER.error("Can't load REI plugins from %s: %s", plugin.getClass(), e.getLocalizedMessage());
            }
        }
    }
}
