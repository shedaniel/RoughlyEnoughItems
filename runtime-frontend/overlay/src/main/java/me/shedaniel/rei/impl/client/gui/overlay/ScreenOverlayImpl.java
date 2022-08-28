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

package me.shedaniel.rei.impl.client.gui.overlay;

import com.mojang.blaze3d.platform.Window;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.DelegateWidget;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.search.SearchFilter;
import me.shedaniel.rei.impl.client.ClientInternals;
import me.shedaniel.rei.impl.client.gui.overlay.entries.EntryListProvider;
import me.shedaniel.rei.impl.client.gui.overlay.entries.EntryListWidget;
import me.shedaniel.rei.impl.client.gui.overlay.entries.FavoritesListProvider;
import me.shedaniel.rei.impl.client.gui.overlay.entries.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.overlay.widgets.OverlayWidgetProvider;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
public final class ScreenOverlayImpl extends AbstractScreenOverlay {
    private static final List<EntryListProvider> ENTRY_LIST_PROVIDERS = ClientInternals.resolveServices(EntryListProvider.class);
    private static final List<FavoritesListProvider> FAVORITES_LIST_PROVIDERS = ClientInternals.resolveServices(FavoritesListProvider.class);
    private static final List<OverlayWidgetProvider> OVERLAY_WIDGET_PROVIDERS = ClientInternals.resolveServices(OverlayWidgetProvider.class);
    private EntryListWidget entryListWidget = null;
    private FavoritesListWidget favoritesListWidget = null;
    private TextField searchField = null;
    
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
        
        this.searchField = null;
        
        FavoritesListWidget favoritesListWidget = getFavoritesListWidget();
        
        if (favoritesListWidget != null) {
            this.children().add(favoritesListWidget.asWidget());
        }
        
        EntryListWidget entryListWidget = getEntryListWidget();
        entryListWidget.initBounds(this.getBounds());
        entryListWidget.initSearch(searchField.getText(), true);
        this.children().add(entryListWidget.asWidget());
        searchField.setResponder(s -> entryListWidget.initSearch(s, false));
        entryListWidget.init(this);
        
        for (OverlayWidgetProvider provider : OVERLAY_WIDGET_PROVIDERS) {
            provider.provide(this, menuAccess(), textField -> this.searchField = textField,
                    LateRenderableWidget::new);
        }
        
        if (this.searchField != null) {
            this.searchField.asWidget().getBounds().setBounds(getSearchFieldArea());
            this.children().add(this.searchField.asWidget());
        } else {
            InternalLogger.getInstance().warn("Search Field is not found! This might cause problems!");
        }
    }
    
    private static class LateRenderableWidget extends DelegateWidget implements LateRenderable {
        private LateRenderableWidget(Widget widget) {
            super(widget);
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
        getEntryListWidget().initSearch(getSearchField().getText(), true);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (!hasSpace()) return false;
        if (!REIRuntime.getInstance().isOverlayVisible()) return false;
        if (ConfigObject.getInstance().getFocusSearchFieldKeybind().matchesKey(keyCode, scanCode)) {
            getSearchField().setFocused(true);
            setFocused(getSearchField().asWidget());
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
        EntryListWidget current = null;
        for (EntryListProvider provider : ENTRY_LIST_PROVIDERS) {
            current = provider.getEntryList();
            if (current != null) break;
        }
        if (current != null) throw new IllegalStateException("No Entry List available!");
        if (current != entryListWidget) {
            entryListWidget = current;
            current.initBounds(Objects.requireNonNullElse(getBounds(), new Rectangle()));
            current.initSearch(getSearchField().getText(), true);
        }
        return entryListWidget;
    }
    
    @Override
    public Optional<OverlayListWidget> getFavoritesList() {
        return Optional.ofNullable(getFavoritesListNullable());
    }
    
    private FavoritesListWidget getFavoritesListNullable() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            FavoritesListWidget current = null;
            for (FavoritesListProvider provider : FAVORITES_LIST_PROVIDERS) {
                current = provider.getFavoritesList();
                if (current != null) break;
            }
            if (current != null) throw new IllegalStateException("No Entry List available!");
            if (current != favoritesListWidget) {
                favoritesListWidget = current;
                current.initBounds();
            }
            return favoritesListWidget;
        } else {
            return favoritesListWidget = null;
        }
    }
    
    @Override
    public TextField getSearchField() {
        return Objects.requireNonNullElseGet(searchField, () -> Widgets.createTextField(new Rectangle()));
    }
    
    @Override
    @Nullable
    public SearchFilter getCurrentSearchFilter() {
        return getEntryList().getSearchFilter();
    }
}
