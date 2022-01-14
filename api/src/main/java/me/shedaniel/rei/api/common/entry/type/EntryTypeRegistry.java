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

package me.shedaniel.rei.api.common.entry.type;

import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Registry for registering alternative entry types.
 *
 * <p>{@link EntryType} must be declared statically, deferring to the actual
 * definition by the identifier of the type. During reload, plugins should
 * register {@link EntryDefinition} for their deferred {@link EntryType},
 * these definitions are dynamic.
 *
 * <p>{@link EntryTypeBridge} may be used to convert and compare between
 * different types
 *
 * @see EntryDefinition
 * @see #registerBridge(EntryType, EntryType, EntryTypeBridge)
 * @see REIPlugin#registerEntryTypes(EntryTypeRegistry)
 */
public interface EntryTypeRegistry extends Reloadable<REIPlugin<?>> {
    static EntryTypeRegistry getInstance() {
        return PluginManager.getInstance().get(EntryTypeRegistry.class);
    }
    
    /**
     * Registers a entry type, with its entry definition.
     *
     * @param type       the entry type
     * @param definition the definition of the entry
     * @param <T>        the type of the entry
     */
    default <T> void register(EntryType<T> type, EntryDefinition<T> definition) {
        register(type.getId(), definition);
    }
    
    /**
     * Registers an entry type, with its entry definition.
     *
     * @param id         the identifier of the entry type
     * @param definition the definition of the entry
     * @param <T>        the type of the entry
     */
    <T> void register(ResourceLocation id, EntryDefinition<T> definition);
    
    /**
     * Returns the entry definition from the entry type.
     *
     * @param type the entry type
     * @return the definition of the entry, may be {@code null} if {@code type} was not registered
     */
    @Nullable
    default <T> EntryDefinition<T> get(EntryType<T> type) {
        return type.getDefinition();
    }
    
    /**
     * Returns the entry definition from an identifier of the entry type.
     *
     * @param id the identifier of the entry type
     * @return the definition of the entry, may be {@code null} if {@code id} is an unknown type
     */
    @Nullable
    EntryDefinition<?> get(ResourceLocation id);
    
    /**
     * Returns the set of types in their identifier form.
     *
     * @return the set of types
     */
    Set<ResourceLocation> keySet();
    
    /**
     * Returns the set of entry definitions.
     *
     * @return the set of entry definitions
     */
    Set<EntryDefinition<?>> values();
    
    /**
     * Register a bridge between two entry types, for example, item to fluid, this is used, to
     * approximately match two entry stacks.
     * <p>
     * For bridging two entry types, only 1 one way bridge is required, two way bridges are discouraged
     * for performance issues and possible recursion.
     *
     * @param original    the original entry type
     * @param destination the destination entry type
     * @param bridge      the bridge to bridge between the original and the destination types
     * @param <A>         the type of the original entry type
     * @param <B>         the type of the destination entry type
     */
    <A, B> void registerBridge(EntryType<A> original, EntryType<B> destination, EntryTypeBridge<A, B> bridge);
    
    <A, B> Iterable<EntryTypeBridge<A, B>> getBridgesFor(EntryType<A> original, EntryType<B> destination);
}