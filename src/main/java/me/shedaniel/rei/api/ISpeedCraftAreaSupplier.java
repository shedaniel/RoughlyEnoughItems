package me.shedaniel.rei.api;

import java.awt.*;

public interface ISpeedCraftAreaSupplier {
    
    public Rectangle get(Rectangle bounds);
    
    default String getButtonText() {
        return "+";
    }
    
}
