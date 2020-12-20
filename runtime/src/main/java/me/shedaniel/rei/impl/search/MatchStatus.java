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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public final class MatchStatus {
    private static final MatchStatus UNMATCHED = new MatchStatus(MatchType.UNMATCHED, null, false);
    private final MatchType type;
    @Nullable
    private final String text;
    private final boolean preserveCasing;
    
    private MatchStatus(MatchType type, @Nullable String text, boolean preserveCasing) {
        this.type = type;
        this.text = text;
        this.preserveCasing = preserveCasing;
    }
    
    public static MatchStatus unmatched() {
        return UNMATCHED;
    }
    
    public static MatchStatus invertMatched(@NotNull String text) {
        return invertMatched(text, false);
    }
    
    public static MatchStatus invertMatched(@NotNull String text, boolean preserveCasing) {
        return new MatchStatus(MatchType.INVERT_MATCHED, Objects.requireNonNull(text), preserveCasing);
    }
    
    public static MatchStatus matched(@NotNull String text) {
        return matched(text, false);
    }
    
    public static MatchStatus matched(@NotNull String text, boolean preserveCasing) {
        return new MatchStatus(MatchType.MATCHED, Objects.requireNonNull(text), preserveCasing);
    }
    
    public boolean isMatched() {
        return type != MatchType.UNMATCHED;
    }
    
    public boolean isInverted() {
        return type == MatchType.INVERT_MATCHED;
    }
    
    public boolean shouldPreserveCasing() {
        return preserveCasing;
    }
    
    @Nullable
    public String getText() {
        return text;
    }
    
    public MatchType getType() {
        return type;
    }
}
