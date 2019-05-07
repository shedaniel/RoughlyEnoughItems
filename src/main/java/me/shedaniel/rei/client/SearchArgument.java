package me.shedaniel.rei.client;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SearchArgument {
    
    public static final Function<Integer, Boolean> INCLUDE = integer -> integer > -1;
    public static final Function<Integer, Boolean> NOT_INCLUDE = integer -> integer <= -1;
    private ArgumentType argumentType;
    private String text;
    private boolean include;
    private Pattern pattern;
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include) {
        this(argumentType, text, include, true);
    }
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include, boolean autoLowerCase) {
        this.argumentType = argumentType;
        this.text = autoLowerCase ? text.toLowerCase(Locale.ROOT) : text;
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
        TEXT,
        MOD,
        TOOLTIP
    }
    
}
