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
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.comparison.EntryComparator;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public enum NbtHasherProviderImpl implements Internals.NbtHasherProvider {
    INSTANCE;
    private final EntryComparator<DataComponentMap> defaultHasher = _provide();
    
    @Override
    public EntryComparator<DataComponentMap> provide(DataComponentType<?>... ignoredKeys) {
        if (ignoredKeys == null || ignoredKeys.length == 0) return defaultHasher;
        return _provide(ignoredKeys);
    }
    
    private EntryComparator<DataComponentMap> _provide(DataComponentType<?>... ignoredKeys) {
        return new Hasher(ignoredKeys);
    }
    
    private static class Hasher implements EntryComparator<DataComponentMap> {
        @Nullable
        private final Predicate<DataComponentType<?>> filter;
        
        private Hasher(@Nullable DataComponentType<?>[] ignoredKeys) {
            if (ignoredKeys == null || ignoredKeys.length == 0) {
                this.filter = null;
            } else if (ignoredKeys.length == 1) {
                DataComponentType<?> s = ignoredKeys[0];
                this.filter = key -> !Objects.equals(s, key);
            } else {
                Set<DataComponentType<?>> set = new ReferenceOpenHashSet<>(Arrays.asList(ignoredKeys));
                this.filter = Predicates.not(set::contains);
            }
        }
        
        @Override
        public long hash(ComparisonContext context, DataComponentMap value) {
            return this.filter == null && value instanceof PatchedDataComponentMap ? value.hashCode() : hashIgnoringKeys(value);
        }
        
        private long hashIgnoringKeys(DataComponentMap tag) {
            long i = 1L;
            for (TypedDataComponent<?> entry : tag) {
                if (filter.test(entry.type())) {
                    i = i * 31 + (Objects.hashCode(entry.type()) ^ Objects.hashCode(entry.value()));
                }
            }
            return i;
        }
    }
}
