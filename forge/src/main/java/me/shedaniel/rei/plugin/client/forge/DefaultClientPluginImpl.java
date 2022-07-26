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

package me.shedaniel.rei.plugin.client.forge;

import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.plugin.client.BuiltinClientPlugin;
import me.shedaniel.rei.plugin.client.DefaultClientPlugin;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

import java.util.Arrays;
import java.util.Set;

public class DefaultClientPluginImpl extends DefaultClientPlugin {
    @Override
    public void registerForgePotions(DisplayRegistry registry, BuiltinClientPlugin clientPlugin) {
        for (IBrewingRecipe recipe : BrewingRecipeRegistry.getRecipes()) {
            if (recipe instanceof VanillaBrewingRecipe) {
                registerVanillaPotions(registry, clientPlugin);
            } else if (recipe instanceof BrewingRecipe) {
                BrewingRecipe brewingRecipe = (BrewingRecipe) recipe;
                clientPlugin.registerBrewingRecipe(brewingRecipe.getInput(), brewingRecipe.getIngredient(), brewingRecipe.getOutput().copy());
            }
        }
    }
    
    private static void registerVanillaPotions(DisplayRegistry registry, BuiltinClientPlugin clientPlugin) {
        Set<Potion> potions = Sets.newLinkedHashSet();
        for (Ingredient container : PotionBrewing.ALLOWED_CONTAINERS) {
            for (PotionBrewing.Mix<Potion> mix : PotionBrewing.POTION_MIXES) {
                Holder.Reference<Potion> from = mix.f_43532_;
                Ingredient ingredient = mix.ingredient;
                Holder.Reference<Potion> to = mix.f_43534_;
                Ingredient base = Ingredient.of(Arrays.stream(container.getItems())
                        .map(ItemStack::copy)
                        .map(stack -> PotionUtils.setPotion(stack, from.get())));
                ItemStack output = Arrays.stream(container.getItems())
                        .map(ItemStack::copy)
                        .map(stack -> PotionUtils.setPotion(stack, to.get()))
                        .findFirst().orElse(ItemStack.EMPTY);
                clientPlugin.registerBrewingRecipe(base, ingredient, output);
                potions.add(from.get());
                potions.add(to.get());
            }
        }
        for (Potion potion : potions) {
            for (PotionBrewing.Mix<Item> mix : PotionBrewing.CONTAINER_MIXES) {
                Holder.Reference<Item> from = mix.f_43532_;
                Ingredient ingredient = mix.ingredient;
                Holder.Reference<Item> to = mix.f_43534_;
                Ingredient base = Ingredient.of(PotionUtils.setPotion(new ItemStack(from.get()), potion));
                ItemStack output = PotionUtils.setPotion(new ItemStack(to.get()), potion);
                clientPlugin.registerBrewingRecipe(base, ingredient, output);
            }
        }
    }
}
