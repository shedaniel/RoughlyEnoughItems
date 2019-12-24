/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.cooking;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class DefaultCookingDisplay implements TransferRecipeDisplay {
    private AbstractCookingRecipe recipe;
    private List<List<EntryStack>> input;
    private List<EntryStack> output;

    public DefaultCookingDisplay(AbstractCookingRecipe recipe) {
        this.recipe = recipe;
        this.input = recipe.getPreviewInputs().stream().map(i -> {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : i.getMatchingStacksClient()) {
                entries.add(EntryStack.create(stack));
            }
            return entries;
        }).collect(Collectors.toList());
        this.input.add(FurnaceBlockEntity.createFuelTimeMap().keySet().stream().map(Item::getStackForRender).map(EntryStack::create).map(e -> e.setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, stack -> Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("category.rei.smelting.fuel")))).collect(Collectors.toList()));
        this.output = Collections.singletonList(EntryStack.create(recipe.getOutput()));
    }

    @Override
    public Optional<Identifier> getRecipeLocation() {
        return Optional.ofNullable(recipe).map(AbstractCookingRecipe::getId);
    }

    @Override
    public List<List<EntryStack>> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryStack> getOutputEntries() {
        return output;
    }

    public List<EntryStack> getFuel() {
        return input.get(1);
    }

    @Override
    public List<List<EntryStack>> getRequiredEntries() {
        return input;
    }

    @Deprecated
    public Optional<AbstractCookingRecipe> getOptionalRecipe() {
        return Optional.ofNullable(recipe);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container) {
        return CollectionUtils.map(recipe.getPreviewInputs(), i -> CollectionUtils.map(i.getMatchingStacksClient(), EntryStack::create));
    }

}
