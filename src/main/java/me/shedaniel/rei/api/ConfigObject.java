/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.api;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.impl.ConfigManagerImpl;
import me.shedaniel.rei.impl.ConfigObjectImpl;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface AddInFrontKeyCode {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface DontApplyFieldName {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseEnumSelectorInstead {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseSpecialRecipeTypeScreen {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UsePercentage {
        double min();
        
        double max();
    }
    
}
