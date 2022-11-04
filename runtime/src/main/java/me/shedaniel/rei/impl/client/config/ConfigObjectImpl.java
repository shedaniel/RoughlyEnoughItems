/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.platform.Platform;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.*;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesEntriesManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
@Config(name = "roughlyenoughitems/config")
@Environment(EnvType.CLIENT)
public class ConfigObjectImpl implements ConfigObject, ConfigData {
    @ConfigEntry.Category("basics") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    public Basics basics = new Basics();
    @ConfigEntry.Category("appearance") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Appearance appearance = new Appearance();
    @ConfigEntry.Category("functionality") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    private Functionality functionality = new Functionality();
    @ConfigEntry.Category("advanced") @ConfigEntry.Gui.TransitiveObject @DontApplyFieldName
    public Advanced advanced = new Advanced();
    
    @Override
    public boolean isOverlayVisible() {
        return basics.overlayVisible;
    }
    
    @Override
    public void setOverlayVisible(boolean overlayVisible) {
        basics.overlayVisible = overlayVisible;
    }
    
    @Override
    public boolean isCheating() {
        return basics.cheating == CheatingMode.ON || (basics.cheating == CheatingMode.WHEN_CREATIVE && Minecraft.getInstance().gameMode != null
                                                      && Minecraft.getInstance().gameMode.getPlayerMode() == GameType.CREATIVE);
    }
    
    @Override
    public void setCheating(boolean cheating) {
        basics.cheating = cheating ? CheatingMode.ON : CheatingMode.OFF;
    }
    
    @Override
    public CheatingMode getCheatingMode() {
        return basics.cheating;
    }
    
    @Override
    public EntryPanelOrdering getItemListOrdering() {
        return advanced.layout.entryPanelOrdering.getOrdering();
    }
    
    @Override
    public boolean isItemListAscending() {
        return advanced.layout.entryPanelOrdering.isAscending();
    }
    
    @Override
    public boolean isUsingDarkTheme() {
        return appearance.theme == AppearanceTheme.DARK;
    }
    
    public void setUsingDarkTheme(boolean dark) {
        appearance.theme = dark ? AppearanceTheme.DARK : AppearanceTheme.LIGHT;
    }
    
    @Override
    public boolean isGrabbingItems() {
        return basics.cheatingStyle == ItemCheatingStyle.GRAB;
    }
    
    @Override
    public boolean isFavoritesAnimated() {
        return basics.motion.favoritesAnimation;
    }
    
    @Override
    public boolean isToastDisplayedOnCopyIdentifier() {
        return advanced.accessibility.toastDisplayedOnCopyIdentifier;
    }
    
    @Override
    public boolean isEntryListWidgetScrolled() {
        return appearance.scrollingEntryListWidget;
    }
    
    public void setEntryListWidgetScrolled(boolean scrollingEntryListWidget) {
        appearance.scrollingEntryListWidget = scrollingEntryListWidget;
    }
    
    @Override
    public boolean shouldAppendModNames() {
        return advanced.tooltips.appendModNames;
    }
    
    @Override
    public DisplayScreenType getRecipeScreenType() {
        return appearance.recipeScreenType;
    }
    
    @Override
    public void setRecipeScreenType(DisplayScreenType displayScreenType) {
        appearance.recipeScreenType = displayScreenType;
    }
    
    @Override
    public SearchFieldLocation getSearchFieldLocation() {
        return appearance.layout.searchFieldLocation;
    }
    
    public void setSearchFieldLocation(SearchFieldLocation location) {
        appearance.layout.searchFieldLocation = location;
    }
    
    @Override
    public DisplayPanelLocation getDisplayPanelLocation() {
        return advanced.accessibility.displayPanelLocation;
    }
    
    public void setDisplayPanelLocation(DisplayPanelLocation location) {
        advanced.accessibility.displayPanelLocation = location;
    }
    
    @Override
    public boolean isCraftableFilterEnabled() {
        return appearance.layout.showCraftableOnlyButton;
    }
    
