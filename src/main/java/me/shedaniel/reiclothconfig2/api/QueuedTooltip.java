package me.shedaniel.reiclothconfig2.api;

import com.google.common.collect.Lists;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class QueuedTooltip {
    
    private Point location;
    private List<String> text;
    
    private QueuedTooltip(Point location, List<String> text) {
        this.location = location;
        this.text = Collections.unmodifiableList(text);
    }
    
    public static QueuedTooltip create(Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public Point getLocation() {
        return location;
    }
    
    public int getX() {
        return getLocation().x;
    }
    
    public int getY() {
        return getLocation().y;
    }
    
    public List<String> getText() {
        return text;
    }
    
}