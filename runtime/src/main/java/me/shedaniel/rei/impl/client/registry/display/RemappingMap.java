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

package me.shedaniel.rei.impl.client.registry.display;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

class RemappingMap<K, V> extends ForwardingMap<K, V> {
    protected final Map<K, V> map;
    protected final UnaryOperator<V> remapper;
    protected final Predicate<K> keyPredicate;
    
    public RemappingMap(Map<K, V> map, UnaryOperator<V> remapper, Predicate<K> keyPredicate) {
        this.map = map;
        this.remapper = remapper;
        this.keyPredicate = keyPredicate;
    }
    
    @Override
    @NotNull
    protected Map<K, V> delegate() {
        return map;
    }
    
    @Override
    public V get(Object key) {
        if (keyPredicate.test((K) key)) {
            return remapper.apply(super.get(key));
        } else {
            return null;
        }
    }
    
    @Override
    public boolean containsKey(@Nullable Object key) {
        return super.containsKey(key) && keyPredicate.test((K) key);
    }
    
    @Override
    public Set<K> keySet() {
        return Sets.filter(super.keySet(), keyPredicate::test);
    }
    
    @SuppressWarnings("UnstableApiUsage")
    @Override
    @NotNull
    public Set<Entry<K, V>> entrySet() {
        return this.new StandardEntrySet() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return mapIterator(map.entrySet().iterator());
            }
        };
    }
    
    @Override
    public int size() {
        return keySet().size();
    }
    
    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return Iterators.transform(entrySet().iterator(), Entry::getValue);
            }
            
            @Override
            public int size() {
                return RemappingMap.this.size();
            }
        };
    }
    
    private Iterator<Entry<K, V>> mapIterator(Iterator<Entry<K, V>> iterator) {
        return Iterators.transform(Iterators.filter(iterator, entry -> this.keyPredicate.test(entry.getKey())),
                this::mapEntry);
    }
    
    private Entry<K, V> mapEntry(Entry<K, V> entry) {
        return new ForwardingMapEntry<>() {
            @Override
            @NotNull
            protected Entry<K, V> delegate() {
                return entry;
            }
            
            @Override
            public V getValue() {
                return remapper.apply(entry.getValue());
            }
        };
    }
}