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

package me.shedaniel.rei.impl.client;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.hints.HintProvider;
import me.shedaniel.rei.impl.client.gui.widget.CachedEntryListRender;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.client.search.argument.Argument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static me.shedaniel.rei.impl.client.gui.widget.EntryListWidget.entrySize;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class REIRuntimeImpl implements REIRuntime {
    private static final ResourceLocation DISPLAY_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/display.png");
    private static final ResourceLocation DISPLAY_TEXTURE_DARK = new ResourceLocation("roughlyenoughitems", "textures/gui/display_dark.png");
    @ApiStatus.Internal
    public static boolean isWithinRecipeViewingScreen = false;
    private ScreenOverlayImpl overlay;
    private OverlaySearchField searchField;
    private AbstractContainerScreen<?> previousContainerScreen = null;
    private Screen previousScreen = null;
    private LinkedHashSet<DisplayScreen> lastDisplayScreen = Sets.newLinkedHashSetWithExpectedSize(10);
    private List<HintProvider> hintProviders = new ArrayList<>();
    
    /**
     * @return the instance of screen helper
     * @see REIRuntime#getInstance()
     */
    @ApiStatus.Internal
    public static REIRuntimeImpl getInstance() {
        return (REIRuntimeImpl) REIRuntime.getInstance();
    }
    
    public void addHintProvider(HintProvider provider) {
        this.hintProviders.add(provider);
    }
    
    public List<HintProvider> getHintProviders() {
        return Collections.unmodifiableList(hintProviders);
    }
    
    @Override
    public void queueTooltip(@Nullable Tooltip tooltip) {
        if (overlay != null && tooltip != null) {
            overlay.addTooltip(tooltip);
        }
    }
    
    @Override
    @Nullable
    public TextField getSearchTextField() {
        if (searchField == null) {
            searchField = new OverlaySearchField(0, 0, 0, 0);
        }
        
        return searchField;
    }
    
    @Nullable
    public static OverlaySearchField getSearchField() {
        return (OverlaySearchField) getInstance().getSearchTextField();
    }
    
    public void storeDisplayScreen(DisplayScreen screen) {
        while (lastDisplayScreen.size() >= 10)
            lastDisplayScreen.remove(Iterables.get(lastDisplayScreen, 0));
        lastDisplayScreen.add(screen);
    }
    
    public boolean hasLastDisplayScreen() {
        return !lastDisplayScreen.isEmpty();
    }
    
    public Screen getLastDisplayScreen() {
        DisplayScreen screen = Iterables.getLast(lastDisplayScreen);
        lastDisplayScreen.remove(screen);
        screen.recalculateCategoryPage();
        return (Screen) screen;
    }
    
    @Override
    public boolean isOverlayVisible() {
        return ConfigObject.getInstance().isOverlayVisible();
    }
    
    @Override
    public void toggleOverlayVisible() {
        ConfigObject.getInstance().setOverlayVisible(!ConfigObject.getInstance().isOverlayVisible());
        ConfigManager.getInstance().saveConfig();
    }
    
    @Override
    public Optional<ScreenOverlay> getOverlay(boolean reset, boolean init) {
        if ((overlay == null && init) || reset) {
            overlay = new ScreenOverlayImpl();
            overlay.init();
            getSearchField().setFocused(false);
        }
        
        return Optional.ofNullable(overlay);
    }
    
    @Override
    @Nullable
    public AbstractContainerScreen<?> getPreviousContainerScreen() {
        return previousContainerScreen;
    }
    
    @Override
    @Nullable
    public Screen getPreviousScreen() {
        return previousScreen;
    }
    
    public void setPreviousScreen(Screen previousScreen) {
        if (previousScreen == null || previousScreen.getClass().getName().contains(".rei.")) {
            return;
        }
        
        this.previousScreen = previousScreen;
        
        if (previousScreen instanceof AbstractContainerScreen<?> containerScreen) {
            this.previousContainerScreen = containerScreen;
        }
    }
    
    @Override
    public boolean isDarkThemeEnabled() {
        return ConfigObject.getInstance().isUsingDarkTheme();
    }
    
    @Override
    public ResourceLocation getDefaultDisplayTexture() {
        return getDefaultDisplayTexture(isDarkThemeEnabled());
    }
    
    @Override
    public ResourceLocation getDefaultDisplayTexture(boolean darkTheme) {
        return darkTheme ? DISPLAY_TEXTURE_DARK : DISPLAY_TEXTURE;
    }
    
    @Override
    public SearchFieldLocation getContextualSearchFieldLocation() {
        SearchFieldLocation location = ConfigObject.getInstance().getSearchFieldLocation();
        Window window = Minecraft.getInstance().getWindow();
        Rectangle screenBounds = ScreenRegistry.getInstance().getScreenBounds(Minecraft.getInstance().screen);
        if (location == SearchFieldLocation.CENTER && window.getGuiScaledHeight() - 20 <= screenBounds.getMaxY()) {
            return SearchFieldLocation.BOTTOM_SIDE;
        }
        
        return location;
    }
    
    @Override
    public Rectangle calculateEntryListArea(Rectangle bounds) {
        SearchFieldLocation searchFieldLocation = getContextualSearchFieldLocation();
        
        int yOffset = 2;
        if (searchFieldLocation == SearchFieldLocation.TOP_SIDE) yOffset += 24;
        if (!ConfigObject.getInstance().isEntryListWidgetScrolled()) yOffset += 22;
        int heightOffset = 0;
        if (searchFieldLocation == SearchFieldLocation.BOTTOM_SIDE) heightOffset += 24;
        return new Rectangle(bounds.x, bounds.y + yOffset, bounds.width, bounds.height - 1 - yOffset - heightOffset);
    }
    
    @Override
    public Rectangle calculateFavoritesListArea() {
        Rectangle bounds = ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation().mirror(), Minecraft.getInstance().screen);
        
        int yOffset = 8;
        if (!ConfigObject.getInstance().isLowerConfigButton()) yOffset += 25;
        bounds = new Rectangle(bounds.x, bounds.y + yOffset, bounds.width, bounds.height - 3 - yOffset);
        
        int widthReduction = (int) Math.round(bounds.width * (1 - ConfigObject.getInstance().getFavoritesHorizontalEntriesBoundariesPercentage()));
        if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.LEFT)
            bounds.x += widthReduction;
        bounds.width -= widthReduction;
        int maxWidth = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getFavoritesHorizontalEntriesBoundariesColumns() + entrySize() * 0.75) + 8;
        if (bounds.width > maxWidth) {
            if (ConfigObject.getInstance().getDisplayPanelLocation() == DisplayPanelLocation.LEFT)
                bounds.x += bounds.width - maxWidth;
            bounds.width = maxWidth;
        }
        
        return bounds;
    }
    
    @Override
    public void startReload() {
        Argument.SEARCH_CACHE.clear();
        getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
        lastDisplayScreen.clear();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(CachedEntryListRender::refresh);
        } else {
            CachedEntryListRender.refresh();
        }
    }
    
    @Override
    public void startReload(ReloadStage stage) {
        startReload();
    }
    
    @Override
    public void endReload(ReloadStage stage) {
        Argument.SEARCH_CACHE.clear();
        getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
    }
    
    public void onInitializeClient() {
        ClientGuiEvent.INIT_PRE.register((screen, access) -> {
            if (previousContainerScreen != screen && screen instanceof AbstractContainerScreen<?> containerScreen)
                previousContainerScreen = containerScreen;
            return EventResult.pass();
        });
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            if (isOverlayVisible() && REIRuntime.getInstance().getOverlay().isPresent()) {
                ScreenOverlayImpl.getInstance().tick();
            }
        });
    }
}
