package me.shedaniel.rei.api.registry;

import me.shedaniel.rei.api.registry.category.DisplayCategory;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public interface CategoryRegistry {
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    void register(DisplayCategory<?> category);
    
    default void register(Iterable<DisplayCategory<?>> categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    default void register(DisplayCategory<?>... categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    void configure(ResourceLocation category, Consumer<RecipeCategoryConfiguration> action);
    
    default void registerWorkingStations(ResourceLocation category, EntryIngredient... stations) {
        configure(category, config -> config.registerWorkingStations(stations));
    }
    
    default void registerWorkingStations(ResourceLocation category, EntryStack<?>... stations) {
        configure(category, config -> config.registerWorkingStations(stations));
    }
    
    interface RecipeCategoryConfiguration {
        void registerWorkingStations(EntryIngredient... stations);
        
        /**
         * Registers the working stations of a category
         *
         * @param category the category
         * @param stations the working stations
         */
        void registerWorkingStations(EntryStack<?>... stations);
    }
    
    List<EntryIngredient> getWorkingStations(ResourceLocation category);
}
