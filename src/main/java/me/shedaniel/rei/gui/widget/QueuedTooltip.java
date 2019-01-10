package me.shedaniel.rei.gui.widget;


import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class QueuedTooltip {
    
    public Point mouse;
    public List<String> text;
    
    public QueuedTooltip(Point mouse, List<String> text) {
        this.mouse = mouse;
        this.text = new LinkedList<>(text);
    }
    
}
