/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.config.ItemListOrdering;
import me.shedaniel.rei.gui.config.ItemListOrderingConfig;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.SearchFieldLocation;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
@Config(name = "roughlyenoughitems/config")
public class ConfigObjectImpl implements ConfigObject, ConfigData {
    
    @ConfigEntry.Category("!general") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName public General general = new General();
    @ConfigEntry.Category("appearance") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName private Appearance appearance = new Appearance();
    @ConfigEntry.Category("modules") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName private Modules modules = new Modules();
    @ConfigEntry.Category("technical") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName private Technical technical = new Technical();
    @ConfigEntry.Category("performance") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName private Performance performance = new Performance();
    //    @ConfigEntry.Category("filtering") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName private Filtering filtering = new Filtering();
    
    @Override
    public boolean isLighterButtonHover() {
        return true;
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
    public boolean doesFastEntryRendering() {
        return performance.newFastEntryRendering;
    }
    
    @Override
    public boolean doDebugRenderTimeRequired() {
        return technical.debugRenderTimeRequired;
    }
    
    @Override
    public boolean doSearchFavorites() {
        return appearance.searchFavorites;
    }
    
    @Override
    public ModifierKeyCode getFavoriteKeyCode() {
        return general.favoriteKeybind == null ? ModifierKeyCode.unknown() : general.favoriteKeybind;
    }
    
    @Override
    public ModifierKeyCode getRecipeKeybind() {
        return general.recipeKeybind == null ? ModifierKeyCode.unknown() : general.recipeKeybind;
    }
    
    @Override
    public ModifierKeyCode getUsageKeybind() {
        return general.usageKeybind == null ? ModifierKeyCode.unknown() : general.usageKeybind;
    }
    
    @Override
    public ModifierKeyCode getHideKeybind() {
        return general.hideKeybind == null ? ModifierKeyCode.unknown() : general.hideKeybind;
    }
    
    @Override
    public ModifierKeyCode getPreviousPageKeybind() {
        return general.previousPageKeybind == null ? ModifierKeyCode.unknown() : general.previousPageKeybind;
    }
    
    @Override
    public ModifierKeyCode getNextPageKeybind() {
        return general.nextPageKeybind == null ? ModifierKeyCode.unknown() : general.nextPageKeybind;
    }
    
    @Override
    public ModifierKeyCode getFocusSearchFieldKeybind() {
        return general.focusSearchFieldKeybind == null ? ModifierKeyCode.unknown() : general.focusSearchFieldKeybind;
    }
    
    @Override
    public ModifierKeyCode getCopyRecipeIdentifierKeybind() {
        return general.copyRecipeIdentifierKeybind == null ? ModifierKeyCode.unknown() : general.copyRecipeIdentifierKeybind;
    }
    
    @Override
    public ModifierKeyCode getExportImageKeybind() {
        return general.exportImageKeybind == null ? ModifierKeyCode.unknown() : general.exportImageKeybind;
    }
    
    @Override
    public double getEntrySize() {
        return appearance.entrySize;
    }
    
    @Deprecated
    @Override
    public General getGeneral() {
        return general;
    }
    
    @Override
    public boolean isUsingCompactTabs() {
        return appearance.useCompactTabs;
    }
    
    @Override
    public boolean isLowerConfigButton() {
        return appearance.lowerConfigButton;
    }
    
    public static class General {
        @ConfigEntry.Gui.Excluded public List<EntryStack> favorites = new ArrayList<>();
        @Comment("Declares whether cheating mode is on.") private boolean cheating = false;
        @Comment("Declares whether REI is visible.") @ConfigEntry.Gui.Excluded private boolean overlayVisible = true;
        private boolean favoritesEnabled = true;
        private ModifierKeyCode recipeKeybind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(82), Modifier.none());
        private ModifierKeyCode usageKeybind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(85), Modifier.none());
        private ModifierKeyCode hideKeybind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(79), Modifier.of(false, true, false));
        private ModifierKeyCode previousPageKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode nextPageKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode focusSearchFieldKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode copyRecipeIdentifierKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode favoriteKeybind = ModifierKeyCode.of(InputUtil.Type.KEYSYM.createFromCode(65), Modifier.none());
        @ConfigEntry.Gui.Excluded private ModifierKeyCode exportImageKeybind = ModifierKeyCode.unknown();
    }
    
    public static class Appearance {
        @UseSpecialRecipeTypeScreen private RecipeScreenType recipeScreenType = RecipeScreenType.UNSET;
        @Comment("The ordering of the items on the item panel.") @UseEnumSelectorInstead
        private ItemListOrderingConfig itemListOrdering = ItemListOrderingConfig.REGISTRY_ASCENDING;
        @Comment("Declares the appearance of REI windows.") private boolean darkTheme = false;
        @Comment("Declares the position of the search field.") @UseEnumSelectorInstead
        private SearchFieldLocation searchFieldLocation = SearchFieldLocation.CENTER;
        @Comment("Declares the position of the item list panel.") private boolean mirrorItemPanel = false;
        @Comment("Declares the maximum amount of recipes displayed in a page if possible.") @ConfigEntry.BoundedDiscrete(min = 2, max = 99)
        private int maxRecipePerPage = 3;
        private boolean clickableRecipeArrows = true;
        @Comment("Declares the appearance of recipe's border.") private boolean lightGrayRecipeBorder = false;
        @Comment("Declares whether REI should append mod names to item stacks.") private boolean appendModNames = true;
        @Comment("Declares how the scrollbar in villager screen should act.") private boolean villagerScreenPermanentScrollBar = false;
        @Comment("Declares whether entry list widget is scrolled.") private boolean scrollingEntryListWidget = false;
        private boolean snapToRows = false;
        private boolean displayFavoritesOnTheLeft = true;
        private boolean displayFavoritesTooltip = false;
        @Comment("Declares whether favorites will be searched.") private boolean searchFavorites = true;
        @UsePercentage(min = 0.5, max = 4.0) private double entrySize = 1.0;
        private boolean useCompactTabs = true;
        private boolean lowerConfigButton = false;
    }
    
    public static class Technical {
        @Comment("To disable REI's default plugin.\nDon't change this unless you understand what you are doing!") private boolean loadDefaultPlugin = true;
        @Comment("Declares the command used to change gamemode.") private String gamemodeCommand = "/gamemode {gamemode}";
        @Comment("Declares the command used in servers to cheat items.") private String giveCommand = "/give {player_name} {item_identifier}{nbt} {count}";
        @Comment("Declares the command used to change weather.") private String weatherCommand = "/weather {weather}";
        private boolean registerRecipesInAnotherThread = true;
        private boolean debugRenderTimeRequired = false;
    }
    
    public static class Modules {
        @Comment("Declares whether the craftable filter button is enabled.") private boolean enableCraftableOnlyButton = true;
        private boolean toastDisplayedOnCopyIdentifier = true;
        @Comment("Declares whether the utils buttons are shown.") private boolean showUtilsButtons = false;
        @Comment("Declares whether REI should remove the recipe book.") private boolean disableRecipeBook = false;
        @Comment("Declares whether REI should fix closing container with tab.") private boolean fixTabCloseContainer = false;
    }
    
    public static class Performance {
        @Comment("Whether REI should render entry's enchantment glint") private boolean renderEntryEnchantmentGlint = true;
        private boolean newFastEntryRendering = true;
    }
    
    public static class Filtering {
        private List<EntryStack> filteredStacks = new ArrayList<>();
    }
}
