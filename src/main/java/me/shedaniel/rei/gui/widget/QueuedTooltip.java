/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;


import com.google.common.collect.Lists;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.impl.PointHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

public class QueuedTooltip {
    
    private Point location;
    private List<String> text;
    private Consumer<QueuedTooltip> consumer = null;
    
    private QueuedTooltip(Point location, List<String> text) {
        this.location = location;
        this.text = Lists.newArrayList(text);
    }
    
    public static QueuedTooltip create(Point location, List<String> text) {
        return new QueuedTooltip(location, text);
    }
    
    public static QueuedTooltip create(Point location, String... text) {
        return QueuedTooltip.create(location, Lists.newArrayList(text));
    }
    
    public static QueuedTooltip create(List<String> text) {
        return QueuedTooltip.create(PointHelper.fromMouse(), text);
    }
    
    public static QueuedTooltip create(String... text) {
        return QueuedTooltip.create(PointHelper.fromMouse(), text);
    }
    
    @ApiStatus.Internal
    public QueuedTooltip setSpecialRenderer(Consumer<QueuedTooltip> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    @ApiStatus.Internal
    public Consumer<QueuedTooltip> getConsumer() {
        return consumer;
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
