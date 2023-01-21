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

package me.shedaniel.rei.plugin.common.displays.tag;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
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
    
    public static <T> TagNode<T> ofValues(HolderSet<T> value) {
        return new ValuesTagNode<>(value);
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
    
    public void addValuesChild(HolderSet<T> child) {
        children.add(ofValues(child));
    }
    
    public void addReferenceChild(TagKey<T> child) {
        children.add(ofReference(child));
    }
    
    public String asTree() {
        StringBuilder builder = new StringBuilder(50);
        printTree(builder, "", "");
        return builder.toString();
    }
    
    private void printTree(StringBuilder builder, String prefix, String childrenPrefix) {
        asText(prefix, builder);
        for (Iterator<TagNode<T>> it = children.iterator(); it.hasNext(); ) {
            TagNode<T> next = it.next();
            if (it.hasNext()) {
                next.printTree(builder, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.printTree(builder, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
    
    protected abstract void asText(String prefix, StringBuilder builder);
    
    @Nullable
    public HolderSet<T> getValue() {
        return null;
    }
    
    @Nullable
    public TagKey<T> getReference() {
        return null;
    }
    
    private static class ValuesTagNode<T> extends TagNode<T> {
        private final HolderSet<T> value;
        
        public ValuesTagNode(HolderSet<T> value) {
            this.value = value;
        }
        
        @Override
        public HolderSet<T> getValue() {
            return value;
        }
        
        @Override
        protected void asText(String prefix, StringBuilder builder) {
            for (Holder<T> holder : value) {
                holder.unwrapKey().ifPresent(key -> {
                    builder.append(prefix);
                    builder.append(key.location().toString());
                    builder.append('\n');
                });
            }
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
        protected void asText(String prefix, StringBuilder builder) {
            builder.append(prefix);
            builder.append(key.location());
            builder.append('\n');
        }
    }
}
