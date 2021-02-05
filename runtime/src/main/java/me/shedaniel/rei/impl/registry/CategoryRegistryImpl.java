package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.registry.CategoryRegistry;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CategoryRegistryImpl implements CategoryRegistry, Reloadable {
    private final Map<ResourceLocation, Configuration<?>> categories = new ConcurrentHashMap<>();
    
    @Override
    public void resetData() {
        this.categories.clear();
    }
    
    @Override
    public <T extends Display> void register(DisplayCategory<T> category) {
        this.categories.put(category.getIdentifier(), new Configuration<>(category));
    }
    
    @Override
    public DisplayCategoryConfiguration<?> get(ResourceLocation category) {
        return this.categories.get(category);
    }
    
    @Override
    public <T extends Display> DisplayCategoryConfiguration<T> get(ResourceLocation category, Class<T> displayClass) {
        return null;
    }
    
    @Override
    public void configure(ResourceLocation category, Consumer<DisplayCategoryConfiguration<?>> action) {
        
    }
    
    @Override
    public <T extends Display> void configure(ResourceLocation category, Class<T> displayClass, Consumer<DisplayCategoryConfiguration<T>> action) {
        
    }
    
    private static class Configuration<T extends Display> implements DisplayCategoryConfiguration<T> {
        private final DisplayCategory<T> category;
        private final List<EntryIngredient> workingStations = Collections.synchronizedList(new ArrayList<>());
        
        public Configuration(DisplayCategory<T> category) {
            
            this.category = category;
        }
        
        @Override
        public void registerWorkingStations(EntryIngredient... stations) {
            this.workingStations.addAll(Arrays.asList(stations));
        }
        
        @Override
        public List<EntryIngredient> getWorkingStations() {
            return this.workingStations;
        }
        
        @Override
        public DisplayCategory<T> getCategory() {
            return this.category;
        }
    }
}
