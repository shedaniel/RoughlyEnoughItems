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

package me.shedaniel.rei.fabric;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.init.PlatformAdapter;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class PlatformAdapterImpl implements PlatformAdapter {
    @Override
    public List<PackResources> gatherClientDataPacks() {
        List<PackResources> packs = new ArrayList<>();
        // Vanilla data pack (the recipe JSONs under data/minecraft/recipe/).
        packs.add(ServerPacksSource.createVanillaPackSource());
        // Every mod's built-in data pack. Fabric only exposes mod CLIENT resources through a public
        // constant, so construct a SERVER_DATA creator explicitly to enumerate modded recipes.
        // ModResourcePackCreator is Fabric-internal API; if it ever changes, fall back to vanilla
        // recipes only rather than breaking the whole local-recipes feature.
        try {
            new ModResourcePackCreator(PackType.SERVER_DATA).loadPacks(pack -> packs.add(pack.open()));
        } catch (Throwable throwable) {
            InternalLogger.getInstance().error("[Local Recipes] Failed to gather mod data packs; modded recipes will be missing from the local fallback", throwable);
        }
        return packs;
    }

    @Override
    public EntryIngredient fromIngredient(Ingredient ingredient) {
        if (ingredient.isEmpty()) return EntryIngredient.empty();
        return EntryIngredients.ofSlotDisplay(ingredient.display());
    }
}
