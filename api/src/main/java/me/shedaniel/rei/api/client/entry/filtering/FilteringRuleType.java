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

package me.shedaniel.rei.api.client.entry.filtering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A type of filtering rule. A filtering rule will filter the entries on the entry panel,
 * dictate what shows up in slots, or hide the entire display if all ingredients are filtered.
 *
 * @param <T> the type of the filtering rule
 */
@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FilteringRuleType<T extends FilteringRule<?>> {
    /**
     * Serializes the specified filtering rule to a compound tag.
     *
     * @param rule the filtering rule
     * @param tag  the compound tag
     * @return the serialized compound tag
     */
    static CompoundTag save(FilteringRule<?> rule, CompoundTag tag) {
        tag.putString("id", rule.getType().getId().toString());
        tag.put("rule", ((FilteringRuleType<FilteringRule<?>>) rule.getType()).saveTo(rule, new CompoundTag()));
        return tag;
    }
    
    /**
     * Serializes the specified filtering rule to a compound tag.
     *
     * @param rule the filtering rule
     * @param tag  the compound tag
     * @return the serialized compound tag
     */
    CompoundTag saveTo(T rule, CompoundTag tag);
    
    /**
     * Deserializes the specified compound tag to a filtering rule.
     *
     * @param tag the compound tag
     * @return the deserialized filtering rule
     */
    @Nullable
    static FilteringRule<?> read(CompoundTag tag) {
        FilteringRuleType<?> type = FilteringRuleTypeRegistry.getInstance().get(ResourceLocation.tryParse(tag.getString("id")));
        if (type == null) return null;
        return type.readFrom(tag.getCompound("rule"));
    }
    
    /**
     * Deserializes the specified compound tag to a filtering rule.
     *
     * @param tag the compound tag
     * @return the deserialized filtering rule
     */
    T readFrom(CompoundTag tag);
    
    /**
     * Returns a function to create the configuration screen for this filtering rule type.
     * The parent of the newly created screen is passed as the argument of the function.
     * <p>
     * If the function returns {@code null}, the filtering rule will not be configurable
     * graphically.
     *
     * @param rule the filtering rule
     * @return the screen function, or {@code null} if the filtering rule is not configurable
     */
    @Nullable
    default Function<Screen, Screen> createEntryScreen(T rule) {
        return null;
    }
    
    /**
     * Returns the name of the filtering rule.
     *
     * @param rule the filtering rule
     * @return the name of the filtering rule
     */
    default Component getTitle(T rule) {
        return Component.nullToEmpty(getId().toString());
    }
    
    /**
     * Returns the description of the filtering rule.
     *
     * @param rule the filtering rule
     * @return the description of the filtering rule
     */
    default Component getSubtitle(T rule) {
        return Component.nullToEmpty(null);
    }
    
    /**
     * Returns the id of the filtering rule type.
     *
     * @return the id of the filtering rule type
     */
    default ResourceLocation getId() {
        return Objects.requireNonNull(FilteringRuleTypeRegistry.getInstance().getId(this), "Id of " + this);
    }
    
    /**
     * Constructs a new filtering rule of this type.
     *
     * @return the new filtering rule
     */
    T createNew();
    
    /**
     * Returns whether this filtering rule type is always enforced,
     * and only one filtering rule of this type can be present in a filtering rule list.
     *
     * @return whether this filtering rule type is always enforced
     */
    boolean isSingular();
}