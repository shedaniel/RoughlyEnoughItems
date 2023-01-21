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

package me.shedaniel.rei.api.common.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A simple lightweight implementation of {@link Component} that holds a style-less text.
 */
public final class ImmutableTextComponent implements MutableComponent {
    public static final ImmutableTextComponent EMPTY = new ImmutableTextComponent("");
    private final String content;
    @Environment(EnvType.CLIENT)
    private FormattedCharSequence visualOrderText;
    
    public ImmutableTextComponent(String content) {
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
    public List<Component> getSiblings() {
        return Collections.emptyList();
    }
    
    @Override
    public MutableComponent plainCopy() {
        return this;
    }
    
    @Override
    public MutableComponent copy() {
        return plainCopy();
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public <T> Optional<T> visit(ContentConsumer<T> visitor) {
        return visitSelf(visitor);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public <T> Optional<T> visit(StyledContentConsumer<T> styledVisitor, Style style) {
        return visitSelf(styledVisitor, style);
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public FormattedCharSequence getVisualOrderText() {
        if (visualOrderText == null) {
            visualOrderText = Language.getInstance().getVisualOrder(this);
        }
        return visualOrderText;
    }
    
    @Override
    public MutableComponent setStyle(Style style) {
        return new TextComponent(content).withStyle(style);
    }
    
    @Override
    public MutableComponent append(Component component) {
        return new TextComponent(content).append(component);
    }
}
