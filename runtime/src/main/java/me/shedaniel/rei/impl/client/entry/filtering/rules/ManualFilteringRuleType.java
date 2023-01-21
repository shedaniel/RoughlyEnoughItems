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

package me.shedaniel.rei.impl.client.entry.filtering.rules;

import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public enum ManualFilteringRuleType implements FilteringRuleType<ManualFilteringRule> {
    INSTANCE;
    
    @Override
    public CompoundTag saveTo(ManualFilteringRule rule, CompoundTag tag) {
        return tag;
    }
    
    @Override
    public ManualFilteringRule readFrom(CompoundTag tag) {
        return new ManualFilteringRule();
    }
    
    @Override
    public Component getTitle(ManualFilteringRule rule) {
        return Component.translatable("rule.roughlyenoughitems.filtering.manual");
    }
    
    @Override
    public Component getSubtitle(ManualFilteringRule rule) {
        return Component.translatable("rule.roughlyenoughitems.filtering.manual.subtitle");
    }
    
    @Override
    public ManualFilteringRule createNew() {
        return new ManualFilteringRule();
    }
    
    @Override
    public boolean isSingular() {
        return true;
    }
}
