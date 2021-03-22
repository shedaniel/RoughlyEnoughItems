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

import me.shedaniel.rei.api.plugins.PluginManager;
import me.shedaniel.rei.gui.plugin.DefaultRuntimePlugin;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.DefaultServerContainerPlugin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PluginDetectorImpl {
    public static void detectServerPlugins() {
        new DefaultServerContainerPlugin().run();
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void detectClientPlugins() {
        PluginManager.getInstance().registerPlugin(new DefaultPlugin());
        PluginManager.getInstance().registerPlugin(new DefaultRuntimePlugin());
        RoughlyEnoughItemsForge.scanAnnotation(REIPlugin.class, (modId, plugin) -> {
            PluginManager.getInstance().registerPlugin(((me.shedaniel.rei.api.plugins.REIPlugin) plugin));
        });
        Internals.attachInstance((Supplier<List<String>>) () -> {
            List<String> modIds = new ArrayList<>();
            for (me.shedaniel.rei.api.plugins.REIPlugin plugin : PluginManager.getInstance().getPlugins()) {
                if (plugin instanceof JEIPluginDetector.JEIPluginWrapper) {
                    modIds.addAll(((JEIPluginDetector.JEIPluginWrapper) plugin).modIds);
                }
            }
            return modIds;
        }, "jeiCompatMods");
        JEIPluginDetector.detect((aClass, consumer) -> RoughlyEnoughItemsForge.scanAnnotation((Class<Object>) aClass, (BiConsumer<List<String>, Object>) consumer),
                PluginManager.getInstance()::registerPlugin);
    }
}
