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

package me.shedaniel.rei.plugin.common.displays.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;
import java.util.Optional;

public class DefaultShapedDisplay extends DefaultCraftingDisplay {
    public static final DisplaySerializer<DefaultCraftingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultCraftingDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultCraftingDisplay::getOutputEntries),
                    Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultCraftingDisplay::getDisplayLocation),
                    Codec.INT.fieldOf("width").forGetter(DefaultCraftingDisplay::getWidth),
                    Codec.INT.fieldOf("height").forGetter(DefaultCraftingDisplay::getHeight)
            ).apply(instance, DefaultCustomShapedDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultCraftingDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultCraftingDisplay::getOutputEntries,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                    DefaultCraftingDisplay::getDisplayLocation,
                    ByteBufCodecs.INT,
                    DefaultCraftingDisplay::getWidth,
                    ByteBufCodecs.INT,
                    DefaultCraftingDisplay::getHeight,
                    DefaultCustomShapedDisplay::new
            ));
    
    private final int width;
    private final int height;
    
    public DefaultShapedDisplay(RecipeHolder<ShapedRecipe> recipe) {
        super(
                CollectionUtils.map(recipe.value().getIngredients(), opt -> opt.map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())),
                List.of(EntryIngredients.ofSlotDisplay(recipe.value().display().getFirst().result())),
                Optional.of(recipe.id().identifier())
        );
        this.width = recipe.value().getWidth();
        this.height = recipe.value().getHeight();
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public boolean isShapeless() {
        return false;
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
