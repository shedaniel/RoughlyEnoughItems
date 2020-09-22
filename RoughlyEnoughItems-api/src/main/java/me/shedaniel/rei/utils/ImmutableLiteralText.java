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

import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ImmutableLiteralText implements IFormattableTextComponent {
    public static final ImmutableLiteralText EMPTY = new ImmutableLiteralText("");
    private final String content;
    private IReorderingProcessor orderedText;
    
    public ImmutableLiteralText(String content) {
        this.content = content;
    }
    
    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }
    
    @Override
    public String getContents() {
        return content;
    }
    
    @Override
    public List<ITextComponent> getSiblings() {
        return Collections.emptyList();
    }
    
    @Override
    public IFormattableTextComponent plainCopy() {
        return this;
    }
    
    @Override
    public IFormattableTextComponent copy() {
        return plainCopy();
    }
    
    @Override
    public <T> Optional<T> visit(ITextAcceptor<T> visitor) {
        return visitSelf(visitor);
    }
    
    @Override
    public <T> Optional<T> visit(IStyledTextAcceptor<T> styledVisitor, Style style) {
        return visitSelf(styledVisitor, style);
    }
    
    @Override
    public IReorderingProcessor getVisualOrderText() {
        if (orderedText == null) {
            orderedText = LanguageMap.getInstance().getVisualOrder(this);
        }
        return orderedText;
    }
    
    @Override
    public IFormattableTextComponent setStyle(Style style) {
        return new StringTextComponent(content).withStyle(style);
    }
    
    @Override
    public IFormattableTextComponent append(ITextComponent component) {
        return new StringTextComponent(content).append(component);
    }
}
