package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public abstract class Argument {
    public Argument() {
    }
    
    private int dataOrdinal = -1;
    
    public abstract String getName();
    
    @Nullable
    public String getPrefix() {
        return null;
    }
    
    public MatchStatus matchesArgumentPrefix(String text) {
        String prefix = getPrefix();
        if (prefix == null) return MatchStatus.unmatched();
        if (text.startsWith("-" + prefix)) return MatchStatus.invertMatched(text.substring(1 + prefix.length()));
        if (text.startsWith(prefix + "-")) return MatchStatus.invertMatched(text.substring(1 + prefix.length()));
        return text.startsWith(prefix) ? MatchStatus.matched(text.substring(prefix.length())) : MatchStatus.unmatched();
    }
    
    public final int getDataOrdinal() {
        if (dataOrdinal == -1) {
            dataOrdinal = ArgumentsRegistry.ARGUMENTS.indexOf(this);
        }
        return dataOrdinal;
    }
    
    public abstract boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData);
    
    public Object prepareSearchData(String searchText) {
        return null;
    }
}
