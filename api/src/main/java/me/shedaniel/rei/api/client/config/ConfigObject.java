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
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public interface ConfigObject {
    /**
     * @return the instance of {@link ConfigObject}
     */
    static ConfigObject getInstance() {
        return ConfigManager.getInstance().getConfig();
    }
    
    /**
     * Returns whether the overlay is visible.
     *
     * @return whether the overlay is visible
     */
    boolean isOverlayVisible();
    
    /**
     * Sets whether the overlay is visible.
     *
     * @param overlayVisible whether the overlay should be visible
     */
    void setOverlayVisible(boolean overlayVisible);
    
    /**
     * Returns whether cheating is enabled. This method may return
     * the contextual cheating state if cheat mode is set to Creative Only.
     *
     * @return whether cheating is enabled
     * @see #getCheatingMode()
     */
    boolean isCheating();
    
    /**
     * Sets whether cheating is enabled.
     *
     * @param cheating whether cheating should be enabled
     */
    void setCheating(boolean cheating);
    
    /**
     * Returns the cheating mode.
     *
     * @return the cheating mode
     * @see #isCheating()
     */
    @ApiStatus.Experimental
    CheatingMode getCheatingMode();
    
    /**
     * Returns the entry panel sorting method.
     *
     * @return the entry panel sorting method
     */
    EntryPanelOrdering getItemListOrdering();
    
    /**
     * Returns whether the entry panel is in ascending order.
     *
     * @return whether the entry panel is in ascending order
     */
    boolean isItemListAscending();
    
    /**
     * Returns whether REI is in dark mode.
     *
     * @return whether REI is in dark mode
     */
    boolean isUsingDarkTheme();
    
    /**
     * Returns whether grabbing items is used to cheat items.
     *
     * @return whether grabbing items is used to cheat items
     */
    boolean isGrabbingItems();
    
    /**
     * Returns whether favorites motions are animated.
     *
     * @return whether favorites motions are animated
     */
    boolean isFavoritesAnimated();
    
    /**
     * Returns whether a toast is shown on screen when a display identifier is copied.
     *
     * @return whether a toast is shown on screen when a display identifier is copied
     */
    boolean isToastDisplayedOnCopyIdentifier();
    
    /**
     * Returns whether the entry list is scrolled, rather than paginated.
     *
     * @return whether the entry list is scrolled, rather than paginated
     */
    boolean isEntryListWidgetScrolled();
    
    /**
     * Returns whether REI should append mod names to tooltips.
     *
     * @return whether REI should append mod names to tooltips
     */
    boolean shouldAppendModNames();
    
    /**
     * Returns the display type of the display screen.
     *
     * @return the display type of the display screen
     */
    DisplayScreenType getRecipeScreenType();
    
    /**
     * Sets the display type of the display screen.
     *
     * @param displayScreenType the display type of the display screen
     */
    void setRecipeScreenType(DisplayScreenType displayScreenType);
    
    /**
     * Returns the location of the search field. This method is not contextual,
     * you might instead want to use {@link REIRuntime#getContextualSearchFieldLocation()}.
     *
     * @return the location of the search field
     */
    SearchFieldLocation getSearchFieldLocation();
    
    /**
     * Returns whether the entry list is displayed on the left side of the screen.
     *
     * @return whether the entry list is displayed on the left side of the screen
     */
    default boolean isLeftHandSidePanel() {
        return getDisplayPanelLocation() == DisplayPanelLocation.LEFT;
    }
    
    /**
     * Returns the location of the entry list, favorites list will go to the opposite side.
     *
     * @return the location of the entry list
     */
    DisplayPanelLocation getDisplayPanelLocation();
    
    /**
     * Returns whether the craftable filter button is visible.
     * For checking whether the filter is active, use {@link ConfigManager#isCraftableOnlyEnabled()}.
     *
     * @return whether the craftable filter button is visible
     */
    boolean isCraftableFilterEnabled();
    
    /**
     * Returns the game mode command used to change the game mode.
     * <p>
     * {@code {gamemode}} is a placeholder for the game mode.
     *
     * @return the game mode command used to change the game mode
     */
    String getGamemodeCommand();
    
    /**
     * Returns the give command used to cheat items on servers
     * <p>
     * {@code {item_name}} is the item path, {@code {item_identifier}} is the item identifier,
     * {@code {count}} is the item count, {@code {player_name}} is the recipient player name.
     *
     * @return the give command used to cheat items on servers
     */
    String getGiveCommand();
    
    /**
     * Returns the weather command used to change the weather.
     * <p>
     * {@code {weather}} is a placeholder for the weather.
     *
     * @return the weather command used to change the weather
     */
    String getWeatherCommand();
    
    /**
     * Returns the time command used to change the time.
     * <p>
     * {@code {time}} is a placeholder for the time.
     *
     * @return the time command used to change the time
     */
    String getTimeCommand();
    
    /**
     * Returns the maximum number of displays that can be displayed per page.
     *
     * @return the maximum number of displays that can be displayed per page
     */
    int getMaxRecipePerPage();
    
    /**
     * Returns the maximum page height for displays.
     *
     * @return the maximum page height for displays
     */
    int getMaxRecipesPageHeight();
    
    @ApiStatus.Experimental
    @Nullable
    ResourceLocation getInputMethodId();
    
    boolean doesDisableRecipeBook();
    
    boolean doesFixTabCloseContainer();
    
    boolean isLeftSideMobEffects();
    
    boolean areClickableRecipeArrowsEnabled();
    
    RecipeBorderType getRecipeBorderType();
    
    
    boolean isCompositeScrollBarPermanent();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    @Deprecated(forRemoval = true)
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
    
    List<FavoriteEntry> getFavoriteEntries();
    
    @ApiStatus.Experimental
    List<EntryStackProvider<?>> getFilteredStackProviders();
    
    @ApiStatus.Experimental
    boolean shouldFilterDisplays();
    
    @ApiStatus.Experimental
    Map<CategoryIdentifier<?>, Boolean> getFilteringQuickCraftCategories();
    
    @ApiStatus.Experimental
    boolean shouldAsyncSearch();
    
    @ApiStatus.Experimental
    int getAsyncSearchPartitionSize();
    
    @ApiStatus.Experimental
    boolean isPatchingAsyncThreadCrash();
    
    @ApiStatus.Experimental
    boolean doDebugSearchTimeRequired();
    
    @Deprecated(forRemoval = true)
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
