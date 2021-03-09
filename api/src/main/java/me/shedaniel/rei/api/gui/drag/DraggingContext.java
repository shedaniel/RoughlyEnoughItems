package me.shedaniel.rei.api.gui.drag;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.REIHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface DraggingContext {
    static DraggingContext getInstance() {
        return REIHelper.getInstance().getOverlay().get().getDraggingContext();
    }
    
    default boolean isDraggingStack() {
        return getCurrentStack() != null;
    }
    
    @Nullable
    DraggableStack getCurrentStack();
    
    @Nullable
    Point getCurrentPosition();
    
    void registerRenderBackToPosition(DraggableStack stack, Supplier<Point> position);
}
