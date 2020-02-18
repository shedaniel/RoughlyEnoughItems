/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.impl.ConfigObjectImpl;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface ConfigObject {
    
    @SuppressWarnings("deprecation")
    static ConfigObject getInstance() {
        return ConfigManager.getInstance().getConfig();
    }
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    boolean isLighterButtonHover();
    
    boolean isOverlayVisible();
    
    void setOverlayVisible(boolean overlayVisible);
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    ItemListOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
    boolean isToastDisplayedOnCopyIdentifier();
    
    @Deprecated
    default boolean doesRenderEntryExtraOverlay() {
        return doesRenderEntryEnchantmentGlint();
    }
    
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
    
    boolean isUsingLightGrayRecipeBorder();
    
    boolean doesVillagerScreenHavePermanentScrollBar();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    boolean doesSnapToRows();
    
    boolean isFavoritesEnabled();
    
    boolean doDisplayFavoritesTooltip();
    
    boolean doDisplayFavoritesOnTheLeft();
    
    boolean doesFastEntryRendering();
    
    boolean doDebugRenderTimeRequired();
    
    boolean doSearchFavorites();
    
    @Deprecated
    default InputUtil.KeyCode getFavoriteKeybind() {
        return getFavoriteKeyCode().getKeyCode();
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
