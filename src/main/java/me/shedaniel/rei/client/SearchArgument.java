package me.shedaniel.rei.client;

import java.util.function.Function;

public class SearchArgument {
    
    public static final Function<Integer, Boolean> INCLUDE = integer -> integer > -1;
    public static final Function<Integer, Boolean> NOT_INCLUDE = integer -> !INCLUDE.apply(integer);
    private ArgumentType argumentType;
    private String text;
    private boolean include;
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include) {
        this.argumentType = argumentType;
        this.text = text;
        this.include = include;
    }
    
    public static Function<Integer, Boolean> getFunction(boolean include) {
        return include ? SearchArgument.INCLUDE : SearchArgument.NOT_INCLUDE;
    }
    
    public ArgumentType getArgumentType() {
        return argumentType;
    }
    
    public String getText() {
        return text;
    }
    
    public boolean isInclude() {
        return include;
    }
    
    @Override
    public String toString() {
        return String.format("Argument[%s]: name = %s, include = %b", argumentType.name(), text, include);
    }
    
    public enum ArgumentType {
        TEXT, MOD, TOOLTIP
    }
    
}
