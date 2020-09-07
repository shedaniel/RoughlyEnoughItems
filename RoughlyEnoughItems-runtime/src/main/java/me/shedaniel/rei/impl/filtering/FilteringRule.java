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

package me.shedaniel.rei.impl.filtering;

import com.mojang.serialization.Lifecycle;
import me.shedaniel.rei.gui.config.entry.FilteringEntry;
import me.shedaniel.rei.impl.filtering.rules.ManualFilteringRule;
import me.shedaniel.rei.impl.filtering.rules.SearchFilteringRule;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

@ApiStatus.Internal
@ApiStatus.Experimental
@OnlyIn(Dist.CLIENT)
public interface FilteringRule<T extends FilteringRule<?>> {
    RegistryKey<Registry<FilteringRule<?>>> REGISTRY_KEY = RegistryKey.createRegistryKey(new ResourceLocation("roughlyenoughitems", "filtering_rule"));
    Registry<FilteringRule<?>> REGISTRY = createRegistry();
    
    @ApiStatus.Internal
    static Registry<FilteringRule<?>> createRegistry() {
        SimpleRegistry<FilteringRule<?>> registry = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, new ResourceLocation("roughlyenoughitems", "search"), new SearchFilteringRule());
        Registry.register(registry, new ResourceLocation("roughlyenoughitems", "manual"), new ManualFilteringRule());
        return registry;
    }
    
    static CompoundNBT toTag(FilteringRule<?> rule, CompoundNBT tag) {
        tag.putString("id", REGISTRY.getKey(rule).toString());
        tag.put("rule", rule.toTag(new CompoundNBT()));
        return tag;
    }
    
    static FilteringRule<?> fromTag(CompoundNBT tag) {
        return REGISTRY.get(ResourceLocation.tryParse(tag.getString("id"))).createFromTag(tag.getCompound("rule"));
    }
    
    CompoundNBT toTag(CompoundNBT tag);
    
    T createFromTag(CompoundNBT tag);
    
    @NotNull
    FilteringResult processFilteredStacks(@NotNull FilteringContext context);
    
    @ApiStatus.Internal
    default Optional<BiFunction<FilteringEntry, Screen, Screen>> createEntryScreen() {
        return Optional.empty();
    }
    
    default ITextComponent getTitle() {
        return ITextComponent.nullToEmpty(FilteringRule.REGISTRY.getKey(this).toString());
    }
    
    default ITextComponent getSubtitle() {
        return ITextComponent.nullToEmpty(null);
    }
    
    T createNew();
}
