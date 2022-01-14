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

package me.shedaniel.rei.plugin.common.displays;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultCampfireDisplay extends BasicDisplay {
    private double cookTime;
    
    public DefaultCampfireDisplay(CampfireCookingRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem())),
                Optional.ofNullable(recipe).map(CampfireCookingRecipe::getId), recipe.getCookingTime());
    }
    
    public DefaultCampfireDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, CompoundTag tag) {
        this(inputs, outputs, location, tag.getDouble("cookTime"));
    }
    
    public DefaultCampfireDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, double cookTime) {
        super(inputs, outputs, location);
        this.cookTime = cookTime;
    }
    
    public double getCookTime() {
        return cookTime;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.CAMPFIRE;
    }
    
    public static BasicDisplay.Serializer<DefaultCampfireDisplay> serializer() {
        return BasicDisplay.Serializer.of(DefaultCampfireDisplay::new, (display, tag) -> {
            tag.putDouble("cookTime", display.cookTime);
        });
    }
}
