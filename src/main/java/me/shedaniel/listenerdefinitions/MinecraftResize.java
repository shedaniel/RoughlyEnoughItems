package me.shedaniel.listenerdefinitions;

/**
 * Created by James on 7/28/2018.
 */
public interface MinecraftResize extends IEvent {
    
    public void resize(int scaledWidth, int scaledHeight);
    
}
