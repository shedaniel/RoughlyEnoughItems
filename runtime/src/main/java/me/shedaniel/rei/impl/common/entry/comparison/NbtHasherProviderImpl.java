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

package me.shedaniel.rei.impl.common.entry.comparison;

import com.google.common.base.Predicates;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public enum NbtHasherProviderImpl implements Internals.NbtHasherProvider {
    INSTANCE;
    private final EntryComparator<Tag> defaultHasher = _provide();
    
    @Override
    public EntryComparator<Tag> provide(String... ignoredKeys) {
        if (ignoredKeys == null || ignoredKeys.length == 0) return defaultHasher;
        return _provide(ignoredKeys);
    }
    
    private EntryComparator<Tag> _provide(String... ignoredKeys) {
        return new Hasher(ignoredKeys);
    }
    
    private static class Hasher implements EntryComparator<Tag> {
        private final Predicate<String> filter;
        
        private Hasher(@Nullable String[] ignoredKeys) {
            if (ignoredKeys == null || ignoredKeys.length == 0) {
                this.filter = key -> true;
            } else if (ignoredKeys.length == 1) {
                String s = ignoredKeys[0];
                this.filter = key -> !Objects.equals(s, key);
            } else {
                Set<String> set = new HashSet<>(Arrays.asList(ignoredKeys));
                this.filter = Predicates.not(set::contains);
            }
        }
        
        private boolean shouldHash(String key) {
            return filter.test(key);
        }
        
        @Override
        public long hash(ComparisonContext context, Tag value) {
            return hashTag(value);
        }
        
        private int hashTag(Tag tag) {
            if (tag == null) return 0;
            if (tag instanceof ListTag list) return hashListTag(list);
            if (tag instanceof CompoundTag compound) return hashCompoundTag(compound);
            return tag.hashCode();
        }
        
        private int hashListTag(ListTag tag) {
            int i = tag.size();
            for (Tag innerTag : tag) {
                i = i * 31 + hashTag(innerTag);
            }
            return i;
        }
        
        private int hashCompoundTag(CompoundTag tag) {
            int i = 1;
            for (Map.Entry<String, Tag> entry : tag.tags.entrySet()) {
                if (shouldHash(entry.getKey())) {
                    i = i * 31 + (Objects.hashCode(entry.getKey()) ^ hashTag(entry.getValue()));
                }
            }
            return i;
        }
    }
}
