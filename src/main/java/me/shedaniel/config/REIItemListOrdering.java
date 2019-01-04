package me.shedaniel.config;

import com.google.gson.annotations.SerializedName;

public enum REIItemListOrdering {
    
    @SerializedName("registry") REGISTRY("ordering.rei.registry"),
    @SerializedName("name") NAME("ordering.rei.name"),
    @SerializedName("item_groups") ITEM_GROUPS("ordering.rei.item_groups");
    
    private String nameTranslationKey;
    
    REIItemListOrdering(String nameTranslationKey) {
        this.nameTranslationKey = nameTranslationKey;
    }
    
    public String getNameTranslationKey() {
        return nameTranslationKey;
    }
}
