package me.shedaniel.rei.api;

import java.awt.*;

public interface SpeedCraftAreaSupplier {
    
    Rectangle get(Rectangle bounds);
    
    default String getButtonText() {
        return "+";
    }
    
}
