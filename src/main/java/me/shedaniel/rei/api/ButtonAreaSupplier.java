package me.shedaniel.rei.api;

import java.awt.*;

public interface ButtonAreaSupplier {
    
    Rectangle get(Rectangle bounds);
    
    default String getButtonText() {
        return "+";
    }
    
}
