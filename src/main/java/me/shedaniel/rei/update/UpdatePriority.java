package me.shedaniel.rei.update;

import java.util.Arrays;

public enum UpdatePriority {
    
    NONE, LIGHT;
    
    public static UpdatePriority fromString(String string) {
        return Arrays.stream(values()).filter(updatePriority -> updatePriority.name().toLowerCase().equals(string)).findFirst().orElse(NONE);
    }
    
}
