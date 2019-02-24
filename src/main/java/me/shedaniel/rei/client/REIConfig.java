package me.shedaniel.rei.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class REIConfig {
    
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public REIItemListOrdering itemListOrdering = REIItemListOrdering.REGISTRY;
    public boolean isAscending = true;
    public boolean enableCraftableOnlyButton = true;
    public boolean sideSearchField = false;
    public String giveCommand = "/give {player_name} {item_identifier}{nbt} {count}";
    public String gamemodeCommand = "/gamemode {gamemode}";
    public String weatherCommand = "/weather {weather}";
    public boolean checkUpdates = true;
    public boolean mirrorItemPanel = false;
    public boolean loadDefaultPlugin = true;
    public boolean disableCreditsButton = false;
    public int maxRecipePerPage = 3;
    public boolean fixRamUsage = false;
    public boolean showUtilsButtons = false;
    
}
