package me.shedaniel.rei.api.registry;

import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public interface CategoryRegistry extends Reloadable {
    static CategoryRegistry getInstance() {
        
    }
    
    /**
     * Registers a category
     *
     * @param category the category to register
     */
    <T extends Display> void register(DisplayCategory<T> category);
    
    default <T extends Display> void register(Iterable<DisplayCategory<T>> categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    default <T extends Display> void register(DisplayCategory<T>... categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    DisplayCategoryConfiguration<?> get(ResourceLocation category);
    
    <T extends Display> DisplayCategoryConfiguration<T> get(ResourceLocation category, Class<T> displayClass);
    
    void configure(ResourceLocation category, Consumer<DisplayCategoryConfiguration<?>> action);
    
    <T extends Display> void configure(ResourceLocation category, Class<T> displayClass, Consumer<DisplayCategoryConfiguration<T>> action);
    
    default void registerWorkingStations(ResourceLocation category, EntryIngredient... stations) {
        configure(category, config -> config.registerWorkingStations(stations));
    }
    
    default void registerWorkingStations(ResourceLocation category, EntryStack<?>... stations) {
        configure(category, config -> config.registerWorkingStations(stations));
    }
    
    interface DisplayCategoryConfiguration<T extends Display> {
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        void registerWorkingStations(EntryIngredient... stations);
        
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        default void registerWorkingStations(EntryStack<?>... stations) {
            registerWorkingStations(CollectionUtils.map(stations, EntryIngredient::of).toArray(new EntryIngredient[0]));
        }
        
        List<EntryIngredient> getWorkingStations();
        
        DisplayCategory<T> getCategory();
    }
}
