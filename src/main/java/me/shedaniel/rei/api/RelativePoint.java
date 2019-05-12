/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

public class RelativePoint {
    
    private double relativeX, relativeY;
    
    public RelativePoint(double relativeX, double relativeY) {
        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }
    
    public double getRelativeX() {
        return relativeX;
    }
    
    public double getRelativeY() {
        return relativeY;
    }
    
    public double getX(double width) {
        return width * relativeX;
    }
    
    public double getY(double height) {
        return height * relativeY;
    }
    
}
