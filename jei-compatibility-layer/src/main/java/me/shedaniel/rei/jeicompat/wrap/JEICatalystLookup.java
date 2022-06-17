package me.shedaniel.rei.jeicompat.wrap;

import lombok.experimental.ExtensionMethod;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.jeicompat.JEIPluginDetector;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeCatalystLookup;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@ExtensionMethod(JEIPluginDetector.class)
public class JEICatalystLookup implements IRecipeCatalystLookup {
    private final CategoryIdentifier<?> category;
    private boolean includeHidden = false;

    public JEICatalystLookup(CategoryIdentifier<?> category) {
        this.category = category;
    }

    @Override
    public IRecipeCatalystLookup includeHidden() {
        this.includeHidden = true;
        return this;
    }

    @Override
    public Stream<ITypedIngredient<?>> get() {
        Stream<ITypedIngredient<?>> stream = CategoryRegistry.getInstance().get(category)
                .getWorkstations()
                .stream()
                .flatMap(Collection::stream)
                .map(entry -> entry.typedJeiValue());
        return includeHidden ? stream : stream.filter(JEIIngredientVisibility.INSTANCE::isIngredientVisible);
    }

    @Override
    public <S> Stream<S> get(IIngredientType<S> ingredientType) {
        return get().map(entry -> entry.getIngredient(ingredientType)).flatMap(Optional::stream);
    }
}
