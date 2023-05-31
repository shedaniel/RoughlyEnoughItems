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

package me.shedaniel.rei.impl.client.search.argument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.search.IntRange;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import me.shedaniel.rei.impl.client.search.collapsed.CollapsedEntriesCache;
import me.shedaniel.rei.impl.client.search.result.ArgumentApplicableResult;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class Argument<T, R> {
    public static final Object NO_CACHE = new Object();
    private static final AtomicReference<String> LAST_LANGUAGE = new AtomicReference<>();
    public static ArgumentCache cache = new ArgumentCache();
    private final ArgumentType<T, R> argumentType;
    private final String text;
    private final T filterData;
    private final boolean regular;
    private final int start;
    private final int end;
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(?:\"([^\"]*)\")|([^\\s]+)");
    
    public Argument(ArgumentType<T, R> argumentType, String text, boolean regular, T filterData, int start, int end, boolean lowercase) {
        this.argumentType = argumentType;
        this.text = lowercase ? text.toLowerCase(Locale.ROOT) : text;
        this.regular = regular;
        this.filterData = filterData;
        this.start = start;
        this.end = end;
    }
    
    public static void resetCache(boolean cache) {
        Argument.cache = new ArgumentCache();
        CollapsedEntriesCache.reset();
        Collection<HashedEntryStackWrapper> stacks = new AbstractCollection<>() {
            @Override
            public Iterator<HashedEntryStackWrapper> iterator() {
                return Iterators.transform(((EntryRegistryImpl) EntryRegistry.getInstance()).getComplexList().iterator(),
                        HashedEntryStackWrapper::normalize);
            }
            
            @Override
            public int size() {
                return ((EntryRegistryImpl) EntryRegistry.getInstance()).getComplexList().size();
            }
        };
        if (cache) {
            Argument.cache.prepareFilter(stacks, ArgumentTypesRegistry.ARGUMENT_TYPE_LIST, ArgumentCache.EXECUTOR_SERVICE);
        }
        CollapsedEntriesCache.getInstance().prepare(stacks);
    }
    
    public static boolean hasCache() {
        return !Argument.cache.isEmpty();
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
        
        void addPart(Argument.Builder<?, ?> argument, boolean usingGrammar, Collection<IntRange> grammarRanges, int index);
    }
    
    public static List<CompoundArgument> bakeArguments(String filter) {
        return bakeArguments(filter, null);
    }
    
    public static List<CompoundArgument> bakeArguments(String filter, @Nullable ProcessedSink sink) {
        List<CompoundArgument> compoundArguments = Lists.newArrayList();
        int tokenStartIndex = 0;
        
        for (String token : StringUtils.splitByWholeSeparatorPreserveAllTokens(filter, "|")) {
            Matcher terms = SPLIT_PATTERN.matcher(token);
            CompoundArgument.Builder builder = CompoundArgument.builder();
            while (terms.find()) {
                AlternativeArgument.Builder alternativeBuilder = AlternativeArgument.builder();
                
                for (ArgumentType<?, ?> type : ArgumentTypesRegistry.ARGUMENT_TYPE_LIST) {
                    applyArgument(type, filter, terms, tokenStartIndex, alternativeBuilder, true, sink);
                    if (!alternativeBuilder.isEmpty()) {
                        break;
                    }
                }
                
                if (alternativeBuilder.isEmpty()) {
                    for (ArgumentType<?, ?> type : ArgumentTypesRegistry.ARGUMENT_TYPE_LIST) {
                        applyArgument(type, filter, terms, tokenStartIndex, alternativeBuilder, false, sink);
                    }
                }
                
                builder.add(alternativeBuilder);
            }
            compoundArguments.add(builder.build());
            tokenStartIndex += 1 + token.length();
            if (sink != null && tokenStartIndex - 1 < filter.length()) {
                sink.addSplitter(tokenStartIndex - 1);
            }
        }
        return compoundArguments;
    }
    
    private static void applyArgument(ArgumentType<?, ?> type, String filter, Matcher terms, int tokenStartIndex, AlternativeArgument.Builder alternativeBuilder, boolean forceGrammar, @Nullable ProcessedSink sink) {
        String term = MoreObjects.firstNonNull(terms.group(1), terms.group(2));
        if (type.getSearchMode() == SearchMode.NEVER) return;
        ArgumentApplicableResult result = type.checkApplicable(term, forceGrammar);
        
        if (result.isApplicable()) {
            int group = terms.group(1) != null ? 1 : 2;
            Argument.Builder<?, ?> argument = new Argument.Builder<>(type, result.getText(), !result.isInverted(),
                    terms.start(group) + tokenStartIndex, terms.end(group) + tokenStartIndex, !result.shouldPreserveCasing());
            alternativeBuilder.add(argument);
            if (sink != null) {
                if (group == 1) {
                    sink.addQuote(terms.start() + tokenStartIndex);
                    if (terms.end() - 1 + tokenStartIndex < filter.length()) {
                        sink.addQuote(terms.end() - 1 + tokenStartIndex);
                    }
                }
                sink.addPart(argument, result.isUsingGrammar(), result.grammarRanges(), terms.start() + tokenStartIndex);
            }
        }
    }
    
    public static boolean matches(EntryStack<?> stack, long hashExact, List<CompoundArgument> compoundArguments, InputMethod<?> inputMethod) {
        if (compoundArguments.isEmpty()) return true;
        String newLanguage = Minecraft.getInstance().options.languageCode;
        if (!Objects.equals(LAST_LANGUAGE.getAndSet(newLanguage), newLanguage)) {
            resetCache(false);
        }
        
        a:
        for (CompoundArgument arguments : compoundArguments) {
            for (AlternativeArgument argument : arguments) {
                if (!matches(stack, hashExact, argument, inputMethod)) {
                    continue a;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private static <T> boolean matches(EntryStack<?> stack, long hashExact, AlternativeArgument alternativeArgument, InputMethod<T> inputMethod) {
        if (alternativeArgument.isEmpty()) return true;
        ResultSinkImpl<T> sink = new ResultSinkImpl<>(inputMethod);
        
        for (Argument<?, ?> argument : alternativeArgument) {
            sink.filters = inputMethod.expendFilter(argument.getText());
            if (matches(argument.getArgument(), stack, hashExact, argument.filterData, sink) == argument.isRegular()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static <T, R, B> boolean matches(ArgumentType<T, B> argumentType, EntryStack<?> stack, long hashExact, R filterData, ResultSinkImpl<?> sink) {
        Long2ObjectMap<Object> map = Argument.cache.getSearchCache(argumentType);
        Object value = map.get(hashExact);
        if (value == null) {
            value = argumentType.cacheData(stack);
            map.put(hashExact, value == null ? NO_CACHE : value);
        }
        sink.matches = false;
        argumentType.matches(value == NO_CACHE ? null : (B) value, stack, (T) filterData, sink);
        return sink.matches;
    }
    
    private static class ResultSinkImpl<T> implements ArgumentType.ResultSink {
        private final InputMethod<T> inputMethod;
        private boolean matches;
        private Iterable<T> filters;
        
        public ResultSinkImpl(InputMethod<T> inputMethod) {
            this.inputMethod = inputMethod;
        }
        
        @Override
        public boolean testTrue() {
            return matches = true;
        }
        
        @Override
        public boolean testString(String text) {
            if (matches) return true;
            if (inputMethod instanceof CharacterUnpackingInputMethod im) {
                for (T filter : filters) {
                    if (InputMethodMatcher.contains(im, IntList.of(text.codePoints().toArray()), (IntList) filter)) {
                        return matches = true;
                    }
                }
            } else {
                for (T filter : filters) {
                    if (inputMethod.contains(text, filter)) {
                        return matches = true;
                    }
                }
            }
            return false;
        }
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
    
    public static class Builder<T, R> {
        private final ArgumentType<T, R> argumentType;
        private final String text;
        private final boolean regular;
        private final int start;
        private final int end;
        private final boolean lowercase;
        
        public Builder(ArgumentType<T, R> argumentType, String text, boolean regular, int start, int end, boolean lowercase) {
            this.argumentType = argumentType;
            this.text = text;
            this.regular = regular;
            this.start = start;
            this.end = end;
            this.lowercase = lowercase;
        }
        
        public Argument<T, R> build() {
            return new Argument<>(argumentType, text, regular, argumentType.prepareSearchFilter(text),
                    start, end, lowercase);
        }
        
        public ArgumentType<T, R> getType() {
            return argumentType;
        }
        
        public int start() {
            return start;
        }
        
        public int end() {
            return end;
        }
        
        public String getText() {
            return text;
        }
        
        public boolean isRegular() {
            return regular;
        }
        
        public boolean isLowercase() {
            return lowercase;
        }
    }
}
