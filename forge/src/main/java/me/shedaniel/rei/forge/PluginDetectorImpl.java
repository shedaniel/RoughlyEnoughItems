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

package me.shedaniel.rei.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.plugin.client.DefaultClientPlugin;
import me.shedaniel.rei.plugin.client.DefaultClientRuntimePlugin;
import me.shedaniel.rei.plugin.common.DefaultPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PluginDetectorImpl {
    public static void detectServerPlugins() {
        PluginView.getServerInstance().registerPlugin(new DefaultPlugin());
        RoughlyEnoughItemsForge.<REIPlugin, REIServerPlugin>scanAnnotation(REIPlugin.class, REIServerPlugin.class::isAssignableFrom, (modId, plugin) -> {
            ((PluginView<REIServerPlugin>) PluginManager.getServerInstance()).registerPlugin(plugin);
        });
    }
    
    public static void detectCommonPlugins() {
        EventBuses.registerModEventBus("roughlyenoughitems", FMLJavaModLoadingContext.get().getModEventBus());
        RoughlyEnoughItemsForge.<REIPlugin, me.shedaniel.rei.api.common.plugins.REIPlugin<?>>scanAnnotation(REIPlugin.class, me.shedaniel.rei.api.common.plugins.REIPlugin.class::isAssignableFrom, (modId, plugin) -> {
            ((PluginView) PluginManager.getInstance()).registerPlugin(plugin);
        });
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void detectClientPlugins() {
        PluginView.getClientInstance().registerPlugin(new DefaultClientPlugin());
        PluginView.getClientInstance().registerPlugin(new DefaultClientRuntimePlugin());
        RoughlyEnoughItemsForge.<REIPlugin, REIClientPlugin>scanAnnotation(REIPlugin.class, REIClientPlugin.class::isAssignableFrom, (modId, plugin) -> {
            ((PluginView<REIClientPlugin>) PluginManager.getClientInstance()).registerPlugin(plugin);
        });
        ClientInternals.attachInstance((Supplier<List<String>>) () -> {
            List<String> modIds = new ArrayList<>();
            for (REIPluginProvider<REIClientPlugin> plugin : PluginManager.getClientInstance().getPluginProviders()) {
                if (plugin instanceof JEIPluginDetector.JEIPluginProvider) {
                    modIds.addAll(((JEIPluginDetector.JEIPluginProvider) plugin).wrapper.modIds);
                }
            }
            return modIds;
        }, "jeiCompatMods");
        JEIPluginDetector.detect((aClass, consumer) -> RoughlyEnoughItemsForge.scanAnnotation((Class<Object>) aClass, clazz -> true, (BiConsumer<List<String>, Object>) consumer),
                PluginView.getClientInstance()::registerPlugin);
    }
}
