/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.annotations.Internal;
import me.shedaniel.rei.gui.config.*;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@Internal
@Config(name = "roughlyenoughitems/config")
public class ConfigObjectImpl implements ConfigObject, ConfigData {
    
    @ConfigEntry.Category("!general") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    public General general = new General();
    @ConfigEntry.Category("appearance") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Appearance appearance = new Appearance();
    @ConfigEntry.Category("modules") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Modules modules = new Modules();
    @ConfigEntry.Category("technical") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Technical technical = new Technical();
    @ConfigEntry.Category("performance") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Performance performance = new Performance();
    
    @Override
    public boolean isLighterButtonHover() {
        return appearance.lighterButtonHover;
    }
    
    @Override
    public void setLighterButtonHover(boolean lighterButtonHover) {
        appearance.lighterButtonHover = lighterButtonHover;
    }
    
    @Override
    public boolean isOverlayVisible() {
        return general.overlayVisible;
    }
    
    @Override
    public void setOverlayVisible(boolean overlayVisible) {
        general.overlayVisible = overlayVisible;
    }
    
    @Override
    public boolean isCheating() {
        return general.cheating;
    }
    
    @Override
    public void setCheating(boolean cheating) {
        general.cheating = cheating;
    }
    
    @Override
    public ItemListOrdering getItemListOrdering() {
        return appearance.itemListOrdering.getOrdering();
    }
    
    @Override
    public boolean isItemListAscending() {
        return appearance.itemListOrdering.isAscending();
    }
    
    @Override
    public boolean isUsingDarkTheme() {
        return appearance.darkTheme;
    }
    
    @Override
    public boolean isToastDisplayedOnCopyIdentifier() {
        return modules.toastDisplayedOnCopyIdentifier;
    }
    
    @Override
    public boolean doesRenderEntryEnchantmentGlint() {
        return performance.renderEntryEnchantmentGlint;
    }
    
    @Override
    public boolean isEntryListWidgetScrolled() {
        return appearance.scrollingEntryListWidget;
    }
    
    @Override
    public boolean shouldAppendModNames() {
        return appearance.appendModNames;
    }
    
    @Override
    public RecipeScreenType getRecipeScreenType() {
        return appearance.recipeScreenType;
    }
    
    @Override
    public void setRecipeScreenType(RecipeScreenType recipeScreenType) {
        appearance.recipeScreenType = recipeScreenType;
    }
    
    @Override
    public boolean isLoadingDefaultPlugin() {
        return technical.loadDefaultPlugin;
    }
    
    @Override
    public SearchFieldLocation getSearchFieldLocation() {
        return appearance.searchFieldLocation;
    }
    
    @Override
    public boolean isLeftHandSidePanel() {
        return appearance.mirrorItemPanel;
    }
    
    @Override
    public boolean isCraftableFilterEnabled() {
        return modules.enableCraftableOnlyButton;
    }
    
    @Override
    public String getGamemodeCommand() {
        return technical.gamemodeCommand;
    }
    
    @Override
    public String getGiveCommand() {
        return technical.giveCommand;
    }
    
    @Override
    public String getWeatherCommand() {
        return technical.weatherCommand;
    }
    
    @Override
    public int getMaxRecipePerPage() {
        return appearance.maxRecipePerPage;
    }
    
    @Override
    public boolean doesShowUtilsButtons() {
        return modules.showUtilsButtons;
    }
    
    @Override
    public boolean doesDisableRecipeBook() {
        return modules.disableRecipeBook;
    }
    
    @Override
    public boolean doesFixTabCloseContainer() {
        return modules.fixTabCloseContainer;
    }
    
    @Override
    public boolean areClickableRecipeArrowsEnabled() {
        return appearance.clickableRecipeArrows;
    }
    
    @Override
    public ItemCheatingMode getItemCheatingMode() {
        return appearance.itemCheatingMode;
    }
    
    @Override
    public boolean isUsingLightGrayRecipeBorder() {
        return appearance.lightGrayRecipeBorder;
    }
    
    @Override
    public boolean doesVillagerScreenHavePermanentScrollBar() {
        return appearance.villagerScreenPermanentScrollBar;
    }
    
    @Override
    public boolean doesRegisterRecipesInAnotherThread() {
        return technical.registerRecipesInAnotherThread;
    }
    
