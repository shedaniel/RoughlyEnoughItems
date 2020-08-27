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

package me.shedaniel.rei.impl;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.impl.search.AlwaysMatchingArgument;
import me.shedaniel.rei.impl.search.Argument;
import me.shedaniel.rei.impl.search.ArgumentsRegistry;
import me.shedaniel.rei.impl.search.MatchStatus;
import me.shedaniel.rei.utils.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class SearchArgument {
    public static final String SPACE = " ", EMPTY = "";
    private static final SearchArgument ALWAYS = new SearchArgument(AlwaysMatchingArgument.INSTANCE, EMPTY, true);
    private Argument argument;
    private String text;
    private Object data;
    private boolean regular;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?:\"([^\"]*)\")|([^\\s]+)");
    
    public SearchArgument(Argument argument, String text, boolean regular) {
        this(argument, text, regular, true);
    }
    
    public SearchArgument(Argument argument, String text, boolean regular, boolean lowercase) {
        this.argument = argument;
        this.text = lowercase ? text.toLowerCase(Locale.ROOT) : text;
        this.regular = regular;
        this.data = null;
    }
    
    @ApiStatus.Internal
    public static List<SearchArgument.SearchArguments> processSearchTerm(String searchTerm) {
        List<SearchArgument.SearchArguments> searchArguments = Lists.newArrayList();
        for (String split : StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|")) {
            Matcher terms = SPLIT_PATTERN.matcher(split);
            List<SearchArgument> arguments = Lists.newArrayList();
            while (terms.find()) {
                String term = MoreObjects.firstNonNull(terms.group(1), terms.group(2));
                for (Argument argument : ArgumentsRegistry.ARGUMENT_LIST) {
                    MatchStatus status = argument.matchesArgumentPrefix(term);
                    if (status.isMatched()) {
                        arguments.add(new SearchArgument(argument, status.getText(), !status.isInverted(), !status.shouldPreserveCasing()));
                        break;
                    }
                }
            }
            if (arguments.isEmpty()) {
                searchArguments.add(SearchArgument.SearchArguments.ALWAYS);
            } else {
                searchArguments.add(new SearchArgument.SearchArguments(arguments.toArray(new SearchArgument[0])));
            }
        }
        for (SearchArguments arguments : searchArguments) {
            for (SearchArgument argument : arguments.getArguments()) {
                argument.data = argument.argument.prepareSearchData(argument.getText());
            }
        }
        return searchArguments;
    }
    
    @ApiStatus.Internal
    public static boolean canSearchTermsBeAppliedTo(EntryStack stack, List<SearchArgument.SearchArguments> searchArguments) {
        if (searchArguments.isEmpty())
            return true;
        Minecraft minecraft = Minecraft.getInstance();
        Object[] data = new Object[ArgumentsRegistry.ARGUMENT_LIST.size()];
        for (SearchArgument.SearchArguments arguments : searchArguments) {
            boolean applicable = true;
            for (SearchArgument argument : arguments.getArguments()) {
                if (argument.getArgument().matches(data, stack, argument.getText(), argument.data) != argument.isRegular()) {
                    applicable = false;
                    break;
                }
            }
            if (applicable)
                return true;
        }
        return false;
    }
    
    public static String tryGetEntryStackTooltip(EntryStack stack) {
        Tooltip tooltip = stack.getTooltip(new Point());
        if (tooltip != null)
            return CollectionUtils.mapAndJoinToString(tooltip.getText(), Component::getString, "\n");
        return "";
    }
    
    public Argument getArgument() {
        return argument;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isRegular() {
        return regular;
    }
    
    @Override
    public String toString() {
        return String.format("Argument[%s]: name = %s, regular = %b", argument.getName(), text, regular);
    }
    
    public static class SearchArguments {
        public static final SearchArguments ALWAYS = new SearchArguments(new SearchArgument[]{SearchArgument.ALWAYS});
        private SearchArgument[] arguments;
        
        public SearchArguments(SearchArgument[] arguments) {
            this.arguments = arguments;
        }
        
        public SearchArgument[] getArguments() {
            return arguments;
        }
        
        public final boolean isAlways() {
            return this == ALWAYS;
        }
    }
    
}
