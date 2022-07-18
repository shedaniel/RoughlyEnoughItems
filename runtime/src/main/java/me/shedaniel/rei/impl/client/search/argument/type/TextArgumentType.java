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

package me.shedaniel.rei.impl.client.search.argument.type;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Unit;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TextArgumentType extends ArgumentType<Unit, String> {
    private static final TooltipContext CONTEXT = TooltipContext.of(new Point(), TooltipFlag.Default.NORMAL, true);
    
    @Override
    public String getName() {
        return "text";
    }
    
    @Override
    @Nullable
    public String getPrefix() {
        return "";
    }
    
    @Override
    public SearchMode getSearchMode() {
        return SearchMode.ALWAYS;
    }
    
    @Override
    public String cacheData(EntryStack<?> stack) {
        return FormattingUtils.stripFormatting(stack.asFormattedText(CONTEXT).getString()).toLowerCase(Locale.ROOT);
    }
    
    @Override
    public void matches(String value, EntryStack<?> stack, Unit filterData, ResultSink sink) {
        sink.testString(value);
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return null;
    }
}