    public void setCraftableFilterEnabled(boolean enabled) {
        appearance.layout.showCraftableOnlyButton = enabled;
    }
    
    @Override
    public String getGamemodeCommand() {
        return advanced.commands.gamemodeCommand;
    }
    
    @Override
    public String getGiveCommand() {
        return advanced.commands.giveCommand;
    }
    
    @Override
    public String getWeatherCommand() {
        return advanced.commands.weatherCommand;
    }
    
    @Override
    public String getTimeCommand() {
        return advanced.commands.timeCommand;
    }
    
    @Override
    public int getMaxRecipePerPage() {
        return advanced.layout.maxRecipesPerPage;
    }
    
    @Override
    public int getMaxRecipesPageHeight() {
        return advanced.layout.maxRecipesPageHeight;
    }
    
    @Override
    @Nullable
    public ResourceLocation getInputMethodId() {
        return functionality.inputMethod;
    }
    
    public void setInputMethodId(@Nullable ResourceLocation id) {
        functionality.inputMethod = id;
    }
    
    @Override
    public boolean doesDisableRecipeBook() {
        return functionality.disableRecipeBook;
    }
    
    public void setDisableRecipeBook(boolean disableRecipeBook) {
        functionality.disableRecipeBook = disableRecipeBook;
    }
    
    @Override
    public boolean doesFixTabCloseContainer() {
        return functionality.disableRecipeBook;
    }
    
    @Override
    public boolean isLeftSideMobEffects() {
        return functionality.leftSideMobEffects;
    }
    
    public void setLeftSideMobEffects(boolean leftSideMobEffects) {
        functionality.leftSideMobEffects = leftSideMobEffects;
    }
    
    @Override
    public boolean areClickableRecipeArrowsEnabled() {
        return advanced.miscellaneous.clickableRecipeArrows;
    }
    
    @Override
    public RecipeBorderType getRecipeBorderType() {
        return appearance.recipeBorder;
    }
    
    @Override
    public boolean isCompositeScrollBarPermanent() {
        return advanced.accessibility.compositeScrollBarPermanent;
    }
    
    @Override
    public boolean doesRegisterRecipesInAnotherThread() {
        return advanced.miscellaneous.registerRecipesInAnotherThread;
    }
    
    @Override
    public boolean doesSnapToRows() {
        return false;
    }
    
    @Override
    public boolean isFavoritesEnabled() {
        return basics.favoritesEnabled;
    }
    
    @Override
    public boolean doDisplayFavoritesTooltip() {
        return isFavoritesEnabled() && advanced.tooltips.displayFavoritesTooltip;
    }
    
    @Override
    public boolean doesFastEntryRendering() {
        return advanced.miscellaneous.newFastEntryRendering;
    }
    
    @Override
    public boolean doesCacheEntryRendering() {
        return advanced.miscellaneous.cachingFastEntryRendering;
    }
    
    public void setDoesCacheEntryRendering(boolean doesCacheEntryRendering) {
        advanced.miscellaneous.cachingFastEntryRendering = doesCacheEntryRendering;
    }
    
    @Override
    public boolean doDebugRenderTimeRequired() {
        return advanced.layout.debugRenderTimeRequired;
    }
    
    @Override
    public boolean doMergeDisplayUnderOne() {
        return advanced.layout.mergeDisplayUnderOne;
    }
    
    @Override
    public FavoriteAddWidgetMode getFavoriteAddWidgetMode() {
        return advanced.layout.favoriteAddWidgetMode;
    }
    
    @Override
    public ModifierKeyCode getFavoriteKeyCode() {
        return basics.keyBindings.favoriteKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.favoriteKeybind;
    }
    
    @Override
    public ModifierKeyCode getRecipeKeybind() {
        return basics.keyBindings.recipeKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.recipeKeybind;
    }
    
    @Override
    public ModifierKeyCode getUsageKeybind() {
        return basics.keyBindings.usageKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.usageKeybind;
    }
    
