/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface TransferRecipeDisplay extends RecipeDisplay {
    
    int getWidth();
    
    int getHeight();
    
    default List<List<ItemStack>> getOrganisedInput(ContainerInfo<Container> containerInfo, Container container) {
        List<List<ItemStack>> list = Lists.newArrayListWithCapacity(containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container));
        for (int i = 0; i < containerInfo.getCraftingWidth(container) * containerInfo.getCraftingHeight(container); i++) {
            list.add(Lists.newArrayList());
        }
        return list;
    }
    
    default List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container) {
        List<List<ItemStack>> input = getOrganisedInput(containerInfo, container);
        if (input.isEmpty())
            return Collections.emptyList();
        List<List<EntryStack>> list = new ArrayList<>();
        for (List<ItemStack> stacks : input) {
            List<EntryStack> entries = new ArrayList<>();
            for (ItemStack stack : stacks) {
                entries.add(EntryStack.create(stack));
            }
            list.add(entries);
        }
        return list;
    }
    
}
