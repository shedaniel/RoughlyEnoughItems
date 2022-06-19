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

package me.shedaniel.rei.api.client.config.addon;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

/**
 * A config addon, which will be shown in the REI config screen.
 *
 * @since 8.3
 */
@ApiStatus.Experimental
public interface ConfigAddon {
    /**
     * Returns the name of the addon, this will be shown in the addons list.
     *
     * @return the name of the addon
     */
    Component getName();
    
    /**
     * Returns the description of the addon, this will be shown in the addons list.
     *
     * @return the description of the addon
     */
    Component getDescription();
    
    /**
     * Opens the config screen for this addon, given the parent screen.
     * Do not call {@link net.minecraft.client.Minecraft#setScreen(Screen)} directly,
     * and make sure to set the screen as the parent screen to exit the config screen.
     *
     * @param parent the parent screen
     */
    Screen createScreen(Screen parent);
}
