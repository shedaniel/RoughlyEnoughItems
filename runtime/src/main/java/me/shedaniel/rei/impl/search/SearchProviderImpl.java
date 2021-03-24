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

import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.ingredient.EntryStack;

import java.util.List;

public class SearchProviderImpl implements SearchProvider {
    @Override
    public void startReload() {
        
    }
    
    @Override
    public SearchFilter createFilter(String searchTerm) {
        return new SearchFilterImpl(Argument.bakeArguments(searchTerm), searchTerm);
    }
    
    public static class SearchFilterImpl implements SearchFilter {
        private final List<CompoundArgument> arguments;
        private final String filter;
        
        public SearchFilterImpl(List<CompoundArgument> arguments, String searchTerm) {
            this.arguments = arguments;
            this.filter = searchTerm;
        }
        
        @Override
        public boolean test(EntryStack<?> stack) {
            return Argument.matches(stack, arguments);
        }
        
        @Override
        public String getFilter() {
            return filter;
        }
    }
}
