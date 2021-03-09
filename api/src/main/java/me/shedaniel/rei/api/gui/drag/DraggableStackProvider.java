package me.shedaniel.rei.api.gui.drag;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@FunctionalInterface
public interface DraggableStackProvider {
    static DraggableStackProvider from(Supplier<Iterable<DraggableStackProvider>> providers) {
        return (context, mouseX, mouseY) -> {
            for (DraggableStackProvider provider : providers.get()) {
                DraggableStack stack = provider.getHoveredStack(context, mouseX, mouseY);
                if (stack != null) return stack;
            }
            return null;
        };
    }
    
    @Nullable
    DraggableStack getHoveredStack(DraggingContext context, double mouseX, double mouseY);
}