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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class DefaultCampfireDisplay extends BasicDisplay implements CampfireDisplay {
    public static final DisplaySerializer<DefaultCampfireDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultCampfireDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultCampfireDisplay::getOutputEntries),
                    Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultCampfireDisplay::getDisplayLocation),
                    Codec.DOUBLE.fieldOf("cookTime").forGetter(d -> d.cookTime)
            ).apply(instance, DefaultCampfireDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultCampfireDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultCampfireDisplay::getOutputEntries,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                    DefaultCampfireDisplay::getDisplayLocation,
                    ByteBufCodecs.DOUBLE,
                    d -> d.cookTime,
                    DefaultCampfireDisplay::new
            ));
    
    private final double cookTime;
    
    public DefaultCampfireDisplay(RecipeHolder<CampfireCookingRecipe> recipe) {
        this(List.of(EntryIngredients.ofIngredient(recipe.value().input())),
                List.of(EntryIngredients.ofSlotDisplay(recipe.value().display().getFirst().result())),
                Optional.of(recipe.id().identifier()), recipe.value().cookingTime());
    }
    
    public DefaultCampfireDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location, CompoundTag tag) {
        this(inputs, outputs, location, tag.getDouble("cookTime").orElseThrow());
    }
    
    public DefaultCampfireDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location, double cookTime) {
        super(inputs, outputs, location);
        this.cookTime = cookTime;
    }
    
    @Override
    public OptionalDouble cookTime() {
        return OptionalDouble.of(cookTime);
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.CAMPFIRE;
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
