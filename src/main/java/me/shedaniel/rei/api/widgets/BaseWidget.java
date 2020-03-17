package me.shedaniel.rei.api.widgets;

import me.shedaniel.math.Point;
import me.shedaniel.rei.gui.widget.WidgetWithBounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;

public abstract class BaseWidget<T extends BaseWidget<T>> extends WidgetWithBounds {
    @Nullable
    private BiPredicate<T, Point> containsMousePredicate;
    
    public final void setContainsMousePredicate(@Nullable BiPredicate<T, Point> predicate) {
        this.containsMousePredicate = predicate;
    }
    
    @NotNull
    public final T containsMousePredicate(@Nullable BiPredicate<T, Point> predicate) {
        setContainsMousePredicate(predicate);
        return (T) this;
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        if (containsMousePredicate != null)
            return containsMousePredicate.test((T) this, new Point(mouseX, mouseY));
        return super.containsMouse(mouseX, mouseY);
    }
}
