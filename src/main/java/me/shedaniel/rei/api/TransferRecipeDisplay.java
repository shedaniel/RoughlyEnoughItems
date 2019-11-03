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
    
    List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container);
    
}
