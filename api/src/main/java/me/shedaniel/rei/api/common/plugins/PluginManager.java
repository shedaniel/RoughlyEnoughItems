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

package me.shedaniel.rei.api.common.plugins;

import dev.architectury.utils.EnvExecutor;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.registry.ParentReloadable;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

/**
 * The plugin manager responsible for reloading and applying plugins.
 */
public interface PluginManager<P extends REIPlugin<?>> extends ParentReloadable<P> {
    @Environment(EnvType.CLIENT)
    static PluginManager<REIClientPlugin> getClientInstance() {
        return ClientInternals.getPluginManager();
    }
    
    static PluginManager<REIPlugin<?>> getInstance() {
        return Internals.getPluginManager();
    }
    
    static PluginManager<REIServerPlugin> getServerInstance() {
        return Internals.getServerPluginManager();
    }
    
    static List<PluginManager<? extends REIPlugin<?>>> getActiveInstances() {
        return EnvExecutor.getEnvSpecific(() -> () -> Arrays.asList(getInstance(), getClientInstance(), getServerInstance()),
                () -> () -> Arrays.asList(getInstance(), getServerInstance()));
    }
    
    static boolean areAnyReloading() {
        return CollectionUtils.anyMatch(getActiveInstances(), PluginManager::isReloading);
    }
    
    boolean isReloading();
    
    <T extends Reloadable<? super P>> T get(Class<T> reloadableClass);
    
    List<REIPluginProvider<P>> getPluginProviders();
    
    Iterable<P> getPlugins();
    
    @ApiStatus.Internal
    PluginView<P> view();
}
