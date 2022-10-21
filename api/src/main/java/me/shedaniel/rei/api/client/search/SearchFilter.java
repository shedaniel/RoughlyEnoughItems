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

package me.shedaniel.rei.api.client.search;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * A search filter that respects different prefixes, matches a {@link EntryStack}.
 *
 * @see SearchProvider
 */
@Environment(EnvType.CLIENT)
public interface SearchFilter extends Predicate<EntryStack<?>> {
    static SearchFilter matchAll() {
        return new SearchFilter() {
            @Override
            public String getFilter() {
                return "";
            }
            
            @Override
            public boolean test(EntryStack<?> entryStack) {
                return true;
            }
        };
    }
    
    static SearchFilter matchNone() {
        return new SearchFilter() {
            @Override
            public String getFilter() {
                return "";
            }
            
            @Override
            public boolean test(EntryStack<?> entryStack) {
                return false;
            }
        };
    }
    
    /**
     * Returns the original filter in {@link String}.
     *
     * @return the original filter
     */
    String getFilter();
    
    /**
     * Prepares the following stacks for matching, this could help to speed up the matching process.<br>
     * However, this is not required.
     *
     * @param stacks the stacks to prepare
     */
    default void prepareFilter(Collection<EntryStack<?>> stacks) {
    }
    
    /**
     * Processes the decoration of the search filter.
     *
     * @param sink the decoration sink
     */
    @ApiStatus.Experimental
    default void processDecoration(ParseDecorationSink sink) {
    }
    
    interface ParseDecorationSink {
        void addQuote(int index);
        
        void addSplitter(int index);
        
        void addPart(IntIntPair range, Style style, boolean usingGrammar, Collection<IntIntPair> grammarRanges, int index);
    }
}
