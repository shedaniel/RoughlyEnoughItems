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
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@ApiStatus.Internal
public final class RegexArgument extends Argument {
    public static final RegexArgument INSTANCE = new RegexArgument();
    
    @Override
    public String getName() {
        return "regex";
    }
    
    @Override
    public MatchStatus matchesArgumentPrefix(String text) {
        boolean inverted = false;
        String matchText = text;
        if (matchText.startsWith("-")) {
            inverted = true;
            matchText = matchText.substring(1);
        }
        if (matchText.length() >= 3 && matchText.startsWith("r/") && matchText.endsWith("/"))
            return !inverted ? MatchStatus.matched(matchText.substring(2, matchText.length() - 1), true) : MatchStatus.invertMatched(matchText.substring(2, matchText.length() - 1), true);
        return MatchStatus.unmatched();
    }
    
    @Override
    public Object prepareSearchData(String searchText) {
        try {
            return Pattern.compile(searchText);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        Pattern pattern = (Pattern) searchData;
        if (pattern == null) return false;
        if (data[getDataOrdinal()] == null) {
            String name = stack.asFormatStrippedText().getString();
            data[getDataOrdinal()] = name;
        }
        Matcher matcher = pattern.matcher((String) data[getDataOrdinal()]);
        return matcher != null && matcher.matches();
    }
    
    private RegexArgument() {
    }
}

