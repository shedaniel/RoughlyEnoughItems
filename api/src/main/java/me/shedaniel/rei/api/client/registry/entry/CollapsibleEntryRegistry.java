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

package me.shedaniel.rei.api.client.registry.entry;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Registry for grouping and collapsing {@link EntryStack}s.
 * <p>
 * The easiest way to use this is to use the {@link me.shedaniel.rei.api.common.util.EntryIngredients#ofItemTag(TagKey)}
 * and collect tags together.
 */
@ApiStatus.Experimental
public interface CollapsibleEntryRegistry extends Reloadable<REIClientPlugin> {
    /**
     * @return the {@link CollapsibleEntryRegistry} instance
     */
    static CollapsibleEntryRegistry getInstance() {
        return PluginManager.getClientInstance().get(CollapsibleEntryRegistry.class);
    }
    
    /**
     * Groups the given {@link EntryStack}s into a single entry in the entry panel.
     *
     * @param id     the identifier of the group
     * @param name   the name of the group
     * @param stacks the stacks to group
     * @param <T>    the type of the stacks
     */
    <T> void group(ResourceLocation id, Component name, List<? extends EntryStack<? extends T>> stacks);
    
    /**
     * Groups the given {@link EntryStack}s into a single entry in the entry panel.
     *
     * @param id     the identifier of the group
     * @param name   the name of the group
     * @param stacks the stacks to group
     * @param <T>    the type of the stacks
     */
    default <T> void group(ResourceLocation id, Component name, EntryStack<? extends T>... stacks) {
        group(id, name, Arrays.asList(stacks));
    }
    
    /**
     * Groups the matching {@link EntryStack}s via the given predicate into
     * a single entry in the entry panel.
     *
     * @param id        the identifier of the group
     * @param name      the name of the group
     * @param predicate the predicate to match the stacks
     */
    void group(ResourceLocation id, Component name, Predicate<? extends EntryStack<?>> predicate);
    
    /**
     * Groups the matching {@link EntryStack}s via the given predicate into
     * a single entry in the entry panel.
     *
     * @param id        the identifier of the group
     * @param name      the name of the group
     * @param type      the entry type to match
     * @param predicate the predicate to match the stacks
     * @param <T>       the type of the stacks
     */
    default <T> void group(ResourceLocation id, Component name, EntryType<T> type, Predicate<EntryStack<T>> predicate) {
        group(id, name, stack -> stack.getType() == type && predicate.test(stack.cast()));
    }
}
