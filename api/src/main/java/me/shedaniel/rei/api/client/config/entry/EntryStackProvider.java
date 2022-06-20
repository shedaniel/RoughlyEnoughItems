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

package me.shedaniel.rei.api.client.config.entry;

import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface EntryStackProvider<T> {
    EntryStack<T> provide();
    
    CompoundTag save();
    
    boolean isValid();
    
    static <T> EntryStackProvider<T> defer(CompoundTag tag) {
        return new EntryStackProvider<T>() {
            private EntryStack<T> stack;
            
            @Override
            public EntryStack<T> provide() {
                if (stack == null) {
                    try {
                        stack = (EntryStack<T>) EntryStack.read(tag);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return (EntryStack<T>) EntryStack.empty();
                    }
                    
                    stack = stack.normalize();
                }
                
                return stack;
            }
            
            @Override
            public CompoundTag save() {
                return tag.copy();
            }
            
            @Override
            public boolean isValid() {
                return !provide().isEmpty();
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof EntryStackProvider)) return false;
                EntryStackProvider<?> that = (EntryStackProvider<?>) o;
                return Objects.equals(provide(), that.provide());
            }
            
            @Override
            public int hashCode() {
                return Long.hashCode(EntryStacks.hashExact(provide()));
            }
        };
    }
    
    static <T> EntryStackProvider<T> ofStack(EntryStack<T> stack) {
        stack = stack.normalize();
        EntryStack<T> finalStack = stack;
        return new EntryStackProvider<T>() {
            @Override
            public EntryStack<T> provide() {
                return finalStack;
            }
            
            @Override
            public CompoundTag save() {
                return finalStack.saveStack();
            }
            
            @Override
            public boolean isValid() {
                return !finalStack.isEmpty();
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof EntryStackProvider)) return false;
                EntryStackProvider<?> that = (EntryStackProvider<?>) o;
                return Objects.equals(provide(), that.provide());
            }
            
            @Override
            public int hashCode() {
                return Long.hashCode(EntryStacks.hashExact(provide()));
            }
        };
    }
}
