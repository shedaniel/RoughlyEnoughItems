/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.shedaniel.rei.impl.ConfigObjectImpl;
import net.minecraft.client.util.InputUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ConfigObject {
    
    @SuppressWarnings("deprecation")
    static ConfigObject getInstance() {
        return ConfigManager.getInstance().getConfig();
    }
    
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
    
    ItemCheatingMode getItemCheatingMode();
    
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
    
    double getEntrySize();
    
    @Deprecated
    abstract ConfigObjectImpl.General getGeneral();
    
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
    @interface UsePercentage {
        double min();
        
        double max();
    }
    
}
