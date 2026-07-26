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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    /**
     * Pinyin initials, as their key positions on the 大千 (standard zhuyin) keyboard.
     */
    private static final Map<String, String> INITIALS = table(new String[][]{
            {"b", "1"}, {"p", "q"}, {"m", "a"}, {"f", "z"},
            {"d", "2"}, {"t", "w"}, {"n", "s"}, {"l", "x"},
            {"g", "e"}, {"k", "d"}, {"h", "c"},
            {"j", "r"}, {"q", "f"}, {"x", "v"},
            {"zh", "5"}, {"ch", "t"}, {"sh", "g"}, {"r", "b"},
            {"z", "y"}, {"c", "h"}, {"s", "n"},
    });
    
    /**
     * Pinyin finals, as their key positions on the 大千 keyboard. Keyed by the normalised
     * spelling produced by {@link #split(String)}: {@code ü} is written {@code v}, and the
     * finals that pinyin abbreviates are listed under both spellings.
     */
    private static final Map<String, String> FINALS = table(new String[][]{
            {"", ""},
            {"a", "8"}, {"o", "i"}, {"e", "k"}, {"ê", ","},
            {"ai", "9"}, {"ei", "o"}, {"ao", "l"}, {"ou", "."},
            {"an", "0"}, {"en", "p"}, {"ang", ";"}, {"eng", "/"}, {"er", "-"},
            {"i", "u"}, {"ia", "u8"}, {"io", "ui"}, {"ie", "u,"}, {"iai", "u9"},
            {"iao", "ul"}, {"iu", "u."}, {"iou", "u."}, {"ian", "u0"}, {"in", "up"},
            {"iang", "u;"}, {"ing", "u/"}, {"iong", "m/"},
            {"u", "j"}, {"ua", "j8"}, {"uo", "ji"}, {"uai", "j9"}, {"ui", "jo"},
            {"uei", "jo"}, {"uan", "j0"}, {"un", "jp"}, {"uen", "jp"}, {"uang", "j;"},
            {"ong", "j/"}, {"ueng", "j/"}, {"uong", "j/"},
            {"v", "m"}, {"ve", "m,"}, {"van", "m0"}, {"vn", "mp"},
    });
    
    /**
     * Syllables that are not built out of an initial and a final: the syllabic nasals.
     */
    private static final Map<String, String> SYLLABIC = table(new String[][]{
            {"m", "a"}, {"n", "p"}, {"ng", "/"}, {"hm", "ca"}, {"hng", "c/"},
    });
    
    /**
     * The initials that form a whole syllable on their own, where the vowel pinyin writes
     * is the empty rime rather than ㄧ.
     */
    private static final Set<String> EMPTY_RIME = Set.of("zh", "ch", "sh", "r", "z", "c", "s");
    
    /**
     * Tone keys on the 大千 keyboard, indexed by tone number. The first tone is unmarked in
     * zhuyin and is typed with a space, which is not usable inside a search filter.
     */
    private static final String[] TONES = {"7", "", "6", "3", "4"};
    
    /**
     * Tone marks that {@link PinyinInputMethod#toneMap} does not carry, because they only
     * ever appear on the syllabic nasals above.
     */
    private static final Int2ObjectMap<ToneEntry> NASAL_TONES = new Int2ObjectOpenHashMap<>();
    
    static {
        NASAL_TONES.put('ḿ', new ToneEntry('m', 2));
        NASAL_TONES.put('ń', new ToneEntry('n', 2));
        NASAL_TONES.put('ň', new ToneEntry('n', 3));
        NASAL_TONES.put('ǹ', new ToneEntry('n', 4));
    }
    
    private static Map<String, String> table(String[][] entries) {
        return Stream.of(entries).collect(Collectors.toUnmodifiableMap(entry -> entry[0], entry -> entry[1]));
    }
    
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
        StringBuilder builder = new StringBuilder(string.length());
        int tone = 0;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == 'ü') {
                builder.append('v');
                continue;
            }
            ToneEntry toneEntry = toneMap.get(c);
            if (toneEntry == null) toneEntry = NASAL_TONES.get(c);
            if (toneEntry == null) {
                builder.append(c);
            } else {
                builder.append((char) toneEntry.codepoint());
                tone = toneEntry.tone();
            }
        }
        String[] keys = split(builder.toString());
        if (keys == null) return List.of();
        List<IntList> phonemes = new ArrayList<>(3);
        addPhoneme(phonemes, keys[0]);
        addPhoneme(phonemes, keys[1]);
        addPhoneme(phonemes, TONES[tone]);
        return List.of(new ExpendedChar(phonemes));
    }
    
    private static void addPhoneme(List<IntList> phonemes, String keys) {
        if (!keys.isEmpty()) phonemes.add(IntList.of(keys.codePoints().toArray()));
    }
    
    /**
     * Splits a toneless pinyin syllable into the keys of its zhuyin initial and its zhuyin
     * final, or returns {@code null} if it is not a syllable this mapping knows.
     */
    private static String[] split(String syllable) {
        if (syllable.isEmpty()) return null;
        String whole = SYLLABIC.get(syllable);
        if (whole != null) return new String[]{whole, ""};
        
        // ㄧㄨㄩ are spelled y- and w- at the start of a syllable, where they are the medial
        // and not an initial of their own.
        String s = syllable;
        if (s.charAt(0) == 'y') {
            String rest = s.substring(1);
            if (rest.startsWith("u")) s = "v" + rest.substring(1);
            else if (rest.isEmpty()) s = "i";
            else if (rest.charAt(0) == 'i') s = rest;
            else s = "i" + rest;
        } else if (s.charAt(0) == 'w') {
            String rest = s.substring(1);
            s = rest.startsWith("u") ? rest : "u" + rest;
        }
        
        String initial = "";
        if (s.length() > 1 && s.charAt(1) == 'h' && "zcs".indexOf(s.charAt(0)) >= 0) {
            initial = s.substring(0, 2);
            s = s.substring(2);
        } else if (INITIALS.containsKey(s.substring(0, 1))) {
            initial = s.substring(0, 1);
            s = s.substring(1);
        }
        
        // ㄐㄑㄒ are never followed by ㄨ, so pinyin drops the diaeresis of ü after them.
        if (s.startsWith("u") && ("j".equals(initial) || "q".equals(initial) || "x".equals(initial))) {
            s = "v" + s.substring(1);
        }
        
        // ㄓㄔㄕㄖㄗㄘㄙ are whole syllables on their own; pinyin writes that empty rime as i.
        if (s.equals("i") && EMPTY_RIME.contains(initial)) {
            s = "";
        }
        
        String finals = FINALS.get(s);
        if (finals == null) return null;
        return new String[]{initial.isEmpty() ? "" : INITIALS.get(initial), finals};
    }
}
