package me.shedaniel.rei.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class REIConfig {
    
    public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public REIItemListOrdering itemListOrdering = REIItemListOrdering.REGISTRY;
    public boolean isAscending = true;
    public boolean enableCraftableOnlyButton = true;
    public boolean sideSearchField = false;
    public String giveCommandPrefix = "/give";
    public boolean checkUpdates = true;
    public boolean mirrorItemPanel = false;
    
}
