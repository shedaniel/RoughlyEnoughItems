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

package me.shedaniel.rei.impl.client.search.method.unihan;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class JyutpingInputMethod extends UniHanInputMethod implements CharacterUnpackingInputMethod {
    public JyutpingInputMethod(UniHanManager manager) {
        super(manager);
    }
    
    @Override
    public Iterable<IntList> expendFilter(String filter) {
        return Collections.singletonList(IntList.of(filter.codePoints().toArray()));
    }
    
    @Override
    public List<ExpendedChar> expendSourceChar(int codePoint) {
        List<ExpendedChar> sequences = dataMap.get(codePoint);
        if (sequences != null && !sequences.isEmpty()) return sequences;
        return List.of(new ExpendedChar(List.of(IntList.of(codePoint))));
    }
    
    @Override
    protected String getFieldKey() {
        return "kCantonese";
    }
    
    @Override
    protected String getFieldDelimiter() {
        return " ";
    }
    
    @Override
    public List<Locale> getMatchingLocales() {
        return CollectionUtils.filterToList(InputMethod.getAllLocales(), locale -> locale.code().startsWith("zh_"));
    }
    
    @Override
    public Component getName() {
        return Component.translatable("text.rei.input.methods.jyutping");
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("text.rei.input.methods.jyutping.description");
    }
}
