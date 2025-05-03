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

package me.shedaniel.rei.plugin.common.displays.grindstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.BiFunction;

public class DefaultGrindstoneDisplay extends BasicDisplay {
    public static final DisplaySerializer<DefaultGrindstoneDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultGrindstoneDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultGrindstoneDisplay::getOutputEntries),
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(DefaultGrindstoneDisplay::getDisplayLocation),
                    Codec.DOUBLE.optionalFieldOf("average_xp_reward").forGetter(d -> d.averageXpReward.stream().boxed().findFirst())
            ).apply(instance, (inputs, outputs, location, averageXpReward) -> new DefaultGrindstoneDisplay(inputs, outputs, location, averageXpReward.stream().mapToDouble(d -> d).findFirst()))),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultGrindstoneDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultGrindstoneDisplay::getOutputEntries,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    DefaultGrindstoneDisplay::getDisplayLocation,
                    ByteBufCodecs.optional(ByteBufCodecs.DOUBLE),
                    d -> d.averageXpReward.stream().boxed().findFirst(),
                    (inputs, outputs, location, averageXpReward) -> new DefaultGrindstoneDisplay(inputs, outputs, location, averageXpReward.stream().mapToDouble(d -> d).findFirst())
            ));
    // cannot get the function immediately, since it is located in an anonymous class, so it is lazily initialized with mixins
    public static BiFunction<Slot, ItemStack, Integer> getExperienceFromItem;
    private final OptionalDouble averageXpReward;

    public DefaultGrindstoneDisplay(GrindstoneRecipe recipe) {
        this(
                Arrays.asList(
                        EntryIngredients.ofItemStacks(recipe.getTopInput()),
                        EntryIngredients.ofItemStacks(recipe.getBottomInput())
                ),
                Collections.singletonList(EntryIngredients.ofItemStacks(recipe.getOutputs())),
                Optional.ofNullable(recipe.getId()),
                recipe.getAverageXpReward()
        );
    }
    
    public DefaultGrindstoneDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
        this(inputs, outputs, location, OptionalDouble.empty());
    }

    public DefaultGrindstoneDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, CompoundTag tag) {
        this(inputs, outputs, location, extractXpReward(tag));
    }

    private static OptionalDouble extractXpReward(CompoundTag tag) {
        if (tag.contains("average_xp_reward")) {
            Optional<Double> value = tag.getDouble("average_xp_reward");
            return value.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
        }
        return OptionalDouble.empty();
    }


    public DefaultGrindstoneDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location, OptionalDouble averageXpReward) {
        super(inputs, outputs, location);
        this.averageXpReward = averageXpReward;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.GRINDSTONE;
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    
    public OptionalDouble getAverageXpReward() {
        return averageXpReward;
    }

    @ApiStatus.Experimental
    @ApiStatus.Internal
    @Environment(EnvType.CLIENT)
    public static Optional<GrindStoneRecipeResult> calculateOutput(ItemStack top, ItemStack bottom) {
        try {
            if (Minecraft.getInstance().player == null) return Optional.empty();
            GrindstoneMenu menu = new GrindstoneMenu(0, new Inventory(Minecraft.getInstance().player, new EntityEquipment()));
            menu.setItem(0, menu.incrementStateId(), top);
            menu.setItem(1, menu.incrementStateId(), bottom);
            Slot outputSlot = menu.getSlot(2);
            int expTop = getExperienceFromItem.apply(outputSlot, top);
            int expBottom = getExperienceFromItem.apply(outputSlot, bottom);
            int maxExp = expTop + expBottom;
            ItemStack outputStack = outputSlot.getItem().copy();
            if (!outputStack.isEmpty()) {
                return Optional.of(new GrindStoneRecipeResult(outputStack, maxExp * 0.75));  // vanilla randomizes between 0.5 and 1.0 of max exp
            } else {
                return Optional.empty();
            }
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public record GrindStoneRecipeResult(ItemStack itemStack, double averageExp) {}
}
