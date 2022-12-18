/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.registry.display;

import com.google.common.base.Preconditions;
import dev.architectury.event.EventResult;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReason;
import me.shedaniel.rei.api.client.registry.display.reason.DisplayAdditionReasons;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.registry.RecipeManagerContextImpl;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class DisplayRegistryImpl extends RecipeManagerContextImpl<REIClientPlugin> implements DisplayRegistry {
    private final Map<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> displayGenerators = new ConcurrentHashMap<>();
    private final List<DynamicDisplayGenerator<?>> globalDisplayGenerators = new ArrayList<>();
    private final List<DisplayVisibilityPredicate> visibilityPredicates = new ArrayList<>();
    private final List<DisplayFiller<?>> fillers = new ArrayList<>();
    private long lastAddWarning = -1;
    private DisplaysHolder displaysHolder = new DisplaysHolderImpl(false);
    
    public DisplayRegistryImpl() {
        super(RecipeManagerContextImpl.supplier());
    }
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerDisplays(this);
    }
    
    @Override
    public int displaySize() {
        return this.displaysHolder.size();
    }
    
    @Override
    public void add(Display display, @Nullable Object origin) {
        if (!PluginManager.areAnyReloading()) {
            if (lastAddWarning < 0 || System.currentTimeMillis() - lastAddWarning > 5000) {
                InternalLogger.getInstance().warn("Detected runtime DisplayRegistry modification, this can be extremely dangerous!");
                InternalLogger.getInstance().debug("Detected runtime DisplayRegistry modification, this can be extremely dangerous!", new Throwable());
            }
            lastAddWarning = System.currentTimeMillis();
        }
        
        this.displaysHolder.add(display, origin);
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<Display>> getAll() {
        return this.displaysHolder.get();
    }
    
    @Override
    public <A extends Display> void registerGlobalDisplayGenerator(DynamicDisplayGenerator<A> generator) {
        globalDisplayGenerators.add(generator);
        InternalLogger.getInstance().debug("Added global display generator: %s", generator);
    }
    
    @Override
    public <A extends Display> void registerDisplayGenerator(CategoryIdentifier<A> categoryId, DynamicDisplayGenerator<A> generator) {
        displayGenerators.computeIfAbsent(categoryId, location -> new ArrayList<>())
                .add(generator);
        InternalLogger.getInstance().debug("Added display generator for category [%s]: %s", categoryId, generator);
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<DynamicDisplayGenerator<?>>> getCategoryDisplayGenerators() {
        return Collections.unmodifiableMap(displayGenerators);
    }
    
    @Override
    public List<DynamicDisplayGenerator<?>> getGlobalDisplayGenerators() {
        return Collections.unmodifiableList(globalDisplayGenerators);
    }
    
    @Override
    public void registerVisibilityPredicate(DisplayVisibilityPredicate predicate) {
        visibilityPredicates.add(predicate);
        visibilityPredicates.sort(Comparator.reverseOrder());
        InternalLogger.getInstance().debug("Added display visibility predicate: %s [%.2f priority]", predicate, predicate.getPriority());
    }
    
    @Override
    public boolean isDisplayVisible(Display display) {
        DisplayCategory<Display> category = (DisplayCategory<Display>) CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory();
        Preconditions.checkNotNull(category, "Failed to resolve category: " + display.getCategoryIdentifier());
        for (DisplayVisibilityPredicate predicate : visibilityPredicates) {
            try {
                EventResult result = predicate.handleDisplay(category, display);
                if (result.interruptsFurtherEvaluation()) {
                    return result.isEmpty() || result.isTrue();
                }
            } catch (Throwable throwable) {
                InternalLogger.getInstance().error("Failed to check if the display is visible!", throwable);
            }
        }
        
        return true;
    }
    
    @Override
    public List<DisplayVisibilityPredicate> getVisibilityPredicates() {
        return Collections.unmodifiableList(visibilityPredicates);
    }
    
    @Override
    public <T, D extends Display> void registerFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<? extends T, D> filler) {
        registerFiller(o -> typeClass.isInstance(o) && ((Predicate<T>) predicate).test((T) o), o -> ((Function<T, D>) filler).apply((T) o));
    }
    
    @Override
    public <T, D extends Display> void registerDisplaysFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<? extends T, @Nullable Collection<? extends D>> filler) {
        registerDisplaysFiller(o -> typeClass.isInstance(o) && ((Predicate<T>) predicate).test((T) o), o -> ((Function<T, Collection<? extends D>>) filler).apply((T) o));
    }
    
    @Override
    public <T, D extends Display> void registerFiller(Class<T> typeClass, BiPredicate<? extends T, DisplayAdditionReasons> predicate, Function<? extends T, D> filler) {
        fillers.add(DisplayFiller.of((o, s) -> typeClass.isInstance(o) && ((BiPredicate<Object, DisplayAdditionReasons>) predicate).test(o, s), (Function<Object, D>) filler));
        InternalLogger.getInstance().debug("Added display filter: %s for %s", filler, typeClass.getName());
    }
    
    @Override
    public <T, D extends Display> void registerDisplaysFiller(Class<T> typeClass, BiPredicate<? extends T, DisplayAdditionReasons> predicate, Function<? extends T, @Nullable Collection<? extends D>> filler) {
        fillers.add(new DisplayFiller<>((o, s) -> typeClass.isInstance(o) && ((BiPredicate<Object, DisplayAdditionReasons>) predicate).test(o, s), (Function<Object, Collection<? extends D>>) filler));
        InternalLogger.getInstance().debug("Added display filter: %s for %s", filler, typeClass.getName());
    }
    
    @Override
    public <D extends Display> void registerFiller(Predicate<?> predicate, Function<?, D> filler) {
        fillers.add(DisplayFiller.of((o, s) -> ((Predicate<Object>) predicate).test(o), (Function<Object, D>) filler));
        InternalLogger.getInstance().debug("Added display filter: %s", filler);
    }
    
    @Override
    public <D extends Display> void registerDisplaysFiller(Predicate<?> predicate, Function<?, @Nullable Collection<? extends D>> filler) {
        fillers.add(new DisplayFiller<>((o, s) -> ((Predicate<Object>) predicate).test(o), (Function<Object, Collection<? extends D>>) filler));
        InternalLogger.getInstance().debug("Added display filter: %s", filler);
    }
    
    @Override
    public void startReload() {
        super.startReload();
        this.displaysHolder = new DisplaysHolderImpl(true);
        this.displayGenerators.clear();
        this.visibilityPredicates.clear();
        this.fillers.clear();
    }
    
    @Override
    public void endReload() {
        if (!fillers.isEmpty()) {
            List<Recipe<?>> allSortedRecipes = getAllSortedRecipes();
            for (int i = allSortedRecipes.size() - 1; i >= 0; i--) {
                Recipe<?> recipe = allSortedRecipes.get(i);
                addWithReason(recipe, DisplayAdditionReason.RECIPE_MANAGER);
            }
        }
        
        for (CategoryIdentifier<?> identifier : getAll().keySet()) {
            if (CategoryRegistry.getInstance().tryGet(identifier).isEmpty()) {
                InternalLogger.getInstance().error("Found displays registered for unknown registry", new IllegalStateException(identifier.toString()));
            }
        }
        
        this.displaysHolder.endReload();
        
        InternalLogger.getInstance().debug("Registered %d displays", displaySize());
    }
    
    public DisplaysHolder displaysHolder() {
        return displaysHolder;
    }
    
    @Override
    public <T> Collection<Display> tryFillDisplay(T value, DisplayAdditionReason... reason) {
        if (value instanceof Display) return Collections.singleton((Display) value);
        List<Display> out = null;
        DisplayAdditionReasons reasons = reason.length == 0 ? DisplayAdditionReasons.Impl.EMPTY : new DisplayAdditionReasons.Impl(reason);
        for (DisplayFiller<?> filler : fillers) {
            Collection<Display> displays = tryFillDisplayGenerics(filler, value, reasons);
            if (displays != null && !displays.isEmpty()) {
                if (out == null) out = new ArrayList<>();
                for (Display display : displays) {
                    if (display != null) out.add(display);
                }
            }
        }
        if (out != null) {
            return out;
        }
        return Collections.emptyList();
    }
    
    private <D extends Display> Collection<D> tryFillDisplayGenerics(DisplayFiller<? extends D> filler, Object value, DisplayAdditionReasons reasons) {
        try {
            if (filler.predicate.test(value, reasons)) {
                return (Collection<D>) filler.mappingFunction.apply(value);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to fill displays!", e);
        }
        
        return null;
    }
    
    @Override
    @Nullable
    public Object getDisplayOrigin(Display display) {
        return this.displaysHolder.getDisplayOrigin(display);
    }
    
    private record DisplayFiller<D extends Display>(
            BiPredicate<Object, DisplayAdditionReasons> predicate,
            
            Function<Object, Collection<? extends D>> mappingFunction
    ) {
        public static <D extends Display> DisplayFiller<D> of(BiPredicate<Object, DisplayAdditionReasons> predicate, Function<Object, D> mappingFunction) {
            return new DisplayFiller<>(predicate, o -> Collections.singleton(mappingFunction.apply(o)));
        }
    }
}
