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

package me.shedaniel.rei.plugin.client.categories.crafting.filler;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.TippedArrowRecipe;

import java.util.*;

public class TippedArrowRecipeFiller implements CraftingRecipeFiller<TippedArrowRecipe> {
    @Override
    public Collection<Display> apply(RecipeHolder<TippedArrowRecipe> recipe) {
        EntryIngredient arrowStack = EntryIngredient.of(EntryStacks.of(Items.ARROW));
        Set<Identifier> registeredPotions = new HashSet<>();
        List<Display> displays = new ArrayList<>();
        
        RegistryAccess registryAccess = BasicDisplay.registryAccess();
        BasicDisplay.registryAccess().lookup(Registries.POTION).stream()
                .flatMap(Registry::listElements)
                .map(reference -> PotionContents.createItemStack(Items.LINGERING_POTION, reference))
                .forEach(itemStack -> {
                    PotionContents potion = itemStack.get(DataComponents.POTION_CONTENTS);
                    if (potion.potion().isPresent() && potion.potion().get().unwrapKey().isPresent() && registeredPotions.add(potion.potion().get().unwrapKey().get().location())) {
                        List<EntryIngredient> input = new ArrayList<>();
                        for (int i = 0; i < 4; i++)
                            input.add(arrowStack);
                        input.add(EntryIngredients.of(itemStack));
                        for (int i = 0; i < 4; i++)
                            input.add(arrowStack);
                        ItemStack outputStack = new ItemStack(Items.TIPPED_ARROW, 8);
                        outputStack.set(DataComponents.POTION_CONTENTS, potion);
                        displays.add(new DefaultCustomDisplay(input, List.of(EntryIngredients.of(outputStack)), Optional.of(recipe.id().location())));
                    }
                });
        
        return displays;
    }
    
    @Override
    public Class<TippedArrowRecipe> getRecipeClass() {
        return TippedArrowRecipe.class;
    }
}
