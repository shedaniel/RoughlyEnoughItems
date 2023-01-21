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

package me.shedaniel.rei.api.client.entry.filtering;

import it.unimi.dsi.fastutil.longs.LongCollection;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Experimental
@Environment(EnvType.CLIENT)
public interface FilteringContext {
    /**
     * Returns the list of stacks that are previously marked as <b>shown</b> from other filtering rules.
     *
     * @return the list of stacks that are previously marked as shown from other filtering rules.
     */
    Collection<EntryStack<?>> getShownStacks();
    
    /**
     * Returns the list of stacks that have not been processed by any filtering rules.
     *
     * @return the list of stacks that have not been processed by any filtering rules.
     */
    Collection<EntryStack<?>> getUnsetStacks();
    
    /**
     * Returns the list of stacks that are previously marked as <b>hidden</b> from other filtering rules.
     *
     * @return the list of stacks that are previously marked as hidden from other filtering rules.
     */
    Collection<EntryStack<?>> getHiddenStacks();
    
    /**
     * Returns the list of hashes that are previously marked as <b>shown</b> from other filtering rules.
     *
     * @return the list of hashes that are previously marked as shown from other filtering rules.
     */
    @ApiStatus.Experimental
    LongCollection getShownExactHashes();
    
    /**
     * Returns the list of hashes that are previously marked as <b>hidden</b> from other filtering rules.
     *
     * @return the list of hashes that are previously marked as hidden from other filtering rules.
     */
    @ApiStatus.Experimental
    LongCollection getUnsetExactHashes();
    
    /**
     * Returns the list of hashes that are previously marked as <b>hidden</b> from other filtering rules.
     *
     * @return the list of hashes that are previously marked as hidden from other filtering rules.
     */
    @ApiStatus.Experimental
    LongCollection getHiddenExactHashes();
}
