package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.api.ButtonAreaSupplier;
import me.shedaniel.rei.api.ingredient.EntryIngredient;
import me.shedaniel.rei.api.registry.CategoryRegistry;
import me.shedaniel.rei.api.registry.Reloadable;
import me.shedaniel.rei.api.registry.display.Display;
import me.shedaniel.rei.api.registry.display.DisplayCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@ApiStatus.Internal
public class CategoryRegistryImpl implements CategoryRegistry, Reloadable {
    private final Map<ResourceLocation, Configuration<?>> categories = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, List<Consumer<CategoryConfiguration<?>>>> listeners = new ConcurrentHashMap<>();
    
    @Override
    public void resetData() {
        this.categories.clear();
    }
    
    @Override
    public <T extends Display> void register(DisplayCategory<T> category, Consumer<CategoryConfiguration<T>> configurator) {
        Configuration<T> configuration = new Configuration<>(category);
        this.categories.put(category.getIdentifier(), configuration);
        configurator.accept(configuration);
        
        List<Consumer<CategoryConfiguration<?>>> listeners = this.listeners.get(category.getIdentifier());
        if (listeners != null) {
            this.listeners.remove(category.getIdentifier());
            for (Consumer<CategoryConfiguration<?>> listener : listeners) {
                listener.accept(configuration);
            }
        }
    }
    
    @Override
    public <T extends Display> CategoryConfiguration<T> get(ResourceLocation category) {
        return (CategoryConfiguration<T>) this.categories.get(category);
    }
    
    @Override
    public <T extends Display> void configure(ResourceLocation category, Consumer<CategoryConfiguration<T>> action) {
        if (this.categories.containsKey(category)) {
            action.accept(get(category));
        } else {
            //noinspection rawtypes
            listeners.computeIfAbsent(category, location -> new ArrayList<>()).add((Consumer) action);
        }
    }
    
    @NotNull
    @Override
    public Iterator<CategoryConfiguration<?>> iterator() {
        return (Iterator) categories.values().iterator();
    }
    
    @Override
    public int size() {
        return categories.size();
    }
    
    private static class Configuration<T extends Display> implements CategoryConfiguration<T> {
        private final DisplayCategory<T> category;
        private final List<EntryIngredient> workstations = Collections.synchronizedList(new ArrayList<>());
        private Optional<ButtonAreaSupplier> plusButtonArea = Optional.empty();
        
        public Configuration(DisplayCategory<T> category) {
            this.category = category;
        }
        
        @Override
        public void addWorkstations(EntryIngredient... stations) {
            this.workstations.addAll(Arrays.asList(stations));
        }
        
        @Override
        public void setPlusButtonArea(ButtonAreaSupplier supplier) {
            this.plusButtonArea = Optional.ofNullable(supplier);
        }
        
        @Override
        public Optional<ButtonAreaSupplier> getPlusButtonArea() {
            return plusButtonArea;
        }
        
        @Override
        public List<EntryIngredient> getWorkstations() {
            return Collections.unmodifiableList(this.workstations);
        }
        
        @Override
        public DisplayCategory<T> getCategory() {
            return this.category;
        }
        
        @Override
        public ResourceLocation getIdentifier() {
            return this.category.getIdentifier();
        }
    }
}
