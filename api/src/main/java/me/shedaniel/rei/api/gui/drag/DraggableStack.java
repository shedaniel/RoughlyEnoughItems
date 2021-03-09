package me.shedaniel.rei.api.gui.drag;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ingredient.EntryStack;

public interface DraggableStack {
    EntryStack<?> getStack();
    
    void drag();
    
    void release(boolean accepted);
    
    default void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        getStack().render(matrices, bounds, mouseX, mouseY, delta);
    }
}