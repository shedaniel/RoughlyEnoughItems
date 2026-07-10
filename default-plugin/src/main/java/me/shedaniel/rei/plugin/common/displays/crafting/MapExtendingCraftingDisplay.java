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
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MapExtendingCraftingDisplay extends DefaultCraftingDisplay {
    public static final DisplaySerializer<MapExtendingCraftingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("i").forGetter(d -> d.i),
                    Identifier.CODEC.optionalFieldOf("location").forGetter(MapExtendingCraftingDisplay::getDisplayLocation)
            ).apply(instance, MapExtendingCraftingDisplay::new)),
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    d -> d.i,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                    MapExtendingCraftingDisplay::getDisplayLocation,
                    MapExtendingCraftingDisplay::new
            ));
    
    private final int i;
    
    public MapExtendingCraftingDisplay(int i, Optional<Identifier> id) {
        super(getInputs(i), List.of(mapWith("X", i + 1, 1)), id);
        this.i = i;
    }
    
    private static List<EntryIngredient> getInputs(int i) {
        EntryIngredient[] inputs = new EntryIngredient[9];
        for (int j = 0; j < 9; j++) {
            if (j == 4) {
                inputs[j] = mapWith("X", i, 1);
            } else {
                inputs[j] = EntryIngredients.of(Items.PAPER);
            }
        }
        return List.of(inputs);
    }
    
    @Override
    @Nullable
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    
    public static EntryIngredient mapWith(String mapId, int scale, int count) {
        if (Platform.getEnvironment() == Env.CLIENT) {
            return mapWithClient(mapId, scale, count);
        } else {
            return EntryIngredients.of(Items.FILLED_MAP, count);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static EntryIngredient mapWithClient(String mapId, int scale, int count) {
        EntryIngredient stacks = EntryIngredients.of(Items.FILLED_MAP, count);
        String unknown = I18n.get("filled_map.unknown");
        for (EntryStack<?> stack : stacks) {
            stack.tooltipProcessor(($, tooltip) -> {
                tooltip.entries().removeIf(entry -> entry.isText() && entry.getAsText().getString().equals(unknown));
                return tooltip;
            });
            stack.tooltip(
                    Component.translatable("filled_map.id", mapId).withStyle(ChatFormatting.GRAY),
                    Component.translatable("filled_map.scale", (1 << scale)).withStyle(ChatFormatting.GRAY),
                    Component.translatable("filled_map.level", scale, 4).withStyle(ChatFormatting.GRAY)
            );
        }
        return stacks;
    }
    
    @Override
    public boolean isShapeless() {
        return false;
    }
    
    @Override
    public int getWidth() {
        return 3;
    }
    
    @Override
    public int getHeight() {
        return 3;
    }
}
