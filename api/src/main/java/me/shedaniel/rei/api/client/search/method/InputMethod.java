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

package me.shedaniel.rei.api.client.search.method;

import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ApiStatus.Experimental
public interface InputMethod<T> {
    static InputMethod<?> active() {
        return InputMethodRegistry.getInstance().getOrDefault(ConfigObject.getInstance().getInputMethodId());
    }
    
    static List<Locale> getAllLocales() {
        return CollectionUtils.map(Minecraft.getInstance().getLanguageManager().getLanguages(), info ->
                new Locale(info.getCode(), Component.literal(info.getName())));
    }
    
    List<Locale> getMatchingLocales();
    
    Iterable<T> expendFilter(String filter);
    
    boolean contains(String str, T substr);
    
    @Nullable
    default String suggestInputString(String str) {
        return null;
    }
    
    CompletableFuture<Void> prepare(Executor executor);
    
    CompletableFuture<Void> dispose(Executor executor);
    
    Component getName();
    
    Component getDescription();
    
    record Locale(String code, Component name) {}
}