    @Override
    public boolean doesSnapToRows() {
        return appearance.snapToRows;
    }
    
    @Override
    public boolean isFavoritesEnabled() {
        return general.favoritesEnabled;
    }
    
    @Override
    public boolean doDisplayFavoritesTooltip() {
        return isFavoritesEnabled() && appearance.displayFavoritesTooltip;
    }
    
    @Override
    public boolean doDisplayFavoritesOnTheLeft() {
        return appearance.displayFavoritesOnTheLeft;
    }
    
    @Override
    public InputUtil.KeyCode getFavoriteKeybind() {
        return general.favoriteKeybind == null ? InputUtil.UNKNOWN_KEYCODE : general.favoriteKeybind;
    }
    
    public static class General {
        @ConfigEntry.Gui.Excluded public List<String> favorites = new ArrayList<>();
        @Comment("Declares whether cheating mode is on.") private boolean cheating = false;
        @Comment("Declares whether REI is visible.") @ConfigEntry.Gui.Excluded private boolean overlayVisible = true;
        private boolean favoritesEnabled = true;
        @AddInFrontKeyCode private InputUtil.KeyCode favoriteKeybind = InputUtil.Type.KEYSYM.createFromCode(65);
    }
    
    public static class Appearance {
        @Comment("The ordering of the items on the item panel.") @UseEnumSelectorInstead
        private ItemListOrderingConfig itemListOrdering = ItemListOrderingConfig.REGISTRY_ASCENDING;
        @Comment("Declares the appearance of REI windows.") private boolean darkTheme = false;
        @Comment("The ordering of the items on the item panel.") @UseEnumSelectorInstead
        private RecipeScreenType recipeScreenType = RecipeScreenType.UNSET;
        @Comment("Declares the position of the search field.") @UseEnumSelectorInstead
        private SearchFieldLocation searchFieldLocation = SearchFieldLocation.CENTER;
        @Comment("Declares the position of the item list panel.") private boolean mirrorItemPanel = false;
        @Comment("Declares the maximum amount of recipes displayed in a page if possible.")
        @ConfigEntry.BoundedDiscrete(min = 2, max = 99) private int maxRecipePerPage = 3;
        @Comment("Declares whether REI should lighten the button if hovered.")
        private boolean lighterButtonHover = true;
        private boolean clickableRecipeArrows = true;
        @UseEnumSelectorInstead private ItemCheatingMode itemCheatingMode = ItemCheatingMode.REI_LIKE;
        @Comment("Declares the appearance of recipe's border.") private boolean lightGrayRecipeBorder = false;
        @Comment("Declares whether REI should append mod names to item stacks.") private boolean appendModNames = true;
        @Comment("Declares how the scrollbar in villager screen should act.")
        private boolean villagerScreenPermanentScrollBar = false;
        @Comment("Declares whether if entry list widget is scrolled.") private boolean scrollingEntryListWidget = false;
        private boolean snapToRows = false;
        private boolean displayFavoritesOnTheLeft = true;
        private boolean displayFavoritesTooltip = true;
    }
    
    public static class Technical {
        @Comment("To disable REI's default plugin.\nDon't change this unless you understand what you are doing!")
        private boolean loadDefaultPlugin = true;
        @Comment("Declares the command used to change gamemode.")
        private String gamemodeCommand = "/gamemode {gamemode}";
        @Comment("Declares the command used in servers to cheat items.")
        private String giveCommand = "/give {player_name} {item_identifier}{nbt} {count}";
        @Comment("Declares the command used to change weather.") private String weatherCommand = "/weather {weather}";
        private boolean registerRecipesInAnotherThread = true;
    }
    
    public static class Modules {
        @Comment("Declares whether the craftable filter button is enabled.")
        private boolean enableCraftableOnlyButton = true;
        private boolean toastDisplayedOnCopyIdentifier = true;
        @Comment("Declares whether the utils buttons are shown.") private boolean showUtilsButtons = false;
        @Comment("Declares whether REI should remove the recipe book.") private boolean disableRecipeBook = false;
        @Comment("Declares whether REI should fix closing container with tab.")
        private boolean fixTabCloseContainer = false;
    }
    
    public static class Performance {
        @Comment("Whether REI should render entry's enchantment glint")
        private boolean renderEntryEnchantmentGlint = true;
    }
}
