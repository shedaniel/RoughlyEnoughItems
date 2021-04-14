package me.shedaniel.rei.jeicompat.unwrap;

import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.jeicompat.JEIPluginDetector.*;

public class JEIIngredientHelper<T> implements IIngredientHelper<T> {
    private final EntryDefinition<T> definition;
    
    public JEIIngredientHelper(EntryDefinition<T> definition) {
        this.definition = definition;
    }
    
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch) {
        return getMatch(ingredients, ingredientToMatch, UidContext.Ingredient);
    }
    
    @Nullable
    @Override
    public T getMatch(Iterable<T> ingredients, T ingredientToMatch, UidContext context) {
        ComparisonContext comparisonContext = wrapContext(context);
        return CollectionUtils.findFirstOrNull(ingredients, t -> definition.equals(t, ingredientToMatch, comparisonContext));
    }
    
    @Override
    public String getDisplayName(T ingredient) {
        return definition.asFormattedText(wrap(definition, ingredient), ingredient).getString();
    }
    
    @Override
    public String getUniqueId(T ingredient) {
        return getUniqueId(ingredient, UidContext.Ingredient);
    }
    
    @Override
    public String getUniqueId(T ingredient, UidContext context) {
        ComparisonContext comparisonContext = wrapContext(context);
        return String.valueOf(EntryStacks.hash(wrap(definition, ingredient), comparisonContext));
    }
    
    @Override
    public String getModId(T ingredient) {
        ResourceLocation location = definition.getIdentifier(wrap(definition, ingredient), ingredient);
        return location == null ? "minecraft" : location.getNamespace();
    }
    
    @Override
    public String getResourceId(T ingredient) {
        ResourceLocation location = definition.getIdentifier(wrap(definition, ingredient), ingredient);
        return location == null ? "minecraft:unknown" : location.toString();
    }
    
    @Override
    public T copyIngredient(T ingredient) {
        return definition.copy(wrap(definition, ingredient), ingredient);
    }
    
    @Override
    public String getErrorInfo(@Nullable T ingredient) {
        throw TODO();
    }
}
