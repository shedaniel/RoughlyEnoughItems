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

package me.shedaniel.rei.jeicompat.wrap;

import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public enum JEIVanillaRecipeFactory implements IVanillaRecipeFactory {
    INSTANCE;
    
    @Override
    public IJeiAnvilRecipe createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        return new JEIAnvilRecipe(null, Collections.singletonList(leftInput), rightInputs, outputs);
    }
    
    @Override
    public IJeiAnvilRecipe createAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        return new JEIAnvilRecipe(null, leftInputs, rightInputs, outputs);
    }
    
    @Override
    public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput) {
        return createBrewingRecipe(ingredients, Collections.singletonList(potionInput), potionOutput);
    }
    
    @Override
    public IJeiBrewingRecipe createBrewingRecipe(List<ItemStack> ingredients, List<ItemStack> potionInputs, ItemStack potionOutput) {
        return new JEIBrewingRecipe(Ingredient.of(potionInputs.toArray(new ItemStack[0])), Ingredient.of(ingredients.toArray(new ItemStack[0])), potionOutput);
    }
    
    private static class JEIAnvilRecipe extends AnvilRecipe implements IJeiAnvilRecipe {
        public JEIAnvilRecipe(@Nullable ResourceLocation id, List<ItemStack> leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
            super(id, leftInput, rightInputs, outputs);
        }
        
        @Override
        public @Unmodifiable List<ItemStack> getLeftInputs() {
            return getLeftInput();
        }
    }
}
