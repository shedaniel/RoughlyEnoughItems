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

package me.shedaniel.rei.impl.client.search.argument;

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod.ExpendedChar;

import java.util.List;

/**
 * MIT License
 * <p>
 * Copyright (c) 2019 Juntong Liu
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class InputMethodMatcher {
    public static boolean contains(CharacterUnpackingInputMethod inputMethod, IntList s1, IntList s2) {
        if (!s1.isEmpty()) {
            for (int i = 0; i < s1.size(); i++)
                if (check(inputMethod, s1, i, s2, 0, true)) return true;
        }
        return false;
    }
    
    public static boolean matches(CharacterUnpackingInputMethod inputMethod, IntList s1, IntList s2) {
        if (s1.isEmpty()) return s2.isEmpty();
        else return check(inputMethod, s1, 0, s2, 0, false);
    }
    
    private static IndexSet match(CharacterUnpackingInputMethod inputMethod, int self, IntList str, int start, boolean partial) {
        List<ExpendedChar> expendedList = inputMethod.expendSourceChar(self);
        IndexSet ret = (str.getInt(start) == self ? IndexSet.ONE : IndexSet.NONE).copy();
        for (ExpendedChar integers : expendedList) {
            ret.merge(match(integers, str, start, partial));
        }
        return ret;
    }
    
    private static IndexSet match(ExpendedChar phonemes, IntList str, int start, boolean partial) {
        IndexSet active = IndexSet.ZERO;
        IndexSet ret = new IndexSet();
        for (IntList phoneme : phonemes.phonemes()) {
            active = matchPhoneme(List.of(phoneme), str, active, start, partial);
            if (active.isEmpty()) return ret;
            ret.merge(active);
        }
        return ret;
    }
    
    private static IndexSet matchPhoneme(List<IntList> strs, IntList source, IndexSet idx, int start, boolean partial) {
        if (strs.size() == 1 && strs.get(0).isEmpty()) return new IndexSet(idx);
        IndexSet ret = new IndexSet();
        idx.foreach(i -> {
            IndexSet is = matchM(strs, source, start + i, partial);
            is.offset(i);
            ret.merge(is);
        });
        return ret;
    }
    
    public static IndexSet matchM(List<IntList> strs, IntList source, int start, boolean partial) {
        IndexSet ret = new IndexSet();
        if (strs.size() == 1 && strs.get(0).isEmpty()) return ret;
        for (IntList str : strs) {
            int size = strCmp(source, str, start);
            if (partial && start + size == source.size()) ret.set(size);  // ending match
            else if (size == str.size()) ret.set(size); // full match
        }
        return ret;
    }
    
    private static int strCmp(IntList a, IntList b, int aStart) {
        int len = Math.min(a.size() - aStart, b.size());
        for (int i = 0; i < len; i++)
            if (a.getInt(i + aStart) != b.getInt(i)) return i;
        return len;
    }
    
    private static boolean check(CharacterUnpackingInputMethod inputMethod, IntList s1, int start1, IntList s2, int start2, boolean partial) {
        if (start2 == s2.size()) return partial || start1 == s1.size();
        
        int ch = s1.getInt(start1);
        IndexSet s = match(inputMethod, ch, s2, start2, partial);
        
        if (start1 == s1.size() - 1) {
            int i = s2.size() - start2;
            return s.get(i);
        } else return !s.traverse(i -> !check(inputMethod, s1, start1 + 1, s2, start2 + i, partial));
    }
}
