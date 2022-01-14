package me.shedaniel.rei.impl.client.gui.modules;

public abstract class AbstractMenuEntry extends MenuEntry {
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    
    @Override
    public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
        this.x = xPos;
        this.y = yPos;
        this.selected = selected;
        this.containsMouse = containsMouse;
        this.rendering = rendering;
        this.width = width;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isRendering() && mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getEntryHeight()) {
            if (onClick(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    protected boolean onClick(double mouseX, double mouseY, int button) {
        return false;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public boolean containsMouse() {
        return containsMouse;
    }
    
    public boolean isRendering() {
        return rendering;
    }
}
