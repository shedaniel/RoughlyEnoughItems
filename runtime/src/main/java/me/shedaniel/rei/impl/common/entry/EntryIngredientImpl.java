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

package me.shedaniel.rei.impl.common.entry;

import com.google.common.collect.Iterators;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.nbt.ListTag;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

public class EntryIngredientImpl {
    public static Internals.EntryIngredientProvider provide() {
        EmptyEntryIngredient empty = new EmptyEntryIngredient();
        return new Internals.EntryIngredientProvider() {
            @Override
            public EntryIngredient empty() {
                return empty;
            }
            
            @Override
            public EntryIngredient of(EntryStack<?> stack) {
                return new SingletonEntryIngredient(stack);
            }
            
            @Override
            public EntryIngredient of(EntryStack<?>... stacks) {
                if (stacks.length == 0) return empty;
                if (stacks.length == 1) return of(stacks[0]);
                return _of(stacks);
            }
            
            @Override
            public EntryIngredient of(Iterable<EntryStack<?>> stacks) {
                if (stacks instanceof EntryIngredient) return (EntryIngredient) stacks;
                if (stacks instanceof Collection) {
                    int size = ((Collection<EntryStack<?>>) stacks).size();
                    if (size == 0) return empty;
                    if (size == 1) return of(stacks.iterator().next());
                    return _of(((Collection<EntryStack<?>>) stacks).toArray(new EntryStack[0]));
                }
                return _of(StreamSupport.stream(stacks.spliterator(), false).toArray(EntryStack[]::new));
            }
            
            private EntryIngredient _of(EntryStack<?>... stacks) {
                return new ArrayIngredient(stacks);
            }
        };
    }
    
    private static class EmptyEntryIngredient extends AbstractList<EntryStack<?>> implements EntryIngredient, RandomAccess {
        @Override
        public Iterator<EntryStack<?>> iterator() {
            return Collections.emptyIterator();
        }
        