    @Override
    public ModifierKeyCode getHideKeybind() {
        return basics.keyBindings.hideKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.hideKeybind;
    }
    
    @Override
    public ModifierKeyCode getPreviousPageKeybind() {
        return basics.keyBindings.previousPageKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.previousPageKeybind;
    }
    
    @Override
    public ModifierKeyCode getNextPageKeybind() {
        return basics.keyBindings.nextPageKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.nextPageKeybind;
    }
    
    @Override
    public ModifierKeyCode getFocusSearchFieldKeybind() {
        return basics.keyBindings.focusSearchFieldKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.focusSearchFieldKeybind;
    }
    
    @Override
    public ModifierKeyCode getCopyRecipeIdentifierKeybind() {
        return basics.keyBindings.copyRecipeIdentifierKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.copyRecipeIdentifierKeybind;
    }
    
    @Override
    public ModifierKeyCode getExportImageKeybind() {
        return basics.keyBindings.exportImageKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.exportImageKeybind;
    }
    
    @Override
    public ModifierKeyCode getPreviousScreenKeybind() {
        return basics.keyBindings.previousScreenKeybind == null ? ModifierKeyCode.unknown() : basics.keyBindings.previousScreenKeybind;
    }
    
    @Override
    public double getEntrySize() {
        return advanced.accessibility.entrySize;
    }
    
    public boolean setEntrySize(double entrySize) {
        double original = advanced.accessibility.entrySize;
        return (advanced.accessibility.entrySize = Mth.clamp(entrySize, 0.25, 4)) != original;
    }
    
    @Override
    public boolean isUsingCompactTabs() {
        return advanced.accessibility.useCompactTabs;
    }
    
    @Override
    public boolean isUsingCompactTabButtons() {
        return advanced.accessibility.useCompactTabButtons;
    }
    
    @Override
    public boolean isLowerConfigButton() {
        return appearance.layout.configButtonLocation == ConfigButtonPosition.LOWER;
    }
    
    @Override
    public List<FavoriteEntry> getFavoriteEntries() {
        return FavoritesEntriesManager.INSTANCE.asListView();
    }
    
    public List<FavoriteEntry> getConfigFavoriteEntries() {
        return basics.favorites;
    }
    
    public List<FavoriteEntry> getHiddenFavoriteEntries() {
        return basics.hiddenFavorites;
    }
    
    public List<CompoundTag> getDisplayHistory() {
        return basics.displayHistory;
    }
    
    @Override
    public List<EntryStackProvider<?>> getFilteredStackProviders() {
        return advanced.filtering.filteredStacks;
    }
    
    @ApiStatus.Experimental
    @Override
    public boolean shouldFilterDisplays() {
        return advanced.filtering.shouldFilterDisplays;
    }
    
    @ApiStatus.Internal
    public List<FilteringRule<?>> getFilteringRules() {
        return advanced.filtering.filteringRules;
    }
    
    @ApiStatus.Experimental
    @Override
    public Map<CategoryIdentifier<?>, Boolean> getFilteringQuickCraftCategories() {
        return advanced.filtering.filteringQuickCraftCategories;
    }
    
    @Override
    @ApiStatus.Experimental
    public boolean shouldAsyncSearch() {
        return advanced.search.asyncSearch;
    }
    
    @Override
    @ApiStatus.Experimental
    public int getAsyncSearchPartitionSize() {
        return advanced.search.asyncSearchPartitionSize;
    }
    
    @Override
    @ApiStatus.Experimental
    public boolean isPatchingAsyncThreadCrash() {
        return advanced.search.patchAsyncThreadCrash;
    }
    
    @Override
    @ApiStatus.Experimental
    public boolean doDebugSearchTimeRequired() {
        return advanced.search.debugSearchTimeRequired;
    }
    
    @Override
    public boolean isSubsetsEnabled() {
        return functionality.isSubsetsEnabled;
    }
    
    @Override
    public boolean isInventoryHighlightingAllowed() {
        return functionality.allowInventoryHighlighting;
    }
    
