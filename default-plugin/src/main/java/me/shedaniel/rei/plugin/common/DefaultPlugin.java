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

package me.shedaniel.rei.plugin.common;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.hooks.fluid.FluidBucketHooks;
import dev.architectury.hooks.fluid.FluidStackHooks;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
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
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@ApiStatus.Internal
public class DefaultPlugin implements BuiltinPlugin, REIServerPlugin {
    static {
        TagNodes.init();
    }
    
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        Function<ItemStack, ItemEnchantments> enchantmentTag = stack -> {
            if (!stack.has(DataComponents.ENCHANTMENTS)) {
                if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
                    return stack.get(DataComponents.STORED_ENCHANTMENTS);
                }
                return null;
            }
            return stack.get(DataComponents.ENCHANTMENTS);
        };
        registry.register((context, stack) -> Objects.hashCode(enchantmentTag.apply(stack)), Items.ENCHANTED_BOOK);
        registry.registerComponents(Items.POTION);
        registry.registerComponents(Items.SPLASH_POTION);
        registry.registerComponents(Items.LINGERING_POTION);
        registry.registerComponents(Items.TIPPED_ARROW);
    }
    
    @Override
    public void registerFluidSupport(FluidSupportProvider support) {
        support.register(entry -> {
            ItemStack stack = entry.getValue();
            Item item = stack.getItem();
            if (item instanceof BucketItem bucketItem) {
                Fluid fluid = FluidBucketHooks.getFluid(bucketItem);
                if (fluid != null) {
                    return CompoundEventResult.interruptTrue(Stream.of(EntryStacks.of(fluid, FluidStackHooks.bucketAmount())));
                }
            }
            return CompoundEventResult.pass();
        });
    }
    
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(CRAFTING, DefaultCraftingDisplay.serializer());
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
        registry.register(WAXING, DefaultWaxingDisplay.serializer());
        registry.register(WAX_SCRAPING, DefaultWaxScrapingDisplay.serializer());
        registry.register(OXIDIZING, DefaultOxidizingDisplay.serializer());
        registry.register(OXIDATION_SCRAPING, DefaultOxidationScrapingDisplay.serializer());
        registry.register(INFO, DefaultInformationDisplay.serializer());
    }
    
    @Override
    public double getPriority() {
        return -100;
    }
}
