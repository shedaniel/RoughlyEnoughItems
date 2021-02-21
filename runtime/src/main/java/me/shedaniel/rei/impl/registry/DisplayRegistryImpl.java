package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.api.DisplayRegistry;
import me.shedaniel.rei.api.LiveDisplayGenerator;
import me.shedaniel.rei.api.registry.display.Display;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class DisplayRegistryImpl extends RecipeManagerContextImpl implements DisplayRegistry {
    private final Map<ResourceLocation, List<Display>> displays = new ConcurrentHashMap<>();
    private final MutableInt displayCount = new MutableInt(0);
    
    @Override
    public int getDisplayCount() {
        return displayCount.getValue();
    }
    
    @Override
    public void registerDisplay(Display display) {
        displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new ArrayList<>())
                .add(display);
    }
    
    @Override
    public Map<ResourceLocation, List<Display>> getAllRecipes() {
        return Collections.unmodifiableMap(displays);
    }
    
    @Override
    public void registerLiveDisplayGenerator(ResourceLocation categoryId, LiveDisplayGenerator<?> generator) {
        
    }
    
    @Override
    public <T extends Recipe<?>> void registerRecipes(ResourceLocation category, Predicate<? extends T> recipeFilter, Function<T, Display> mappingFunction) {
        
    }
    
    @Override
    public void resetData() {
        super.resetData();
        this.displays.clear();
        this.displayCount.setValue(0);
    }
}
