package me.shedaniel.rei.api.common.util;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CondensedEntryStacks {

    private static final Function<Item, ItemStack> ITEM_DEFAULT_STACK = Item::getDefaultInstance;
    private static final Function<Fluid, FluidStack> FLUID_DEFAULT_STACK = fluid -> FluidStack.create(fluid, FluidStack.bucketAmount());

    private static final Function<ItemStack, Item> ITEM_STACK_TO_ITEM = ItemStack::getItem;
    private static final Function<FluidStack, Fluid> FLUID_STACK_TO_FLUID = FluidStack::getFluid;

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, TagKey<Fluid> fluidTagKey) {
        return of(location, fluid, fluidTagKey, FluidStack.bucketAmount());
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, TagKey<Fluid> fluidTagKey, long amount) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, FluidStack.create(fluid, amount), location, fluidTagKey, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, FluidStack stack, TagKey<Fluid> fluidTagKey) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, stack, location, fluidTagKey, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemStack stack, TagKey<Item> itemTagKey) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.ITEM, stack, location,itemTagKey, ITEM_DEFAULT_STACK, ITEM_STACK_TO_ITEM);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, TagKey<Item> itemTagKey) {
        return of(location, new ItemStack(item), itemTagKey);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, TagKey<Item> itemTagKey, int amount) {
        return of(location, new ItemStack(item, amount), itemTagKey);
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, Predicate<Fluid> predicate) {
        return of(location, fluid, predicate, FluidStack.bucketAmount());
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, Predicate<Fluid> predicate, long amount) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, FluidStack.create(fluid, amount), location, Registry.FLUID_REGISTRY, predicate, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, FluidStack stack, Predicate<Fluid> predicate) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, stack, location, Registry.FLUID_REGISTRY, predicate, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemStack stack, Predicate<Item> predicate) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.ITEM, stack, location, Registry.ITEM_REGISTRY, predicate, ITEM_DEFAULT_STACK, ITEM_STACK_TO_ITEM);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, Predicate<Item> predicate) {
        return of(location, new ItemStack(item), predicate);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, Predicate<Item> predicate, int amount) {
        return of(location, new ItemStack(item, amount), predicate);
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, Collection<Fluid> collection) {
        return of(location, fluid, collection, FluidStack.bucketAmount());
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, Fluid fluid, Collection<Fluid> collection, long amount) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, FluidStack.create(fluid, amount), location, Registry.FLUID_REGISTRY, collection, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<FluidStack> of(ResourceLocation location, FluidStack stack, Collection<Fluid> collection) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.FLUID, stack, location, Registry.FLUID_REGISTRY, collection, FLUID_DEFAULT_STACK, FLUID_STACK_TO_FLUID);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemStack stack, Collection<Item> collection) {
        return EntryStack.ofCondensedEntry(VanillaEntryTypes.ITEM, stack, location, Registry.ITEM_REGISTRY, collection, ITEM_DEFAULT_STACK, ITEM_STACK_TO_ITEM);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, Collection<Item> collection) {
        return of(location, new ItemStack(item), collection);
    }

    public static EntryStack<ItemStack> of(ResourceLocation location, ItemLike item, Collection<Item> collection, int amount) {
        return of(location, new ItemStack(item, amount), collection);
    }
}
