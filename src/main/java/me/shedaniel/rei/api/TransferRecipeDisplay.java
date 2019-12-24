/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.container.Container;

import java.util.List;

public interface TransferRecipeDisplay extends RecipeDisplay {

    int getWidth();

    int getHeight();

    List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<Container> containerInfo, Container container);

}
