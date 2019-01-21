package me.shedaniel.rei.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class REIPluginInfo {
    
    public static Gson GSON = new GsonBuilder().create();
    
    private List<REIPlugin> plugins;
    
    public List<REIPlugin> getPlugins() {
        return plugins;
    }
    
    public static class REIPlugin {
        private String identifier;
        @SerializedName("class") private String pluginClass;
        
        public String getIdentifier() {
            if (identifier == null)
                return "null:null";
            return identifier;
        }
        
        public String getPluginClass() {
            return pluginClass;
        }
    }
    
}
