package me.shedaniel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.event.KeyEvent;

public class REIConfig {
    
    public static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    public int recipeKeyBind = KeyEvent.VK_R;
    public int usageKeyBind = KeyEvent.VK_U;
    public int hideKeyBind = KeyEvent.VK_O;
    public boolean centreSearchBox = false;
    public REIItemListOrdering itemListOrdering = REIItemListOrdering.REGISTRY;
    public boolean isAscending = true;
    public boolean enableCraftableOnlyButton = true;
    
}
