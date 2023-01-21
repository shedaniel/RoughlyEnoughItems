/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.api.client.config.addon;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registry for {@link ConfigAddon}s.
 * Registered config addons will show up at the top of the config screen,
 * and allow users to search and configure them easily.
 * <p>
 * REI does not provide serialization for config addons,
 * that is up to the developer to implement.
 *
 * @since 8.3
 */
@ApiStatus.Experimental
public interface ConfigAddonRegistry extends Reloadable<REIClientPlugin> {
    /**
     * @return the {@link PluginManager} instance
     */
    static ConfigAddonRegistry getInstance() {
        return PluginManager.getClientInstance().get(ConfigAddonRegistry.class);
    }
    
    /**
     * Registers a config addon. The addons displayed will be sorted by their name alphabetically.
     *
     * @param addon the addon to register
     */
    void register(ConfigAddon addon);
}