    @Override
    public ItemCheatingMode getItemCheatingMode() {
        return functionality.itemCheatingMode;
    }
    
    @ApiStatus.Experimental
    @Override
    public double getHorizontalEntriesBoundariesPercentage() {
        return Mth.clamp(appearance.horizontalEntriesBoundaries, 0.1, 1);
    }
    
    @ApiStatus.Experimental
    @Override
    public double getVerticalEntriesBoundariesPercentage() {
        return Mth.clamp(appearance.verticalEntriesBoundaries, 0.1, 1);
    }
    
    @ApiStatus.Experimental
    @Override
    public double getHorizontalEntriesBoundariesColumns() {
        return Mth.clamp(appearance.horizontalEntriesBoundariesColumns, 1, 1000);
    }
    
    @ApiStatus.Experimental
    @Override
    public double getVerticalEntriesBoundariesRows() {
        return Mth.clamp(appearance.verticalEntriesBoundariesRows, 1, 1000);
    }
    
    @ApiStatus.Experimental
    @Override
    public double getFavoritesHorizontalEntriesBoundariesPercentage() {
        return Mth.clamp(appearance.favoritesHorizontalEntriesBoundaries, 0.1, 1);
    }
    
    @Override
    public double getFavoritesHorizontalEntriesBoundariesColumns() {
        return Mth.clamp(appearance.favoritesHorizontalEntriesBoundariesColumns, 1, 1000);
    }
    
    @Override
    public SyntaxHighlightingMode getSyntaxHighlightingMode() {
        return appearance.syntaxHighlightingMode;
    }
    
    public void setSyntaxHighlightingMode(SyntaxHighlightingMode mode) {
        appearance.syntaxHighlightingMode = mode;
    }
    
    @Override
    public boolean isFocusModeZoomed() {
        return appearance.isFocusModeZoomed;
    }
    
    @Override
    public SearchMode getTooltipSearchMode() {
        return advanced.search.tooltipSearch;
    }
    
    @Override
    public SearchMode getTagSearchMode() {
        return advanced.search.tagSearch;
    }
    
    @Override
    public SearchMode getIdentifierSearchMode() {
        return advanced.search.identifierSearch;
    }
    
    @Override
    public SearchMode getModSearchMode() {
        return advanced.search.modSearch;
    }
    
    @Override
    public boolean isJEICompatibilityLayerEnabled() {
        return Platform.isForge() && advanced.enableJeiCompatibilityLayer;
    }
    
