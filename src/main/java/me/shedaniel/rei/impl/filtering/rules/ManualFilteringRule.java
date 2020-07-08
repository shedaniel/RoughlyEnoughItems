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

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.filtering.AbstractFilteringRule;
import me.shedaniel.rei.impl.filtering.FilteringContext;
import me.shedaniel.rei.impl.filtering.FilteringResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    
    private void processList(Set<EntryStack> stacks, FilteringResult result) {
        List<EntryStack> filteredStacks = ConfigObject.getInstance().getFilteredStacks();
        List<EntryStack> filtered = Lists.newArrayList();
        for (EntryStack stack : stacks) {
            if (findFirstOrNullEqualsEntryIgnoreAmount(filteredStacks, stack) != null)
                filtered.add(stack);
        }
        result.hide(filtered);
    }
    
    private static EntryStack findFirstOrNullEqualsEntryIgnoreAmount(Collection<EntryStack> list, EntryStack obj) {
        for (EntryStack t : list) {
            if (t.equalsIgnoreAmount(obj))
                return t;
        }
        return null;
    }
    
    @Override
    public Text getTitle() {
        return new TranslatableText("rule.roughlyenoughitems.filtering.manual");
    }
    
    @Override
    public Text getSubtitle() {
        return new TranslatableText("rule.roughlyenoughitems.filtering.manual.subtitle");
    }
    
    @Override
    public ManualFilteringRule createNew() {
        throw new UnsupportedOperationException();
    }
}
