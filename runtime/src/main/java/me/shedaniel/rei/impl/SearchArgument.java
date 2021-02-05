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
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.search.AlwaysMatchingArgument;
import me.shedaniel.rei.impl.search.Argument;
import me.shedaniel.rei.impl.search.ArgumentsRegistry;
import me.shedaniel.rei.impl.search.MatchStatus;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.IntRange;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class SearchArgument<T, R> {
    public static final String SPACE = " ", EMPTY = "";
    private static final SearchArgument<Unit, Unit> ALWAYS = new SearchArgument<>(AlwaysMatchingArgument.INSTANCE, EMPTY, true, -1, -1);
    private Argument<T, R> argument;
    private String text;
    private T filterData;
    private boolean regular;
    private final int start;
    private final int end;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?:\"([^\"]*)\")|([^\\s]+)");
    
    public SearchArgument(Argument<T, R> argument, String text, boolean regular, int start, int end) {
        this(argument, text, regular, start, end, true);
    }
    
    public SearchArgument(Argument<T, R> argument, String text, boolean regular, int start, int end, boolean lowercase) {
        this.argument = argument;
        this.text = lowercase ? text.toLowerCase(Locale.ROOT) : text;
        this.regular = regular;
        this.filterData = null;
        this.start = start;
        this.end = end;
    }
    
    public int start() {
        return start;
    }
    
    public int end() {
        return end;
    }
    
    public interface ProcessedSink {
        void addQuote(int index);
        
        void addSplitter(int index);
        
        void addPart(SearchArgument<?, ?> argument, Collection<IntRange> grammarRanges, int index);
    }
    
    @ApiStatus.Internal
    public static List<SearchArgument.SearchArguments> processSearchTerm(String searchTerm) {
        return processSearchTerm(searchTerm, null);
    }
    
    @ApiStatus.Internal
    public static List<SearchArgument.SearchArguments> processSearchTerm(String searchTerm, @Nullable ProcessedSink sink) {
        List<SearchArgument.SearchArguments> searchArguments = Lists.newArrayList();
        int tokenStartIndex = 0;
        String[] allTokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|");
        for (String token : allTokens) {
            Matcher terms = SPLIT_PATTERN.matcher(token);
            List<SearchArgument<?, ?>> arguments = Lists.newArrayList();
            while (terms.find()) {
                String term = MoreObjects.firstNonNull(terms.group(1), terms.group(2));
                for (Argument<?, ?> argument : ArgumentsRegistry.ARGUMENT_LIST) {
                    MatchStatus status = argument.matchesArgumentPrefix(term);
                    if (status.isMatched()) {
                        SearchArgument<?, ?> searchArgument;
                        if (terms.group(1) != null) {
                            arguments.add(searchArgument = new SearchArgument<>(argument, status.getText(), !status.isInverted(), terms.start(1) + tokenStartIndex, terms.end(1) + tokenStartIndex, !status.shouldPreserveCasing()));
                            if (sink != null) {
                                sink.addQuote(terms.start() + tokenStartIndex);
                                if (terms.end() - 1 + tokenStartIndex < searchTerm.length()) {
                                    sink.addQuote(terms.end() - 1 + tokenStartIndex);
                                }
                            }
                        } else {
                            arguments.add(searchArgument = new SearchArgument<>(argument, status.getText(), !status.isInverted(), terms.start(2) + tokenStartIndex, terms.end(2) + tokenStartIndex, !status.shouldPreserveCasing()));
                        }
                        if (sink != null) {
                            sink.addPart(searchArgument, status.grammarRanges(), terms.start() + tokenStartIndex);
                        }
                        break;
                    }
                }
            }
            if (arguments.isEmpty()) {
                searchArguments.add(SearchArgument.SearchArguments.ALWAYS);
            } else {
                searchArguments.add(new SearchArgument.SearchArguments(arguments.toArray(new SearchArgument[0])));
            }
            tokenStartIndex += 1 + token.length();
            if (sink != null && tokenStartIndex - 1 < searchTerm.length()) {
                sink.addSplitter(tokenStartIndex - 1);
            }
        }
        for (SearchArguments arguments : searchArguments) {
            for (SearchArgument<?, ?> argument : arguments.getArguments()) {
                //noinspection RedundantCast
                ((SearchArgument<Object, Object>) argument).filterData = argument.argument.prepareSearchFilter(argument.getText());
            }
        }
        return searchArguments;
    }
    
    @ApiStatus.Internal
    public static boolean canSearchTermsBeAppliedTo(EntryStack<?> stack, List<SearchArgument.SearchArguments> searchArguments) {
        if (searchArguments.isEmpty())
            return true;
        Minecraft minecraft = Minecraft.getInstance();
        Mutable<?> mutable = new MutableObject<>();
        for (SearchArgument.SearchArguments arguments : searchArguments) {
            boolean applicable = true;
            for (SearchArgument<?, ?> argument : arguments.getArguments()) {
                mutable.setValue(null);
                if (matches(argument.getArgument(), mutable, stack, argument.getText(), argument.filterData) != argument.isRegular()) {
                    applicable = false;
                    break;
                }
            }
            if (applicable)
                return true;
        }
        return false;
    }
    
    private static <T, R, Z, B> boolean matches(Argument<T, B> argument, Mutable<Z> data, EntryStack<?> stack, String filter, R filterData) {
        return argument.matches((Mutable<B>) data, stack, filter, (T) filterData);
    }
    
    public static String tryGetEntryStackTooltip(EntryStack<?> stack) {
        Tooltip tooltip = stack.getTooltip(new Point());
        if (tooltip != null)
            return CollectionUtils.mapAndJoinToString(tooltip.getText(), Component::getString, "\n");
        return "";
    }
    
    public Argument<?, ?> getArgument() {
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
        public static final SearchArguments ALWAYS = new SearchArguments(SearchArgument.ALWAYS);
        private SearchArgument<?, ?>[] arguments;
        
        public SearchArguments(SearchArgument<?, ?>... arguments) {
            this.arguments = arguments;
        }
        
        public SearchArgument<?, ?>[] getArguments() {
            return arguments;
        }
        
        public final boolean isAlways() {
            return this == ALWAYS;
        }
    }
}
