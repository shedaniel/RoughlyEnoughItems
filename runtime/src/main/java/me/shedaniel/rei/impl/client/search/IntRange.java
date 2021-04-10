package me.shedaniel.rei.impl.client.search;

import java.util.Objects;

public class IntRange {
    private final int min;
    private final int max;
    
    private IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    public static IntRange of(int min, int max) {
        return new IntRange(min, max);
    }
    
    public int min() {
        return min;
    }
    
    public int max() {
        return max;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntRange intRange = (IntRange) o;
        return min == intRange.min && max == intRange.max;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
