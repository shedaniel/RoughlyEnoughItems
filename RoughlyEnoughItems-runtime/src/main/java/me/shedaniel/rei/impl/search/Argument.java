/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public abstract class Argument {
    public Argument() {
    }
    
    private int dataOrdinal = -1;
    
    public abstract String getName();
    
    @Nullable
    public String getPrefix() {
        return null;
    }
    
    public MatchStatus matchesArgumentPrefix(String text) {
        String prefix = getPrefix();
        if (prefix == null) return MatchStatus.unmatched();
        if (text.startsWith("-" + prefix)) return MatchStatus.invertMatched(text.substring(1 + prefix.length()));
        if (text.startsWith(prefix + "-")) return MatchStatus.invertMatched(text.substring(1 + prefix.length()));
        return text.startsWith(prefix) ? MatchStatus.matched(text.substring(prefix.length())) : MatchStatus.unmatched();
    }
    
    public final int getDataOrdinal() {
        if (dataOrdinal == -1) {
            dataOrdinal = ArgumentsRegistry.ARGUMENT_LIST.indexOf(this);
        }
        return dataOrdinal;
    }
    
    public abstract boolean matches(Object[] data, EntryStack<?> stack, String searchText, Object searchData);
    
    public Object prepareSearchData(String searchText) {
        return null;
    }
}
