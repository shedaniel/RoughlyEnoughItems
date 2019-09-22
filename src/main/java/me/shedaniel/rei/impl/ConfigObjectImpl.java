/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.gui.config.*;
import me.zeroeightsix.fiber.exception.FiberException;
import me.zeroeightsix.fiber.tree.ConfigNode;
import me.zeroeightsix.fiber.tree.ConfigValue;
import me.zeroeightsix.fiber.tree.Node;

public class ConfigObjectImpl implements ConfigObject {
    
    public ConfigNode configNode = new ConfigNode();
    
    private Node general = configNode.fork("!general");
    private Node appearance = configNode.fork("appearance");
    private Node modules = configNode.fork("modules");
    private Node technical = configNode.fork("technical");
    
    private ConfigValue<Boolean> cheating = ConfigValue.builder(Boolean.class)
            .withParent(general)
            .withDefaultValue(false)
            .withComment("Declares whether cheating mode is on.")
            .withName("cheating")
            .build();
    
    private ConfigValue<ItemListOrderingConfig> itemListOrdering = ConfigValue.builder(ItemListOrderingConfig.class)
            .withParent(appearance)
            .withDefaultValue(ItemListOrderingConfig.REGISTRY_ASCENDING)
            .withComment("The ordering of the items on the item panel.")
            .withName("itemListOrdering")
            .build();
    
    private ConfigValue<Boolean> darkTheme = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares the appearance of REI windows.")
            .withName("darkTheme")
            .build();
    
    private ConfigValue<Boolean> renderEntryExtraOverlay = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(true)
            .withComment("Whether REI should render entry's overlay.\nExample: Enchantment Glint")
            .withName("renderEntryExtraOverlay")
            .build();
    
    private ConfigValue<RecipeScreenType> recipeScreenType = ConfigValue.builder(RecipeScreenType.class)
            .withParent(appearance)
            .withDefaultValue(RecipeScreenType.UNSET)
            .withComment("The ordering of the items on the item panel.")
            .withName("recipeScreenType")
            .build();
    
    private ConfigValue<Boolean> loadDefaultPlugin = ConfigValue.builder(Boolean.class)
            .withParent(technical)
            .withDefaultValue(true)
            .withComment("To disable REI's default plugin.\nDon't change this unless you understand what you are doing")
            .withName("loadDefaultPlugin")
            .build();
    
    private ConfigValue<SearchFieldLocation> sideSearchField = ConfigValue.builder(SearchFieldLocation.class)
            .withParent(appearance)
            .withDefaultValue(SearchFieldLocation.CENTER)
            .withComment("Declares the position of the search field.")
            .withName("searchFieldLocation")
            .build();
    
    private ConfigValue<Boolean> mirrorItemPanel = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares the position of the item list panel.")
            .withName("mirrorItemPanel")
            .build();
    
    private ConfigValue<Boolean> enableCraftableOnlyButton = ConfigValue.builder(Boolean.class)
            .withParent(modules)
            .withDefaultValue(true)
            .withComment("Declares whether the craftable filter button is enabled.")
            .withName("enableCraftableOnlyButton")
            .build();
    
    private ConfigValue<String> gamemodeCommand = ConfigValue.builder(String.class)
            .withParent(technical)
            .withDefaultValue("/gamemode {gamemode}")
            .withComment("Declares the command used to change gamemode.")
            .withName("gamemodeCommand")
            .build();
    
    private ConfigValue<String> giveCommand = ConfigValue.builder(String.class)
            .withParent(technical)
            .withDefaultValue("/give {player_name} {item_identifier}{nbt} {count}")
            .withComment("Declares the command used in servers to cheat items.")
            .withName("giveCommand")
            .build();
    
    private ConfigValue<String> weatherCommand = ConfigValue.builder(String.class)
            .withParent(technical)
            .withDefaultValue("/weather {weather}")
            .withComment("Declares the command used to change weather.")
            .withName("weatherCommand")
            .build();
    
    private ConfigValue<Integer> maxRecipePerPage = ConfigValue.builder(Integer.class)
            .withParent(appearance)
            .withDefaultValue(3)
            .withComment("Declares the maximum amount of recipes displayed in a page if possible.")
            .withName("maxRecipePerPage")
            .constraints()
            .minNumerical(2)
            .maxNumerical(99)
            .finish()
            .build();
    
    private ConfigValue<Boolean> showUtilsButtons = ConfigValue.builder(Boolean.class)
            .withParent(modules)
            .withDefaultValue(false)
            .withComment("Declares whether the utils buttons are shown.")
            .withName("showUtilsButtons")
            .build();
    
    private ConfigValue<Boolean> disableRecipeBook = ConfigValue.builder(Boolean.class)
            .withParent(modules)
            .withDefaultValue(false)
            .withComment("Declares whether REI should remove the recipe book.")
            .withName("disableRecipeBook")
            .build();
    
    private ConfigValue<Boolean> fixTabCloseContainer = ConfigValue.builder(Boolean.class)
            .withParent(modules)
            .withDefaultValue(false)
            .withComment("Declares whether REI should fix closing container with tab.")
            .withName("fixTabCloseContainer")
            .build();
    
    private ConfigValue<Boolean> clickableRecipeArrows = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(true)
            .withName("clickableRecipeArrows")
            .build();
    
