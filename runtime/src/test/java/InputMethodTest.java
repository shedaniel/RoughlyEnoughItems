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

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.client.search.argument.InputMethodMatcher;
import me.shedaniel.rei.impl.client.search.method.unihan.BomopofoInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.PinyinInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.UniHanManager;
import me.shedaniel.rei.impl.common.InternalLogger;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputMethodTest {
    public static final InternalLogger LOGGER = new InternalLogger() {
        @Override
        public void throwException(Throwable throwable) {
            throwable.printStackTrace();
        }
        
        @Override
        public void log(Level level, String message) {
            System.out.println("[" + level.name() + "] " + message);
        }
        
        @Override
        public void log(Level level, String message, Throwable throwable) {
            System.out.println("[" + level.name() + "] " + message);
            throwable.printStackTrace();
        }
    };
    
    @TempDir
    static Path tempDir;
    
    static PinyinInputMethod pinyinInputMethod;
    static BomopofoInputMethod bomopofoInputMethod;
    
    @BeforeAll
    static void setup() {
        Internals.attachInstanceSupplier(LOGGER, "logger");
        
        UniHanManager manager = new UniHanManager(tempDir.resolve("unihan.zip"));
        pinyinInputMethod = new TestPinyinInputMethod(manager);
        bomopofoInputMethod = new TestBomopofoInputMethod(manager);
        ExecutorService service = Executors.newSingleThreadExecutor();
        pinyinInputMethod.prepare(service).join();
        bomopofoInputMethod.prepare(service).join();
        service.shutdown();
    }
    
    /**
     * The fuzzy matching options live in the game's config folder, which does not exist
     * outside of a running game; the tests run with every option left at its default.
     */
    static class TestPinyinInputMethod extends PinyinInputMethod {
        TestPinyinInputMethod(UniHanManager manager) {
            super(manager);
        }
        
        @Override
        protected void read() {}
        
        @Override
        protected void write() {}
    }
    
    static class TestBomopofoInputMethod extends BomopofoInputMethod {
        TestBomopofoInputMethod(UniHanManager manager) {
            super(manager);
        }
        
        @Override
        protected void read() {}
        
        @Override
        protected void write() {}
    }
    
    @Test
    void testBopomofo() {
        // Sequences are 大千 (standard zhuyin) keyboard positions: ㄕ is g, ㄨ is j, ˋ is 4.
        
        // ㄓㄔㄕㄖㄗㄘㄙ are whole syllables on their own; the i of shi/zhi/zi is the empty
        // rime and not ㄧ.
        assertTrue(bopomofoContains("室", "g4"));
        assertTrue(bopomofoContains("室", "g"));
        assertFalse(bopomofoContains("室", "gu4"));
        assertTrue(bopomofoContains("知", "5"));
        assertTrue(bopomofoContains("字", "y4"));
        
        // ㄐㄑㄒ are never followed by ㄨ: the u of ju/qu/xu is ㄩ.
        assertTrue(bopomofoContains("居", "rm"));
        assertFalse(bopomofoContains("居", "rj"));
        assertTrue(bopomofoContains("窮", "fm/6"));
        
        // y and w are not initials, they spell a syllable-initial ㄧ/ㄨ/ㄩ.
        assertTrue(bopomofoContains("一", "u"));
        assertFalse(bopomofoContains("一", "uu"));
        assertTrue(bopomofoContains("語", "m3"));
        assertTrue(bopomofoContains("雲", "mp6"));
        assertTrue(bopomofoContains("永", "m/3"));
        assertTrue(bopomofoContains("翁", "j/"));
        assertTrue(bopomofoContains("五", "j3"));
        
        // ㄩ finals.
        assertTrue(bopomofoContains("雪", "vm,3"));
        assertTrue(bopomofoContains("雪", "vm"));
        assertFalse(bopomofoContains("雪", "vue3"));
        assertTrue(bopomofoContains("月", "m,4"));
        assertTrue(bopomofoContains("略", "xm,4"));
        assertTrue(bopomofoContains("綠", "xm4"));
        
        // Finals that pinyin abbreviates, and the neutral tone.
        assertTrue(bopomofoContains("流", "xu.6"));
        assertTrue(bopomofoContains("水", "gjo3"));
        assertTrue(bopomofoContains("文", "jp6"));
        assertTrue(bopomofoContains("的", "2k7"));
        assertTrue(bopomofoContains("的", "2k"));
        assertTrue(bopomofoContains("兒", "-6"));
        
        // Whole strings, both fully typed and with syllables left unfinished.
        assertTrue(bopomofoContains("測試文本", "hk4g4jp61p3"));
        assertTrue(bopomofoContains("測試文本", "hgjp1p"));
        assertTrue(bopomofoContains("洗礦場", "vu3dj;4t;3"));
        assertTrue(bopomofoContains("合金爐", "ckrupxj"));
        assertFalse(bopomofoContains("測試文本", "hk6g4jp61p3"));
    }
    
    void testPinyin() {
        assertTrue(pinyinContains("漢", "han"));
        assertTrue(pinyinContains("漢語", "hanyu"));
        assertTrue(pinyinContains("漢", "ha"));
        assertTrue(pinyinContains("漢語", "hayu"));
        assertTrue(pinyinContains("测试文本", "ceshiwenben"));
        assertTrue(pinyinContains("测试文本", "ceshiwenbe"));
        assertTrue(pinyinContains("测试文本", "ceshiwben"));
        assertTrue(pinyinContains("测试文本", "ceshwbe"));
        assertTrue(pinyinContains("测试文本", "ce4shi4wb"));
        assertFalse(pinyinContains("测试文本", "ce2shi4wb"));
        assertTrue(pinyinContains("合金炉", "hejinlu"));
        assertTrue(pinyinContains("洗矿场", "xikuangchang"));
        assertTrue(pinyinContains("洗矿场", "xikuachang"));
        assertTrue(pinyinContains("流体", "liuti"));
        assertTrue(pinyinContains("轰20", "hong2"));
        assertTrue(pinyinContains("hong2", "hong2"));
    }
    
    boolean pinyinContains(String input, String substr) {
        return InputMethodMatcher.contains(pinyinInputMethod, IntList.of(input.codePoints().toArray()), IntList.of(substr.codePoints().toArray()));
    }
    
    boolean bopomofoContains(String input, String substr) {
        return InputMethodMatcher.contains(bomopofoInputMethod, IntList.of(input.codePoints().toArray()), IntList.of(substr.codePoints().toArray()));
    }
}
