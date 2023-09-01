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

package me.shedaniel.rei.plugin.common.displays;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class DefaultSmithingDisplay extends BasicDisplay {
    @ApiStatus.Experimental
    public static DefaultSmithingDisplay ofTransforming(RecipeHolder<SmithingTransformRecipe> recipe) {
        return new DefaultSmithingDisplay(
                recipe.value(),
                recipe.id(),
                List.of(
                        EntryIngredients.ofIngredient(recipe.value().template),
                        EntryIngredients.ofIngredient(recipe.value().base),
                        EntryIngredients.ofIngredient(recipe.value().addition)
                )
        );
    }
    
    @ApiStatus.Experimental
    public static DefaultSmithingDisplay ofTrimming(RecipeHolder<SmithingTrimRecipe> recipe) {
        return new DefaultSmithingDisplay(
                recipe.value(),
                recipe.id(),
                List.of(
                        EntryIngredients.ofIngredient(recipe.value().template),
                        EntryIngredients.ofIngredient(recipe.value().base),
                        EntryIngredients.ofIngredient(recipe.value().addition)
                )
        );
    }
    
//    @ApiStatus.Experimental
//    public DefaultSmithingDisplay(SmithingTrimRecipe recipe) {
//        this(
//                List.of(
//                        EntryIngredients.ofIngredient(recipe.template),
//                        EntryIngredients.ofIngredient(recipe.base),
//                        EntryIngredients.ofIngredient(recipe.addition)
//                ),
//                List.of(EntryIngredients.ofItemStacks(((Supplier<List<ItemStack>>) () -> {
//                    RegistryAccess registryAccess = BasicDisplay.registryAccess();
//                    ItemStack[] templateItems = recipe.template.getItems();
//                    ItemStack[] baseItems = recipe.base.getItems();
//                    if (templateItems.length != 0) {
//                        Holder.Reference<TrimPattern> trimPattern = TrimPatterns.getFromTemplate(registryAccess, templateItems[0])
//                                .orElse(null);
//                        if (trimPattern != null) {
//                            for (ItemStack additionItem : recipe.addition.getItems()) {
//                                Holder.Reference<TrimMaterial> trimMaterial = TrimMaterials.getFromIngredient(registryAccess, additionItem)
//                                        .orElse(null);
//                                if (trimMaterial != null) {
//                                    Optional<ArmorTrim> trim = ArmorTrim.getTrim(registryAccess, itemStack);
//                                    if (trim.isEmpty() || !trim.get().hasPatternAndMaterial(trimPattern, trimMaterial)) {
//                                        ItemStack itemStack2 = itemStack.copy();
//                                        itemStack2.setCount(1);
//                                        ArmorTrim armorTrim = new ArmorTrim((Holder) optional.get(), (Holder) optional2.get());
//                                        if (ArmorTrim.setTrim(registryAccess, itemStack2, armorTrim)) {
//                                            return itemStack2;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    return List.of(recipe.getResultItem(registryAccess));
//                }).get())),
//                Optional.ofNullable(recipe.getId())
//        );
//    }
    
    public DefaultSmithingDisplay(SmithingRecipe recipe, @Nullable ResourceLocation id, List<EntryIngredient> inputs) {
        this(
                inputs,
                List.of(EntryIngredients.of(recipe.getResultItem(BasicDisplay.registryAccess()))),
                Optional.ofNullable(id)
        );
    }
    
    public DefaultSmithingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
        super(inputs, outputs, location);
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.SMITHING;
    }
    
    public static BasicDisplay.Serializer<DefaultSmithingDisplay> serializer() {
        return BasicDisplay.Serializer.ofSimple(DefaultSmithingDisplay::new);
    }
}
