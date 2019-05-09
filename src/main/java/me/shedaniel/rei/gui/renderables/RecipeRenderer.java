package me.shedaniel.rei.gui.renderables;

import me.shedaniel.rei.api.Renderer;

public abstract class RecipeRenderer extends Renderer {
    
    public abstract int getHeight();
    
    public final int getWidth() {
        return 100;
    }
    
}
