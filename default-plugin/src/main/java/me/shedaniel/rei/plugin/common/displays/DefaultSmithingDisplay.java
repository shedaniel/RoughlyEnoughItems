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

import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.SmithingDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class DefaultSmithingDisplay extends BasicDisplay implements SmithingDisplay {
    public static final DisplaySerializer<DefaultSmithingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultSmithingDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultSmithingDisplay::getOutputEntries),
                    SmithingRecipeType.CODEC.optionalFieldOf("type").forGetter(d -> d.type),
                    ResourceLocation.CODEC.optionalFieldOf("location").forGetter(DefaultSmithingDisplay::getDisplayLocation)
            ).apply(instance, DefaultSmithingDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultSmithingDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultSmithingDisplay::getOutputEntries,
                    ByteBufCodecs.optional(SmithingRecipeType.STREAM_CODEC),
                    d -> d.type,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
                    DefaultSmithingDisplay::getDisplayLocation,
                    DefaultSmithingDisplay::new
            ));
    
    private final Optional<SmithingRecipeType> type;
    
    @ApiStatus.Experimental
    public static DefaultSmithingDisplay ofTransforming(RecipeHolder<SmithingTransformRecipe> recipe) {
        return new DefaultSmithingDisplay(
                List.of(
                        recipe.value().templateIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty()),
                        recipe.value().baseIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty()),
                        recipe.value().additionIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())
                ),
                List.of(EntryIngredients.of(recipe.value().result)),
                Optional.of(SmithingRecipeType.TRANSFORM),
                Optional.of(recipe.id().location())
        );
    }
    
    public static List<DefaultSmithingDisplay> fromTrimming(RecipeHolder<SmithingTrimRecipe> recipe) {
        RegistryAccess registryAccess = BasicDisplay.registryAccess();
        List<DefaultSmithingDisplay> displays = new ArrayList<>();
        for (Holder<Item> templateItem : (Iterable<Holder<Item>>) recipe.value().templateIngredient().map(Ingredient::items).orElse(Stream.of())::iterator) {
            Holder.Reference<TrimPattern> trimPattern = getPatternFromTemplate(registryAccess, templateItem)
                    .orElse(null);
            if (trimPattern == null) continue;
            
            for (Holder<Item> additionStack : (Iterable<Holder<Item>>) recipe.value().additionIngredient().map(Ingredient::items).orElse(Stream.of())::iterator) {
                Holder.Reference<TrimMaterial> trimMaterial = getMaterialFromIngredient(registryAccess, additionStack)
                        .orElse(null);
                if (trimMaterial == null) continue;
                
                EntryIngredient baseIngredient = recipe.value().baseIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty());
                EntryIngredient templateOutput = baseIngredient.isEmpty() ? EntryIngredient.empty()
                        : getTrimmingOutput(registryAccess, EntryStacks.ofItemHolder(templateItem), baseIngredient.get(0), EntryStacks.ofItemHolder(additionStack));
                
                displays.add(new DefaultSmithingDisplay(List.of(
                        EntryIngredients.ofItemHolder(templateItem),
                        baseIngredient,
                        EntryIngredients.ofItemHolder(additionStack)
                ), List.of(templateOutput), Optional.of(SmithingRecipeType.TRIM), Optional.of(recipe.id().location())));
            }
        }
        return displays;
    }
    
    public DefaultSmithingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<ResourceLocation> location) {
        this(inputs, outputs, Optional.empty(), location);
    }
    
    @ApiStatus.Experimental
    public DefaultSmithingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<SmithingRecipeType> type, Optional<ResourceLocation> location) {
        super(inputs, outputs, location);
        this.type = type;
    }
    
    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BuiltinPlugin.SMITHING;
    }
    
    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
    
    @Nullable
    @Override
    public SmithingRecipeType type() {
        return type.orElse(null);
    }
    
    @ApiStatus.Experimental
    @ApiStatus.Internal
    public static EntryIngredient getTrimmingOutput(RegistryAccess registryAccess, EntryStack<?> template, EntryStack<?> base, EntryStack<?> addition) {
        if (template.getType() != VanillaEntryTypes.ITEM || base.getType() != VanillaEntryTypes.ITEM || addition.getType() != VanillaEntryTypes.ITEM) return EntryIngredient.empty();
        ItemStack templateItem = template.castValue();
        ItemStack baseItem = base.castValue();
        ItemStack additionItem = addition.castValue();
        Holder.Reference<TrimPattern> trimPattern = TrimPatterns.getFromTemplate(registryAccess, templateItem)
                .orElse(null);
        if (trimPattern == null) return EntryIngredient.empty();
        Holder.Reference<TrimMaterial> trimMaterial = TrimMaterials.getFromIngredient(registryAccess, additionItem)
                .orElse(null);
        if (trimMaterial == null) return EntryIngredient.empty();
        ArmorTrim armorTrim = new ArmorTrim(trimMaterial, trimPattern);
        ArmorTrim trim = baseItem.get(DataComponents.TRIM);
        if (trim != null && trim.hasPatternAndMaterial(trimPattern, trimMaterial)) return EntryIngredient.empty();
        ItemStack newItem = baseItem.copyWithCount(1);
        newItem.set(DataComponents.TRIM, armorTrim);
        return EntryIngredients.of(newItem);
    }
    
    private static Optional<Holder.Reference<TrimPattern>> getPatternFromTemplate(HolderLookup.Provider provider, Holder<Item> item) {
        return provider.lookupOrThrow(Registries.TRIM_PATTERN)
                .listElements()
                .filter(reference -> item == reference.value().templateItem())
                .findFirst();
    }
    
    private static Optional<Holder.Reference<TrimMaterial>> getMaterialFromIngredient(HolderLookup.Provider provider, Holder<Item> item) {
        return provider.lookupOrThrow(Registries.TRIM_MATERIAL)
                .listElements()
                .filter(reference -> item == reference.value().ingredient())
                .findFirst();
    }
}
