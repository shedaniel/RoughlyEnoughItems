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

package me.shedaniel.rei.api.common.category;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.util.Identifiable;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * A category identifier is used to identify a category. This is the typed version of {@link ResourceLocation}.
 *
 * @param <D> the type of display
 */
@ApiStatus.NonExtendable
public interface CategoryIdentifier<D extends Display> extends Identifiable {
    /**
     * Creates a new category identifier from the given resource location string.
     * <p>
     * The namespace and path of the resource location is seperated by a colon.
     *
     * @param str the string to create the identifier from
     * @param <D> the type of display
     * @return the identifier
     * @throws ResourceLocationException if the string is not a valid resource location
     */
    static <D extends Display> CategoryIdentifier<D> of(String str) {
        return Internals.getCategoryIdentifier(str);
    }
    
    /**
     * Creates a new category identifier from the given namespace and path.
     *
     * @param namespace the namespace of the identifier, usually the mod id
     * @param path      the path of the identifier
     * @param <D>       the type of display
     * @return the identifier
     * @throws ResourceLocationException if the string is not a valid resource location
     */
    static <D extends Display> CategoryIdentifier<D> of(String namespace, String path) {
        return of(namespace + ":" + path);
    }
    
    /**
     * Creates a new category identifier from the given identifier.
     *
     * @param identifier the identifier
     * @param <D>        the type of display
     * @return the identifier
     */
    static <D extends Display> CategoryIdentifier<D> of(ResourceLocation identifier) {
        return of(identifier.toString());
    }
    
    /**
     * Returns the namespace of the identifier, this is usually the mod id.
     *
     * @return the namespace
     */
    default String getNamespace() {
        return getIdentifier().getNamespace();
    }
    
    /**
     * Returns the path of the identifier.
     *
     * @return the path
     */
    default String getPath() {
        return getIdentifier().getPath();
    }
    
    /**
     * Casts this {@link CategoryIdentifier} to a {@link CategoryIdentifier} of the given type.
     *
     * @param <O> the new type
     * @return the casted {@link CategoryIdentifier}
     */
    @ApiStatus.NonExtendable
    default <O extends Display> CategoryIdentifier<O> cast() {
        return (CategoryIdentifier<O>) this;
    }
}
