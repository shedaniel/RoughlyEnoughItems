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

package me.shedaniel.rei.plugin.common.displays.tag;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ApiStatus.Experimental
public class DefaultTagDisplay<S, T> implements Display {
    private final TagKey<S> key;
    private final Function<Holder<S>, EntryStack<T>> mapper;
    private final List<EntryIngredient> ingredients;
    
    public DefaultTagDisplay(TagKey<S> key, Function<Holder<S>, EntryStack<T>> mapper) {
        this.key = key;
        this.mapper = mapper;
        this.ingredients = CollectionUtils.map(EntryIngredients.ofTag(key, mapper), EntryIngredient::of);
    }
    
    public static DefaultTagDisplay<ItemLike, ItemStack> ofItems(TagKey<ItemLike> key) {
        return new DefaultTagDisplay<>(key, DefaultTagDisplay::extractItem);
    }
    
    public static DefaultTagDisplay<Fluid, FluidStack> ofFluids(TagKey<Fluid> key) {
        return new DefaultTagDisplay<>(key, DefaultTagDisplay::extractFluid);
    }
    
    private static EntryStack<ItemStack> extractItem(Holder<ItemLike> holder) {
        return EntryStacks.of(holder.value());
    }
    
    private static EntryStack<FluidStack> extractFluid(Holder<Fluid> holder) {
        return EntryStacks.of(holder.value());
    }
    
    @Override
    public List<EntryIngredient> getInputEntries() {
        return this.ingredients;
    }
    
    @Override
    public List<EntryIngredient> getOutputEntries() {
        return this.ingredients;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.TAG;
    }
    
    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.of(key.location());
    }
    
    public TagKey<S> getKey() {
        return key;
    }
}
