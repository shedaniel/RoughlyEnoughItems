package me.shedaniel.rei.impl.client.gui.widget.favorites.element;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitorWidget;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface FavoritesListElement extends Widget, GuiEventListener, DraggableComponentProviderWidget<Object>, DraggableComponentVisitorWidget {
    default EntryStack<?> getFocusedStack(Point mouse) {
        return EntryStack.empty();
    }
    
    @Override
    @Nullable
    default DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        return null;
    }
    
    @Override
    default DraggedAcceptorResult acceptDragged(DraggingContext<Screen> context, DraggableComponent<?> component) {
        return DraggedAcceptorResult.PASS;
    }
    
    default Stream<Rectangle> getExclusionZones() {
        return Stream.empty();
    }
}
