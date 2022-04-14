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

package me.shedaniel.rei.api.client.config;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ConfigObject {
    /**
     * @return the instance of {@link ConfigObject}
     */
    static ConfigObject getInstance() {
        return ConfigManager.getInstance().getConfig();
    }
    
    boolean isOverlayVisible();
    
    void setOverlayVisible(boolean overlayVisible);
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    @ApiStatus.Experimental
    CheatingMode getCheatingMode();
    
    EntryPanelOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
    boolean isGrabbingItems();
    
    boolean isFavoritesAnimated();
    
    boolean isToastDisplayedOnCopyIdentifier();
    
    boolean isEntryListWidgetScrolled();
    
    boolean shouldAppendModNames();
    
    DisplayScreenType getRecipeScreenType();
    
    void setRecipeScreenType(DisplayScreenType displayScreenType);
    
    SearchFieldLocation getSearchFieldLocation();
    
    default boolean isLeftHandSidePanel() {
        return getDisplayPanelLocation() == DisplayPanelLocation.LEFT;
    }
    
    DisplayPanelLocation getDisplayPanelLocation();
    
    boolean isCraftableFilterEnabled();
    
    String getGamemodeCommand();
    
    String getGiveCommand();
    
    String getWeatherCommand();
    
    String getTimeCommand();
    
    int getMaxRecipePerPage();
    
    int getMaxRecipesPageHeight();
    
    boolean doesDisableRecipeBook();
    
    boolean doesFixTabCloseContainer();
    
    boolean isLeftSideMobEffects();
    
    boolean areClickableRecipeArrowsEnabled();
    
    RecipeBorderType getRecipeBorderType();
    
    
    boolean isCompositeScrollBarPermanent();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    boolean doesSnapToRows();
    
    boolean isFavoritesEnabled();
    
    boolean doDisplayFavoritesTooltip();
    
    boolean doesFastEntryRendering();
    
    boolean doesCacheEntryRendering();
    
    boolean doDebugRenderTimeRequired();
    
    boolean doMergeDisplayUnderOne();
    
    FavoriteAddWidgetMode getFavoriteAddWidgetMode();
    
    ModifierKeyCode getFavoriteKeyCode();
    
    ModifierKeyCode getRecipeKeybind();
    
    ModifierKeyCode getUsageKeybind();
    
    ModifierKeyCode getHideKeybind();
    
    ModifierKeyCode getPreviousPageKeybind();
    
    ModifierKeyCode getNextPageKeybind();
    
    ModifierKeyCode getFocusSearchFieldKeybind();
    
    ModifierKeyCode getCopyRecipeIdentifierKeybind();
    
    ModifierKeyCode getExportImageKeybind();
    
    ModifierKeyCode getPreviousScreenKeybind();
    
    double getEntrySize();
    
    boolean isUsingCompactTabs();
    
    @ApiStatus.Experimental
    boolean isUsingCompactTabButtons();
    
    boolean isLowerConfigButton();
    
    @ApiStatus.Experimental
    List<FavoriteEntry> getFavoriteEntries();
    
    @ApiStatus.Experimental
    List<EntryStackProvider<?>> getFilteredStackProviders();
    
    @ApiStatus.Experimental
    boolean shouldFilterDisplays();
    
    @ApiStatus.Experimental
    boolean shouldAsyncSearch();
    
    @ApiStatus.Experimental
    int getAsyncSearchPartitionSize();
    
    @ApiStatus.Experimental
    boolean doDebugSearchTimeRequired();
    
    boolean isSubsetsEnabled();
    
    boolean isInventoryHighlightingAllowed();
    
    ItemCheatingMode getItemCheatingMode();
    
    @ApiStatus.Experimental
    double getHorizontalEntriesBoundariesPercentage();
    
    @ApiStatus.Experimental
    double getVerticalEntriesBoundariesPercentage();
    
    @ApiStatus.Experimental
    double getHorizontalEntriesBoundariesColumns();
    
    @ApiStatus.Experimental
    double getVerticalEntriesBoundariesRows();
    
    @ApiStatus.Experimental
    double getFavoritesHorizontalEntriesBoundariesPercentage();
    
    @ApiStatus.Experimental
    double getFavoritesHorizontalEntriesBoundariesColumns();
    
    @ApiStatus.Experimental
    SyntaxHighlightingMode getSyntaxHighlightingMode();
    
    @ApiStatus.Experimental
    boolean isFocusModeZoomed();
    
    SearchMode getTooltipSearchMode();
    
    SearchMode getTagSearchMode();
    
    SearchMode getIdentifierSearchMode();
    
    SearchMode getModSearchMode();
    
    boolean isJEICompatibilityLayerEnabled();
}
