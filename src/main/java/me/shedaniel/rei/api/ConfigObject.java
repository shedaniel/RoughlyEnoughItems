/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.config.ItemCheatingMode;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import me.zeroeightsix.fiber.tree.ConfigNode;
import me.zeroeightsix.fiber.tree.Node;

public interface ConfigObject {
    
    Node getGeneral();
    
    ConfigNode getConfigNode();
    
    boolean isCheating();
    
    void setCheating(boolean cheating);
    
    ItemListOrdering getItemListOrdering();
    
    boolean isItemListAscending();
    
    boolean isUsingDarkTheme();
    
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
    
    boolean areClickableRecipeArrowsEnabled();
    
    ItemCheatingMode getItemCheatingMode();
    
    boolean isUsingLightGrayRecipeBorder();
    
    boolean doesVillagerScreenHavePermanentScrollBar();
    
    boolean doesRegisterRecipesInAnotherThread();
    
    RelativePoint getChoosePageDialogPoint();
    
    void setChoosePageDialogPoint(RelativePoint choosePageDialogPoint);
    
    public static class RelativePoint {
        
        private double relativeX, relativeY;
        
        public RelativePoint(double relativeX, double relativeY) {
            this.relativeX = relativeX;
            this.relativeY = relativeY;
        }
        
        public double getRelativeX() {
            return relativeX;
        }
        
        public double getRelativeY() {
            return relativeY;
        }
        
        public double getX(double width) {
            return width * relativeX;
        }
        
        public double getY(double height) {
            return height * relativeY;
        }
        
    }
    
}
