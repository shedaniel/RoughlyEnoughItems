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

package me.shedaniel.rei.impl.client.subsets;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class SubsetsRegistryImpl implements SubsetsRegistry {
    private final Map<String, Set<EntryStack<?>>> paths = Maps.newHashMap();
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerSubsets(this);
    }
    
    @Override
    public void startReload() {
        paths.clear();
    }
    
    @Override
    public List<String> getEntryPaths(EntryStack<?> stack) {
        List<String> strings = null;
        for (Map.Entry<String, Set<EntryStack<?>>> entry : paths.entrySet()) {
            if (CollectionUtils.findFirstOrNullEqualsExact(entry.getValue(), stack) != null) {
                if (strings == null)
                    strings = new ArrayList<>();
                strings.add(entry.getKey());
            }
        }
        return strings == null ? Collections.emptyList() : strings;
    }
    
    @Override
    public void registerPathEntry(String path, EntryStack<?> stack) {
        getOrCreatePathEntries(path).add(stack.normalize());
    }
    
    @Override
    public void registerPathEntries(String path, Collection<? extends EntryStack<?>> stacks) {
        Set<EntryStack<?>> entries = getOrCreatePathEntries(path);
        for (EntryStack<?> stack : stacks) {
            entries.add(stack.normalize());
        }
    }
    
    @Nullable
    @Override
    public Set<EntryStack<?>> getPathEntries(String path) {
        if (!isPathValid(path))
            throw new IllegalArgumentException("Illegal path: " + path);
        return paths.get(path);
    }
    
    @Override
    public Set<String> getPaths() {
        return paths.keySet();
    }
    
    @Override
    public Set<EntryStack<?>> getOrCreatePathEntries(String path) {
        Set<EntryStack<?>> paths = getPathEntries(path);
        if (paths == null) {
            this.paths.put(path, Sets.newLinkedHashSet());
            paths = Objects.requireNonNull(getPathEntries(path));
        }
        return paths;
    }
    
    private boolean isPathValid(String path) {
        String[] pathSegments = path.split("/");
        for (String pathSegment : pathSegments) {
            if (!ResourceLocation.isValidResourceLocation(pathSegment))
                return false;
        }
        return true;
    }
}
