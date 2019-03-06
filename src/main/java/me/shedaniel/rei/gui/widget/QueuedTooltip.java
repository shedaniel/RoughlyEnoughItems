package me.shedaniel.rei.gui.widget;


import com.google.common.collect.Lists;
import me.shedaniel.rei.client.ClientHelper;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class QueuedTooltip {
    
    private Point location;
    private List<String> text;
    
    protected QueuedTooltip(Point location, List<String> text) {
        this.location = location;
        this.text = new LinkedList<>(text);
    }
    
    public static QueuedTooltip create(Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public static QueuedTooltip create(List<String> text) {
        return QueuedTooltip.create(ClientHelper.getMouseLocation(), text);
    }
    
    public static QueuedTooltip create(String... text) {
        return QueuedTooltip.create(ClientHelper.getMouseLocation(), text);
    }
    
    public Point getLocation() {
        return location;
    }
    
    public List<String> getText() {
        return text;
    }
    
}
