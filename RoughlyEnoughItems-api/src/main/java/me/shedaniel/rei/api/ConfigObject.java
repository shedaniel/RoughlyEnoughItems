/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.api;

import me.shedaniel.clothconfig2.forge.api.ModifierKeyCode;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.gui.config.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface ConfigObject {
    
    /**
     * @return the instance of {@link me.shedaniel.rei.api.ConfigObject}
     */
    static ConfigObject getInstance() {
        return ConfigManager.getInstance().getConfig();
    }
    
    boolean isOverlayVisible();
    
    void setOverlayVisible(boolean overlayVisible);
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    EntryPanelOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
    boolean isGrabbingItems();
    
    boolean isReducedMotion();
    
    boolean isToastDisplayedOnCopyIdentifier();
    
    boolean isEntryListWidgetScrolled();
    
    boolean shouldAppendModNames();
    
    RecipeScreenType getRecipeScreenType();
    
    void setRecipeScreenType(RecipeScreenType recipeScreenType);
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean isLoadingDefaultPlugin();
    
    SearchFieldLocation getSearchFieldLocation();
    
    default boolean isLeftHandSidePanel() {
        return getDisplayPanelLocation() == DisplayPanelLocation.LEFT;
    }
    
    DisplayPanelLocation getDisplayPanelLocation();
    
    boolean isCraftableFilterEnabled();
    
    String getGamemodeCommand();
    
    String getGiveCommand();
    
    String getWeatherCommand();
    
    int getMaxRecipePerPage();
    
    boolean doesShowUtilsButtons();
    
    boolean doesDisableRecipeBook();
    
    boolean doesFixTabCloseContainer();
    
    boolean areClickableRecipeArrowsEnabled();
    
    RecipeBorderType getRecipeBorderType();
    
    boolean doesVillagerScreenHavePermanentScrollBar();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    boolean doesSnapToRows();
    
    boolean isFavoritesEnabled();
    
    boolean doDisplayFavoritesTooltip();
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean doDisplayFavoritesOnTheLeft() {
        return true;
    }
    
    boolean doesFastEntryRendering();
    
    boolean doDebugRenderTimeRequired();
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default boolean doSearchFavorites() {
        return false;
    }
    
    ModifierKeyCode getFavoriteKeyCode();
    
    ModifierKeyCode getRecipeKeybind();
    
    ModifierKeyCode getUsageKeybind();
    
    ModifierKeyCode getHideKeybind();
    
    ModifierKeyCode getPreviousPageKeybind();
    
    ModifierKeyCode getNextPageKeybind();
    
    ModifierKeyCode getFocusSearchFieldKeybind();
    
    ModifierKeyCode getCopyRecipeIdentifierKeybind();
    
    ModifierKeyCode getExportImageKeybind();
    
    double getEntrySize();
    
    boolean isUsingCompactTabs();
    
    boolean isLowerConfigButton();
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default List<EntryStack> getFavorites() {
        return Collections.emptyList();
    }
    
    @ApiStatus.Experimental
    List<FavoriteEntry> getFavoriteEntries();
    
    List<EntryStack> getFilteredStacks();
    
    @ApiStatus.Experimental
    boolean shouldAsyncSearch();
    
    @ApiStatus.Experimental
    int getAsyncSearchPartitionSize();
    
    @ApiStatus.Experimental
    boolean doDebugSearchTimeRequired();
    
    boolean isSubsetsEnabled();
    
    boolean isInventoryHighlightingAllowed();
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean shouldResizeDynamically();
    
    @ApiStatus.Experimental
    double getHorizontalEntriesBoundaries();
    
    @ApiStatus.Experimental
    double getVerticalEntriesBoundaries();
    
    @ApiStatus.Experimental
    SyntaxHighlightingMode getSyntaxHighlightingMode();
}
