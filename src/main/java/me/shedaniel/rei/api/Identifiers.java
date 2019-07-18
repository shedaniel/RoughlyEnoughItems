package me.shedaniel.rei.api;

import net.minecraft.util.ResourceLocation;

public class Identifiers {
    
    public static Identifier of(ResourceLocation location) {
        return new Identifier(location.toString());
    }
    
    public static ResourceLocation of(Identifier identifier) {
        return new ResourceLocation(identifier.toString());
    }
    
}
