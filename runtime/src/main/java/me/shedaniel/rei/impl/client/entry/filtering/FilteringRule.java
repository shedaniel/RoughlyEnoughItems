/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.entry.filtering;

import com.mojang.serialization.Lifecycle;
import me.shedaniel.rei.impl.client.config.entries.FilteringEntry;
import me.shedaniel.rei.impl.client.entry.filtering.rules.ManualFilteringRule;
import me.shedaniel.rei.impl.client.entry.filtering.rules.SearchFilteringRule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.function.BiFunction;

@ApiStatus.Internal
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FilteringRule<T extends FilteringRule<?>> {
    ResourceKey<Registry<FilteringRule<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("roughlyenoughitems", "filtering_rule"));
    Registry<FilteringRule<?>> REGISTRY = Util.make(new MappedRegistry<>(REGISTRY_KEY, Lifecycle.stable()), registry -> {
        Registry.register(registry, new ResourceLocation("roughlyenoughitems", "search"), new SearchFilteringRule());
        Registry.register(registry, new ResourceLocation("roughlyenoughitems", "manual"), new ManualFilteringRule());
    });
    
    static CompoundTag save(FilteringRule<?> rule, CompoundTag tag) {
        tag.putString("id", REGISTRY.getKey(rule).toString());
        tag.put("rule", rule.save(new CompoundTag()));
        return tag;
    }
    
    static FilteringRule<?> read(CompoundTag tag) {
        return REGISTRY.get(ResourceLocation.tryParse(tag.getString("id"))).createFromTag(tag.getCompound("rule"));
    }
    
    CompoundTag save(CompoundTag tag);
    
    T createFromTag(CompoundTag tag);
    
    FilteringResult processFilteredStacks(FilteringContext context);
    
    @ApiStatus.Internal
    default Optional<BiFunction<FilteringEntry, Screen, Screen>> createEntryScreen() {
        return Optional.empty();
    }
    
    default Component getTitle() {
        return Component.nullToEmpty(FilteringRule.REGISTRY.getKey(this).toString());
    }
    
    default Component getSubtitle() {
        return Component.nullToEmpty(null);
    }
    
    T createNew();
}
