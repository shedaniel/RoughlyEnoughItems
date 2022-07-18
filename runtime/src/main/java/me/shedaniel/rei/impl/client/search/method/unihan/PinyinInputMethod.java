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

package me.shedaniel.rei.impl.client.search.method.unihan;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PinyinInputMethod extends UniHanInputMethod implements CharacterUnpackingInputMethod {
    protected final Int2ObjectMap<ToneEntry> toneMap;
    
    protected record ToneEntry(int codepoint, int tone) {}
    
    public PinyinInputMethod(UniHanManager manager) {
        super(manager);
        toneMap = new Int2ObjectOpenHashMap<>();
        addTone('ā', "a1");
        addTone('á', "a2");
        addTone('ǎ', "a3");
        addTone('à', "a4");
        addTone('ē', "e1");
        addTone('é', "e2");
        addTone('ě', "e3");
        addTone('è', "e4");
        addTone('ī', "i1");
        addTone('í', "i2");
        addTone('ǐ', "i3");
        addTone('ì', "i4");
        addTone('ō', "o1");
        addTone('ó', "o2");
        addTone('ǒ', "o3");
        addTone('ò', "o4");
        addTone('ū', "u1");
        addTone('ú', "u2");
        addTone('ǔ', "u3");
        addTone('ù', "u4");
        addTone('ǖ', "v1");
        addTone('ǘ', "v2");
        addTone('ǚ', "v3");
        addTone('ǜ', "v4");
    }
    
    private void addTone(char c, String s) {
        toneMap.put(c, new ToneEntry(s.charAt(0), Character.digit(s.charAt(1), 10)));
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
        return "kMandarin";
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
        return Component.translatable("text.rei.input.methods.pinyin");
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("text.rei.input.methods.pinyin.description");
    }
    
    @Override
    protected ExpendedChar asExpendedChar(String string) {
        List<IntList> codepoints = new ArrayList<>(string.length() + 1);
        int[] tone = {-1};
        string.codePoints().forEach(codepoint -> {
            if (codepoint == 'ü') {
                codepoints.add(IntList.of('v'));
                return;
            }
            ToneEntry toneEntry = toneMap.get(codepoint);
            if (toneEntry == null) {
                codepoints.add(IntList.of(codepoint));
            } else {
                codepoints.add(IntList.of(toneEntry.codepoint));
                tone[0] = toneEntry.tone;
            }
        });
        if (tone[0] != -1) {
            codepoints.add(IntList.of(Character.forDigit(tone[0], 10)));
        }
        return new ExpendedChar(codepoints);
    }
}
