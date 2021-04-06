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

package me.shedaniel.rei.plugin.common;

import me.shedaniel.architectury.hooks.FluidStackHooks;
import me.shedaniel.architectury.utils.NbtType;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparator;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.simple.RecipeBookGridMenuInfo;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.*;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultCookingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class DefaultPlugin implements BuiltinPlugin, REIServerPlugin {
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        ToLongFunction<Tag> nbtHasher = ItemComparator.nbtHasher();
        Function<ItemStack, ListTag> enchantmentTag = stack -> {
            CompoundTag tag = stack.getTag();
            if (tag == null) return null;
            if (!tag.contains("Enchantments", NbtType.LIST)) return null;
            return tag.getList("Enchantments", NbtType.COMPOUND);
        };
        registry.register((context, stack) -> nbtHasher.applyAsLong(enchantmentTag.apply(stack)), Items.ENCHANTED_BOOK);
        registry.registerNbt(Items.POTION);
        registry.registerNbt(Items.SPLASH_POTION);
        registry.registerNbt(Items.LINGERING_POTION);
        registry.registerNbt(Items.TIPPED_ARROW);
    }
    
    @Override
    public void registerFluidSupport(FluidSupportProvider support) {
        support.register(entry -> {
            ItemStack stack = entry.getValue();
            if (stack.getItem() instanceof BucketItem) {
                return InteractionResultHolder.success(Stream.of(EntryStacks.of(((BucketItem) stack.getItem()).content, FluidStackHooks.bucketAmount())));
            }
            return InteractionResultHolder.pass(null);
        });
    }
    
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(CRAFTING, DefaultCraftingDisplay.Serializer.INSTANCE);
        registry.register(SMELTING, DefaultCookingDisplay.serializer(DefaultSmeltingDisplay::new));
        registry.register(SMOKING, DefaultCookingDisplay.serializer(DefaultSmokingDisplay::new));
        registry.register(BLASTING, DefaultCookingDisplay.serializer(DefaultBlastingDisplay::new));
        registry.register(CAMPFIRE, DefaultCampfireDisplay.serializer());
        registry.register(STONE_CUTTING, DefaultStoneCuttingDisplay.serializer());
        registry.register(STRIPPING, DefaultStrippingDisplay.serializer());
        registry.register(BREWING, DefaultBrewingDisplay.serializer());
        registry.register(COMPOSTING, DefaultCompostingDisplay.serializer());
        registry.register(FUEL, DefaultFuelDisplay.serializer());
        registry.register(SMITHING, DefaultSmithingDisplay.serializer());
        registry.register(BEACON_BASE, DefaultBeaconDisplay.serializer(DefaultBeaconBaseDisplay::new));
        registry.register(BEACON_PAYMENT, DefaultBeaconDisplay.serializer(DefaultBeaconPaymentDisplay::new));
        registry.register(TILLING, DefaultTillingDisplay.serializer());
        registry.register(PATHING, DefaultPathingDisplay.serializer());
        registry.register(INFO, DefaultInformationDisplay.serializer());
    }
    
    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        registry.register(BuiltinPlugin.CRAFTING, CraftingMenu.class, new RecipeBookGridMenuInfo<CraftingMenu, DefaultCraftingDisplay>() {
            @Override
            public List<List<ItemStack>> getDisplayInputs(MenuInfoContext<CraftingMenu, ?, DefaultCraftingDisplay> context) {
                return context.getDisplay().getOrganisedInputEntries(this, context.getMenu());
            }
        });
        registry.register(BuiltinPlugin.CRAFTING, InventoryMenu.class, new RecipeBookGridMenuInfo<InventoryMenu, DefaultCraftingDisplay>() {
            @Override
            public List<List<ItemStack>> getDisplayInputs(MenuInfoContext<InventoryMenu, ?, DefaultCraftingDisplay> context) {
                return context.getDisplay().getOrganisedInputEntries(this, context.getMenu());
            }
        });
        registry.register(BuiltinPlugin.SMELTING, FurnaceMenu.class, new RecipeBookGridMenuInfo<>());
        registry.register(BuiltinPlugin.SMOKING, SmokerMenu.class, new RecipeBookGridMenuInfo<>());
        registry.register(BuiltinPlugin.BLASTING, BlastFurnaceMenu.class, new RecipeBookGridMenuInfo<>());
    }
    
    @Override
    public int getPriority() {
        return -100;
    }
}
