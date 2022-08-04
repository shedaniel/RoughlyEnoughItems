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

package me.shedaniel.rei.impl.client.gui.widget.basewidgets;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

interface ForwardingList<E> extends List<E> {
    @Override
    default int size() {
        return delegate().size();
    }
    
    @Override
    default boolean isEmpty() {
        return delegate().isEmpty();
    }
    
    @Override
    default boolean contains(Object o) {
        return delegate().contains(o);
    }
    
    @NotNull
    @Override
    default Iterator<E> iterator() {
        return delegate().iterator();
    }
    
    @NotNull
    @Override
    default Object[] toArray() {
        return delegate().toArray();
    }
    
    @NotNull
    @Override
    default <T> T[] toArray(@NotNull T[] a) {
        return delegate().toArray(a);
    }
    
    @Override
    default boolean add(E e) {
        return delegate().add(e);
    }
    
    @Override
    default boolean remove(Object o) {
        return delegate().remove(o);
    }
    
    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        return delegate().containsAll(c);
    }
    
    @Override
    default boolean addAll(@NotNull Collection<? extends E> c) {
        return delegate().addAll(c);
    }
    
    @Override
    default boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return delegate().addAll(index, c);
    }
    
    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return delegate().removeAll(c);
    }
    
    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return delegate().retainAll(c);
    }
    
    @Override
    default void clear() {
        delegate().clear();
    }
    
    @Override
    default boolean equals(Object o) {
        return delegate().equals(o);
    }
    
    @Override
    default int hashCode() {
        return delegate().hashCode();
    }
    
    @Override
    default E get(int index) {
        return delegate().get(index);
    }
    
    @Override
    default E set(int index, E element) {
        return delegate().set(index, element);
    }
    
    @Override
    default void add(int index, E element) {
        delegate().add(index, element);
    }
    
    @Override
    default E remove(int index) {
        return delegate().remove(index);
    }
    
    @Override
    default int indexOf(Object o) {
        return delegate().indexOf(o);
    }
    
    @Override
    default int lastIndexOf(Object o) {
        return delegate().lastIndexOf(o);
    }
    
    @NotNull
    @Override
    default ListIterator<E> listIterator() {
        return delegate().listIterator();
    }
    
    @NotNull
    @Override
    default ListIterator<E> listIterator(int index) {
        return delegate().listIterator(index);
    }
    
    @NotNull
    @Override
    default List<E> subList(int fromIndex, int toIndex) {
        return delegate().subList(fromIndex, toIndex);
    }
    
    List<E> delegate();
}
