/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import java.util.Locale;
import java.util.function.Function;

public class SearchArgument {
    
    public static final SearchArgument ALWAYS = new SearchArgument(ArgumentType.ALWAYS, "", true);
    private ArgumentType argumentType;
    private String text;
    public final Function<String, Boolean> INCLUDE = s -> s.contains(text);
    public final Function<String, Boolean> NOT_INCLUDE = s -> !s.contains(text);
    private boolean include;
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include) {
        this(argumentType, text, include, true);
    }
    
    public SearchArgument(ArgumentType argumentType, String text, boolean include, boolean autoLowerCase) {
        this.argumentType = argumentType;
        this.text = autoLowerCase ? text.toLowerCase(Locale.ROOT) : text;
        this.include = include;
    }
    
    public Function<String, Boolean> getFunction(boolean include) {
        return include ? INCLUDE : NOT_INCLUDE;
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
        TOOLTIP,
        ALWAYS
    }
    
}
