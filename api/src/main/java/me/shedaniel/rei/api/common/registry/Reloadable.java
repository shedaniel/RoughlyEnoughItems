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

package me.shedaniel.rei.api.common.registry;

import me.shedaniel.rei.api.common.plugins.REIPlugin;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface Reloadable<P extends REIPlugin<?>> {
    default ReloadStage getStage() {
        return ReloadStage.END;
    }
    
    void startReload();
    
    default void startReload(ReloadStage stage) {
        if (stage == getStage()) {
            startReload();
        }
    }
    
    default void endReload() {}
    
    default void endReload(ReloadStage stage) {
        if (stage == getStage()) {
            endReload();
        }
    }
    
    @ApiStatus.Experimental
    default void beforeReloadable(ReloadStage stage, Reloadable<P> other) {}
    
    @ApiStatus.Experimental
    default void afterReloadable(ReloadStage stage, Reloadable<P> other) {}
    
    @ApiStatus.Experimental
    default void beforeReloadablePlugin(ReloadStage stage, Reloadable<P> other, P plugin) {}
    
    @ApiStatus.Experimental
    default void afterReloadablePlugin(ReloadStage stage, Reloadable<P> other, P plugin) {}
    
    /**
     * Accepts a {@link REIPlugin}
     *
     * @param plugin the plugin to accept
     */
    default void acceptPlugin(P plugin) {}
    
    default void acceptPlugin(P plugin, ReloadStage stage) {
        if (stage == getStage()) {
            acceptPlugin(plugin);
        }
    }
    
    /**
     * Returns whether {@link Reloadable#acceptPlugin(REIPlugin)} should be done in parallel.
     *
     * @return whether {@link Reloadable#acceptPlugin(REIPlugin)} should be done in parallel.
     */
    default boolean isConcurrent() {
        return false;
    }
}
