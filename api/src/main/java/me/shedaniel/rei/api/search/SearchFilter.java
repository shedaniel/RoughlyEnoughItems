package me.shedaniel.rei.api.search;

import me.shedaniel.rei.api.ingredient.EntryStack;

import java.util.function.Predicate;

public interface SearchFilter extends Predicate<EntryStack<?>> {
    static SearchFilter matchAll() {
        return new SearchFilter() {
            @Override
            public String getFilter() {
                return "";
            }
            
            @Override
            public boolean test(EntryStack<?> entryStack) {
                return true;
            }
        };
    }
    
    static SearchFilter matchNone() {
        return new SearchFilter() {
            @Override
            public String getFilter() {
                return "";
            }
            
            @Override
            public boolean test(EntryStack<?> entryStack) {
                return false;
            }
        };
    }
    
    /**
     * Returns the original filter in {@link String}.
     *
     * @return the original filter
     */
    String getFilter();
}
