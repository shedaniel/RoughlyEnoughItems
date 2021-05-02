/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.search.argument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.search.IntRange;
import me.shedaniel.rei.impl.client.search.argument.type.AlwaysMatchingArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import me.shedaniel.rei.impl.client.search.result.ArgumentApplicableResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
public class Argument<T, R> {
    public static final String SPACE = " ", EMPTY = "";
    public static final Short2ObjectMap<Long2ObjectMap<Object>> SEARCH_CACHE = new Short2ObjectOpenHashMap<>();
    static final Argument<Unit, Unit> ALWAYS = new Argument<>(AlwaysMatchingArgumentType.INSTANCE, EMPTY, true, -1, -1, true);
    private ArgumentType<T, R> argumentType;
    private String text;
    private T filterData;
    private boolean regular;
    private final int start;
    private final int end;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?:\"([^\"]*)\")|([^\\s]+)");
    
    public Argument(ArgumentType<T, R> argumentType, String text, boolean regular, int start, int end, boolean lowercase) {
        this.argumentType = argumentType;
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
        
        void addPart(Argument<?, ?> argument, boolean usingGrammar, Collection<IntRange> grammarRanges, int index);
    }
    
    @ApiStatus.Internal
    public static List<CompoundArgument> bakeArguments(String searchTerm) {
        return bakeArguments(searchTerm, null);
    }
    
    @ApiStatus.Internal
    public static List<CompoundArgument> bakeArguments(String searchTerm, @Nullable ProcessedSink sink) {
        List<CompoundArgument> compoundArguments = Lists.newArrayList();
        int tokenStartIndex = 0;
        
        for (String token : StringUtils.splitByWholeSeparatorPreserveAllTokens(searchTerm, "|")) {
            Matcher terms = SPLIT_PATTERN.matcher(token);
            CompoundArgument.Builder builder = CompoundArgument.builder();
            while (terms.find()) {
                AlternativeArgument.Builder alternativeBuilder = AlternativeArgument.builder();
                
                for (ArgumentType<?, ?> type : ArgumentTypesRegistry.ARGUMENT_TYPE_LIST) {
                    applyArgument(type, searchTerm, terms, tokenStartIndex, alternativeBuilder, true, sink);
                    if (!alternativeBuilder.isEmpty()) {
                        break;
                    }
                }
                
                if (alternativeBuilder.isEmpty()) {
                    for (ArgumentType<?, ?> type : ArgumentTypesRegistry.ARGUMENT_TYPE_LIST) {
                        applyArgument(type, searchTerm, terms, tokenStartIndex, alternativeBuilder, false, sink);
                    }
                }
                
                builder.add(alternativeBuilder);
            }
            compoundArguments.add(builder.build());
            tokenStartIndex += 1 + token.length();
            if (sink != null && tokenStartIndex - 1 < searchTerm.length()) {
                sink.addSplitter(tokenStartIndex - 1);
            }
        }
        prepareSearchFilter(compoundArguments);
        return compoundArguments;
    }
    
    private static void prepareSearchFilter(List<CompoundArgument> compoundArguments) {
        for (CompoundArgument arguments : compoundArguments) {
            for (AlternativeArgument alternativeArgument : arguments) {
                for (Argument<?, ?> argument : alternativeArgument) {
                    //noinspection RedundantCast
                    ((Argument<Object, Object>) argument).filterData = argument.argumentType.prepareSearchFilter(argument.getText());
                }
            }
        }
    }
    
    private static void applyArgument(ArgumentType<?, ?> type, String searchTerm, Matcher terms, int tokenStartIndex, AlternativeArgument.Builder alternativeBuilder, boolean forceGrammar, @Nullable ProcessedSink sink) {
        String term = MoreObjects.firstNonNull(terms.group(1), terms.group(2));
        if (type.getSearchMode() == SearchMode.NEVER) return;
        ArgumentApplicableResult result = type.checkApplicable(term, forceGrammar);
        
        if (result.isApplicable()) {
            int group = terms.group(1) != null ? 1 : 2;
            Argument<?, ?> argument = new Argument<>(type, result.getText(), !result.isInverted(),
                    terms.start(group) + tokenStartIndex, terms.end(group) + tokenStartIndex, !result.shouldPreserveCasing());
            alternativeBuilder.add(argument);
            if (sink != null) {
                if (group == 1) {
                    sink.addQuote(terms.start() + tokenStartIndex);
                    if (terms.end() - 1 + tokenStartIndex < searchTerm.length()) {
                        sink.addQuote(terms.end() - 1 + tokenStartIndex);
                    }
                }
                sink.addPart(argument, result.isUsingGrammar(), result.grammarRanges(), terms.start() + tokenStartIndex);
            }
        }
    }
    
    @ApiStatus.Internal
    public static boolean matches(EntryStack<?> stack, List<CompoundArgument> compoundArguments) {
        if (compoundArguments.isEmpty()) return true;
        Mutable<?> mutable = new MutableObject<>();
        
        a:
        for (CompoundArgument arguments : compoundArguments) {
            for (AlternativeArgument argument : arguments) {
                if (!matches(stack, argument, mutable)) {
                    continue a;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private static <T, R, Z, B> boolean matches(EntryStack<?> stack, AlternativeArgument alternativeArgument, Mutable<?> mutable) {
        if (alternativeArgument.isEmpty()) return true;
        long hashExact = EntryStacks.hashExact(stack);
        
        for (Argument<?, ?> argument : alternativeArgument) {
            if (matches((short) argument.getArgument().getIndex(), argument.getArgument(), mutable, stack, hashExact, argument.getText(), argument.filterData) == argument.isRegular()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static <T, R, Z, B> boolean matches(short argumentIndex, ArgumentType<T, B> argumentType, Mutable<Z> data, EntryStack<?> stack, long hashExact, String filter, R filterData) {
        Long2ObjectMap<Object> map = SEARCH_CACHE.get(argumentIndex);
        if (map == null) {
            SEARCH_CACHE.put(argumentIndex, map = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>()));
        }
        Z value = (Z) map.get(hashExact);
        data.setValue(value);
        boolean matches = argumentType.matches((Mutable<B>) data, stack, filter, (T) filterData);
        if (value == null) {
            map.put(hashExact, data.getValue());
        }
        return matches;
    }
    
    public ArgumentType<?, ?> getArgument() {
        return argumentType;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isRegular() {
        return regular;
    }
    
    @Override
    public String toString() {
        return String.format("Argument[%s]: name = %s, regular = %b", argumentType.getName(), text, regular);
    }
    
}