    public void setJEICompatibilityLayerEnabled(boolean value) {
        advanced.enableJeiCompatibilityLayer = value;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface DontApplyFieldName {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseSpecialRecipeTypeScreen {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseSpecialSearchFilterSyntaxHighlightingScreen {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseFilteringScreen {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UseFilteringCategoriesScreen {}
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    @interface UsePercentage {
        double min();
        
        double max();
        
        String prefix() default "Size: ";
    }
    
    public static class Basics {
        @ConfigEntry.Gui.Excluded public List<FavoriteEntry> favorites = new ArrayList<>();
        @ConfigEntry.Gui.Excluded public List<FavoriteEntry> hiddenFavorites = new ArrayList<>();
        @ConfigEntry.Gui.Excluded public List<CompoundTag> displayHistory = new ArrayList<>();
        @Comment("Declares whether cheating mode is on.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private CheatingMode cheating = CheatingMode.OFF;
        private boolean favoritesEnabled = true;
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        private KeyBindings keyBindings = new KeyBindings();
        @Comment("Declares whether REI is visible.") @ConfigEntry.Gui.Excluded private boolean overlayVisible = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private ItemCheatingStyle cheatingStyle = ItemCheatingStyle.GRAB;
        @ConfigEntry.Gui.CollapsibleObject
        private Motion motion = new Motion();
    }
    
    public static class Motion {
        private boolean favoritesAnimation = true;
    }
    
    public static class KeyBindings {
        private ModifierKeyCode recipeKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_R), Modifier.none());
        private ModifierKeyCode usageKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_U), Modifier.none());
        private ModifierKeyCode hideKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_O), Modifier.of(false, true, false));
        private ModifierKeyCode previousPageKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode nextPageKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode focusSearchFieldKeybind = ModifierKeyCode.unknown();
        private ModifierKeyCode copyRecipeIdentifierKeybind = ModifierKeyCode.of(InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_MIDDLE), Modifier.none());
        private ModifierKeyCode favoriteKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_A), Modifier.none());
        private ModifierKeyCode exportImageKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_F8), Modifier.none());
        private ModifierKeyCode previousScreenKeybind = ModifierKeyCode.of(InputConstants.Type.KEYSYM.getOrCreate(InputConstants.KEY_BACKSPACE), Modifier.none());
    }
    
    public static class Appearance {
        @UseSpecialRecipeTypeScreen private DisplayScreenType recipeScreenType = DisplayScreenType.UNSET;
        @Comment("Declares the appearance of REI windows.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private AppearanceTheme theme = AppearanceTheme.LIGHT;
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        private Layout layout = new Layout();
        @Comment("Declares the appearance of recipe's border.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private RecipeBorderType recipeBorder = RecipeBorderType.DEFAULT;
        @Comment("Declares whether entry panel is scrolled.") private boolean scrollingEntryListWidget = false;
        
        public static class Layout {
            @Comment("Declares the position of the search field.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private SearchFieldLocation searchFieldLocation = SearchFieldLocation.CENTER;
            @Comment("Declares the position of the config button.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private ConfigButtonPosition configButtonLocation = ConfigButtonPosition.LOWER;
            @Comment("Declares whether the craftable filter button is enabled.") private boolean showCraftableOnlyButton = true;
        }
        
        @UsePercentage(min = 0.1, max = 1.0, prefix = "Limit: ") private double horizontalEntriesBoundaries = 1.0;
        @UsePercentage(min = 0.1, max = 1.0, prefix = "Limit: ") private double verticalEntriesBoundaries = 1.0;
        private int horizontalEntriesBoundariesColumns = 50;
        private int verticalEntriesBoundariesRows = 1000;
        @UsePercentage(min = 0.1, max = 1.0, prefix = "Limit: ") private double favoritesHorizontalEntriesBoundaries = 1.0;
        private int favoritesHorizontalEntriesBoundariesColumns = 50;
        @UseSpecialSearchFilterSyntaxHighlightingScreen private SyntaxHighlightingMode syntaxHighlightingMode = SyntaxHighlightingMode.COLORFUL;
        private boolean isFocusModeZoomed = false;
    }
    
    public static class Functionality {
        @ConfigEntry.Gui.Excluded @Nullable private ResourceLocation inputMethod = null;
        @Comment("Declares whether REI should remove the recipe book.") private boolean disableRecipeBook = false;
        @Comment("Declares whether mob effects should be on the left side instead of the right side.") private boolean leftSideMobEffects = false;
        @Comment("Declares whether subsets is enabled.") private boolean isSubsetsEnabled = false;
        private boolean allowInventoryHighlighting = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        private ItemCheatingMode itemCheatingMode = ItemCheatingMode.REI_LIKE;
    }
    
    public static class Advanced {
        @ConfigEntry.Gui.CollapsibleObject
        private Tooltips tooltips = new Tooltips();
        @ConfigEntry.Gui.CollapsibleObject
        private Layout layout = new Layout();
        @ConfigEntry.Gui.CollapsibleObject
        private Accessibility accessibility = new Accessibility();
        @ConfigEntry.Gui.CollapsibleObject
        private Search search = new Search();
        @ConfigEntry.Gui.CollapsibleObject
        private Commands commands = new Commands();
        @ConfigEntry.Gui.CollapsibleObject
        private Miscellaneous miscellaneous = new Miscellaneous();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Filtering filtering = new Filtering();
        @ConfigEntry.Gui.Excluded
        public boolean enableJeiCompatibilityLayer = true;
        
        public static class Tooltips {
            @Comment("Declares whether REI should append mod names to entries.") private boolean appendModNames = true;
            @Comment("Declares whether favorites tooltip should be displayed.") private boolean displayFavoritesTooltip = false;
        }
        
        public static class Layout {
            @Comment("The ordering of the items on the entry panel.")
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private EntryPanelOrderingConfig entryPanelOrdering = EntryPanelOrderingConfig.REGISTRY_ASCENDING;
            @Comment("Declares the maximum amount of recipes displayed in a page if possible.") @ConfigEntry.BoundedDiscrete(min = 2, max = 99)
            private int maxRecipesPerPage = 8;
            @Comment("Declares the maximum amount of recipes displayed in a page if possible.") @ConfigEntry.BoundedDiscrete(min = 100, max = 1000)
            private int maxRecipesPageHeight = 300;
            @Comment("Declares whether entry rendering time should be debugged.") private boolean debugRenderTimeRequired = false;
            @Comment("Merges displays with equal contents under 1 display.") private boolean mergeDisplayUnderOne = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private FavoriteAddWidgetMode favoriteAddWidgetMode = FavoriteAddWidgetMode.ALWAYS_VISIBLE;
        }
        
        public static class Accessibility {
            @UsePercentage(min = 0.25, max = 4.0) private double entrySize = 1.0;
            @Comment("Declares the position of the entry panel.") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private DisplayPanelLocation displayPanelLocation = DisplayPanelLocation.RIGHT;
            @Comment("Declares how the scrollbar in composite screen should act.") private boolean compositeScrollBarPermanent = false;
            private boolean toastDisplayedOnCopyIdentifier = true;
            @Comment("Declares whether REI should use compact tabs for categories.") private boolean useCompactTabs = true;
            @Comment("Declares whether REI should use compact tab buttons for categories.") @ConfigEntry.Gui.Excluded private boolean useCompactTabButtons = false;
        }
        
        public static class Search {
            @Comment("Declares whether search time should be debugged.") private boolean debugSearchTimeRequired = false;
            @Comment("Declares whether REI should search async.") private boolean asyncSearch = true;
            @Comment("Declares how many entries should be grouped one async search.") @ConfigEntry.BoundedDiscrete(min = 25, max = 400)
            private int asyncSearchPartitionSize = 100;
            private boolean patchAsyncThreadCrash = true;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private SearchMode tooltipSearch = SearchMode.ALWAYS;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private SearchMode tagSearch = SearchMode.PREFIX;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private SearchMode identifierSearch = SearchMode.ALWAYS;
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            private SearchMode modSearch = SearchMode.PREFIX;
        }
        
        public static class Commands {
            @Comment("Declares the command used to change gamemode.") private String gamemodeCommand = "/gamemode {gamemode}";
            @Comment("Declares the command used in servers to cheat items.") private String giveCommand = "/give {player_name} {item_identifier}{nbt} {count}";
            @Comment("Declares the command used to change weather.") private String weatherCommand = "/weather {weather}";
            @Comment("Declares the command used to change time.") private String timeCommand = "/time set {time}";
        }
        
        public static class Miscellaneous {
            @Comment("Declares whether arrows in containers should be clickable.") private boolean clickableRecipeArrows = true;
            private boolean registerRecipesInAnotherThread = true;
            private boolean newFastEntryRendering = true;
            @ConfigEntry.Gui.PrefixText
            private boolean cachingFastEntryRendering = false;
        }
        
        public static class Filtering {
            @UseFilteringScreen private List<EntryStackProvider<?>> filteredStacks = new ArrayList<>();
            public boolean shouldFilterDisplays = true;
            @ConfigEntry.Gui.Excluded public List<FilteringRule<?>> filteringRules = new ArrayList<>();
            @UseFilteringCategoriesScreen public Map<CategoryIdentifier<?>, Boolean> filteringQuickCraftCategories = new HashMap<>();
        }
    }
}
