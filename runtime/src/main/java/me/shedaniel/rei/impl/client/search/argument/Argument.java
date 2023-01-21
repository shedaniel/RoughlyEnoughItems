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
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.search.method.CharacterUnpackingInputMethod;
import me.shedaniel.rei.api.client.search.method.InputMethod;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.search.IntRange;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentTypesRegistry;
import me.shedaniel.rei.impl.client.search.result.ArgumentApplicableResult;
import me.shedaniel.rei.impl.client.util.ThreadCreator;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.util.HashedEntryStackWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class Argument<T, R> {
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadCreator("REI-ArgumentCache").asService();
    private static final Short2ObjectMap<Long2ObjectMap<Object>> SEARCH_CACHE = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    private static final Object NO_CACHE = new Object();
    private static final AtomicReference<String> lastLanguage = new AtomicReference<>();
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
    
    public static void resetCache(boolean cache) {
        SEARCH_CACHE.clear();
        if (cache) {
            Argument.prepareFilter(new AbstractCollection<>() {
                @Override
                public Iterator<EntryStack<?>> iterator() {
                    return Iterators.transform(EntryRegistry.getInstance().getPreFilteredList().iterator(),
                            EntryStack::normalize);
                }
                
                @Override
                public int size() {
                    return EntryRegistry.getInstance().getPreFilteredList().size();
                }
            }, ArgumentTypesRegistry.ARGUMENT_TYPE_LIST, () -> true, EXECUTOR_SERVICE);
        }
    }
    
    public static boolean hasCache() {
        return !SEARCH_CACHE.isEmpty();
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
    
    private static void applyArgument(ArgumentType<?, ?> type, String filter, Matcher terms, int tokenStartIndex, AlternativeArgument.Builder alternativeBuilder, boolean forceGrammar, @Nullable ProcessedSink sink) {
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
                    if (terms.end() - 1 + tokenStartIndex < filter.length()) {
                        sink.addQuote(terms.end() - 1 + tokenStartIndex);
                    }
                }
                sink.addPart(argument, result.isUsingGrammar(), result.grammarRanges(), terms.start() + tokenStartIndex);
            }
        }
    }
    
    public static boolean matches(EntryStack<?> stack, List<CompoundArgument> compoundArguments, InputMethod<?> inputMethod) {
        if (compoundArguments.isEmpty()) return true;
        String newLanguage = Minecraft.getInstance().options.languageCode;
        if (!Objects.equals(lastLanguage.getAndSet(newLanguage), newLanguage)) {
            resetCache(false);
        }
        
        a:
        for (CompoundArgument arguments : compoundArguments) {
            for (AlternativeArgument argument : arguments) {
                if (!matches(stack, argument, inputMethod)) {
                    continue a;
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private static <T> boolean matches(EntryStack<?> stack, AlternativeArgument alternativeArgument, InputMethod<T> inputMethod) {
        if (alternativeArgument.isEmpty()) return true;
        long hashExact = EntryStacks.hashExact(stack);
        ResultSinkImpl<T> sink = new ResultSinkImpl<>(inputMethod);
        
        for (Argument<?, ?> argument : alternativeArgument) {
            sink.filters = inputMethod.expendFilter(argument.getText());
            if (matches(argument.getArgument(), stack, hashExact, argument.filterData, sink) == argument.isRegular()) {
                return true;
            }
        }
        
        return false;
    }
    
    private static Long2ObjectMap<Object> getSearchCache(ArgumentType<?, ?> argumentType) {
        short argumentIndex = (short) argumentType.getIndex();
        Long2ObjectMap<Object> map = SEARCH_CACHE.get(argumentIndex);
        if (map == null) {
            SEARCH_CACHE.put(argumentIndex, map = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>()));
        }
        return map;
    }
    
    private static <T, R, B> boolean matches(ArgumentType<T, B> argumentType, EntryStack<?> stack, long hashExact, R filterData, ResultSinkImpl<?> sink) {
        Long2ObjectMap<Object> map = getSearchCache(argumentType);
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
    
    public static Long prepareStart = null;
    public static List<HashedEntryStackWrapper> prepareStacks = null;
    public static IntIntPair prepareStage = null;
    public static IntIntPair[] currentStages = null;
    
    public static void prepareFilter(Collection<EntryStack<?>> stacks, Collection<ArgumentType<?, ?>> argumentTypes) {
        Argument.prepareFilter(stacks, argumentTypes, () -> true, null);
    }
    
    public static void prepareFilter(Collection<EntryStack<?>> stacks, Collection<ArgumentType<?, ?>> argumentTypes, BooleanSupplier isValid, @Nullable Executor executor) {
        if (prepareStage != null || currentStages != null) return;
        try {
            prepareStart = Util.getEpochMillis();
            Long2ObjectMap<Object>[] caches = CollectionUtils.map(argumentTypes, Argument::getSearchCache).toArray(Long2ObjectMap[]::new);
            prepareStacks = CollectionUtils.mapAndFilter(stacks, stack -> {
                for (Long2ObjectMap<Object> cache : caches) {
                    if (!cache.containsKey(stack.hashExact())) {
                        return true;
                    }
                }
                
                return false;
            }, HashedEntryStackWrapper::new);
            if (prepareStacks.isEmpty() && !isValid.getAsBoolean()) {
                return;
            }
            InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Preparing " + (prepareStacks.size() * argumentTypes.size()) + " stacks for search arguments");
            prepareStage = new IntIntMutablePair(0, argumentTypes.size());
            currentStages = new IntIntPair[argumentTypes.size()];
            int searchPartitionSize = ConfigObject.getInstance().getAsyncSearchPartitionSize();
            boolean async = ConfigObject.getInstance().shouldAsyncSearch() && prepareStacks.size() > searchPartitionSize * 4;
            List<CompletableFuture<Long2ObjectMap<Object>>> futures = Lists.newArrayList();
            List<Pair<ArgumentType<?, ?>, CompletableFuture<Long2ObjectMap<Object>>>> pairs = Lists.newArrayList();
            
            for (ArgumentType<?, ?> argumentType : argumentTypes) {
                prepareStage.first(prepareStage.firstInt() + 1);
                Long2ObjectMap<Object> map = getSearchCache(argumentType);
                IntIntPair currentStage = currentStages[prepareStage.firstInt() - 1] = new IntIntMutablePair(0, prepareStacks.size());
                if (!isValid.getAsBoolean()) return;
                
                if (async) {
                    for (Collection<HashedEntryStackWrapper> partitionStacks : CollectionUtils.partition(prepareStacks, searchPartitionSize)) {
                        CompletableFuture<Long2ObjectMap<Object>> future = CompletableFuture.supplyAsync(() -> {
                            Long2ObjectMap<Object> out = new Long2ObjectArrayMap<>(searchPartitionSize + 1);
                            int i = 0;
                            for (HashedEntryStackWrapper stack : partitionStacks) {
                                if (map.get(stack.hashExact()) == null) {
                                    Object data = argumentType.cacheData(stack.unwrap());
                                    
                                    if (data != null) {
                                        out.put(stack.hashExact(), data);
                                    }
                                }
                                if (i++ % 40 == 0) if (!isValid.getAsBoolean()) return Long2ObjectMaps.emptyMap();
                            }
                            if (!isValid.getAsBoolean()) return Long2ObjectMaps.emptyMap();
                            return out;
                        }, Objects.requireNonNullElse(executor, EXECUTOR_SERVICE)).whenComplete((objectLong2ObjectMap, throwable) -> {
                            currentStage.first(currentStage.firstInt() + partitionStacks.size());
                        });
                        futures.add(future);
                        pairs.add(Pair.of(argumentType, future));
                    }
                } else {
                    for (HashedEntryStackWrapper stack : prepareStacks) {
                        currentStage.first(currentStage.firstInt() + 1);
                        
                        if (map.get(stack.hashExact()) == null) {
                            Object data = argumentType.cacheData(stack.unwrap());
                            
                            if (data != null) {
                                map.put(stack.hashExact(), data);
                            }
                        }
                    }
                }
            }
            
            if (async) {
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
                } catch (ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException ignore) {
                } finally {
                    int sum = 0;
                    for (Pair<ArgumentType<?, ?>, CompletableFuture<Long2ObjectMap<Object>>> pair : pairs) {
                        Long2ObjectMap<Object> now = pair.second().getNow(null);
                        if (now != null) {
                            getSearchCache(pair.left()).putAll(now);
                            sum += now.size();
                        }
                    }
                    InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Prepared " + sum + " / " + (prepareStacks.size() * argumentTypes.size()) + " stacks for search arguments in " + (Util.getEpochMillis() - prepareStart) + "ms");
                }
            } else {
                InternalLogger.getInstance().log(ConfigObject.getInstance().doDebugSearchTimeRequired() ? Level.INFO : Level.TRACE, "Prepared " + (prepareStacks.size() * argumentTypes.size()) + " stacks for search arguments in " + (Util.getEpochMillis() - prepareStart) + "ms");
            }
        } finally {
            prepareStart = null;
            prepareStacks = null;
            prepareStage = null;
            currentStages = null;
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
    
}
