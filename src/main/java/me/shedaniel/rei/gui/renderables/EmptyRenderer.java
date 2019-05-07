package me.shedaniel.rei.gui.renderables;

import me.shedaniel.rei.api.Renderer;

public class EmptyRenderer extends Renderer {
    
    public static final EmptyRenderer INSTANCE = new EmptyRenderer();
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
    
    }
}
