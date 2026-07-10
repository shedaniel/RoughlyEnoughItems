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
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.SmithingDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class DefaultSmithingDisplay extends BasicDisplay implements SmithingDisplay {
    public static final DisplaySerializer<DefaultSmithingDisplay> SERIALIZER = DisplaySerializer.of(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultSmithingDisplay::getInputEntries),
                    EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultSmithingDisplay::getOutputEntries),
                    SmithingRecipeType.CODEC.optionalFieldOf("type").forGetter(d -> d.type),
                    Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultSmithingDisplay::getDisplayLocation)
            ).apply(instance, DefaultSmithingDisplay::new)),
            StreamCodec.composite(
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultSmithingDisplay::getInputEntries,
                    EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                    DefaultSmithingDisplay::getOutputEntries,
                    ByteBufCodecs.optional(SmithingRecipeType.STREAM_CODEC),
                    d -> d.type,
                    ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                    DefaultSmithingDisplay::getDisplayLocation,
                    DefaultSmithingDisplay::new
            ));
    
    protected final Optional<SmithingRecipeType> type;
    
    @ApiStatus.Experimental
    public static DefaultSmithingDisplay ofTransforming(RecipeHolder<SmithingTransformRecipe> recipe) {
        return new DefaultSmithingDisplay(
                List.of(
                        recipe.value().templateIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty()),
                        EntryIngredients.ofIngredient(recipe.value().baseIngredient()),
                        recipe.value().additionIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty())
                ),
                List.of(EntryIngredients.ofSlotDisplay(recipe.value().display().getFirst().result())),
                Optional.of(SmithingRecipeType.TRANSFORM),
                Optional.of(recipe.id().identifier())
        );
    }
    
    public static List<DefaultSmithingDisplay> fromTrimming(RecipeHolder<SmithingTrimRecipe> recipe) {
        RegistryAccess registryAccess = BasicDisplay.registryAccess();
        List<DefaultSmithingDisplay> displays = new ArrayList<>();
        Holder<TrimPattern> trimPattern = recipe.value().pattern;
        for (Holder<Item> additionStack : (Iterable<Holder<Item>>) recipe.value().additionIngredient().map(Ingredient::items).orElse(Stream.of())::iterator) {
            Holder<TrimMaterial> trimMaterial = getMaterialFromIngredient(registryAccess, additionStack)
                    .orElse(null);
            if (trimMaterial == null) continue;
            
            EntryIngredient baseIngredient = EntryIngredients.ofIngredient(recipe.value().baseIngredient());
            
            displays.add(new DefaultSmithingDisplay.Trimming(List.of(
                    recipe.value().templateIngredient().map(EntryIngredients::ofIngredient).orElse(EntryIngredient.empty()),
                    baseIngredient,
                    EntryIngredients.ofItemHolder(additionStack)
            ), List.of(baseIngredient), Optional.of(SmithingRecipeType.TRIM), Optional.of(recipe.id().identifier()), recipe.value().pattern));
        }
        return displays;
    }
    
    public DefaultSmithingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<Identifier> location) {
        this(inputs, outputs, Optional.empty(), location);
    }
    
    @ApiStatus.Experimental
    public DefaultSmithingDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<SmithingRecipeType> type, Optional<Identifier> location) {
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
    public static EntryIngredient getTrimmingOutput(RegistryAccess registryAccess, Holder<TrimPattern> trimPattern, EntryStack<?> base, EntryStack<?> addition) {
        if (base.getType() != VanillaEntryTypes.ITEM || addition.getType() != VanillaEntryTypes.ITEM) return EntryIngredient.empty();
        ItemStack baseItem = base.castValue();
        ItemStack additionItem = addition.castValue();
        if (trimPattern == null) return EntryIngredient.empty();
        Holder<TrimMaterial> trimMaterial = getMaterialFromIngredient(registryAccess, additionItem.typeHolder())
                .orElse(null);
        if (trimMaterial == null) return EntryIngredient.empty();
        ArmorTrim armorTrim = new ArmorTrim(trimMaterial, trimPattern);
        ArmorTrim trim = baseItem.get(DataComponents.TRIM);
        if (Objects.equals(trim, armorTrim)) return EntryIngredient.empty();
        ItemStack newItem = baseItem.copyWithCount(1);
        newItem.set(DataComponents.TRIM, armorTrim);
        return EntryIngredients.of(newItem);
    }
    
    private static Optional<Holder<TrimMaterial>> getMaterialFromIngredient(HolderLookup.Provider provider, Holder<Item> item) {
        Holder<TrimMaterial> material = new ItemStack(item).get(DataComponents.PROVIDES_TRIM_MATERIAL);
        return Optional.ofNullable(material);
    }
    
    public static class Trimming extends DefaultSmithingDisplay implements SmithingDisplay.Trimming {
        public static final DisplaySerializer<DefaultSmithingDisplay.Trimming> SERIALIZER = DisplaySerializer.of(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(DefaultSmithingDisplay.Trimming::getInputEntries),
                        EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(DefaultSmithingDisplay.Trimming::getOutputEntries),
                        SmithingRecipeType.CODEC.optionalFieldOf("smithing_type").forGetter(d -> d.type),
                        Identifier.CODEC.optionalFieldOf("location").forGetter(DefaultSmithingDisplay.Trimming::getDisplayLocation),
                        TrimPattern.CODEC.fieldOf("pattern").forGetter(DefaultSmithingDisplay.Trimming::pattern)
                ).apply(instance, DefaultSmithingDisplay.Trimming::new)),
                StreamCodec.composite(
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        DefaultSmithingDisplay.Trimming::getInputEntries,
                        EntryIngredient.streamCodec().apply(ByteBufCodecs.list()),
                        DefaultSmithingDisplay.Trimming::getOutputEntries,
                        ByteBufCodecs.optional(SmithingRecipeType.STREAM_CODEC),
                        d -> d.type,
                        ByteBufCodecs.optional(Identifier.STREAM_CODEC),
                        DefaultSmithingDisplay.Trimming::getDisplayLocation,
                        TrimPattern.STREAM_CODEC,
                        DefaultSmithingDisplay.Trimming::pattern,
                        DefaultSmithingDisplay.Trimming::new
                ));
        
        private final Holder<TrimPattern> pattern;
        
        public Trimming(List<EntryIngredient> inputs, List<EntryIngredient> outputs, Optional<SmithingRecipeType> type, Optional<Identifier> location, Holder<TrimPattern> pattern) {
            super(inputs, outputs, type, location);
            this.pattern = pattern;
        }
        
        @Override
        public Holder<TrimPattern> pattern() {
            return pattern;
        }
        
        @Override
        public DisplaySerializer<? extends Display> getSerializer() {
            return DefaultSmithingDisplay.Trimming.SERIALIZER;
        }
    }
}
