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

package me.shedaniel.rei.api.client.registry.entry;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.Internals;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

@ApiStatus.Experimental
public interface PreFilteredEntryList extends Reloadable<REIClientPlugin> {
    static PreFilteredEntryList getInstance() {
        return ClientInternals.getPreFilteredEntryList();
    }
    
    // TODO Re-evaluate the need for this
    @ApiStatus.Internal
    Collection<EntryStack<?>> refilterNew(boolean warn, Collection<EntryStack<?>> entries);
    
    /**
     * @return the unmodifiable list of filtered entry stacks,
     * only available <b>after</b> plugins reload.
     */
    List<EntryStack<?>> getList();
}
