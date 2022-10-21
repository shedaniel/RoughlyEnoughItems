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
import me.shedaniel.rei.impl.common.Internals;

import java.util.List;

public interface ParentReloadable<P extends REIPlugin<?>> extends Reloadable<P> {
    List<Reloadable<P>> getReloadables();
    
    void registerReloadable(Reloadable<? extends P> reloadable);
    
    default <R extends Reloadable<? extends P>> void registerReloadable(Class<R> reloadableClass) {
        registerReloadable(Internals.resolveService(reloadableClass));
    }
    
    @Override
    default void startReload() {
        for (ReloadStage stage : ReloadStage.values()) {
            startReload(stage);
        }
    }
    
    @Override
    default void endReload() {
        for (ReloadStage stage : ReloadStage.values()) {
            endReload(stage);
        }
    }
    
    @Override
    default void startReload(ReloadStage stage) {
        for (Reloadable<P> reloadable : getReloadables()) {
            reloadable.startReload(stage);
        }
    }
    
    @Override
    default void endReload(ReloadStage stage) {
        for (Reloadable<P> reloadable : getReloadables()) {
            reloadable.endReload(stage);
        }
    }
    
    @Override
    default void beforeReloadable(ReloadStage stage, Reloadable<P> other) {
        for (Reloadable<P> reloadable : getReloadables()) {
            reloadable.beforeReloadable(stage, other);
        }
    }
    
    @Override
    default void afterReloadable(ReloadStage stage, Reloadable<P> other) {
        for (Reloadable<P> reloadable : getReloadables()) {
            reloadable.afterReloadable(stage, other);
        }
    }
}