    private ConfigValue<ItemCheatingMode> itemCheatingMode = ConfigValue.builder(ItemCheatingMode.class)
            .withParent(appearance)
            .withDefaultValue(ItemCheatingMode.REI_LIKE)
            .withName("itemCheatingMode")
            .build();
    
    private ConfigValue<Boolean> lightGrayRecipeBorder = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares the appearance of recipe's border.")
            .withName("lightGrayRecipeBorder")
            .build();
    
    private ConfigValue<Boolean> appendModNames = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares whether REI should append mod names to item stacks.")
            .withName("appendModNames")
            .build();
    
    private ConfigValue<Boolean> villagerScreenPermanentScrollBar = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares how the scrollbar in villager screen act.")
            .withName("villagerScreenPermanentScrollBar")
            .build();
    
    private ConfigValue<Boolean> registerRecipesInAnotherThread = ConfigValue.builder(Boolean.class)
            .withParent(technical)
            .withDefaultValue(true)
            .withName("registerRecipesInAnotherThread")
            .build();
    
    private ConfigValue<Boolean> scrollingEntryListWidget = ConfigValue.builder(Boolean.class)
            .withParent(appearance)
            .withDefaultValue(false)
            .withComment("Declares whether if entry list widget is scrolled.")
            .withName("scrollingEntryListWidget")
            .build();
    
    //    private ConfigValue<RelativePoint> choosePageDialogPoint = ConfigValue.builder(RelativePoint.class)
    //            .withParent(technical)
    //            .withDefaultValue(new RelativePoint(.5, .5))
    //            .withName("choosePageDialogPoint")
    //            .build();
    
    public ConfigObjectImpl() throws FiberException {
    
    }
    
    @Override
    public Node getGeneral() {
        return general;
    }
    
    @Override
    public ConfigNode getConfigNode() {
        return configNode;
    }
    
    @Override
    public boolean isCheating() {
        return cheating.getValue();
    }
    
    @Override
    public void setCheating(boolean cheating) {
        this.cheating.setValue(cheating);
    }
    
    @Override
    public ItemListOrdering getItemListOrdering() {
        return itemListOrdering.getValue().getOrdering();
    }
    
    @Override
    public boolean isItemListAscending() {
        return itemListOrdering.getValue().isAscending();
    }
    
    @Override
    public boolean isUsingDarkTheme() {
        return darkTheme.getValue().booleanValue();
    }
    
    @Override
    public boolean doesRenderEntryExtraOverlay() {
        return renderEntryExtraOverlay.getValue().booleanValue();
    }
    
    @Override
    public boolean isEntryListWidgetScrolled() {
        return scrollingEntryListWidget.getValue().booleanValue();
    }
    
    @Override
    public boolean shouldAppendModNames() {
        return appendModNames.getValue().booleanValue();
    }
    
    @Override
    public RecipeScreenType getRecipeScreenType() {
        return recipeScreenType.getValue();
    }
    
    @Override
    public void setRecipeScreenType(RecipeScreenType recipeScreenType) {
        this.recipeScreenType.setValue(recipeScreenType);
    }
    
    @Override
    public boolean isLoadingDefaultPlugin() {
        return loadDefaultPlugin.getValue().booleanValue();
    }
    
    @Override
    public SearchFieldLocation getSearchFieldLocation() {
        return sideSearchField.getValue();
    }
    
    @Override
    public boolean isLeftHandSidePanel() {
        return mirrorItemPanel.getValue().booleanValue();
    }
    
    @Override
    public boolean isCraftableFilterEnabled() {
        return enableCraftableOnlyButton.getValue().booleanValue();
    }
    
    @Override
    public String getGamemodeCommand() {
        return gamemodeCommand.getValue();
    }
    
    @Override
    public String getGiveCommand() {
        return giveCommand.getValue();
    }
    
    @Override
    public String getWeatherCommand() {
        return weatherCommand.getValue();
    }
    
    @Override
    public int getMaxRecipePerPage() {
        return maxRecipePerPage.getValue().intValue();
    }
    
    @Override
    public boolean doesShowUtilsButtons() {
        return showUtilsButtons.getValue().booleanValue();
    }
    
    @Override
    public boolean doesDisableRecipeBook() {
        return disableRecipeBook.getValue().booleanValue();
    }
    
    @Override
    public boolean doesFixTabCloseContainer() {
        return fixTabCloseContainer.getValue().booleanValue();
    }
    
    @Override
    public boolean areClickableRecipeArrowsEnabled() {
        return clickableRecipeArrows.getValue().booleanValue();
    }
    
    @Override
    public ItemCheatingMode getItemCheatingMode() {
        return itemCheatingMode.getValue();
    }
    
    @Override
    public boolean isUsingLightGrayRecipeBorder() {
        return lightGrayRecipeBorder.getValue().booleanValue();
    }
    
    @Override
    public boolean doesVillagerScreenHavePermanentScrollBar() {
        return villagerScreenPermanentScrollBar.getValue().booleanValue();
    }
    
    @Override
    public boolean doesRegisterRecipesInAnotherThread() {
        return registerRecipesInAnotherThread.getValue().booleanValue();
    }
    
    @Override
    public RelativePoint getChoosePageDialogPoint() {
        //        return choosePageDialogPoint.getValue();
        return new RelativePoint(.5, .5);
    }
    
    @Override
    public void setChoosePageDialogPoint(RelativePoint choosePageDialogPoint) {
        //        this.choosePageDialogPoint.setValue(choosePageDialogPoint);
    }
}
