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

package me.shedaniel.rei.api.common.entry.settings;

import me.shedaniel.rei.api.common.entry.EntryStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface to adapt {@link EntryStack.Settings} for {@link EntryStack}.
 *
 * @param <T> the entry type
 * @param <S> the setting type
 * @see EntryStack.Settings
 */
@FunctionalInterface
@ApiStatus.Experimental
public interface EntrySettingsAdapter<T, S> {
    static <T, S> EntrySettingsAdapter<T, S> empty() {
        return (entry, settings, value) -> value;
    }
    
    /**
     * Modifies the settings of the entry stack.
     *
     * @param entry    the entry stack, do not store or cache this
     * @param settings the settings instance
     * @param value    the value to adapt, could be {@code null}
     * @return the adapted value, could be {@code null}
     */
    @Nullable
    S provide(EntryStack<T> entry, EntryStack.Settings<S> settings, @Nullable S value);
}
