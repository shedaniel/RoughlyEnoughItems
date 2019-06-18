package me.shedaniel.rei.api;

import me.shedaniel.rei.gui.ContainerScreenOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;

public interface AutoCraftingHandler {
    
    default double getPriority() {
        return 0d;
    }
    
    boolean handle(MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay);
    
}
