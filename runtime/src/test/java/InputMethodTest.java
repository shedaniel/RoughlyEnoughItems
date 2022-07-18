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

import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.impl.Internals;
import me.shedaniel.rei.impl.client.search.argument.InputMethodMatcher;
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
    
    @BeforeAll
    static void setup() {
        Internals.attachInstanceSupplier(LOGGER, "logger");
        
        UniHanManager manager = new UniHanManager(tempDir.resolve("unihan.zip"));
        pinyinInputMethod = new PinyinInputMethod(manager);
        ExecutorService service = Executors.newSingleThreadExecutor();
        pinyinInputMethod.prepare(service).join();
        service.shutdown();
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
}
