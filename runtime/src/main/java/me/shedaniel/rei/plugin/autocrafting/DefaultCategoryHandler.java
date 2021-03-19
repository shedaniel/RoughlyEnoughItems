/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.plugin.autocrafting;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.registry.display.TransferDisplay;
import me.shedaniel.rei.api.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.server.ContainerContext;
import me.shedaniel.rei.api.server.ContainerInfo;
import me.shedaniel.rei.api.server.ContainerInfoHandler;
import me.shedaniel.rei.api.server.RecipeFinder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCategoryHandler implements TransferHandler {
    @NotNull
    @Override
    public Result handle(@NotNull Context context) {
        if (!(context.getDisplay() instanceof TransferDisplay))
            return Result.createNotApplicable();
        TransferDisplay recipe = (TransferDisplay) context.getDisplay();
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        if (containerScreen == null)
            return Result.createNotApplicable();
        AbstractContainerMenu container = context.getContainer();
        ContainerInfo<AbstractContainerMenu> containerInfo = (ContainerInfo<AbstractContainerMenu>) ContainerInfoHandler.getContainerInfo(recipe.getCategoryIdentifier(), container.getClass());
        if (containerInfo == null)
            return Result.createNotApplicable();
        if (recipe.getHeight() > containerInfo.getCraftingHeight(container) || recipe.getWidth() > containerInfo.getCraftingWidth(container))
            return Result.createFailed(I18n.get("error.rei.transfer.too_small", containerInfo.getCraftingWidth(container), containerInfo.getCraftingHeight(container)));
        List<? extends List<? extends EntryStack<?>>> input = recipe.getOrganisedInputEntries(containerInfo, container);
        IntList intList = hasItems(container, containerInfo, input);
        if (!intList.isEmpty())
            return Result.createFailed("error.rei.not.enough.materials", intList);
        if (!ClientHelper.getInstance().canUseMovePackets())
            return Result.createFailed("error.rei.not.on.server");
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        context.getMinecraft().setScreen(containerScreen);
        if (containerScreen instanceof RecipeUpdateListener)
            ((RecipeUpdateListener) containerScreen).getRecipeBookComponent().ghostRecipe.clear();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(recipe.getCategoryIdentifier());
        buf.writeBoolean(Screen.hasShiftDown());
        
        buf.writeInt(input.size());
        for (List<? extends EntryStack<?>> stacks : input) {
            buf.writeInt(stacks.size());
            for (EntryStack<?> stack : stacks) {
                if (stack.getValueType() == ItemStack.class)
                    buf.writeItem((ItemStack) stack.getValue());
                else
                    buf.writeItem(ItemStack.EMPTY);
            }
        }
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    public IntList hasItems(AbstractContainerMenu container, ContainerInfo<AbstractContainerMenu> containerInfo, List<? extends List<? extends EntryStack<?>>> inputs) {
        // Create a clone of player's inventory, and count
        RecipeFinder recipeFinder = new RecipeFinder();
        containerInfo.getRecipeFinderPopulator().populate(new ContainerContext<AbstractContainerMenu>() {
            @Override
            public AbstractContainerMenu getContainer() {
                return container;
            }
            
            @Override
            public Player getPlayerEntity() {
                return Minecraft.getInstance().player;
            }
            
            @Override
            public ContainerInfo<AbstractContainerMenu> getContainerInfo() {
                return containerInfo;
            }
        }).accept(recipeFinder);
        IntList intList = new IntArrayList();
        for (int i = 0; i < inputs.size(); i++) {
            List<? extends EntryStack<?>> possibleStacks = inputs.get(i);
            boolean done = possibleStacks.isEmpty();
            for (EntryStack<?> possibleStack : possibleStacks) {
                if (!done) {
                    if (possibleStack.getType() == VanillaEntryTypes.ITEM) {
                        int invRequiredCount = possibleStack.<ItemStack>cast().getValue().getCount();
                        int key = RecipeFinder.getItemId((ItemStack) possibleStack.getValue());
                        while (invRequiredCount > 0 && recipeFinder.contains(key)) {
                            invRequiredCount--;
                            recipeFinder.take(key, 1);
                        }
                        if (invRequiredCount <= 0) {
                            done = true;
                            break;
                        }
                    }
                }
            }
            if (!done) {
                intList.add(i);
            }
        }
        return intList;
    }
}
