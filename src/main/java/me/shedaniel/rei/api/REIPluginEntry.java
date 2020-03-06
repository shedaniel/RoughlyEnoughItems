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

package me.shedaniel.rei.api;

import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.util.version.VersionParsingException;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Get base class of a REI plugin.
 */
public interface REIPluginEntry {
    
    /**
     * @return the minimum version for the REI plugin to load
     * @deprecated deprecated due to the lack of need of this method, please declare conflicts with fabric.mod.json
     */
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default SemanticVersion getMinimumVersion() throws VersionParsingException {
        return null;
    }
    
    /**
     * @return the priority of the plugin, the smaller the number, the earlier it is called.
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * @return the unique identifier of the plugin.
     */
    Identifier getPluginIdentifier();
    
}
