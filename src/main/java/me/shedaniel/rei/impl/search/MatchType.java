package me.shedaniel.rei.impl.search;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public enum MatchType {
    INVERT_MATCHED,
    UNMATCHED,
    MATCHED;
    
    public boolean isMatched() {
        return this != UNMATCHED;
    }
    
    public boolean isInverted() {
        return this == INVERT_MATCHED;
    }
}
