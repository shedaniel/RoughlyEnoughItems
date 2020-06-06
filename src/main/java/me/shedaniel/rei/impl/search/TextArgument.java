package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
public final class TextArgument extends Argument {
    public static final TextArgument INSTANCE = new TextArgument();
    
    @Override
    public String getName() {
        return "text";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            data[getDataOrdinal()] = stack.asFormatStrippedText().getString().toLowerCase(Locale.ROOT);
        }
        return ((String) data[getDataOrdinal()]).contains(searchText);
    }
    
    private TextArgument() {
    }
}
