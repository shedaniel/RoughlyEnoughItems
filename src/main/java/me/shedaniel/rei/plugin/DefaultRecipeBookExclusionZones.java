/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.api.ContainerScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.util.RecipeBookClient;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class DefaultRecipeBookExclusionZones implements Function<Boolean, List<Rectangle>> {
    
    @Override
    public List<Rectangle> apply(Boolean isOnRightSide) {
        if (isOnRightSide || !Minecraft.getInstance().player.getRecipeBook().isGuiOpen() || !(Minecraft.getInstance().currentScreen instanceof IRecipeShownListener))
            return Collections.emptyList();
        ContainerScreenHooks screenHooks = ScreenHelper.getLastContainerScreenHooks();
        List<Rectangle> l = Lists.newArrayList(new Rectangle(screenHooks.rei_getContainerLeft() - 4 - 145, screenHooks.rei_getContainerTop(), 4 + 145 + 30, screenHooks.rei_getContainerHeight()));
        int size = RecipeBookClient.getCategoriesForContainer(ScreenHelper.getLastContainerScreen().inventorySlots).size();
        if (size > 0)
            l.add(new Rectangle(screenHooks.rei_getContainerLeft() - 4 - 145 - 30, screenHooks.rei_getContainerTop(), 30, size * 27));
        return l;
    }
    
}
