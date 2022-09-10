package me.shedaniel.rei.impl.client.gui.overlay.widgets;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.impl.client.gui.menu.MenuAccess;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.search.OverlaySearchField;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.UnaryOperator;

public class SearchFieldWidgetProvider implements OverlayWidgetProvider {
    private OverlaySearchField searchField;
    
    @Override
    public List<Widget> provide(ScreenOverlay overlay, MenuAccess access, TextFieldSink textFieldSink, UnaryOperator<Widget> lateRenderable) {
        if (searchField == null) {
            searchField = new OverlaySearchField(access);
        }
        
        searchField.getBounds().setBounds(getSearchFieldArea(overlay));
        textFieldSink.accept(searchField, searchField::isHighlighting);
        return List.of(lateRenderable.apply(searchField));
    }
    
    private Rectangle getSearchFieldArea(ScreenOverlay overlay) {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        return switch (REIRuntime.getInstance().getContextualSearchFieldLocation()) {
            case TOP_SIDE -> getTopSideSearchFieldArea(overlay, widthRemoved);
            case BOTTOM_SIDE -> getBottomSideSearchFieldArea(overlay, widthRemoved);
            case CENTER -> getCenterSearchFieldArea(overlay, widthRemoved);
        };
    }
    
    private Rectangle getTopSideSearchFieldArea(ScreenOverlay overlay, int widthRemoved) {
        return new Rectangle(overlay.getBounds().x + 2, 4, overlay.getBounds().width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(ScreenOverlay overlay, int widthRemoved) {
        Window window = Minecraft.getInstance().getWindow();
        return new Rectangle(overlay.getBounds().x + 2, window.getGuiScaledHeight() - 22, overlay.getBounds().width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getCenterSearchFieldArea(ScreenOverlay overlay, int widthRemoved) {
        Window window = Minecraft.getInstance().getWindow();
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(Minecraft.getInstance().screen);
        return new Rectangle(screenBounds.x, window.getGuiScaledHeight() - 22, screenBounds.width - widthRemoved, 18);
    }
}
