package me.shedaniel.rei.api.registry;

import me.shedaniel.rei.api.ButtonAreaSupplier;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import me.shedaniel.rei.api.util.CollectionUtils;
import me.shedaniel.rei.api.util.Identifiable;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface CategoryRegistry extends Reloadable, Iterable<CategoryRegistry.CategoryConfiguration<?>> {
    /**
     * @return the instance of {@link CategoryRegistry}
     */
    static CategoryRegistry getInstance() {
        return Internals.getCategoryRegistry();
    }
    
    /**
     * Registers a category.
     *
     * @param category the category to register
     */
    default <T extends Display> void register(DisplayCategory<T> category) {
        register(category, config -> {});
    }
    
    /**
     * Registers a category.
     *
     * @param category     the category to register
     * @param configurator the consumer for configuring the attributes of the category
     */
    <T extends Display> void register(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator);
    
    /**
     * Registers the categories supplied.
     *
     * @param categories the categories to register
     */
    default <T extends Display> void register(Iterable<DisplayCategory<? extends T>> categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    /**
     * Registers the categories supplied.
     *
     * @param categories the categories to register
     */
    default <T extends Display> void register(DisplayCategory<? extends T>... categories) {
        for (DisplayCategory<?> category : categories) {
            register(category);
        }
    }
    
    <T extends Display> CategoryConfiguration<T> get(ResourceLocation category);
    
    <T extends Display> void configure(ResourceLocation category, Consumer<CategoryConfiguration<T>> action);
    
    int size();
    
    default void addWorkstations(ResourceLocation category, EntryIngredient... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    default void addWorkstations(ResourceLocation category, EntryStack<?>... stations) {
        configure(category, config -> config.addWorkstations(stations));
    }
    
    default void removePlusButton(ResourceLocation category) {
        configure(category, CategoryConfiguration::removePlusButton);
    }
    
    default void setPlusButtonArea(ResourceLocation category, ButtonAreaSupplier supplier) {
        configure(category, config -> config.setPlusButtonArea(supplier));
    }
    
    interface CategoryConfiguration<T extends Display> extends Identifiable {
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        void addWorkstations(EntryIngredient... stations);
        
        /**
         * Registers the working stations of a category
         *
         * @param stations the working stations
         */
        default void addWorkstations(EntryStack<?>... stations) {
            addWorkstations(CollectionUtils.map(stations, EntryIngredient::of).toArray(new EntryIngredient[0]));
        }
        
        /**
         * Removes the plus button.
         */
        default void removePlusButton() {
            setPlusButtonArea(bounds -> null);
        }
        
        /**
         * Sets the plus button area
         *
         * @param supplier the supplier of the button area
         */
        void setPlusButtonArea(ButtonAreaSupplier supplier);
        
        /**
         * Returns the optional plus button area
         *
         * @return the optional plus button area
         */
        Optional<ButtonAreaSupplier> getPlusButtonArea();
        
        List<EntryIngredient> getWorkstations();
        
        DisplayCategory<T> getCategory();
    }
}
