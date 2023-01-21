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

package me.shedaniel.rei.api.common.util;

import com.google.common.collect.ImmutableList;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class EntryIngredients {
    private EntryIngredients() {}
    
    public static EntryIngredient of(ItemLike stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(ItemLike stack, int amount) {
        return EntryIngredient.of(EntryStacks.of(stack, amount));
    }
    
    public static EntryIngredient of(ItemStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(Fluid fluid) {
        return EntryIngredient.of(EntryStacks.of(fluid));
    }
    
    public static EntryIngredient of(Fluid fluid, long amount) {
        return EntryIngredient.of(EntryStacks.of(fluid, amount));
    }
    
    public static EntryIngredient of(FluidStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static <T> EntryIngredient of(EntryType<T> type, Collection<T> values) {
        return of(type.getDefinition(), values);
    }
    
    public static <T> EntryIngredient of(EntryDefinition<T> definition, Collection<T> values) {
        if (values.size() == 0) return EntryIngredient.empty();
        if (values.size() == 1) return EntryIngredient.of(EntryStack.of(definition, values.iterator().next()));
        EntryIngredient.Builder result = EntryIngredient.builder(values.size());
        for (T value : values) {
            result.add(EntryStack.of(definition, value));
        }
        return result.build();
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks) {
        return ofItems(stacks, 1);
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks, int amount) {
        if (stacks.size() == 0) return EntryIngredient.empty();
        if (stacks.size() == 1) return EntryIngredient.of(EntryStacks.of(stacks.iterator().next(), amount));
        EntryIngredient.Builder result = EntryIngredient.builder(stacks.size());
        for (ItemLike stack : stacks) {
            result.add(EntryStacks.of(stack, amount));
        }
        return result.build();
    }
    
    public static EntryIngredient ofItemStacks(Collection<ItemStack> stacks) {
        return of(VanillaEntryTypes.ITEM, stacks);
    }
    
    public static EntryIngredient ofIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return EntryIngredient.empty();
        ItemStack[] matchingStacks = ingredient.getItems();
        if (matchingStacks.length == 0) return EntryIngredient.empty();
        if (matchingStacks.length == 1) return EntryIngredient.of(EntryStacks.of(matchingStacks[0]));
        EntryIngredient.Builder result = EntryIngredient.builder(matchingStacks.length);
        for (ItemStack matchingStack : matchingStacks) {
            if (!matchingStack.isEmpty()) {
                result.add(EntryStacks.of(matchingStack));
            }
        }
        return result.build();
    }
    
    public static List<EntryIngredient> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            if (ingredient.isEmpty()) return Collections.emptyList();
            return Collections.singletonList(ofIngredient(ingredient));
        }
        boolean emptyFlag = true;
        List<EntryIngredient> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            if (emptyFlag && ingredient.isEmpty()) continue;
            result.add(0, ofIngredient(ingredient));
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <S, T> EntryIngredient ofTag(TagKey<S> tagKey, Function<Holder<S>, EntryStack<T>> mapper) {
        Registry<S> registry = ((Registry<Registry<S>>) BuiltInRegistries.REGISTRY).get((ResourceKey<Registry<S>>) tagKey.registry());
        HolderSet.Named<S> holders = registry.getTag(tagKey).orElse(null);
        if (holders == null) return EntryIngredient.empty();
        EntryIngredient.Builder result = EntryIngredient.builder(holders.size());
        for (Holder<S> holder : holders) {
            EntryStack<T> stack = mapper.apply(holder);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result.build();
    }
    
    public static <S, T> List<EntryIngredient> ofTags(Iterable<TagKey<S>> tagKeys, Function<Holder<S>, EntryStack<T>> mapper) {
        if (tagKeys instanceof Collection collection && collection.isEmpty()) return Collections.emptyList();
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (TagKey<S> tagKey : tagKeys) {
            ingredients.add(ofTag(tagKey, mapper));
        }
        return ingredients.build();
    }
    
    public static <T extends ItemLike> EntryIngredient ofItemTag(TagKey<T> tagKey) {
        return ofTag(tagKey, holder -> EntryStacks.of(holder.value()));
    }
    
    public static EntryIngredient ofFluidTag(TagKey<Fluid> tagKey) {
        return ofTag(tagKey, holder -> EntryStacks.of(holder.value()));
    }
    
    public static <T extends ItemLike> List<EntryIngredient> ofItemTags(Iterable<TagKey<T>> tagKey) {
        return ofTags(tagKey, holder -> EntryStacks.of(holder.value()));
    }
    
    public static List<EntryIngredient> ofFluidTags(Iterable<TagKey<Fluid>> tagKey) {
        return ofTags(tagKey, holder -> EntryStacks.of(holder.value()));
    }
    
    public static <T> boolean testFuzzy(EntryIngredient ingredient, EntryStack<T> stack) {
        for (EntryStack<?> ingredientStack : ingredient) {
            if (EntryStacks.equalsFuzzy(ingredientStack, stack)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static ListTag save(List<EntryIngredient> ingredients) {
        ListTag listTag = new ListTag();
        for (EntryIngredient ingredient : ingredients) {
            listTag.add(ingredient.saveIngredient());
        }
        return listTag;
    }
    
    public static List<EntryIngredient> read(ListTag listTag) {
        if (listTag.isEmpty()) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (Tag tag : listTag) {
            ingredients.add(EntryIngredient.read((ListTag) tag));
        }
        return ingredients.build();
    }
}
