package me.shedaniel.api;

import me.shedaniel.listenerdefinitions.IEvent;

/**
 * Created by James on 7/27/2018.
 */
public interface IREIPlugin extends IEvent {
    
    public void register();
}
