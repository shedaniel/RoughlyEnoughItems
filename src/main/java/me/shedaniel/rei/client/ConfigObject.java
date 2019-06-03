/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.client;

import blue.endless.jankson.Comment;
import me.shedaniel.rei.api.ItemCheatingMode;
import me.shedaniel.rei.api.RelativePoint;

public class ConfigObject {
    
    public boolean cheating = false;
    
    @Comment("The ordering of the items on the item panel.")
    public ItemListOrdering itemListOrdering = ItemListOrdering.registry;
    
    @Comment("The ordering of the items on the item panel.") public boolean isAscending = true;
    
    @Comment("To toggle the craftable button next to the search field.")
    public boolean enableCraftableOnlyButton = true;
    
    @Comment("True: search field will be on the side (left / right), false: in the middle")
    public boolean sideSearchField = false;
    
    @Comment("The command used in servers to cheat items")
    public String giveCommand = "/minecraft:give {player_name} {item_identifier}{nbt} {count}";
    
    @Comment("The command used to change gamemode") public String gamemodeCommand = "/gamemode {gamemode}";
    
    @Comment("The command used to change weather") public String weatherCommand = "/weather {weather}";
    
    @Comment("True: item panel on the left, false: on the right") public boolean mirrorItemPanel = false;
    
    @Comment("To disable REI's default plugin, don't change this unless you understand what you are doing")
    public boolean loadDefaultPlugin = true;
    
    @Comment("Maximum recipes viewed at one time.") public int maxRecipePerPage = 3;
    
    @Comment("Toggle utils buttons") public boolean showUtilsButtons = false;
    
    @Comment("Disable Recipe Book") public boolean disableRecipeBook = false;
    
    @Comment("Force enable 2019 REI April Fools' joke") public boolean aprilFoolsFish2019 = false;
    
    public ItemCheatingMode itemCheatingMode = ItemCheatingMode.REI_LIKE;
    
    public boolean lightGrayRecipeBorder = false;
    
    public RecipeScreenType screenType = RecipeScreenType.UNSET;
    
    @Comment(
            "The location of choose page dialog, will automatically be set to your last location so there is no need to change this.")
    public RelativePoint choosePageDialogPoint = new RelativePoint(.5, .5);
    
}
