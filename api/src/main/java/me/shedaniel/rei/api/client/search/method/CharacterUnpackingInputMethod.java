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

package me.shedaniel.rei.api.client.search.method;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@ApiStatus.Experimental
public interface CharacterUnpackingInputMethod extends InputMethod<IntList> {
    List<ExpendedChar> expendSourceChar(int codePoint);
    
    @Override
    default boolean contains(String str, IntList substr) {
        // This is implemented in the runtime
        throw new UnsupportedOperationException();
    }
    
    @Override
    @Nullable
    default String suggestInputString(String str) {
        return str.codePoints().mapToObj(c -> {
            List<ExpendedChar> chars = expendSourceChar(c);
            if (chars.isEmpty()) return ((char) c) + "";
            int i = Mth.floor((System.currentTimeMillis() / 1000L % (double) chars.size()));
            String result = chars.get(i).phonemes().stream()
                    .flatMap(integers -> integers.intStream().mapToObj(value -> ((char) value) + ""))
                    .collect(Collectors.joining());
            if (result.codePointCount(0, result.length()) == 1 && result.codePointAt(0) == c) {
                return result;
            }
            return " " + result + " ";
        }).collect(Collectors.joining()).trim().replace("  ", " ");
    }
    
    record ExpendedChar(List<IntList> phonemes) {}
}
