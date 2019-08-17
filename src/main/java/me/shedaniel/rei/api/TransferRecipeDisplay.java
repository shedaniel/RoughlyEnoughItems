package me.shedaniel.rei.api;

import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface TransferRecipeDisplay extends RecipeDisplay {
    
    int getWidth();
    
    int getHeight();
    
    List<List<ItemStack>> getOrganisedInput(ContainerInfo<Container> containerInfo, Container container);
    
}
