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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import dev.architectury.platform.Platform;
import dev.architectury.utils.value.BooleanValue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DoublePinyinInputMethod extends PinyinInputMethod {
    private Converter converter = Converters.SOUGOU;
    
    public DoublePinyinInputMethod(UniHanManager manager) {
        super(manager);
    }
    
    @Override
    protected void read() {
        Path path = Platform.getConfigFolder().resolve("roughlyenoughitems/pinyin_double.properties");
        this.converter = Converters.SOUGOU;
        if (Files.exists(path)) {
            try {
                Properties properties = new Properties();
                try (InputStream stream = Files.newInputStream(path)) {
                    properties.load(stream);
                }
                this.converter = Converters.CONVERTERS.getOrDefault(Objects.toString(properties.getOrDefault("Converter", "sougou")), Converters.SOUGOU);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        this.write();
    }
    
    @Override
    protected void write() {
        Path path = Platform.getConfigFolder().resolve("roughlyenoughitems/pinyin_double.properties");
        Properties properties = new Properties();
        properties.put("Converter", Converters.CONVERTERS.inverse().get(this.converter));
        try (OutputStream stream = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            properties.store(stream, "Double Pinyin Options");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public Component getName() {
        return Component.translatable("text.rei.input.methods.pinyin.double");
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("text.rei.input.methods.pinyin.double.description");
    }
    
    @Override
    public List<FavoriteMenuEntry> getOptionsMenuEntries() {
        List<FavoriteMenuEntry> innerEntries = new ArrayList<>();
        for (Map.Entry<String, Converter> entry : Converters.CONVERTERS.entrySet()) {
            innerEntries.add(FavoriteMenuEntry.createToggle(Component.translatable("text.rei.input.methods.pinyin.double.scheme." + entry.getKey()),
                    new BooleanValue() {
                        @Override
                        public void accept(boolean t) {
                            DoublePinyinInputMethod.this.converter = entry.getValue();
                            DoublePinyinInputMethod.this.write();
                            DoublePinyinInputMethod.this.dataMap.clear();
                            DoublePinyinInputMethod.this.load();
                        }
                        
                        @Override
                        public boolean getAsBoolean() {
                            return DoublePinyinInputMethod.this.converter == entry.getValue();
                        }
                    }));
        }
        return List.of(FavoriteMenuEntry.createSubMenu(Component.translatable("text.rei.input.methods.pinyin.double.scheme"),
                innerEntries));
    }
    
    @Override
    protected List<IntList> expendSimple(String string) {
        return List.of(this.converter.convert(string));
    }
    
    @Override
    protected List<IntList>[] expendSingles(List<IntList> codepoint) {
        if (this.converter == Converters.SOUGOU || this.converter == Converters.MICROSOFT) {
            return new List[]{List.of(IntList.of('o')), codepoint};
        } else {
            return new List[]{codepoint, codepoint};
        }
    }
    
    public interface Converter {
        IntList convert(String input);
    }
    
    public static class MapConverter implements Converter {
        private final Object2IntMap<String> map;
        
        public MapConverter(Object2IntMap<String> map) {
            this.map = map;
        }
        
        public MapConverter(Object... args) {
            Object2IntMap<String> map = new Object2IntOpenHashMap<>(args.length / 2);
            for (int i = 0; i < args.length; i += 2) {
                map.put((String) args[i], (char) args[i + 1]);
            }
            this.map = Object2IntMaps.unmodifiable(map);
        }
        
        @Override
        public IntList convert(String input) {
            int i = map.getOrDefault(input, -1);
            if (i == -1) return IntList.of(input.codePoints().toArray());
            return IntList.of(i);
        }
    }
    
    public interface Converters {
        Converter SOUGOU = new MapConverter(
                "iu", 'q',
                "ia", 'w',
                "ua", 'w',
                "er", 'r',
                "uan", 'r',
                "ue", 't',
                "ve", 't',
                "uai", 'y',
                "v", 'y',
                "sh", 'u',
                "ch", 'i',
                "uo", 'o',
                "un", 'p',
                "ong", 's',
                "iong", 's',
                "uang", 'd',
                "iang", 'd',
                "en", 'f',
                "eng", 'g',
                "ang", 'h',
                "an", 'j',
                "ao", 'k',
                "ai", 'l',
                "ing", ';',
                "ei", 'z',
                "ie", 'x',
                "iao", 'c',
                "zh", 'v',
                "ui", 'v',
                "ou", 'b',
                "in", 'n',
                "ian", 'm'
        );
        Converter MICROSOFT = new MapConverter(
                "iu", 'q',
                "ia", 'w',
                "ua", 'w',
                "er", 'r',
                "uan", 'r',
                "ue", 't',
                "uai", 'y',
                "v", 'y',
                "sh", 'u',
                "ch", 'i',
                "uo", 'o',
                "un", 'p',
                "ong", 's',
                "iong", 's',
                "uang", 'd',
                "iang", 'd',
                "en", 'f',
                "eng", 'g',
                "ang", 'h',
                "an", 'j',
                "ao", 'k',
                "ai", 'l',
                "ing", ';',
                "ei", 'z',
                "ie", 'x',
                "iao", 'c',
                "zh", 'v',
                "ui", 'v',
                "ve", 'v',
                "ou", 'b',
                "in", 'n',
                "ian", 'm'
        );
        Converter PINYINPP = new MapConverter(
                "er", 'q',
                "ing", 'q',
                "ei", 'w',
                "en", 'r',
                "eng", 't',
                "iong", 'y',
                "ong", 'y',
                "ch", 'u',
                "sh", 'i',
                "uo", 'o',
                "ou", 'p',
                "ai", 's',
                "ao", 'd',
                "an", 'f',
                "ang", 'g',
                "iang", 'h',
                "uang", 'h',
                "ian", 'j',
                "iao", 'k',
                "in", 'l',
                "un", 'z',
                "uai", 'x',
                "ue", 'x',
                "uan", 'c',
                "zh", 'v',
                "ui", 'v',
                "ia", 'b',
                "ua", 'b',
                "iu", 'n',
                "ie", 'm'
        );
        Converter XIAOHE = new MapConverter(
                "iu", 'q',
                "ei", 'w',
                "uan", 'r',
                "ue", 't',
                "ve", 't',
                "un", 'y',
                "sh", 'u',
                "ch", 'i',
                "uo", 'o',
                "ie", 'p',
                "ong", 's',
                "iong", 's',
                "ai", 'd',
                "en", 'f',
                "eng", 'g',
                "ang", 'h',
                "an", 'j',
                "ing", 'k',
                "uai", 'k',
                "iang", 'l',
                "uang", 'l',
                "ou", 'z',
                "ia", 'x',
                "ua", 'x',
                "ao", 'c',
                "zh", 'v',
                "ui", 'v',
                "in", 'b',
                "iao", 'n',
                "ian", 'm'
        );
        Converter NATURAL = new MapConverter(
                "iu", 'q',
                "ia", 'w',
                "ua", 'w',
                "uan", 'r',
                "ue", 't',
                "ve", 't',
                "ing", 'y',
                "uai", 'y',
                "sh", 'u',
                "ch", 'i',
                "uo", 'o',
                "un", 'p',
                "ong", 's',
                "iong", 's',
                "uang", 'd',
                "iang", 'd',
                "en", 'f',
                "eng", 'g',
                "ang", 'h',
                "an", 'j',
                "ao", 'k',
                "ai", 'l',
                "ei", 'z',
                "ie", 'x',
                "iao", 'c',
                "zh", 'v',
                "ui", 'v',
                "ou", 'b',
                "in", 'n',
                "ian", 'm'
        );
        BiMap<String, Converter> CONVERTERS = ImmutableBiMap.of(
                "sougou", SOUGOU,
                "microsoft", MICROSOFT,
                "pinyinpp", PINYINPP,
                "xiaohe", XIAOHE,
                "natural", NATURAL
        );
    }
}
