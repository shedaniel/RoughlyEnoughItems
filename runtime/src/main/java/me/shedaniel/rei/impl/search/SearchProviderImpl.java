package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.search.SearchFilter;
import me.shedaniel.rei.api.search.SearchProvider;

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
