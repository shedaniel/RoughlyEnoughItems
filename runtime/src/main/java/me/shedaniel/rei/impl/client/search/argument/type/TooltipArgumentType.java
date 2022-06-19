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
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Unit;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.StringJoiner;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class TooltipArgumentType extends ArgumentType<Unit, String> {
    public static final TooltipArgumentType INSTANCE = new TooltipArgumentType();
    private static final TooltipContext CONTEXT = TooltipContext.of(new Point(), TooltipFlag.Default.NORMAL, true);
    public static String INVALID = "INVALID_PIECE_OF_TOOLTIP_I_DONT_THINK_PEOPLE_WILL_EXACTLY_HAVE_THIS_REI_REI_REI";
    private static final Style STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xffe0ad));
    
    @Override
    public String getName() {
        return "tooltip";
    }
    
    @Override
    @Nullable
    public String getPrefix() {
        return "#";
    }
    
    @Override
    public Style getHighlightedStyle() {
        return STYLE;
    }
    
    @Override
    public SearchMode getSearchMode() {
        return ConfigObject.getInstance().getTooltipSearchMode();
    }
    
    @Override
    public String cacheData(EntryStack<?> stack) {
        String tooltip = tryGetEntryStackTooltip(stack, 0);
        if (tooltip == null) return INVALID;
        return tooltip.toLowerCase(Locale.ROOT);
    }
    
    @Override
    public boolean matches(String tooltip, EntryStack<?> stack, String searchText, Unit filterData) {
        //noinspection StringEquality
        if (tooltip == INVALID) return false;
        return tooltip.contains(searchText);
    }
    
    @Nullable
    public static String tryGetEntryStackTooltip(EntryStack<?> stack, int attempt) {
        try {
            Tooltip tooltip = stack.getTooltip(CONTEXT, false);
            if (tooltip != null) {
                StringJoiner joiner = new StringJoiner("\n");
                for (Tooltip.Entry entry : tooltip.entries()) {
                    if (entry.isText()) {
                        joiner.add(entry.getAsText().getString());
                    }
                }
                return joiner.toString();
            }
            return "";
        } catch (Throwable throwable) {
            Throwable temp = throwable;
            while (temp != null) {
                temp = temp.getCause();
                if (temp instanceof ConcurrentModificationException) {
                    // yes, this is a hack
                    if (attempt < 10) {
                        return tryGetEntryStackTooltip(stack, attempt + 1);
                    }
                    
                    return null;
                }
            }
            
            throw throwable;
        }
    }
    
    @Override
    public Unit prepareSearchFilter(String searchText) {
        return Unit.INSTANCE;
    }
    
    private TooltipArgumentType() {
    }
}
