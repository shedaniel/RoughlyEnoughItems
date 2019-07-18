package me.shedaniel.reiclothconfig2.gui.entries;

import net.minecraft.client.gui.GuiEventHandler;

import java.util.Optional;

public abstract class BaseListCell extends GuiEventHandler {
    
    public abstract Optional<String> getError();
    
    public abstract int getCellHeight();
    
    public abstract void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta);
    
}