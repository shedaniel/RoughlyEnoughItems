package me.shedaniel.rei.api.common.entry;

import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

public interface InputIngredient<T> {
    static <T> InputIngredient<T> empty(int index) {
        return of(index, Collections.emptyList());
    }
    
    static <T> InputIngredient<T> of(int index, List<T> ingredient) {
        return new InputIngredient<>() {
            @Override
            public List<T> get() {
                return ingredient;
            }
            
            @Override
            public int getIndex() {
                return index;
            }
        };
    }
    
    static <T> InputIngredient<T> withType(InputIngredient<EntryStack<?>> ingredient, EntryType<T> type) {
        return new InputIngredient<>() {
            @SuppressWarnings("RedundantTypeArguments")
            List<T> list = CollectionUtils.<EntryStack<?>, T>filterAndMap(ingredient.get(),
                    stack -> stack.getType() == type, EntryStack::castValue);
            
            @Override
            public List<T> get() {
                return list;
            }
            
            @Override
            public int getIndex() {
                return ingredient.getIndex();
            }
        };
    }
    
    List<T> get();
    
    int getIndex();
}
