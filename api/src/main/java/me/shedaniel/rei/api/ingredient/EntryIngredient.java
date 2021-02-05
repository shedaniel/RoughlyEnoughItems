package me.shedaniel.rei.api.ingredient;

import java.util.List;

/**
 * An immutable representation of a list of {@link EntryStack}.
 */
public interface EntryIngredient extends List<EntryStack<?>> {
    static EntryIngredient empty() {
        
    }
    
    static <T> EntryIngredient of(EntryStack<T> stack) {
        
    }
    
    @SafeVarargs
    static <T> EntryIngredient of(EntryStack<T>... stacks) {
        
    }
    
    static <T> EntryIngredient of(Iterable<EntryStack<T>> stacks) {
        
    }
}
