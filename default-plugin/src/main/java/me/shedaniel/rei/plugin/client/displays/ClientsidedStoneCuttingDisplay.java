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

package me.shedaniel.rei.plugin.client.displays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.DefaultStoneCuttingDisplay;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;

import java.util.List;
import java.util.Optional;

public class ClientsidedStoneCuttingDisplay extends DefaultStoneCuttingDisplay implements ClientsidedRecipeBookDisplay {
    public static final DisplaySerializer<ClientsidedStoneCuttingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(ClientsidedStoneCuttingDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(ClientsidedStoneCuttingDisplay::getOutputEntries),
                    Codec.INT.xmap(RecipeDisplayId::new, RecipeDisplayId::index).optionalFieldOf("id").forGetter(ClientsidedStoneCuttingDisplay::recipeDisplayId)
            ).apply(instance, ClientsidedStoneCuttingDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    ClientsidedStoneCuttingDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    ClientsidedStoneCuttingDisplay::getOutputEntries,
                    ByteBufCodecs.optional(ByteBufCodecs.INT.map(RecipeDisplayId::new, RecipeDisplayId::index)),
                    ClientsidedStoneCuttingDisplay::recipeDisplayId,
                    ClientsidedStoneCuttingDisplay::new
            ), false);

    private final Optional<RecipeDisplayId> id;

    public ClientsidedStoneCuttingDisplay(StonecutterRecipeDisplay recipe, Optional<RecipeDisplayId> id) {
        this(List.of(EntryIngredients.ofSlotDisplay(recipe.input())), List.of(EntryIngredients.ofSlotDisplay(recipe.result())), id);
    }

    public ClientsidedStoneCuttingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<RecipeDisplayId> id) {
        super(inputs, outputs, Optional.empty());
        this.id = id;
    }

    @Override
    public Optional<RecipeDisplayId> recipeDisplayId() {
        return id;
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
