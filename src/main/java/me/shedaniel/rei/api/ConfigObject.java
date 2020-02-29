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

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeBorderType;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.impl.ConfigManagerImpl;
import me.shedaniel.rei.impl.ConfigObjectImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface ConfigObject {
    
    static ConfigObject getInstance() {
        return ((ConfigManagerImpl) ConfigManager.getInstance()).getConfig();
    }
    
    boolean isOverlayVisible();
    
    void setOverlayVisible(boolean overlayVisible);
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    ItemListOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
    boolean isToastDisplayedOnCopyIdentifier();
    
    boolean doesRenderEntryEnchantmentGlint();
    
    boolean isEntryListWidgetScrolled();
    
    boolean shouldAppendModNames();
    
    RecipeScreenType getRecipeScreenType();
    
    void setRecipeScreenType(RecipeScreenType recipeScreenType);
    
    boolean isLoadingDefaultPlugin();
    
    SearchFieldLocation getSearchFieldLocation();
    
    boolean isLeftHandSidePanel();
    
    boolean isCraftableFilterEnabled();
    
    String getGamemodeCommand();
    
    String getGiveCommand();
    
    String getWeatherCommand();
    
    int getMaxRecipePerPage();
    
    boolean doesShowUtilsButtons();
    
    boolean doesDisableRecipeBook();
    
    boolean doesFixTabCloseContainer();
    
    boolean areClickableRecipeArrowsEnabled();
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    boolean isUsingLightGrayRecipeBorder();
    
    RecipeBorderType getRecipeBorderType();
    
    boolean doesVillagerScreenHavePermanentScrollBar();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    boolean doesSnapToRows();
    
    boolean isFavoritesEnabled();
    
    boolean doDisplayFavoritesTooltip();
    
    boolean doDisplayFavoritesOnTheLeft();
    
    boolean doesFastEntryRendering();
    
    boolean doDebugRenderTimeRequired();
    
    boolean doSearchFavorites();
    
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
    
    @ApiStatus.Internal
    ConfigObjectImpl.General getGeneral();
    
    boolean isUsingCompactTabs();
    
    boolean isLowerConfigButton();
    
    List<EntryStack> getFavorites();
    
    List<EntryStack> getFilteredStacks();
    
    @ApiStatus.Experimental
    boolean shouldAsyncSearch();
    
    @ApiStatus.Experimental
    int getNumberAsyncSearch();
    
    @ApiStatus.Experimental
    boolean doDebugSearchTimeRequired();
}
