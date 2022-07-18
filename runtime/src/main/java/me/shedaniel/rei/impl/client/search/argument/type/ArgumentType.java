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

import me.shedaniel.rei.api.client.gui.config.SearchMode;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.search.result.ArgumentApplicableResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public abstract class ArgumentType<T, R> {
    private int index = -1;
    
    public ArgumentType() {
    }
    
    public abstract String getName();
    
    @Nullable
    public String getPrefix() {
        return null;
    }
    
    public Style getHighlightedStyle() {
        return Style.EMPTY;
    }
    
    public SearchMode getSearchMode() {
        return SearchMode.PREFIX;
    }
    
    public ArgumentApplicableResult checkApplicable(String text, boolean forceGrammar) {
        String prefix = getPrefix();
        if (forceGrammar && !prefix.isEmpty()) {
            ArgumentApplicableResult status = checkApplicable(text, prefix);
            if (status.isApplicable()) {
                return status;
            }
        }
        if (!forceGrammar && getSearchMode() == SearchMode.ALWAYS) {
            ArgumentApplicableResult status = checkApplicable(text, "");
            if (status.isApplicable()) {
                status.notUsingGrammar();
            }
            return status;
        }
        return ArgumentApplicableResult.notApplicable();
    }
    
    private ArgumentApplicableResult checkApplicable(String text, String prefix) {
        if (prefix == null) return ArgumentApplicableResult.notApplicable();
        if (text.startsWith("-" + prefix)) return ArgumentApplicableResult.applyInverted(text.substring(1 + prefix.length())).grammar(0, prefix.length() + 1);
        if (!prefix.isEmpty() && text.startsWith(prefix + "-"))
            return ArgumentApplicableResult.applyInverted(text.substring(1 + prefix.length())).grammar(0, prefix.length() + 1);
        if (text.startsWith(prefix)) return ArgumentApplicableResult.apply(text.substring(prefix.length())).grammar(0, prefix.length());
        return ArgumentApplicableResult.notApplicable();
    }
    
    public abstract R cacheData(EntryStack<?> stack);
    
    public abstract T prepareSearchFilter(String searchText);
    
    public abstract void matches(R data, EntryStack<?> stack, T filterData, ResultSink sink);
    
    public int getIndex() {
        if (index >= 0) return index;
        return index = ArgumentTypesRegistry.ARGUMENT_TYPE_LIST.indexOf(this);
    }
    
    public interface ResultSink {
        boolean testTrue();
        
        boolean testString(String text);
    }
}
