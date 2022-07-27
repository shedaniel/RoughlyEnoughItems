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

package me.shedaniel.rei.impl.init;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.impl.Internals;

import java.util.List;
import java.util.function.Supplier;

public class ServiceBasedPluginDetector implements PluginDetector {
    @Override
    public void detectServerPlugins() {
        List<REIServerPlugin> plugins = Internals.resolveServices(REIServerPlugin.class);
        for (REIServerPlugin plugin : plugins) {
            PluginManager.getServerInstance().view().registerPlugin(plugin);
        }
    }
    
    @Override
    public void detectCommonPlugins() {
        List<REIPlugin<?>> plugins = Internals.resolveServices((Class<REIPlugin<?>>) (Class<?>) REIPlugin.class);
        for (REIPlugin<?> plugin : plugins) {
            PluginManager.getInstance().view().registerPlugin(plugin);
        }
    }
    
    @Override
    public Supplier<Runnable> detectClientPlugins() {
        return () -> () -> {
            List<REIClientPlugin> plugins = Internals.resolveServices(REIClientPlugin.class);
            for (REIClientPlugin plugin : plugins) {
                PluginManager.getClientInstance().view().registerPlugin(plugin);
            }
        };
    }
}
