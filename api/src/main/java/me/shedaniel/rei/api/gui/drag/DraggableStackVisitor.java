package me.shedaniel.rei.api.gui.drag;

import java.util.Optional;
import java.util.function.Supplier;

@FunctionalInterface
public interface DraggableStackVisitor {
    static DraggableStackVisitor from(Supplier<Iterable<DraggableStackVisitor>> visitors) {
        return (stack) -> {
            for (DraggableStackVisitor visitor : visitors.get()) {
                Optional<Acceptor> acceptor = visitor.visitDraggedStack(stack);
                if (acceptor.isPresent()) return acceptor;
            }
            return Optional.empty();
        };
    }
    
    Optional<Acceptor> visitDraggedStack(DraggableStack stack);
    
    interface Acceptor {
        void accept(DraggableStack stack);
    }
}
