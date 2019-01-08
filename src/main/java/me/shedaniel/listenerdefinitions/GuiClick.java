package me.shedaniel.listenerdefinitions;

/**
 * Created by James on 7/29/2018.
 */
public interface GuiClick extends IEvent {
    
    public boolean onClick(int x, int y, int button);
    
}
