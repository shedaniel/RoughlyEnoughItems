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
import me.shedaniel.rei.impl.SearchArgument;
import net.minecraft.util.Unit;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public final class TooltipArgument extends Argument<Unit, String> {
    public static final TooltipArgument INSTANCE = new TooltipArgument();
    private static final Style STYLE = Style.EMPTY.withColor(Color.fromRgb(0xffe0ad));
    
    @Override
    public String getName() {
        return "tooltip";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "#";
    }
    
    @Override
    public @NotNull Style getHighlightedStyle() {
        return STYLE;
    }
    
    @Override
    public boolean matches(Mutable<String> data, EntryStack stack, String searchText, Unit filterData) {
        if (data.getValue() == null) {
            data.setValue(SearchArgument.tryGetEntryStackTooltip(stack).toLowerCase(Locale.ROOT));
        }
        String tooltip = data.getValue();
        return tooltip.isEmpty() || tooltip.contains(searchText);
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    private TooltipArgument() {
    }
}
