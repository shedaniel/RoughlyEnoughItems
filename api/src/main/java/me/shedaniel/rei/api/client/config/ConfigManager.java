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

package me.shedaniel.rei.api.client.config;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public interface ConfigManager extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link ConfigManager}
     */
    static ConfigManager getInstance() {
        return PluginManager.getClientInstance().get(ConfigManager.class);
    }
    
    /**
     * Saves the config.
     */
    void saveConfig();
    
    /**
     * Returns whether the craftable only filter is enabled.
     *
     * @return whether craftable only filter is enabled
     */
    boolean isCraftableOnlyEnabled();
    
    /**
     * Toggles the craftable only filter.
     */
    void toggleCraftableOnly();
    
    /**
     * Opens the config screen.
     *
     * @param parent the screen shown before
     */
    default void openConfigScreen(Screen parent) {
        Minecraft.getInstance().setScreen(getConfigScreen(parent));
    }
    
    /**
     * Returns the config screen.
     *
     * @param parent the screen shown before
     * @return the config screen
     */
    Screen getConfigScreen(Screen parent);
    
    /**
     * Returns the config object.
     *
     * @return the config object
     * @see ConfigObject#getInstance()
     */
    ConfigObject getConfig();
}
