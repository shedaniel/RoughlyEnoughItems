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

import me.shedaniel.rei.impl.client.util.CyclingList;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CyclingListTest {
    @Test
    void testSimple() {
        CyclingList<String> list = CyclingList.of(List.of("a", "b", "c"), () -> "empty");
    
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("b", list.next());
        
        assert list.currentIndex() == 1;
        assert list.nextIndex() == 2;
        assert list.previousIndex() == 0;
        assertEquals("b", list.peek());
        assertEquals("c", list.next());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("a", list.next());
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("c", list.previous());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("b", list.previous());
        
        assert list.currentIndex() == 1;
        assert list.nextIndex() == 2;
        assert list.previousIndex() == 0;
        assertEquals("b", list.peek());
        assertEquals("a", list.previous());
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("c", list.previous());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("a", list.next());
    }
    
    @Test
    void testMutable() {
        CyclingList.Mutable<String> list = CyclingList.ofMutable(new ArrayList<>(List.of("a", "b", "c")), () -> "empty");
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("b", list.next());
    
        assert list.currentIndex() == 1;
        assert list.nextIndex() == 2;
        assert list.previousIndex() == 0;
        assertEquals("b", list.peek());
        assertEquals("c", list.next());
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("a", list.next());
    
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("c", list.previous());
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        
        list.add("d");
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("a", list.next());
        
        list.add("e");
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 4;
        assertEquals("a", list.peek());
        assertEquals("e", list.previous());
        
        assert list.currentIndex() == 4;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 3;
        assertEquals("e", list.peek());
        assertEquals("d", list.previous());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("c", list.previous());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("e", list.next());
    }
    
    @Test
    void testImmutableAppendMutableManual() {
        CyclingList.Mutable<String> mutable = CyclingList.ofMutable(() -> "empty");
        CyclingList<String> list = CyclingList.concat(List.of(CyclingList.of(List.of("a", "b", "c"), () -> "empty"), mutable), () -> "empty");
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("b", list.next());
        
        assert list.currentIndex() == 1;
        assert list.nextIndex() == 2;
        assert list.previousIndex() == 0;
        assertEquals("b", list.peek());
        assertEquals("c", list.next());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("a", list.next());
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("c", list.previous());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        
        mutable.add("d");
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("a", list.next());
    
        mutable.add("e");
        
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 4;
        assertEquals("a", list.peek());
        assertEquals("e", list.previous());
        
        assert list.currentIndex() == 4;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 3;
        assertEquals("e", list.peek());
        assertEquals("d", list.previous());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("c", list.previous());
        
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
        
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("e", list.next());
    }
    
    @Test
    void testImmutableAppendMutable() {
        CyclingList.Mutable<String> list = CyclingList.ofMutable(CyclingList.of(List.of("a", "b", "c"), () -> "empty"), () -> "empty");
    
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("b", list.next());
    
        assert list.currentIndex() == 1;
        assert list.nextIndex() == 2;
        assert list.previousIndex() == 0;
        assertEquals("b", list.peek());
        assertEquals("c", list.next());
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("a", list.next());
    
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 2;
        assertEquals("a", list.peek());
        assertEquals("c", list.previous());
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
    
        list.add("d");
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
    
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("a", list.next());
    
        list.add("e");
    
        assert list.currentIndex() == 0;
        assert list.nextIndex() == 1;
        assert list.previousIndex() == list.size() - 1;
        assert list.previousIndex() == 4;
        assertEquals("a", list.peek());
        assertEquals("e", list.previous());
    
        assert list.currentIndex() == 4;
        assert list.nextIndex() == 0;
        assert list.previousIndex() == 3;
        assertEquals("e", list.peek());
        assertEquals("d", list.previous());
    
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("c", list.previous());
    
        assert list.currentIndex() == 2;
        assert list.nextIndex() == 3;
        assert list.previousIndex() == 1;
        assertEquals("c", list.peek());
        assertEquals("d", list.next());
    
        assert list.currentIndex() == 3;
        assert list.nextIndex() == 4;
        assert list.previousIndex() == 2;
        assertEquals("d", list.peek());
        assertEquals("e", list.next());
    }
}
