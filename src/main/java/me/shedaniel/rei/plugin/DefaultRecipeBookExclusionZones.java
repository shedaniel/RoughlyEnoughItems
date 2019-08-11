/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.recipe.book.ClientRecipeBook;
import net.minecraft.container.CraftingContainer;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DefaultRecipeBookExclusionZones implements Function<Boolean, List<Rectangle>> {
    
    @Override
    public List<Rectangle> apply(Boolean isOnRightSide) {
        if (isOnRightSide || !MinecraftClient.getInstance().player.getRecipeBook().isGuiOpen() || !(MinecraftClient.getInstance().currentScreen instanceof RecipeBookProvider) || !(ScreenHelper.getLastContainerScreen().getContainer() instanceof CraftingContainer))
            return Collections.emptyList();
        ContainerScreenHooks screenHooks = ScreenHelper.getLastContainerScreenHooks();
        List<Rectangle> l = Lists.newArrayList(new Rectangle(screenHooks.rei_getContainerLeft() - 4 - 145, screenHooks.rei_getContainerTop(), 4 + 145 + 30, screenHooks.rei_getContainerHeight()));
        int size = ClientRecipeBook.getGroupsForContainer((CraftingContainer<?>) ScreenHelper.getLastContainerScreen().getContainer()).size();
        if (size > 0)
            l.add(new Rectangle(screenHooks.rei_getContainerLeft() - 4 - 145 - 30, screenHooks.rei_getContainerTop(), 30, size * 27));
        return l;
    }
    
}
