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

package me.shedaniel.rei.api.common.entry.type;

import me.shedaniel.rei.api.client.entry.type.BuiltinClientEntryTypes;
import me.shedaniel.rei.api.common.util.Identifiable;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * A type of entry, used to defer an {@link EntryDefinition} once loaded into the game.
 * <p>
 * There should only be one instance of each {@link EntryType} in the game,
 * reference equality is used to determine if two {@link EntryType}s are the same.
 *
 * @param <T> the type of entry
 * @see BuiltinEntryTypes
 * @see VanillaEntryTypes
 * @see BuiltinClientEntryTypes
 * @see EntryTypeRegistry
 */
@ApiStatus.NonExtendable
public interface EntryType<T> extends Identifiable {
    /**
     * Creates a deferred {@link EntryType} from the given {@link ResourceLocation}.
     * It is crucial that the {@link ResourceLocation} is the same as the one used to register the {@link EntryDefinition}.
     *
     * @param id  the identifier used to resolve the {@link EntryDefinition}
     * @param <T> the type of entry
     * @return the deferred {@link EntryType}
     */
    static <T> EntryType<T> deferred(ResourceLocation id) {
        return Internals.deferEntryType(id).cast();
    }
    
    ResourceLocation getId();
    
    @Override
    default ResourceLocation getIdentifier() {
        return getId();
    }
    
    /**
     * Resolves the {@link EntryDefinition} for this {@link EntryType} using {@link EntryTypeRegistry}.
     * It is not expected that the {@link EntryDefinition} will be different once resolved.
     *
     * @return the resolved {@link EntryDefinition}
     * @throws NullPointerException if the {@link EntryDefinition} is not found
     */
    EntryDefinition<T> getDefinition();
    
    @ApiStatus.NonExtendable
    default <O> EntryType<O> cast() {
        return (EntryType<O>) this;
    }
}
