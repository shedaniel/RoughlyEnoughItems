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

package me.shedaniel.rei.api.client.subsets;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public interface SubsetsRegistry extends Reloadable<REIClientPlugin> {
    static SubsetsRegistry getInstance() {
        return PluginManager.getClientInstance().get(SubsetsRegistry.class);
    }
    
    /**
     * Gets all paths an entry is in, note that this is a really slow call as it looks through all paths.
     */
    List<String> getEntryPaths(EntryStack<?> stack);
    
    @Nullable
    Set<EntryStack<?>> getPathEntries(String path);
    
    Set<EntryStack<?>> getOrCreatePathEntries(String path);
    
    Set<String> getPaths();
    
    void registerPathEntry(String path, EntryStack<?> stack);
    
    void registerPathEntries(String path, Collection<? extends EntryStack<?>> stacks);
    
    default void registerPathEntries(String path, EntryStack<?>... stacks) {
        registerPathEntries(path, Arrays.asList(stacks));
    }
}
