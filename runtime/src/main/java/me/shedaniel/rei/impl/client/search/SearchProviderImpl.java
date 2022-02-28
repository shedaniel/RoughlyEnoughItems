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

package me.shedaniel.rei.impl.client.search;

import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.api.client.search.SearchProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.search.argument.AlternativeArgument;
import me.shedaniel.rei.impl.client.search.argument.Argument;
import me.shedaniel.rei.impl.client.search.argument.CompoundArgument;
import me.shedaniel.rei.impl.client.search.argument.type.ArgumentType;
import me.shedaniel.rei.impl.client.util.CrashReportUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        private final List<ArgumentType<?, ?>> argumentTypes;
        
        public SearchFilterImpl(List<CompoundArgument> arguments, String searchTerm) {
            this.arguments = arguments;
            this.filter = searchTerm;
            this.argumentTypes = arguments.stream()
                    .flatMap(CompoundArgument::stream)
                    .flatMap(AlternativeArgument::stream)
                    .map(Argument::getArgument)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        @Override
        public boolean test(EntryStack<?> stack) {
            try {
                return Argument.matches(stack, arguments);
            } catch (Throwable throwable) {
                CrashReport report = CrashReportUtils.essential(throwable, "Testing entry with search filter");
                CrashReportCategory category = report.addCategory("Search entry details");
                try {
                    stack.fillCrashReport(report, category);
                } catch (Throwable throwable1) {
                    category.setDetailError("Filling Report", throwable1);
                }
                throw CrashReportUtils.throwReport(report);
            }
        }
        
        @Override
        public void prepareFilter(Collection<EntryStack<?>> stacks) {
            Argument.prepareFilter(stacks, argumentTypes);
        }
        
        @Override
        public String getFilter() {
            return filter;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SearchFilterImpl that = (SearchFilterImpl) o;
            return Objects.equals(filter, that.filter);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(filter);
        }
    }
}
