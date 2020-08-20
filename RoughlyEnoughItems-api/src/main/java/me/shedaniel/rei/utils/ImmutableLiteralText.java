/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.utils;

import net.minecraft.text.*;
import net.minecraft.util.Language;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ImmutableLiteralText implements Text {
    public static final ImmutableLiteralText EMPTY = new ImmutableLiteralText("");
    private final String content;
    private OrderedText orderedText;
    
    public ImmutableLiteralText(String content) {
        this.content = content;
    }
    
    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }
    
    @Override
    public String asString() {
        return content;
    }
    
    @Override
    public List<Text> getSiblings() {
        return Collections.emptyList();
    }
    
    @Override
    public MutableText copy() {
        return new LiteralText(content);
    }
    
    @Override
    public MutableText shallowCopy() {
        return copy();
    }
    
    @Override
    public <T> Optional<T> visit(Visitor<T> visitor) {
        return visitSelf(visitor);
    }
    
    @Override
    public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style) {
        return visitSelf(styledVisitor, style);
    }
    
    @Override
    public OrderedText asOrderedText() {
        if (orderedText != null) {
            orderedText = Language.getInstance().reorder(this);
        }
        return orderedText;
    }
}
