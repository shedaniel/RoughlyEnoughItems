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

package me.shedaniel.rei.plugin.common.displays.cooking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public abstract class DefaultCookingDisplay extends BasicDisplay implements CookingDisplay {
    protected float xp;
    protected double cookTime;
    
    public DefaultCookingDisplay(RecipeHolder<? extends AbstractCookingRecipe> recipe) {
        this(List.of(EntryIngredients.ofIngredient(recipe.value().input())),
                List.of(EntryIngredients.ofSlotDisplay(recipe.value().display().getFirst().result())),
                Optional.of(recipe.id().identifier()), recipe.value().experience(), recipe.value().cookingTime());
    }
    
    public DefaultCookingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> id, CompoundTag tag) {
        this(input, output, id, tag.getFloat("xp").orElseThrow(), tag.getDouble("cookTime").orElseThrow());
    }
    
    public DefaultCookingDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Optional<Identifier> id, float xp, double cookTime) {
        super(input, output, id);
        this.xp = xp;
        this.cookTime = cookTime;
    }
    
    @Override
    public OptionalDouble xp() {
        return OptionalDouble.of(xp);
    }
    
    @Override
    public OptionalDouble cookTime() {
        return OptionalDouble.of(cookTime);
    }
    
    protected static <D extends DefaultCookingDisplay> DisplaySerializer<D> serializer(Constructor<D> constructor) {
        return DisplaySerializer.of(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(D::getInputEntries),
                        EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(D::getOutputEntries),
                        Identifier.CODEC.optionalFieldOf("location").forGetter(D::getDisplayLocation),
                        Codec.FLOAT.fieldOf("xp").forGetter(display -> display.xp),
                        Codec.DOUBLE.fieldOf("cookTime").forGetter(display -> display.cookTime)
                ).apply(instance, constructor::create)),
                StreamCodec.composite(
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        D::getInputEntries,
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        D::getOutputEntries,
                        ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                        D::getDisplayLocation,
                        ByteBufCodecs.FLOAT,
                        display -> display.xp,
                        ByteBufCodecs.DOUBLE,
                        display -> display.cookTime,
                        constructor::create
                ));
    }
    
    protected interface Constructor<T extends DefaultCookingDisplay> {
        T create(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location, float xp, double cookTime);
    }
}
