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
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class EntryIngredients {
    private EntryIngredients() {
    }
    
    public static EntryIngredient of(ItemLike stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }
    
    public static EntryIngredient of(ItemLike stack, int amount) {
        return EntryIngredient.of(EntryStacks.of(stack, amount));
    }
    
    public static EntryIngredient of(ItemStack stack) {
        return EntryIngredient.of(EntryStacks.of(stack));
    }

    public static EntryIngredient of(ItemStackTemplate stack) {
        return EntryIngredient.of(EntryStacks.of(stack.create()));
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
    
    public static EntryIngredient ofFluidHolder(Holder<Fluid> fluid) {
        return EntryIngredient.of(EntryStacks.ofFluidHolder(fluid));
    }
    
    public static EntryIngredient ofFluidHolder(Holder<Fluid> fluid, long amount) {
        return EntryIngredient.of(EntryStacks.ofFluidHolder(fluid, amount));
    }
    
    public static EntryIngredient ofItemHolder(Holder<? extends ItemLike> item) {
        return EntryIngredient.of(EntryStacks.ofItemHolder(item));
    }
    
    public static EntryIngredient ofItemHolder(Holder<? extends ItemLike> item, int amount) {
        return EntryIngredient.of(EntryStacks.ofItemHolder(item, amount));
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
    
    public static <T> EntryIngredient from(Iterable<T> stacks, Function<T, ? extends EntryStack<?>> function) {
        if (stacks instanceof Collection<T> collection) {
            return from(collection, collection.size(), function);
        } else {
            if (!stacks.iterator().hasNext()) return EntryIngredient.empty();
            EntryIngredient.Builder result = EntryIngredient.builder();
            for (T t : stacks) {
                EntryStack<?> stack = function.apply(t);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            }
            return result.build();
        }
    }
    
    public static <T> EntryIngredient from(Iterable<T> stacks, int size, Function<T, ? extends EntryStack<?>> function) {
        if (size == 0) return EntryIngredient.empty();
        if (size == 1) return EntryIngredient.of(function.apply(stacks.iterator().next()));
        EntryIngredient.Builder result = EntryIngredient.builder(size);
        for (T t : stacks) {
            EntryStack<?> stack = function.apply(t);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result.build();
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks) {
        return ofItems(stacks, 1);
    }
    
    public static EntryIngredient ofItems(Collection<ItemLike> stacks, int amount) {
        return from(stacks, stack -> EntryStacks.of(stack, amount));
    }
    
    public static EntryIngredient ofItemStacks(Collection<ItemStack> stacks) {
        return of(VanillaEntryTypes.ITEM, stacks);
    }
    
    public static EntryIngredient ofIngredient(Ingredient ingredient) {
        return Internals.toEntryIngredient(ingredient);
    }
    
    public static List<EntryIngredient> ofIngredients(List<Ingredient> ingredients) {
        if (ingredients.size() == 0) return Collections.emptyList();
        if (ingredients.size() == 1) {
            Ingredient ingredient = ingredients.get(0);
            EntryIngredient entryIngredient = ofIngredient(ingredient);
            if (entryIngredient.isEmpty()) return List.of();
            return List.of(entryIngredient);
        }
        boolean emptyFlag = true;
        List<EntryIngredient> result = new ArrayList<>(ingredients.size());
        for (int i = ingredients.size() - 1; i >= 0; i--) {
            Ingredient ingredient = ingredients.get(i);
            EntryIngredient entryIngredient = ofIngredient(ingredient);
            if (emptyFlag && entryIngredient.isEmpty()) continue;
            result.add(0, entryIngredient);
            emptyFlag = false;
        }
        return ImmutableList.copyOf(result);
    }
    
    public static <S, T> EntryIngredient ofTag(HolderGetter.Provider provider, TagKey<S> tagKey, Function<Holder<S>, EntryStack<T>> mapper) {
        HolderGetter<S> getter = provider.lookupOrThrow(tagKey.registry());
        HolderSet.Named<S> holders = getter.get(tagKey).orElse(null);
        if (holders == null) return EntryIngredient.empty();
        return EntryIngredients.from(holders, holders.size(), mapper);
    }
    
    public static <S, T> List<EntryIngredient> ofTags(HolderGetter.Provider provider, Iterable<TagKey<S>> tagKeys, Function<Holder<S>, EntryStack<T>> mapper) {
        if (tagKeys instanceof Collection<?> collection && collection.isEmpty()) return Collections.emptyList();
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (TagKey<S> tagKey : tagKeys) {
            ingredients.add(ofTag(provider, tagKey, mapper));
        }
        return ingredients.build();
    }
    
    public static <T extends ItemLike> EntryIngredient ofItemTag(TagKey<T> tagKey) {
        return ofTag(BasicDisplay.registryAccess(), tagKey, EntryStacks::ofItemHolder);
    }
    
    public static EntryIngredient ofFluidTag(TagKey<Fluid> tagKey) {
        return ofTag(BasicDisplay.registryAccess(), tagKey, EntryStacks::ofFluidHolder);
    }
    
    public static <T extends ItemLike> List<EntryIngredient> ofItemTags(Iterable<TagKey<T>> tagKey) {
        return ofTags(BasicDisplay.registryAccess(), tagKey, EntryStacks::ofItemHolder);
    }
    
    public static List<EntryIngredient> ofFluidTags(Iterable<TagKey<Fluid>> tagKey) {
        return ofTags(BasicDisplay.registryAccess(), tagKey, EntryStacks::ofFluidHolder);
    }
    
    public static EntryIngredient ofItemsHolderSet(HolderSet<Item> stacks) {
        return stacks.unwrap().map(EntryIngredients::ofItemTag, holders -> from(holders, EntryStacks::ofItemHolder));
    }
    
    public static EntryIngredient ofFluidHolderSet(HolderSet<Fluid> stacks) {
        return stacks.unwrap().map(EntryIngredients::ofFluidTag, holders -> from(holders, EntryStacks::ofFluidHolder));
    }
    
    public static EntryIngredient ofSlotDisplay(SlotDisplay slot) {
        return switch (slot) {
            case SlotDisplay.Empty $ -> EntryIngredient.empty();
            case SlotDisplay.ItemSlotDisplay s -> ofItemHolder(s.item());
            case SlotDisplay.ItemStackSlotDisplay s -> of(s.stack());
            case SlotDisplay.TagSlotDisplay s -> ofItemTag(s.tag());
            case SlotDisplay.Composite s -> {
                EntryIngredient.Builder builder = EntryIngredient.builder();
                for (SlotDisplay slotDisplay : s.contents()) {
                    builder.addAll(ofSlotDisplay(slotDisplay));
                }
                yield builder.build();
            }
            case SlotDisplay.AnyFuel s -> resolveSlotDisplay(s);
            default -> resolveSlotDisplay(slot);
        };
    }

    public static ContextMap slotDisplayContext() {
        return Internals.getSlotDisplayContext();
    }

    private static EntryIngredient resolveSlotDisplay(SlotDisplay slot) {
        try {
            return ofItemStacks(slot.resolveForStacks(slotDisplayContext()));
        } catch (Exception e) {
            InternalLogger.getInstance().warn("Failed to resolve slot display: " + slot, e);
            return EntryIngredient.empty();
        }
    }
    
    public static List<EntryIngredient> ofSlotDisplays(Iterable<SlotDisplay> slots) {
        if (slots instanceof Collection<?> collection && collection.isEmpty()) return Collections.emptyList();
        ImmutableList.Builder<EntryIngredient> ingredients = ImmutableList.builder();
        for (SlotDisplay slot : slots) {
            ingredients.add(ofSlotDisplay(slot));
        }
        return ingredients.build();
    }
    
    public static <T> boolean testFuzzy(EntryIngredient ingredient, EntryStack<T> stack) {
        for (EntryStack<?> ingredientStack : ingredient) {
            if (EntryStacks.equalsFuzzy(ingredientStack, stack)) {
                return true;
            }
        }
        
        return false;
    }
}
