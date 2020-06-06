package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class AlwaysMatchingArgument extends Argument {
    public static final AlwaysMatchingArgument INSTANCE = new AlwaysMatchingArgument();
    
    @Override
    public String getName() {
        return "always";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        return true;
    }
    
    @Override
    public MatchStatus matchesArgumentPrefix(String text) {
        return MatchStatus.unmatched();
    }
    
    private AlwaysMatchingArgument() {
    }
}
