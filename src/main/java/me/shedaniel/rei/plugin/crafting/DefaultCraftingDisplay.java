/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.crafting;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.plugin.DefaultPlugin;
import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.container.Container;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface DefaultCraftingDisplay extends TransferRecipeDisplay {

    @Override
    default Identifier getRecipeCategory() {
        return DefaultPlugin.CRAFTING;
    }

    @Override
    default public int getWidth() {
        return 2;
    }

    @Override
    default public int getHeight() {
        return 2;
    }

    Optional<Recipe<?>> getOptionalRecipe();

    @Override
    default List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container) {
        List<List<EntryStack>> list = Lists.newArrayListWithCapacity(containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container));
        for (int i = 0; i < containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container); i++) {
            list.add(Collections.emptyList());
        }
        for (int i = 0; i < getInputEntries().size(); i++) {
            List<EntryStack> stacks = getInputEntries().get(i);
            list.set(DefaultCraftingCategory.getSlotWithSize(this, i, containerInfo.getCraftingWidth(container)), stacks);
        }
        return list;
    }
}
