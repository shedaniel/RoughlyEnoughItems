/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
