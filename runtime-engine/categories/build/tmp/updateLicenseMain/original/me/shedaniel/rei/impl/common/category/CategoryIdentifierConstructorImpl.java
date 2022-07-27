package me.shedaniel.rei.impl.common.category;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CategoryIdentifierConstructorImpl implements Internals.CategoryIdentifierConstructor {
    private static final Map<String, CategoryIdentifier<?>> CACHE = new ConcurrentHashMap<>();
    
    @Override
    public <T extends Display> CategoryIdentifier<T> create(String location) {
        CategoryIdentifier<?> identifier = CACHE.get(location);
        if (identifier != null) return identifier.cast();
        identifier = new CategoryIdentifierImpl<>(new ResourceLocation(location));
        CACHE.put(location, identifier);
        return identifier.cast();
    }
}
