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

package me.shedaniel.rei.plugin.common.displays.tag;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ApiStatus.Internal
public abstract class TagNode<T> {
    private final List<TagNode<T>> children;
    
    public TagNode() {
        this.children = new ArrayList<>();
    }
    
    public static <T> TagNode<T> ofValue(Holder<T> value) {
        return new ValueTagNode<>(value);
    }
    
    public static <T> TagNode<T> ofReference(TagKey<T> key) {
        return new ReferenceTagNode<>(key);
    }
    
    public List<TagNode<T>> children() {
        return children;
    }
    
    public void addChild(TagNode<T> child) {
        children.add(child);
    }
    
    public void addValueChild(Holder<T> child) {
        children.add(ofValue(child));
    }
    
    public void addReferenceChild(TagKey<T> child) {
        children.add(ofReference(child));
    }
    
    public String asTree() {
        StringBuilder buffer = new StringBuilder(50);
        printTree(buffer, "", "");
        return buffer.toString();
    }
    
    private void printTree(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(asText());
        buffer.append('\n');
        for (Iterator<TagNode<T>> it = children.iterator(); it.hasNext(); ) {
            TagNode<T> next = it.next();
            if (it.hasNext()) {
                next.printTree(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.printTree(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
    
    protected abstract String asText();
    
    @Nullable
    public Holder<T> getValue() {
        return null;
    }
    
    @Nullable
    public TagKey<T> getReference() {
        return null;
    }
    
    private static class ValueTagNode<T> extends TagNode<T> {
        private final Holder<T> value;
        
        public ValueTagNode(Holder<T> value) {
            this.value = value;
        }
        
        @Override
        public Holder<T> getValue() {
            return value;
        }
        
        @Override
        protected String asText() {
            return value.unwrapKey().map(ResourceKey::location).orElse(null) + "";
        }
    }
    
    private static class ReferenceTagNode<T> extends TagNode<T> {
        private final TagKey<T> key;
        
        public ReferenceTagNode(TagKey<T> key) {
            this.key = key;
        }
        
        @Override
        public TagKey<T> getReference() {
            return key;
        }
        
        @Override
        protected String asText() {
            return key.location() + "";
        }
    }
}
