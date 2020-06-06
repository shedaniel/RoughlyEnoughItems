package me.shedaniel.rei.impl.search;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ApiStatus.Internal
public final class MatchStatus {
    private static final MatchStatus UNMATCHED = new MatchStatus(MatchType.UNMATCHED, null, false);
    private final MatchType type;
    @Nullable
    private final String text;
    private final boolean preserveCasing;
    
    private MatchStatus(MatchType type, @Nullable String text, boolean preserveCasing) {
        this.type = type;
        this.text = text;
        this.preserveCasing = preserveCasing;
    }
    
    public static MatchStatus unmatched() {
        return UNMATCHED;
    }
    
    public static MatchStatus invertMatched(@NotNull String text) {
        return invertMatched(text, false);
    }
    
    public static MatchStatus invertMatched(@NotNull String text, boolean preserveCasing) {
        return new MatchStatus(MatchType.INVERT_MATCHED, Objects.requireNonNull(text), preserveCasing);
    }
    
    public static MatchStatus matched(@NotNull String text) {
        return matched(text, false);
    }
    
    public static MatchStatus matched(@NotNull String text, boolean preserveCasing) {
        return new MatchStatus(MatchType.MATCHED, Objects.requireNonNull(text), preserveCasing);
    }
    
    public boolean isMatched() {
        return type != MatchType.UNMATCHED;
    }
    
    public boolean isInverted() {
        return type == MatchType.INVERT_MATCHED;
    }
    
    public boolean shouldPreserveCasing() {
        return preserveCasing;
    }
    
    @Nullable
    public String getText() {
        return text;
    }
    
    public MatchType getType() {
        return type;
    }
}
