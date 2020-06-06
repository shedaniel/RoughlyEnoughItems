package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@ApiStatus.Internal
public final class RegexArgument extends Argument {
    public static final RegexArgument INSTANCE = new RegexArgument();
    
    @Override
    public String getName() {
        return "regex";
    }
    
    @Override
    public MatchStatus matchesArgumentPrefix(String text) {
        boolean inverted = false;
        String matchText = text;
        if (matchText.startsWith("-")) {
            inverted = true;
            matchText = matchText.substring(1);
        }
        if (matchText.length() >= 3 && matchText.startsWith("r/") && matchText.endsWith("/"))
            return !inverted ? MatchStatus.matched(matchText.substring(2, matchText.length() - 1), true) : MatchStatus.invertMatched(matchText.substring(2, matchText.length() - 1), true);
        return MatchStatus.unmatched();
    }
    
    @Override
    public Object prepareSearchData(String searchText) {
        try {
            return Pattern.compile(searchText);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        Pattern pattern = (Pattern) searchData;
        if (pattern == null) return false;
        if (data[getDataOrdinal()] == null) {
            String name = stack.asFormatStrippedText().getString();
            data[getDataOrdinal()] = name;
        }
        Matcher matcher = pattern.matcher((String) data[getDataOrdinal()]);
        return matcher != null && matcher.matches();
    }
    
    private RegexArgument() {
    }
}

