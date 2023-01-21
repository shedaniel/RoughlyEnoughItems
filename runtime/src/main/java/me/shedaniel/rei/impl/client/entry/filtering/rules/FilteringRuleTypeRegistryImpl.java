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

package me.shedaniel.rei.impl.client.entry.filtering.rules;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Iterator;

@ApiStatus.Internal
public class FilteringRuleTypeRegistryImpl extends AbstractList<FilteringRuleType<?>> implements FilteringRuleTypeRegistry {
    private final BiMap<ResourceLocation, FilteringRuleType<?>> types = HashBiMap.create();
    
    public FilteringRuleTypeRegistryImpl() {
        register(new ResourceLocation("roughlyenoughitems", "search"), SearchFilteringRuleType.INSTANCE);
        register(new ResourceLocation("roughlyenoughitems", "manual"), ManualFilteringRuleType.INSTANCE);
        register(new ResourceLocation("roughlyenoughitems", "basic"), BasicFilteringRuleType.INSTANCE);
    }
    
    @Override
    public Iterator<FilteringRuleType<?>> iterator() {
        return types.values().iterator();
    }
    
    @Override
    public FilteringRuleType<?> get(int index) {
        return Iterators.get(iterator(), index);
    }
    
    @Override
    public int size() {
        return types.size();
    }
    
    @Override
    @Nullable
    public FilteringRuleType<?> get(ResourceLocation id) {
        return types.get(id);
    }
    
    @Override
    @Nullable
    public ResourceLocation getId(FilteringRuleType<?> rule) {
        return types.inverse().get(rule);
    }
    
    @Override
    public void register(ResourceLocation id, FilteringRuleType<?> rule) {
        types.put(id, rule);
        InternalLogger.getInstance().debug("Added filtering rule [%s]: %s", id, rule);
    }
    
    @Override
    public BasicFilteringRule<?> basic() {
        return BasicFilteringRuleImpl.INSTANCE;
    }
}
