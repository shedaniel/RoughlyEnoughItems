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

import com.google.common.collect.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionUtils {
    public static <A, B> List<B> getOrPutEmptyList(Map<A, List<B>> map, A key) {
        List<B> b = map.get(key);
        if (b != null) {
            return b;
        }
        map.put(key, new ArrayList<>());
        return map.get(key);
    }
    
    public static <T> T findFirstOrNullEquals(Iterable<T> list, T obj) {
        for (T t : list) {
            if (t.equals(obj)) {
                return t;
            }
        }
        return null;
    }
    
    public static <T, R> List<R> castAndMap(Iterable<T> list, Class<R> castClass) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            if (castClass.isAssignableFrom(t.getClass())) {
                l.add((R) t);
            }
        }
        return l;
    }
    
    public static <T> T findFirstOrNull(Iterable<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }
    
    public static <T> boolean allMatch(Iterable<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }
    
    public static <T> boolean anyMatch(Iterable<T> list, Predicate<T> predicate) {
        return findFirstOrNull(list, predicate) != null;
    }
    
    public static EntryStack<?> findFirstOrNullEqualsExact(Iterable<? extends EntryStack<?>> list, EntryStack<?> stack) {
        for (EntryStack<?> t : list) {
            if (EntryStacks.equalsExact(t, stack))
                return t;
        }
        return null;
    }
    
    public static <T> List<T> filterToList(Iterable<T> list, Predicate<T> predicate) {
        List<T> l = Lists.newArrayList();
        for (T t : list) {
            if (predicate.test(t)) {
                l.add(t);
            }
        }
        return l;
    }
    
    public static <T> Set<T> filterToSet(Iterable<T> list, Predicate<T> predicate) {
        Set<T> l = Sets.newLinkedHashSet();
        for (T t : list) {
            if (predicate.test(t)) {
                l.add(t);
            }
        }
        return l;
    }
    
    public static <T, R> List<R> map(Collection<T> list, Function<T, R> function) {
        List<R> l = new ArrayList<>(list.size() + 1);
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> List<R> map(Iterable<T> list, Function<T, R> function) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> Set<R> mapToSet(Collection<T> list, Function<T, R> function) {
        Set<R> l = new HashSet<>(list.size() + 1);
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> Set<R> mapToSet(Iterable<T> list, Function<T, R> function) {
        Set<R> l = new HashSet<>();
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> List<R> mapIndexed(Iterable<T> list, IndexedFunction<T, R> function) {
        List<R> l = list instanceof Collection ? new ArrayList<>(((Collection<T>) list).size() + 1) : new ArrayList<>();
        int i = 0;
        for (T t : list) {
            l.add(function.apply(i++, t));
        }
        return l;
    }
    
    @FunctionalInterface
    public interface IndexedFunction<T, R> {
        R apply(int index, T object);
    }
    
    public static <T, R> List<R> flatMap(Iterable<T> list, Function<T, Collection<R>> function) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            l.addAll(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> List<R> flatMap(T[] list, Function<T, Collection<R>> function) {
        List<R> l = new ArrayList<>();
        for (T t : list) {
            l.addAll(function.apply(t));
        }
        return l;
    }
    
    public static <T> IntList mapToInt(Collection<T> list, ToIntFunction<T> function) {
        IntList l = new IntArrayList(list.size() + 1);
        for (T t : list) {
            l.add(function.applyAsInt(t));
        }
        return l;
    }
    
    public static <T, R> List<R> mapParallel(Collection<T> list, Function<T, R> function) {
        return list.parallelStream().map(function).collect(Collectors.toList());
    }
    
    public static <T, R, C extends Collection<R>> C mapParallel(Collection<T> list, Function<T, R> function, Supplier<C> supplier) {
        return list.parallelStream().map(function).collect(Collectors.toCollection(supplier));
    }
    
    public static <T, R, C extends Collection<R>> C filterAndMapParallel(Collection<T> list, Predicate<T> filter, Function<T, R> function, Supplier<C> supplier) {
        return list.parallelStream().filter(filter).map(function).collect(Collectors.toCollection(supplier));
    }
    
    public static <T, R> List<R> map(T[] list, Function<T, R> function) {
        List<R> l = new ArrayList<>(list.length + 1);
        for (T t : list) {
            l.add(function.apply(t));
        }
        return l;
    }
    
    public static <T, R> Optional<R> mapAndMax(Collection<T> list, Function<T, R> function, Comparator<R> comparator) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return list.stream().max(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T, R> Optional<R> mapAndMax(T[] list, Function<T, R> function, Comparator<R> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        return Stream.of(list).max(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T> Optional<T> max(Collection<T> list, Comparator<T> comparator) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return list.stream().max(comparator);
    }
    
    public static <T> Optional<T> max(T[] list, Comparator<T> comparator) {
        if (list.length <= 0) {
            return Optional.empty();
        }
        return Stream.of(list).max(comparator);
    }
    
    public static <T, R> Optional<R> mapAndMin(Collection<T> list, Function<T, R> function, Comparator<R> comparator) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return list.stream().min(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T, R> Optional<R> mapAndMin(T[] list, Function<T, R> function, Comparator<R> comparator) {
        if (list.length <= 0)
            return Optional.empty();
        return Stream.of(list).min(Comparator.comparing(function, comparator)).map(function);
    }
    
    public static <T> Optional<T> min(Collection<T> list, Comparator<T> comparator) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return list.stream().min(comparator);
    }
    
    public static <T> Optional<T> min(T[] list, Comparator<T> comparator) {
        if (list.length <= 0) {
            return Optional.empty();
        }
        return Stream.of(list).min(comparator);
    }
    
    public static String joinToString(Iterable<CharSequence> list, CharSequence separator) {
        return String.join(separator, list);
    }
    
    public static String joinToString(CharSequence[] list, CharSequence separator) {
        return String.join(separator, list);
    }
    
    public static <T> String mapAndJoinToString(Iterable<T> list, Function<T, CharSequence> function, CharSequence separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static <T> String mapAndJoinToString(T[] list, Function<T, CharSequence> function, CharSequence separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (T t : list) {
            joiner.add(function.apply(t));
        }
        return joiner.toString();
    }
    
    public static <T> Component mapAndJoinToComponent(Iterable<T> list, Function<T, Component> function, Component separator) {
        TextComponent joiner = new TextComponent("");
        boolean first = true;
        for (T t : list) {
            if (first) {
                first = false;
            } else {
                joiner.append(separator.copy());
            }
            joiner.append(function.apply(t));
        }
        return joiner;
    }
    
    public static <T> Component mapAndJoinToComponent(T[] list, Function<T, Component> function, Component separator) {
        TextComponent joiner = new TextComponent("");
        boolean first = true;
        for (T t : list) {
            if (first) {
                first = false;
            } else {
                joiner.append(separator.copy());
            }
            joiner.append(function.apply(t));
        }
        return joiner;
    }
    
    public static <T, R> List<R> filterAndMap(Iterable<T> list, Predicate<T> predicate, Function<T, R> function) {
        List<R> l = null;
        for (T t : list) {
            if (predicate.test(t)) {
                if (l == null)
                    l = Lists.newArrayList();
                l.add(function.apply(t));
            }
        }
        return l == null ? Collections.emptyList() : l;
    }
    
    public static <T, R> List<R> mapAndFilter(Iterable<T> list, Predicate<R> predicate, Function<T, R> function) {
        List<R> l = null;
        for (T t : list) {
            R r = function.apply(t);
            if (predicate.test(r)) {
                if (l == null)
                    l = Lists.newArrayList();
                l.add(r);
            }
        }
        return l == null ? Collections.emptyList() : l;
    }
    
    public static <T> int sumInt(Iterable<T> list, Function<T, Integer> function) {
        int sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static <T> int sumInt(Iterable<Integer> list) {
        int sum = 0;
        for (int t : list) {
            sum += t;
        }
        return sum;
    }
    
    public static <T> double sumDouble(Iterable<T> list, Function<T, Double> function) {
        double sum = 0;
        for (T t : list) {
            sum += function.apply(t);
        }
        return sum;
    }
    
    public static <T> double sumDouble(Iterable<Double> list) {
        double sum = 0;
        for (double t : list) {
            sum += t;
        }
        return sum;
    }
    
    public static <T> List<T> distinctToList(Iterable<T> list) {
        List<T> newList = new ArrayList<>();
        Set<T> set = new HashSet<>();
        for (T t : list) {
            if (set.add(t)) {
                newList.add(t);
            }
        }
        return newList;
    }
    
    public static <T> Iterable<List<T>> partition(List<T> list, int size) {
        return () -> new UnmodifiableIterator<>() {
            int i = 0;
            int partitionSize = Mth.ceil(list.size() / (float) size);
            
            @Override
            public boolean hasNext() {
                return i < partitionSize;
            }
            
            @Override
            public List<T> next() {
                int cursor = i++ * size;
                int realSize = Math.min(list.size() - cursor, size);
                return new AbstractList<>() {
                    @Override
                    public T get(int index) {
                        if (index < 0 || index >= realSize)
                            return null;
                        return list.get(cursor + index);
                    }
                    
                    @Override
                    public int size() {
                        return realSize;
                    }
                    
                    @Override
                    public Iterator<T> iterator() {
                        Iterator<T> iterator = super.iterator();
                        return new Iterator<T>() {
                            boolean endReached = false;
                            
                            @Override
                            public boolean hasNext() {
                                return iterator.hasNext() && !endReached;
                            }
                            
                            @Override
                            public T next() {
                                try {
                                    return iterator.next();
                                } catch (NoSuchElementException e) {
                                    endReached = true;
                                    return null;
                                }
                            }
                        };
                    }
                };
            }
        };
    }
    
    public static <T> Iterable<Iterator<T>> partitionIterator(Iterator<T> iterator, int iteratorSize, int size) {
        return partitionCollection(new AbstractCollection<>() {
            
            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
            
            @Override
            public int size() {
                return iteratorSize;
            }
        }, size);
    }
    
    public static <T> Iterable<Iterator<T>> partitionCollection(Collection<T> collection, int size) {
        if (collection instanceof List) {
            return Iterables.transform(partition((List<T>) collection, size), List::iterator);
        }
        
        return () -> new Iterator<>() {
            int i = 0;
            int partitionSize = Mth.ceil(collection.size() / (float) size);
            int advanced = 0;
            Iterator<T> iterator = collection.iterator();
            
            @Override
            public boolean hasNext() {
                return i < partitionSize;
            }
            
            @Override
            public Iterator<T> next() {
                int cursor = i++ * size;
                int realSize = Math.min(collection.size() - cursor, size);
                
                if (advanced < cursor) {
                    for (int j = 0; j < cursor - advanced; j++) {
                        if (iterator.hasNext()) {
                            iterator.next();
                        } else {
                            advanced = cursor;
                            return Collections.emptyIterator();
                        }
                    }
                    advanced = cursor;
                }
                
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext() && advanced < cursor + realSize;
                    }
                    
                    @Override
                    public T next() {
                        advanced++;
                        return iterator.next();
                    }
                };
            }
        };
    }
    
    public static Ingredient toIngredient(ItemStack stack) {
        return Ingredient.of(Stream.of(stack));
    }
    
    public static Ingredient toIngredient(Iterable<ItemStack> stacks) {
        return Ingredient.of(StreamSupport.stream(stacks.spliterator(), false));
    }
    
    @SafeVarargs
    public static <T> List<T> concatUnmodifiable(List<? extends T>... lists) {
        return new ListConcatenationView<>(Arrays.asList(lists));
    }
    
    public static <T> List<T> concatUnmodifiable(Iterable<List<? extends T>> lists) {
        return new ListConcatenationView<>(lists);
    }
    
    /**
     * A list which acts as view of the concatenation of a number of lists.
     */
    private static class ListConcatenationView<E> extends AbstractList<E> {
        private final Iterable<List<? extends E>> lists;
        
        public ListConcatenationView(Iterable<List<? extends E>> lists) {
            this.lists = lists;
        }
        
        @Override
        public E get(int ix) {
            int localIx = ix;
            for (List<? extends E> l : lists) {
                if (localIx < 0) throw new IndexOutOfBoundsException(ix);
                if (localIx < l.size()) return l.get(localIx);
                localIx -= l.size();
            }
            return null;
        }
        
        @Override
        public int size() {
            int size = 0;
            for (List<? extends E> l : lists) {
                size += l.size();
            }
            return size;
        }
    }
    
    public static <T> Iterable<T> distinctReferenceOf(Iterable<T> iterable) {
        return () -> new AbstractIterator<T>() {
            private final Set<T> set = new ReferenceOpenHashSet<>();
            private final Iterator<T> iterator = iterable.iterator();
            
            @Override
            protected T computeNext() {
                while (iterator.hasNext()) {
                    T next = iterator.next();
                    if (set.add(next)) {
                        return next;
                    }
                }
                
                return endOfData();
            }
        };
    }
}
