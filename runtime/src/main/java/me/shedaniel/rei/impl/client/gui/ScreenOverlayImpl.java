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

package me.shedaniel.rei.impl.client.gui;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.impl.client.gui.overlay.AbstractScreenOverlay;
import me.shedaniel.rei.impl.client.gui.widget.ConfigButtonWidget;
import me.shedaniel.rei.impl.client.gui.widget.CraftableFilterButtonWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.PaginatedEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.ScrolledEntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
public abstract class ScreenOverlayImpl extends AbstractScreenOverlay {
    private EntryListWidget entryListWidget = null;
    private FavoritesListWidget favoritesListWidget = null;
    private OverlaySearchField searchField = null;
    
    public static EntryListWidget getEntryListWidget() {
        return getInstance().getEntryList();
    }
    
    @Nullable
    public static FavoritesListWidget getFavoritesListWidget() {
        return getInstance().getFavoritesListNullable();
    }
    
    public static ScreenOverlayImpl getInstance() {
        return (ScreenOverlayImpl) REIRuntime.getInstance().getOverlay().get();
    }
    
    @Override
    public void init() {
        super.init();
        
        TextField searchField = getSearchField();
        searchField.asWidget().getBounds().setBounds(getSearchFieldArea());
        this.children().add(searchField.asWidget());
        
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            getFavoritesListWidget().favoritePanel.resetRows();
            this.children().add(getFavoritesListWidget());
        }
        
        EntryListWidget entryListWidget = getEntryListWidget();
        entryListWidget.updateArea(this.getBounds(), searchField.getText());
        this.children().add(entryListWidget);
        searchField.setResponder(s -> entryListWidget.updateSearch(s, false));
        entryListWidget.init(this);
        
        this.children().add(ConfigButtonWidget.create(this));
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) {
            this.children().add(CraftableFilterButtonWidget.create(this));
        }
    }
    
    private Rectangle getSearchFieldArea() {
        int widthRemoved = 1;
        if (ConfigObject.getInstance().isCraftableFilterEnabled()) widthRemoved += 22;
        if (ConfigObject.getInstance().isLowerConfigButton()) widthRemoved += 22;
        SearchFieldLocation searchFieldLocation = REIRuntime.getInstance().getContextualSearchFieldLocation();
        return switch (searchFieldLocation) {
            case TOP_SIDE -> getTopSideSearchFieldArea(widthRemoved);
            case BOTTOM_SIDE -> getBottomSideSearchFieldArea(widthRemoved);
            case CENTER -> getCenterSearchFieldArea(widthRemoved);
        };
    }
    
    private Rectangle getTopSideSearchFieldArea(int widthRemoved) {
        return new Rectangle(getBounds().x + 2, 4, getBounds().width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getBottomSideSearchFieldArea(int widthRemoved) {
        Window window = Minecraft.getInstance().getWindow();
        return new Rectangle(getBounds().x + 2, window.getGuiScaledHeight() - 22, getBounds().width - 6 - widthRemoved, 18);
    }
    
    private Rectangle getCenterSearchFieldArea(int widthRemoved) {
        Window window = Minecraft.getInstance().getWindow();
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(minecraft.screen);
        return new Rectangle(screenBounds.x, window.getGuiScaledHeight() - 22, screenBounds.width - widthRemoved, 18);
    }
    
    @Override
    protected void updateSearch() {
        getEntryListWidget().updateSearch(getSearchField().getText(), true);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (!hasSpace()) return false;
        if (!REIRuntime.getInstance().isOverlayVisible()) return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            getSearchField().setFocused(true);
            setFocused(getSearchField());
            getSearchField().keybindFocusTime = System.currentTimeMillis();
            getSearchField().keybindFocusKey = keyCode;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        boolean visible = REIRuntime.getInstance().isOverlayVisible();
        if (!hasSpace() || !visible) return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesMouse(button)) {
            getSearchField().setFocused(true);
            setFocused(getSearchField().asWidget());
            getSearchField().keybindFocusTime = -1;
            getSearchField().keybindFocusKey = -1;
            return true;
        }
        return false;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        getSearchField().setFocused(focused == getSearchField().asWidget());
    }
    
    @Override
    public EntryListWidget getEntryList() {
        boolean widgetScrolled = ConfigObject.getInstance().isEntryListWidgetScrolled();
        
        if (entryListWidget != null) {
            if (widgetScrolled && entryListWidget instanceof ScrolledEntryListWidget) {
                return entryListWidget;
            } else if (!widgetScrolled && entryListWidget instanceof PaginatedEntryListWidget) {
                return entryListWidget;
            }
        }
        
        entryListWidget = widgetScrolled ? new ScrolledEntryListWidget() : new PaginatedEntryListWidget();
        
        Rectangle overlayBounds = getBounds();
        entryListWidget.updateArea(Objects.requireNonNullElse(overlayBounds, new Rectangle()), getSearchField().getText());
        entryListWidget.updateEntriesPosition();
        
        return entryListWidget;
    }
    
    @Override
    public Optional<OverlayListWidget> getFavoritesList() {
        return Optional.ofNullable(getFavoritesListNullable());
    }
    
    private FavoritesListWidget getFavoritesListNullable() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            if (favoritesListWidget == null) {
                favoritesListWidget = new FavoritesListWidget();
            }
            
            return favoritesListWidget;
        } else {
            return favoritesListWidget = null;
        }
    }
    
    @Override
    public OverlaySearchField getSearchField() {
        if (searchField == null) {
            searchField = new OverlaySearchField(0, 0, 0, 0);
        }
        
        return searchField;
    }
}
