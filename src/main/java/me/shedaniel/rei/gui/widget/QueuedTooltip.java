package me.shedaniel.rei.gui.widget;


import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class QueuedTooltip {
    
    public Point mouse;
    public List<String> text;
    
    public QueuedTooltip(@NotNull Point mouse, @NotNull List<String> text) {
        this.mouse = mouse;
        this.text = new LinkedList<>(text);
    }
    
}
