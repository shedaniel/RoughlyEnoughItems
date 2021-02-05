package me.shedaniel.rei.api.ingredient;

import me.shedaniel.rei.impl.Internals;

import java.util.List;

/**
 * An immutable representation of a list of {@link EntryStack}.
 */
public interface EntryIngredient extends List<EntryStack<?>> {
    static EntryIngredient empty() {
        return Internals.getEntryIngredientProvider().empty();
    }
    
    static <T> EntryIngredient of(EntryStack<T> stack) {
        return Internals.getEntryIngredientProvider().of(stack);
    }
    
    @SafeVarargs
    static <T> EntryIngredient of(EntryStack<T>... stacks) {
        return Internals.getEntryIngredientProvider().of(stacks);
    }
    
    static <T> EntryIngredient of(Iterable<EntryStack<T>> stacks) {
        return Internals.getEntryIngredientProvider().of(stacks);
    }
}
