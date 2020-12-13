package me.shedaniel.rei.api.entry;

public enum ComparisonContext {
    /**
     * Should only compare the type of the object
     */
    FUZZY(true, true),
    /**
     * Should compare the nbt and the type of the object
     */
    IGNORE_COUNT(true, false),
    /**
     * Should compare the amount and the type of the object
     */
    IGNORE_NBT(true, false),
    /**
     * Should compare the amount, the nbt and the type of the object
     */
    EXACT(false, false);
    
    boolean ignoresCount;
    boolean ignoresNbt;
    
    ComparisonContext(boolean ignoresCount, boolean ignoresNbt) {
        this.ignoresCount = ignoresCount;
        this.ignoresNbt = ignoresNbt;
    }
    
    public boolean isIgnoresCount() {
        return ignoresCount;
    }
    
    public boolean isIgnoresNbt() {
        return ignoresNbt;
    }
}