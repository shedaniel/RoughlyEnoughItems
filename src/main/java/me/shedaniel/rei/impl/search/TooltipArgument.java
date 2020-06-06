package me.shedaniel.rei.impl.search;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.impl.SearchArgument;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@ApiStatus.Internal
public final class TooltipArgument extends Argument {
    public static final TooltipArgument INSTANCE = new TooltipArgument();
    
    @Override
    public String getName() {
        return "tooltip";
    }
    
    @Override
    public @Nullable String getPrefix() {
        return "#";
    }
    
    @Override
    public boolean matches(Object[] data, EntryStack stack, String searchText, Object searchData) {
        if (data[getDataOrdinal()] == null) {
            data[getDataOrdinal()] = SearchArgument.tryGetEntryStackTooltip(stack).toLowerCase(Locale.ROOT);
        }
        String tooltip = (String) data[getDataOrdinal()];
        return tooltip.isEmpty() || tooltip.contains(searchText);
    }
    
    private TooltipArgument() {
    }
}
