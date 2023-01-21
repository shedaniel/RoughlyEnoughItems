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

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.plugin.common.displays.*;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconBaseDisplay;
import me.shedaniel.rei.plugin.common.displays.beacon.DefaultBeaconPaymentDisplay;
import me.shedaniel.rei.plugin.common.displays.brewing.DefaultBrewingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultBlastingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmeltingDisplay;
import me.shedaniel.rei.plugin.common.displays.cooking.DefaultSmokingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.common.displays.tag.DefaultTagDisplay;
import org.jetbrains.annotations.ApiStatus;

public interface BuiltinPlugin {
    CategoryIdentifier<DefaultCraftingDisplay<?>> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");
    CategoryIdentifier<DefaultSmeltingDisplay> SMELTING = CategoryIdentifier.of("minecraft", "plugins/smelting");
    CategoryIdentifier<DefaultSmokingDisplay> SMOKING = CategoryIdentifier.of("minecraft", "plugins/smoking");
    CategoryIdentifier<DefaultBlastingDisplay> BLASTING = CategoryIdentifier.of("minecraft", "plugins/blasting");
    CategoryIdentifier<DefaultCampfireDisplay> CAMPFIRE = CategoryIdentifier.of("minecraft", "plugins/campfire");
    CategoryIdentifier<DefaultStoneCuttingDisplay> STONE_CUTTING = CategoryIdentifier.of("minecraft", "plugins/stone_cutting");
    CategoryIdentifier<DefaultStrippingDisplay> STRIPPING = CategoryIdentifier.of("minecraft", "plugins/stripping");
    CategoryIdentifier<DefaultBrewingDisplay> BREWING = CategoryIdentifier.of("minecraft", "plugins/brewing");
    CategoryIdentifier<DefaultCompostingDisplay> COMPOSTING = CategoryIdentifier.of("minecraft", "plugins/composting");
    CategoryIdentifier<DefaultFuelDisplay> FUEL = CategoryIdentifier.of("minecraft", "plugins/fuel");
    CategoryIdentifier<DefaultSmithingDisplay> SMITHING = CategoryIdentifier.of("minecraft", "plugins/smithing");
    CategoryIdentifier<DefaultAnvilDisplay> ANVIL = CategoryIdentifier.of("minecraft", "plugins/anvil");
    CategoryIdentifier<DefaultBeaconBaseDisplay> BEACON_BASE = CategoryIdentifier.of("minecraft", "plugins/beacon_base");
    CategoryIdentifier<DefaultBeaconPaymentDisplay> BEACON_PAYMENT = CategoryIdentifier.of("minecraft", "plugins/beacon_payment");
    CategoryIdentifier<DefaultTillingDisplay> TILLING = CategoryIdentifier.of("minecraft", "plugins/tilling");
    CategoryIdentifier<DefaultPathingDisplay> PATHING = CategoryIdentifier.of("minecraft", "plugins/pathing");
    @ApiStatus.Experimental
    CategoryIdentifier<DefaultWaxingDisplay> WAXING = CategoryIdentifier.of("minecraft", "plugins/waxing");
    @ApiStatus.Experimental
    CategoryIdentifier<DefaultWaxScrapingDisplay> WAX_SCRAPING = CategoryIdentifier.of("minecraft", "plugins/wax_scraping");
    @ApiStatus.Experimental
    CategoryIdentifier<DefaultOxidizingDisplay> OXIDIZING = CategoryIdentifier.of("minecraft", "plugins/oxidizing");
    @ApiStatus.Experimental
    CategoryIdentifier<DefaultOxidationScrapingDisplay> OXIDATION_SCRAPING = CategoryIdentifier.of("minecraft", "plugins/oxidation_scraping");
    @ApiStatus.Experimental
    CategoryIdentifier<DefaultTagDisplay<?, ?>> TAG = CategoryIdentifier.of("minecraft", "plugins/tag");
    CategoryIdentifier<DefaultInformationDisplay> INFO = CategoryIdentifier.of("roughlyenoughitems", "plugins/information");
}
