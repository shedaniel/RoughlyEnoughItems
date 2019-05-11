/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import java.awt.*;

public interface ButtonAreaSupplier {
    
    /**
     * Declares the button bounds
     *
     * @param bounds the bounds of the recipe display
     * @return the bounds of the button
     */
    Rectangle get(Rectangle bounds);
    
    /**
     * Declares the button text
     *
     * @return the text of the button
     */
    default String getButtonText() {
        return "+";
    }
    
}
