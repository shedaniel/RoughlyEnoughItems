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

package me.shedaniel.rei.api.client.entry.filtering;

import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FilteringRuleTypeRegistry extends List<FilteringRuleType<?>> {
    static FilteringRuleTypeRegistry getInstance() {
        return ClientInternals.getFilteringRuleTypeRegistry();
    }
    
    /**
     * Returns the filtering rule type with the specified id, or {@code null} if none exists.
     *
     * @param id the id of the filtering rule type
     * @return the filtering rule type with the specified id, or {@code null} if none exists
     */
    @Nullable
    FilteringRuleType<?> get(ResourceLocation id);
    
    /**
     * Returns the id of the specified filtering rule type, or {@code null} if none exists.
     *
     * @param rule the filtering rule type
     * @return the id of the specified filtering rule type, or {@code null} if none exists
     */
    @Nullable
    ResourceLocation getId(FilteringRuleType<?> rule);
    
    /**
     * Registers the specified filtering rule type.
     *
     * @param id   the id of the filtering rule type
     * @param rule the filtering rule type
     */
    void register(ResourceLocation id, FilteringRuleType<?> rule);
    
    /**
     * Returns the basic filtering rule that can be used to filter entries,
     * without registering a new filtering rule type, for external
     * plugins.
     *
     * @return the base filtering rule
     */
    BasicFilteringRule<?> basic();
}
