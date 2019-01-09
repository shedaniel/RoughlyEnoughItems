package me.shedaniel.rei.gui.widget;

import net.minecraft.client.gui.GuiEventListener;

public interface IWidget extends GuiEventListener {
    
    public void draw(int mouseX, int mouseY, float partialTicks);
    
}
