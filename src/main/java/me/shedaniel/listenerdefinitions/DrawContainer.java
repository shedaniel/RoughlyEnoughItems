package me.shedaniel.listenerdefinitions;

import net.minecraft.client.gui.ContainerGui;

/**
 * Created by James on 7/27/2018.
 */
public interface DrawContainer extends IEvent {
    public void draw(int mouseX, int mouseY, float dunno, ContainerGui gui);
}
