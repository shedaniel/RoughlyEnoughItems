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

package me.shedaniel.rei.api.client;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.SearchFieldLocation;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.Reloadable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public interface REIRuntime extends Reloadable<REIClientPlugin> {
    /**
     * @return the instance of {@link REIRuntime}
     */
    static REIRuntime getInstance() {
        return PluginManager.getClientInstance().get(REIRuntime.class);
    }
    
    boolean isOverlayVisible();
    
    void toggleOverlayVisible();
    
    default Optional<ScreenOverlay> getOverlay() {
        return getOverlay(false, false);
    }
    
    default Optional<ScreenOverlay> getOverlay(boolean reset) {
        return getOverlay(reset, true);
    }
    
    Optional<ScreenOverlay> getOverlay(boolean reset, boolean init);
    
    @Nullable
    AbstractContainerScreen<?> getPreviousContainerScreen();
    
    @Nullable
    Screen getPreviousScreen();
    
    boolean isDarkThemeEnabled();
    
    @Nullable
    TextField getSearchTextField();
    
    void queueTooltip(@Nullable Tooltip tooltip);
    
    ResourceLocation getDefaultDisplayTexture();
    
    ResourceLocation getDefaultDisplayTexture(boolean darkTheme);
    
    SearchFieldLocation getContextualSearchFieldLocation();
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    default Rectangle calculateEntryListArea() {
        return calculateEntryListArea(ScreenRegistry.getInstance().getOverlayBounds(ConfigObject.getInstance().getDisplayPanelLocation(), Minecraft.getInstance().screen));
    }
    
    Rectangle calculateEntryListArea(Rectangle bounds);
    
    Rectangle calculateFavoritesListArea();
}
