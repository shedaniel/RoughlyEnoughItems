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

package me.shedaniel.rei.impl.client.search.argument.type;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import me.shedaniel.rei.impl.client.search.result.ArgumentApplicableResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class RegexArgumentType extends ArgumentType<@Nullable Pattern, String> {
    private static final TooltipContext CONTEXT = TooltipContext.of(new Point(), TooltipFlag.Default.NORMAL, true);
    private static final String EMPTY = "";
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xbfffa8));
    
    @Override
    public String getName() {
        return "regex";
    }
    
    @Override
    public ArgumentApplicableResult checkApplicable(String text, boolean forceGrammar) {
        boolean inverted = false;
        String matchText = text;
        if (matchText.startsWith("-")) {
            inverted = true;
            matchText = matchText.substring(1);
        }
        if (matchText.length() >= 3 && matchText.startsWith("r/") && matchText.endsWith("/"))
            return ArgumentApplicableResult.result(matchText.substring(2, matchText.length() - 1), true, inverted)
                    .grammar(0, inverted ? 3 : 2)
                    .grammar(text.length() - 1, text.length());
        return ArgumentApplicableResult.notApplicable();
    }
    
    @Override
    @Nullable
    public Pattern prepareSearchFilter(String searchText) {
        try {
            return Pattern.compile(searchText);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }
    
    @Override
    public Style getHighlightedStyle() {
        return STYLE;
    }
    
    @Override
    public String cacheData(EntryStack<?> stack) {
        return FormattingUtils.stripFormatting(stack.asFormattedText(CONTEXT).getString());
    }
    
    @Override
    public void matches(String data, EntryStack<?> stack, @Nullable Pattern filterData, ResultSink sink) {
        if (filterData == null) return;
        Matcher matcher = filterData.matcher(data);
        if (matcher != null && matcher.matches()) {
            sink.testTrue();
        }
    }
}

