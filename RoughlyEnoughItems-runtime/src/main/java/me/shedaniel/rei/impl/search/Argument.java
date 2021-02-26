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
import me.shedaniel.rei.gui.config.SearchMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public abstract class Argument<T, R> {
    public Argument() {
    }
    
    public abstract String getName();
    
    @Nullable
    public String getPrefix() {
        return null;
    }
    
    @NotNull
    public Style getHighlightedStyle() {
        return Style.EMPTY;
    }
    
    public SearchMode getSearchMode() {
        return SearchMode.PREFIX;
    }
    
    public MatchStatus matchesArgumentPrefix(String text) {
        MatchStatus status = checkMatchPrefix(text, getPrefix());
        if (status.isMatched()) {
            return status;
        } else if (getSearchMode() == SearchMode.ALWAYS) {
            status = checkMatchPrefix(text, "");
            if (status.isMatched()) {
                status.notUsingGrammar();
            }
            return status;
        }
        return MatchStatus.unmatched();
    }
    
    private MatchStatus checkMatchPrefix(String text, String prefix) {
        if (prefix == null) return MatchStatus.unmatched();
        if (text.startsWith("-" + prefix)) return MatchStatus.invertMatched(text.substring(1 + prefix.length())).grammar(0, prefix.length() + 1);
        if (text.startsWith(prefix + "-")) return MatchStatus.invertMatched(text.substring(1 + prefix.length())).grammar(0, prefix.length() + 1);
        if (text.startsWith(prefix)) return MatchStatus.matched(text.substring(prefix.length())).grammar(0, prefix.length());
        return MatchStatus.unmatched();
    }
    
    public abstract boolean matches(Mutable<R> data, EntryStack stack, String searchText, T filterData);
    
    public abstract T prepareSearchFilter(String searchText);
}