        @Override
        public ListIterator<EntryStack<?>> listIterator() {
            return Collections.emptyListIterator();
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public boolean isEmpty() {
            return true;
        }
        
        @Override
        public boolean contains(Object obj) {
            return false;
        }
        
        @Override
        public boolean containsAll(Collection<?> c) {
            return c.isEmpty();
        }
        
        @Override
        public Object[] toArray() {
            return new Object[0];
        }
        
        @Override
        public <T> T[] toArray(T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }
        
        @Override
        public EntryStack<?> get(int index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        
        @Override
        public boolean equals(Object o) {
            return (o instanceof List) && ((List<?>) o).isEmpty();
        }
        
        @Override
        public int hashCode() {
            return 1;
        }
        
        @Override
        public boolean removeIf(Predicate<? super EntryStack<?>> filter) {
            Objects.requireNonNull(filter);
            return false;
        }
        
        @Override
        public void replaceAll(UnaryOperator<EntryStack<?>> operator) {
            Objects.requireNonNull(operator);
        }
        
        @Override
        public void sort(Comparator<? super EntryStack<?>> c) {
        }
        
        @Override
        public void forEach(Consumer<? super EntryStack<?>> action) {
            Objects.requireNonNull(action);
        }
        
        @Override
        public Spliterator<EntryStack<?>> spliterator() {
            return Spliterators.emptySpliterator();
        }
    
        @Override
        public ListTag save() {
            return new ListTag();
        }
    }
    
    private static class SingletonEntryIngredient extends AbstractList<EntryStack<?>> implements EntryIngredient, RandomAccess {
        private EntryStack<?> stack;
        
        public SingletonEntryIngredient(EntryStack<?> stack) {
            this.stack = stack;
        }
        
        @Override
        public Iterator<EntryStack<?>> iterator() {
            return Iterators.singletonIterator(stack);
        }
        
        @Override
        public int size() {
            return 1;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public boolean contains(Object obj) {
            return Objects.equals(obj, stack);
        }
        
        @Override
        public Object[] toArray() {
            return new Object[]{stack};
        }
        
        @Override
        public EntryStack<?> get(int index) {
            if (index != 0)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: 1");
            return stack;
        }
        
        @Override
        public boolean removeIf(Predicate<? super EntryStack<?>> filter) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void replaceAll(UnaryOperator<EntryStack<?>> operator) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void sort(Comparator<? super EntryStack<?>> c) {
        }
        
        @Override
        public void forEach(Consumer<? super EntryStack<?>> action) {
            action.accept(stack);
        }
        
        @Override
        public Spliterator<EntryStack<?>> spliterator() {
            return singletonSpliterator(stack);
        }
        
        static <T> Spliterator<T> singletonSpliterator(final T element) {
            return new Spliterator<T>() {
                long est = 1;
                
                @Override
                public Spliterator<T> trySplit() {
                    return null;
                }
                
                @Override
                public boolean tryAdvance(Consumer<? super T> consumer) {
                    Objects.requireNonNull(consumer);
                    if (est > 0) {
                        est--;
                        consumer.accept(element);
                        return true;
                    }
                    return false;
                }
                
                @Override
                public void forEachRemaining(Consumer<? super T> consumer) {
                    tryAdvance(consumer);
                }
                
                @Override
                public long estimateSize() {
                    return est;
                }
                
                @Override
                public int characteristics() {
                    int value = (element != null) ? Spliterator.NONNULL : 0;
                    
                    return value | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE |
                           Spliterator.DISTINCT | Spliterator.ORDERED;
                }
            };
        }
    
        @Override
        public ListTag save() {
            ListTag listTag = new ListTag();
            listTag.add(stack.save());
            return listTag;
        }
    }
    
    private static class ArrayIngredient extends AbstractList<EntryStack<?>> implements EntryIngredient, RandomAccess {
        private static final long serialVersionUID = -2764017481108945198L;
        private final EntryStack<?>[] array;
        
        ArrayIngredient(EntryStack<?>[] array) {
            this.array = Objects.requireNonNull(array);
        }
        
        @Override
        public int size() {
            return array.length;
        }
        
        @Override
        public Object[] toArray() {
            return toArray(new Object[0]);
        }
        
        @Override
        public <T> T[] toArray(T[] a) {
            int size = size();
            if (a.length < size) {
                return Arrays.copyOf(this.array, size, (Class<? extends T[]>) a.getClass());
            }
            System.arraycopy(this.array, 0, a, 0, size);
            if (a.length > size) {
                a[size] = null;
            }
            return a;
        }
        
        @Override
        public EntryStack<?> get(int index) {
            return array[index];
        }
        
        @Override
        public EntryStack<?> set(int index, EntryStack<?> element) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int indexOf(Object o) {
            EntryStack<?>[] a = this.array;
            if (o == null) {
                for (int i = 0; i < a.length; i++)
                    if (a[i] == null)
                        return i;
            } else {
                for (int i = 0; i < a.length; i++)
                    if (o.equals(a[i]))
                        return i;
            }
            return -1;
        }
        
        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }
        
        @Override
        public Spliterator<EntryStack<?>> spliterator() {
            return Spliterators.spliterator(array, Spliterator.ORDERED | Spliterator.IMMUTABLE);
        }
        
        @Override
        public void forEach(Consumer<? super EntryStack<?>> action) {
            Objects.requireNonNull(action);
            for (EntryStack<?> stack : array) {
                action.accept(stack);
            }
        }
        
        @Override
        public void replaceAll(UnaryOperator<EntryStack<?>> operator) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void sort(Comparator<? super EntryStack<?>> c) {
            throw new UnsupportedOperationException();
        }
    
        @Override
        public ListTag save() {
            ListTag listTag = new ListTag();
            for (EntryStack<?> stack : array) {
                listTag.add(stack.save());
            }
            return listTag;
        }
    }
}
