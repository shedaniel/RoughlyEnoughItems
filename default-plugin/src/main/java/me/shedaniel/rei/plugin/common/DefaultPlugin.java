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
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.client.categories.crafting.filler.*;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCookingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedCraftingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedSmithingDisplay;
import me.shedaniel.rei.plugin.client.displays.ClientsidedStoneCuttingDisplay;
import me.shedaniel.rei.plugin.common.displays.*;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.*;
import me.shedaniel.rei.plugin.common.displays.tag.TagNodes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@ApiStatus.Internal
public class DefaultPlugin implements BuiltinPlugin, REICommonPlugin {
    private static final CraftingRecipeFiller<?>[] CRAFTING_RECIPE_FILLERS = new CraftingRecipeFiller[]{
            new BannerDuplicateRecipeFiller(),
            new ShieldDecorationRecipeFiller(),
            new BookCloningRecipeFiller(),
            new FireworkRocketRecipeFiller(),
            new MapExtendingRecipeFiller()
    };
    
    static {
        TagNodes.init();
    }
    
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry) {
        Function<ItemStack, ItemEnchantments> enchantmentTag = stack -> {
            if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
                return stack.get(DataComponents.STORED_ENCHANTMENTS);
            }
            return stack.get(DataComponents.ENCHANTMENTS);
        };
        registry.register((context, stack) -> Objects.hashCode(enchantmentTag.apply(stack)), Items.ENCHANTED_BOOK);
        registry.registerComponents(Items.POTION);
        registry.registerComponents(Items.SPLASH_POTION);
        registry.registerComponents(Items.LINGERING_POTION);
        registry.registerComponents(Items.TIPPED_ARROW);
        registry.register((context, stack) -> 0, Items.FIREWORK_ROCKET, Items.FILLED_MAP);
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
    public void registerDisplays(ServerDisplayRegistry registry) {
        registry.beginRecipeFiller(CraftingRecipe.class)
                .filterType(RecipeType.CRAFTING)
                .fill(DefaultCraftingDisplay::of);
        registry.beginRecipeFiller(SmeltingRecipe.class)
                .filterType(RecipeType.SMELTING)
                .fill(DefaultSmeltingDisplay::new);
        registry.beginRecipeFiller(SmokingRecipe.class)
                .filterType(RecipeType.SMOKING)
                .fill(DefaultSmokingDisplay::new);
        registry.beginRecipeFiller(BlastingRecipe.class)
                .filterType(RecipeType.BLASTING)
                .fill(DefaultBlastingDisplay::new);
        registry.beginRecipeFiller(CampfireCookingRecipe.class)
                .filterType(RecipeType.CAMPFIRE_COOKING)
                .fill(DefaultCampfireDisplay::new);
        registry.beginRecipeFiller(StonecutterRecipe.class)
                .filterType(RecipeType.STONECUTTING)
                .fill(DefaultStoneCuttingDisplay::new);
        registry.beginRecipeFiller(SmithingTransformRecipe.class)
                .filterType(RecipeType.SMITHING)
                .fill(DefaultSmithingDisplay::ofTransforming);
        registry.beginRecipeFiller(SmithingTrimRecipe.class)
                .filterType(RecipeType.SMITHING)
                .fillMultiple(DefaultSmithingDisplay::fromTrimming);
        
        for (CraftingRecipeFiller<?> filler : CRAFTING_RECIPE_FILLERS) {
            filler.registerDisplays(registry);
        }
    }
    
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.register(id("client/crafting/shaped"), ClientsidedCraftingDisplay.Shaped.SERIALIZER);
        registry.register(id("client/crafting/shapeless"), ClientsidedCraftingDisplay.Shapeless.SERIALIZER);
        registry.register(id("client/smelting"), ClientsidedCookingDisplay.Smelting.SERIALIZER);
        registry.register(id("client/smoking"), ClientsidedCookingDisplay.Smoking.SERIALIZER);
        registry.register(id("client/blasting"), ClientsidedCookingDisplay.Blasting.SERIALIZER);
        registry.register(id("client/stone_cutting"), ClientsidedStoneCuttingDisplay.SERIALIZER);
        registry.register(id("client/smithing"), ClientsidedSmithingDisplay.SERIALIZER);
        registry.register(id("default/crafting/shaped"), DefaultShapedDisplay.SERIALIZER);
        registry.register(id("default/crafting/shapeless"), DefaultShapelessDisplay.SERIALIZER);
        registry.register(id("default/crafting/custom"), DefaultCustomDisplay.SERIALIZER);
        registry.register(id("default/crafting/custom_shaped"), DefaultCustomShapedDisplay.SERIALIZER);
        registry.register(id("default/crafting/custom_shapeless"), DefaultCustomShapelessDisplay.SERIALIZER);
        registry.register(id("extension/crafting/map_extending"), MapExtendingCraftingDisplay.SERIALIZER);
        registry.register(id("default/smelting"), DefaultSmeltingDisplay.SERIALIZER);
        registry.register(id("default/smoking"), DefaultSmokingDisplay.SERIALIZER);
        registry.register(id("default/blasting"), DefaultBlastingDisplay.SERIALIZER);
        registry.register(id("default/campfire"), DefaultCampfireDisplay.SERIALIZER);
        registry.register(id("default/stone_cutting"), DefaultStoneCuttingDisplay.SERIALIZER);
        registry.register(id("default/stripping"), DefaultStrippingDisplay.SERIALIZER);
        registry.register(id("default/brewing"), DefaultBrewingDisplay.SERIALIZER);
        registry.register(id("default/composting"), DefaultCompostingDisplay.SERIALIZER);
        registry.register(id("default/fuel"), DefaultFuelDisplay.SERIALIZER);
        registry.register(id("default/smithing"), DefaultSmithingDisplay.SERIALIZER);
        registry.register(id("default/smithing/trimming"), DefaultSmithingDisplay.Trimming.SERIALIZER);
        registry.register(id("default/anvil"), DefaultAnvilDisplay.SERIALIZER);
        registry.register(id("default/beacon_base"), DefaultBeaconBaseDisplay.SERIALIZER);
        registry.register(id("default/beacon_payment"), DefaultBeaconPaymentDisplay.SERIALIZER);
        registry.register(id("default/tilling"), DefaultTillingDisplay.SERIALIZER);
        registry.register(id("default/pathing"), DefaultPathingDisplay.SERIALIZER);
        registry.register(id("default/waxing"), DefaultWaxingDisplay.SERIALIZER);
        registry.register(id("default/waxing_scraping"), DefaultWaxScrapingDisplay.SERIALIZER);
        registry.register(id("default/oxidizing"), DefaultOxidizingDisplay.SERIALIZER);
        registry.register(id("default/oxidizing_scraping"), DefaultOxidationScrapingDisplay.SERIALIZER);
        registry.register(id("roughlyenoughitems:default/information"), DefaultInformationDisplay.SERIALIZER);
    }
    
    @Override
    public double getPriority() {
        return -100;
    }
    
    private static Identifier id(String path) {
        return Identifier.parse(path);
    }
}
