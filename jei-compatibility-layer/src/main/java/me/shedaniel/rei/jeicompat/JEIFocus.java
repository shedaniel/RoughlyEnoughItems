package me.shedaniel.rei.jeicompat;

import mezz.jei.api.recipe.IFocus;
import org.jetbrains.annotations.NotNull;

public class JEIFocus<T> implements IFocus<T> {
    private final Mode mode;
    private final T value;
    
    public JEIFocus(IFocus<T> focus) {
        this(focus.getMode(), focus.getValue());
    }
    
    public JEIFocus(Mode mode, T value) {
        this.mode = mode;
        this.value = value;
    }
    
    @Override
    @NotNull
    public Mode getMode() {
        return mode;
    }
    
    @Override
    @NotNull
    public T getValue() {
        return value;
    }
}
