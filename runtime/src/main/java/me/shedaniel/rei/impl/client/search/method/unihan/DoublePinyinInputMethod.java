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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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
        this.read();
    }
    
    private void read() {
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
    
    private void write() {
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
        return new TranslatableComponent("text.rei.input.methods.pinyin.double");
    }
    
    @Override
    public Component getDescription() {
        return new TranslatableComponent("text.rei.input.methods.pinyin.double.description");
    }
    
    @Override
    public List<FavoriteMenuEntry> getOptionsMenuEntries() {
        List<FavoriteMenuEntry> innerEntries = new ArrayList<>();
        for (Map.Entry<String, Converter> entry : Converters.CONVERTERS.entrySet()) {
            innerEntries.add(FavoriteMenuEntry.createToggle(new TranslatableComponent("text.rei.input.methods.pinyin.double.scheme." + entry.getKey()),
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
        return List.of(FavoriteMenuEntry.createSubMenu(new TranslatableComponent("text.rei.input.methods.pinyin.double.scheme"),
                innerEntries));
    }
    
    @Override
    protected ExpendedChar asExpendedChar(String string) {
        IntList[] codepoints = new IntList[3];
        int skip = 2;
        int tone = -1;
        char[] chars = string.toCharArray();
        if (chars[0] == 's' && chars[1] == 'h') {
            codepoints[0] = this.converter.convert("sh");
        } else if (chars[0] == 'c' && chars[1] == 'h') {
            codepoints[0] = this.converter.convert("ch");
        } else if (chars[0] == 'z' && chars[1] == 'h') {
            codepoints[0] = this.converter.convert("zh");
        } else {
            skip = 1;
            ToneEntry toneEntry = toneMap.get(chars[0]);
            if (toneEntry == null) {
                codepoints[0] = this.converter.convert(chars[0] + "");
            } else {
                codepoints[0] = this.converter.convert(((char) toneEntry.codepoint()) + "");
                tone = toneEntry.tone();
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = skip; i < chars.length; i++) {
            char c = chars[i];
            if (c == 'ü') {
                builder.append('v');
            } else {
                ToneEntry toneEntry = toneMap.get(c);
                if (toneEntry == null) {
                    builder.append(c);
                } else {
                    builder.append((char) toneEntry.codepoint());
                    tone = toneEntry.tone();
                }
            }
        }
        if (builder.isEmpty()) {
            codepoints[1] = codepoints[0];
            if (this.converter == Converters.SOUGOU || this.converter == Converters.MICROSOFT) {
                codepoints[0] = IntList.of('o');
            }
        } else {
            codepoints[1] = this.converter.convert(builder.toString());
        }
        if (tone != -1) {
            codepoints[2] = IntList.of(Character.forDigit(tone, 10));
        }
        return new ExpendedChar(Arrays.asList(codepoints).subList(0, tone == -1 ? 2 : 3));
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
