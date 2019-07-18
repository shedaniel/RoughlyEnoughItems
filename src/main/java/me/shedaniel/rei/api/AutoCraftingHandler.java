/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.function.Supplier;

public interface AutoCraftingHandler {
    
    default double getPriority() {
        return 0d;
    }
    
    boolean handle(Supplier<RecipeDisplay> displaySupplier, Minecraft minecraft, GuiScreen recipeViewingScreen, GuiContainer parentScreen, ContainerScreenOverlay overlay);
    
    boolean canHandle(Supplier<RecipeDisplay> displaySupplier, Minecraft minecraft, GuiScreen recipeViewingScreen, GuiContainer parentScreen, ContainerScreenOverlay overlay);
    
}
