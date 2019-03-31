package me.shedaniel.rei.client;

import blue.endless.jankson.Comment;
import me.shedaniel.rei.api.RelativePoint;

public class ConfigObject {
    
    public boolean cheating = false;
    
    @Comment("The ordering of the items on the item panel.")
    public ItemListOrdering itemListOrdering = ItemListOrdering.registry;
    
    @Comment("The ordering of the items on the item panel.")
    public boolean isAscending = true;
    
    @Comment("To toggle the craftable button next to the search field.")
    public boolean enableCraftableOnlyButton = true;
    
    @Comment("True: search field will be on the side (left / right), false: in the middle")
    public boolean sideSearchField = false;
    
    @Comment("The command used in servers to cheat items")
    public String giveCommand = "/give {player_name} {item_identifier}{nbt} {count}";
    
    @Comment("The command used to change gamemode")
    public String gamemodeCommand = "/gamemode {gamemode}";
    
    @Comment("The command used to change weather")
    public String weatherCommand = "/weather {weather}";
    
    @Comment("True: item panel on the left, false: on the right")
    public boolean mirrorItemPanel = false;
    
    @Comment("To disable REI's default plugin, don't change this unless you understand what you are doing")
    public boolean loadDefaultPlugin = true;
    
    @Comment("Maximum recipes viewed at one time.")
    public int maxRecipePerPage = 3;
    
    @Comment("Toggle utils buttons")
    public boolean showUtilsButtons = false;
    
    @Comment("Disable Recipe Book")
    public boolean disableRecipeBook = false;
    
    public boolean preferVisibleRecipes = false;
    
    @Comment("Enable support for old REI plugins which uses registerSpeedCraft")
    public boolean enableLegacySpeedCraftSupport = false;
    
    @Comment("The location of choose page dialog")
    public RelativePoint choosePageDialogPoint = new RelativePoint(.5, .5);
    
}
