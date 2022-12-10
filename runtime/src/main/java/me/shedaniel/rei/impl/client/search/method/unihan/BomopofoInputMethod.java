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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class BomopofoInputMethod extends PinyinInputMethod {
    private static final Map<IntList, IntList> CONVERSION = Stream.of(new String[][]{
            {"", ""}, {"0", ""}, {"1", " "}, {"2", "6"}, {"3", "3"},
            {"4", "4"}, {"a", "8"}, {"ai", "9"}, {"an", "0"}, {"ang", ";"},
            {"ao", "l"}, {"b", "1"}, {"c", "h"}, {"ch", "t"}, {"d", "2"},
            {"e", "k"}, {"ei", "o"}, {"en", "p"}, {"eng", "/"}, {"er", "-"},
            {"f", "z"}, {"g", "e"}, {"h", "c"}, {"i", "u"}, {"ia", "u8"},
            {"ian", "u0"}, {"iang", "u;"}, {"iao", "ul"}, {"ie", "u,"}, {"in", "up"},
            {"ing", "u/"}, {"iong", "m/"}, {"iu", "u."}, {"j", "r"}, {"k", "d"},
            {"l", "x"}, {"m", "a"}, {"n", "s"}, {"o", "i"}, {"ong", "j/"},
            {"ou", "."}, {"p", "q"}, {"q", "f"}, {"r", "b"}, {"s", "n"},
            {"sh", "g"}, {"t", "w"}, {"u", "j"}, {"ua", "j8"}, {"uai", "j9"},
            {"uan", "j0"}, {"uang", "j;"}, {"uen", "mp"}, {"ueng", "j/"}, {"ui", "jo"},
            {"un", "jp"}, {"uo", "ji"}, {"v", "m"}, {"van", "m0"}, {"vang", "m;"},
            {"ve", "m,"}, {"vn", "mp"}, {"w", "j"}, {"x", "v"}, {"y", "u"},
            {"z", "y"}, {"zh", "5"},
    }).collect(Collectors.toMap(d -> IntList.of(d[0].codePoints().toArray()), d -> IntList.of(d[1].trim().codePoints().toArray())));
    
    public BomopofoInputMethod(UniHanManager manager) {
        super(manager);
    }
    
    @Override
    public Component getName() {
        return Component.translatable("text.rei.input.methods.bopomofo");
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("text.rei.input.methods.bopomofo.description");
    }
    
    @Override
    public List<FavoriteMenuEntry> getOptionsMenuEntries() {
        return List.of();
    }
    
    @Override
    protected List<ExpendedChar> asExpendedChars(String string) {
        IntList codepoints = new IntArrayList(string.length() + 1);
        int[] tone = {-1};
        string.codePoints().forEach(codepoint -> {
            if (codepoint == 'ü') {
                codepoints.add('v');
                return;
            }
            ToneEntry toneEntry = toneMap.get(codepoint);
            if (toneEntry == null) {
                codepoints.add(codepoint);
            } else {
                codepoints.add(toneEntry.codepoint());
                tone[0] = toneEntry.tone();
            }
        });
        if (tone[0] != -1) {
            codepoints.add(Character.forDigit(tone[0], 10));
        }
        List<IntList> phonemes = standard(codepoints).stream().map(str -> CONVERSION.getOrDefault(str, str)).toList();
        return List.of(new ExpendedChar(phonemes));
    }
    
    private static List<IntList> standard(IntList s) {
        List<IntList> ret = new ArrayList<>();
        int cursor = 0;
        
        // initial
        if (hasInitial(s)) {
            cursor = s.size() > 2 && s.getInt(1) == 'h' ? 2 : 1;
            ret.add(s.subList(0, cursor));
        }
        
        // final
        if (s.size() != cursor + 1 && s.size() - 1 > cursor) {
            ret.add(s.subList(cursor, s.size() - 1));
        }
        
        // tone
        if (s.size() >= 1) {
            ret.add(s.subList(s.size() - 1, s.size()));
        }
        
        return ret;
    }
    
    private static boolean hasInitial(IntList s) {
        return Stream.of('a', 'e', 'i', 'o', 'u', 'v').noneMatch(i -> s.getInt(0) == i);
    }
}
