/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl.filtering.rules;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.ComparisonContext;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.impl.filtering.AbstractFilteringRule;
import me.shedaniel.rei.impl.filtering.FilteringContext;
import me.shedaniel.rei.impl.filtering.FilteringResult;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class ManualFilteringRule extends AbstractFilteringRule<ManualFilteringRule> {
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        return tag;
    }
    
    @Override
    public ManualFilteringRule createFromTag(CompoundTag tag) {
        return new ManualFilteringRule();
    }
    
    @Override
    public @NotNull FilteringResult processFilteredStacks(@NotNull FilteringContext context) {
        FilteringResult result = FilteringResult.create();
        processList(context.getShownStacks(), result);
        processList(context.getUnsetStacks(), result);
        return result;
    }
    
    private void processList(Collection<EntryStack<?>> stacks, FilteringResult result) {
        IntSet filteredStacks = CollectionUtils.mapParallel(ConfigObject.getInstance().getFilteredStacks(), EntryStacks::hashIgnoreCount, IntOpenHashSet::new);
        result.hide(stacks.parallelStream().filter(stack -> filteredStacks.contains(stack.hash(ComparisonContext.IGNORE_COUNT))).collect(Collectors.toList()));
    }
    
    @Override
    public Component getTitle() {
        return new TranslatableComponent("rule.roughlyenoughitems.filtering.manual");
    }
    
    @Override
    public Component getSubtitle() {
        return new TranslatableComponent("rule.roughlyenoughitems.filtering.manual.subtitle");
    }
    
    @Override
    public ManualFilteringRule createNew() {
        throw new UnsupportedOperationException();
    }
}
