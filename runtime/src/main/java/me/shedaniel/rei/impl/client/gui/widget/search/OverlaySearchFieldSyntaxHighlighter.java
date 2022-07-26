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

import me.shedaniel.rei.impl.client.search.IntRange;
import me.shedaniel.rei.impl.client.search.argument.Argument;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Consumer;

@ApiStatus.Internal
public class OverlaySearchFieldSyntaxHighlighter implements Consumer<String> {
    public byte[] highlighted;
    
    public OverlaySearchFieldSyntaxHighlighter(String text) {
        this.accept(text);
    }
    
    @Override
    public void accept(String text) {
        this.highlighted = new byte[text.length()];
        Argument.bakeArguments(text, new Argument.ProcessedSink() {
            @Override
            public void addQuote(int index) {
                highlighted[index] = -2;
            }
            
            @Override
            public void addSplitter(int index) {
                highlighted[index] = -1;
            }
            
            @Override
            public void addPart(Argument<?, ?> argument, boolean usingGrammar, Collection<IntRange> grammarRanges, int index) {
                if (usingGrammar) {
                    int argIndex = ArgumentTypesRegistry.ARGUMENT_TYPE_LIST.indexOf(argument.getArgument()) * 2 + 1;
                    for (int i = argument.start(); i < argument.end(); i++) {
                        highlighted[i] = (byte) argIndex;
                    }
                    for (IntRange grammarRange : grammarRanges) {
                        for (int i = grammarRange.min(); i <= grammarRange.max(); i++) {
                            highlighted[i + index] = (byte) (argIndex + 1);
                        }
                    }
                }
            }
        });
    }
}
