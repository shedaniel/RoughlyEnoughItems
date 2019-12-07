/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ConfigObject {
    
    boolean isLighterButtonHover();
    
    void setLighterButtonHover(boolean lighterButtonHover);
    
    boolean isOverlayVisible();
    
    void setOverlayVisible(boolean overlayVisible);
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    ItemListOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
    boolean isToastDisplayedOnCopyIdentifier();
    
    boolean doesRenderEntryExtraOverlay();
    
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
    
    ItemCheatingMode getItemCheatingMode();
    
    boolean isUsingLightGrayRecipeBorder();
    
    boolean doesVillagerScreenHavePermanentScrollBar();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    boolean doesSnapToRows();
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface DontApplyFieldName {
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface UseEnumSelectorInstead {
    }
    
}
