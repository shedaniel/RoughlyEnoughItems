package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.search.OverlaySearchField;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class SearchFieldWidgetProvider implements OverlayWidgetProvider {
    private OverlaySearchField searchField;
    
    @Override
    public List<Widget> provide(ScreenOverlay overlay, MenuAccess access, Consumer<TextField> textFieldSink, UnaryOperator<Widget> lateRenderable) {
        if (searchField == null) {
            searchField = new OverlaySearchField(access);
        }
        
        textFieldSink.accept(searchField);
        return List.of(lateRenderable.apply(searchField));
    }
}
