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
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * An input method to match a search filter to another source input.
 *
 * @param <T> the type of expansion from the search filter
 * @see me.shedaniel.rei.impl.client.search.method.DefaultInputMethod
 */
@ApiStatus.Experimental
public interface InputMethod<T> {
    /**
     * Returns the active input method.
     *
     * @return the active input method
     */
    static InputMethod<?> active() {
        return InputMethodRegistry.getInstance().getOrDefault(ConfigObject.getInstance().getInputMethodId());
    }
    
    /**
     * Returns the list of all Minecraft supported locales.
     *
     * @return the list of all Minecraft supported locales
     */
    static List<Locale> getAllLocales() {
        return CollectionUtils.map(Minecraft.getInstance().getLanguageManager().getLanguages(), info ->
                new Locale(info.getCode(), Component.literal(info.getName())));
    }
    
    /**
     * Returns the list of locales that are supported by this input method.
     * You should use {@link #getAllLocales()} to get the list of all Minecraft supported locales.
     *
     * @return the list of locales that are supported by this input method
     */
    List<Locale> getMatchingLocales();
    
    /**
     * Returns all the possible expansions from the search filter.
     *
     * @param filter the search filter
     * @return all the possible expansions from the search filter
     */
    Iterable<T> expendFilter(String filter);
    
    /**
     * Returns whether the search filter matches the input.
     *
     * @param str    the input
     * @param substr the expanded search filter
     * @return whether the search filter matches the input
     */
    boolean contains(String str, T substr);
    
    /**
     * Returns a suggested expansion from the search filter.
     *
     * @param str the input
     * @return a suggested expansion from the search filter, or {@code null} if no suggestion is available
     */
    @Nullable
    default String suggestInputString(String str) {
        return null;
    }
    
    /**
     * Prepares the input method for activation.
     *
     * @param executor the executor to run the preparation on
     * @return a future that completes when the preparation is done
     */
    CompletableFuture<Void> prepare(Executor executor);
    
    /**
     * Prepares the input method for activation.
     *
     * @param executor         the executor to run the preparation on
     * @param progressCallback the callback to call when the progress is updated
     * @return a future that completes when the preparation is done
     */
    default CompletableFuture<Void> prepare(Executor executor, ProgressCallback progressCallback) {
        progressCallback.onProgress(0.0);
        return prepare(executor).whenComplete((aVoid, throwable) -> progressCallback.onProgress(1.0));
    }
    
    /**
     * Disposes the input method.
     *
     * @param executor the executor to run the disposal on
     * @return a future that completes when the disposal is done
     */
    CompletableFuture<Void> dispose(Executor executor);
    
    /**
     * Disposes the input method.
     *
     * @param executor         the executor to run the disposal on
     * @param progressCallback the callback to call when the progress is updated
     * @return a future that completes when the disposal is done
     */
    default CompletableFuture<Void> dispose(Executor executor, ProgressCallback progressCallback) {
        progressCallback.onProgress(0.0);
        return dispose(executor).whenComplete((aVoid, throwable) -> progressCallback.onProgress(1.0));
    }
    
    /**
     * Returns the name of this input method.
     *
     * @return the name of this input method
     */
    Component getName();
    
    /**
     * Returns the description of this input method.
     *
     * @return the description of this input method
     */
    Component getDescription();
    
    /**
     * Returns the list of menu entries that will be used as options.
     *
     * @return the list of menu entries that will be used as options
     */
    default List<FavoriteMenuEntry> getOptionsMenuEntries() {
        return List.of();
    }
    
    record Locale(String code, Component name) {}
    
    @FunctionalInterface
    interface ProgressCallback {
        /**
         * Called when the progress of the preparation is updated.
         *
         * @param progress the progress of the preparation, between 0.0 and 1.0
         */
        void onProgress(double progress);
    }
}
