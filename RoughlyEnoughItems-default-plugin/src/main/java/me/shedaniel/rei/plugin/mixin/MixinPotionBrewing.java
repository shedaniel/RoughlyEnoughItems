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

package me.shedaniel.rei.plugin.mixin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.plugin.brewing.BrewingRecipe;
import me.shedaniel.rei.plugin.brewing.RegisteredBrewingRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PotionBrewing.class)
@Environment(EnvType.CLIENT)
public class MixinPotionBrewing {
    
    @Unique private static final List<BrewingRecipe> SELF_ITEM_RECIPES = Lists.newArrayList();
    @Unique private static final List<Potion> REGISTERED_POTION_TYPES = Lists.newArrayList();
    @Unique private static final List<Ingredient> SELF_POTION_TYPES = Lists.newArrayList();
    
    @Inject(method = "addContainer", at = @At("RETURN"))
    private static void addContainer(Item item_1, CallbackInfo ci) {
        if (item_1 instanceof PotionItem)
            SELF_POTION_TYPES.add(Ingredient.of(item_1));
    }
    
    @Inject(method = "addContainerRecipe", at = @At("RETURN"))
    private static void addContainerRecipe(Item item_1, Item item_2, Item item_3, CallbackInfo ci) {
        if (item_1 instanceof PotionItem && item_3 instanceof PotionItem)
            SELF_ITEM_RECIPES.add(new BrewingRecipe(item_1, Ingredient.of(item_2), item_3));
    }
    
    @Inject(method = "addMix", at = @At("RETURN"))
    private static void addMix(Potion potion_1, Item item_1, Potion potion_2, CallbackInfo ci) {
        if (!REGISTERED_POTION_TYPES.contains(potion_1))
            rei_registerPotionType(potion_1);
        if (!REGISTERED_POTION_TYPES.contains(potion_2))
            rei_registerPotionType(potion_2);
        for (Ingredient type : SELF_POTION_TYPES) {
            for (ItemStack stack : type.getItems()) {
                DefaultPlugin.registerBrewingRecipe(new RegisteredBrewingRecipe(PotionUtils.setPotion(stack.copy(), potion_1), Ingredient.of(item_1), PotionUtils.setPotion(stack.copy(), potion_2)));
            }
        }
    }
    
    @Unique
    private static void rei_registerPotionType(Potion potion) {
        REGISTERED_POTION_TYPES.add(potion);
        for (BrewingRecipe recipe : SELF_ITEM_RECIPES) {
            DefaultPlugin.registerBrewingRecipe(new RegisteredBrewingRecipe(PotionUtils.setPotion(recipe.input.getDefaultInstance(), potion), recipe.ingredient, PotionUtils.setPotion(recipe.output.getDefaultInstance(), potion)));
        }
    }
    
}
