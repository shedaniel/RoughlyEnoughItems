package me.shedaniel.rei.plugin.smithing;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultSmithingDisplay implements RecipeDisplay {
    @NotNull
    private List<List<EntryStack>> input;
    @NotNull
    private List<EntryStack> output;
    @Nullable
    private Identifier location;
    
    public DefaultSmithingDisplay(@NotNull SmithingRecipe recipe) {
        this(
                Lists.newArrayList(
                        CollectionUtils.map(recipe.base.getMatchingStacksClient(), EntryStack::create),
                        CollectionUtils.map(recipe.addition.getMatchingStacksClient(), EntryStack::create)
                ),
                Collections.singletonList(EntryStack.create(recipe.getOutput())),
                recipe.getId()
        );
    }
    
    public DefaultSmithingDisplay(@NotNull List<List<EntryStack>> input, @NotNull List<EntryStack> output, @Nullable Identifier location) {
        this.input = input;
        this.output = output;
        if (this.input.size() != 2) throw new IllegalArgumentException("input must have 2 entries.");
        this.location = location;
    }
    
    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }
    
    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }
    
    @Override
    public Identifier getRecipeCategory() {
        return DefaultPlugin.SMITHING;
    }
    
    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(location);
    }
}
