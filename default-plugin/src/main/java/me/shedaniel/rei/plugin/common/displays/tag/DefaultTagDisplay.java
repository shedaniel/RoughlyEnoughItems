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

package me.shedaniel.rei.plugin.common.displays.tag;

import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.mixin.FluidTagsAccessor;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ApiStatus.Experimental
public class DefaultTagDisplay<S, T> implements Display {
    private final TagCollection<S> tagCollection;
    private final String tagCollectionId;
    private final ResourceLocation key;
    private final Registry<S> registry;
    private final Function<S, EntryStack<T>> mapper;
    private final List<EntryIngredient> ingredients;
    
    public DefaultTagDisplay(TagCollection<S> tagCollection, String tagCollectionId, ResourceLocation key, Registry<S> registry, Function<S, EntryStack<T>> mapper) {
        this.tagCollection = tagCollection;
        this.tagCollectionId = tagCollectionId;
        this.key = key;
        this.registry = registry;
        this.mapper = mapper;
        this.ingredients = CollectionUtils.map(EntryIngredients.ofTag(tagCollection, key, mapper), EntryIngredient::of);
    }
    
    public static DefaultTagDisplay<Item, ItemStack> ofItems(ResourceLocation key) {
        return new DefaultTagDisplay<>(ItemTags.getAllTags(), "items", key, Registry.ITEM, DefaultTagDisplay::extractItem);
    }
    
    public static DefaultTagDisplay<Block, ItemStack> ofBlocks(ResourceLocation key) {
        return new DefaultTagDisplay<>(BlockTags.getAllTags(), "blocks", key, Registry.BLOCK, DefaultTagDisplay::extractBlock);
    }
    
    public static DefaultTagDisplay<Fluid, FluidStack> ofFluids(ResourceLocation key) {
        return new DefaultTagDisplay<>(FluidTagsAccessor.getHelper().getAllTags(), "fluids", key, Registry.FLUID, DefaultTagDisplay::extractFluid);
    }
    
    private static EntryStack<ItemStack> extractItem(Item holder) {
        return EntryStacks.of(holder);
    }
    
    private static EntryStack<ItemStack> extractBlock(Block holder) {
        return EntryStacks.of(holder);
    }
    
    private static EntryStack<FluidStack> extractFluid(Fluid holder) {
        return EntryStacks.of(holder);
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
        return Optional.of(key);
    }
    
    public String getTagCollectionId() {
        return tagCollectionId;
    }
    
    public TagCollection<S> getTagCollection() {
        return tagCollection;
    }
    
    public ResourceLocation getKey() {
        return key;
    }
    
    public Registry<S> getRegistry() {
        return registry;
    }
    
    public Function<S, EntryStack<T>> getMapper() {
        return mapper;
    }
}
