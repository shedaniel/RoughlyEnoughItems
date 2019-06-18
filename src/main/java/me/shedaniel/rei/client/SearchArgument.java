/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SearchArgument {
    
    public static final SearchArgument ALWAYS = new SearchArgument(ArgumentType.ALWAYS, "", true);
    private ArgumentType argumentType;
    private String text;
    public final Function<String, Boolean> INCLUDE = s -> boyerMooreHorspoolSearch(text, s) > -1;
    public final Function<String, Boolean> NOT_INCLUDE = s -> boyerMooreHorspoolSearch(text, s) <= -1;
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
    
    public static int boyerMooreHorspoolSearch(CharSequence pattern, CharSequence text) {
        int shift[] = new int[256];
        for(int k = 0; k < 256; k++)
            shift[k] = pattern.length();
        for(int k = 0; k < pattern.length() - 1; k++)
            shift[pattern.charAt(k)] = pattern.length() - 1 - k;
        int i = 0, j = 0;
        while ((i + pattern.length()) <= text.length()) {
            j = pattern.length() - 1;
            while (text.charAt(i + j) == pattern.charAt(j)) {
                j -= 1;
                if (j < 0)
                    return i;
            }
            i = i + shift[text.charAt(i + pattern.length() - 1)];
        }
        return -1;
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
