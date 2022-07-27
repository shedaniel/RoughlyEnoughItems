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

package me.shedaniel.rei.impl.client.gui.widget.search;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Consumer;

@ApiStatus.Internal
public class OverlaySearchFieldSyntaxHighlighter implements Consumer<String> {
    public HighlightInfo[] highlighted;
    
    public OverlaySearchFieldSyntaxHighlighter(String text) {
        this.accept(text);
    }
    
    @Override
    public void accept(String text) {
        this.highlighted = new HighlightInfo[text.length()];
        SearchProvider.getInstance().createFilter(text).processDecoration(new SearchFilter.ParseDecorationSink() {
            @Override
            public void addQuote(int index) {
                highlighted[index] = QuoteHighlightInfo.INSTANCE;
            }
            
            @Override
            public void addSplitter(int index) {
                highlighted[index] = SplitterHighlightInfo.INSTANCE;
            }
            
            @Override
            public void addPart(IntIntPair range, Style style, boolean usingGrammar, Collection<IntIntPair> grammarRanges, int index) {
                if (usingGrammar) {
                    PartHighlightInfo base = new PartHighlightInfo(style, false);
                    PartHighlightInfo grammar = new PartHighlightInfo(style, true);
                    for (int i = range.leftInt(); i < range.rightInt(); i++) {
                        highlighted[i] = base;
                    }
                    for (IntIntPair grammarRange : grammarRanges) {
                        for (int i = grammarRange.leftInt(); i <= grammarRange.rightInt(); i++) {
                            highlighted[i + index] = grammar;
                        }
                    }
                }
            }
        });
    }
    
    public sealed interface HighlightInfo {
    }
    
    public record PartHighlightInfo(Style style, boolean grammar) implements HighlightInfo {}
    
    public enum QuoteHighlightInfo implements HighlightInfo {
        INSTANCE,
    }
    
    public enum SplitterHighlightInfo implements HighlightInfo {
        INSTANCE,
    }
}
