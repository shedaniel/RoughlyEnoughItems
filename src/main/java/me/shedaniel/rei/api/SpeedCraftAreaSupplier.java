package me.shedaniel.rei.api;

import java.awt.*;

public interface SpeedCraftAreaSupplier {
    
    public Rectangle get(Rectangle bounds);
    
    default String getButtonText() {
        return "+";
    }
    
}
