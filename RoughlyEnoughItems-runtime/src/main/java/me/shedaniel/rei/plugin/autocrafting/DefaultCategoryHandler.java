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
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerContext;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import me.shedaniel.rei.server.RecipeFinder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
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
public class DefaultCategoryHandler implements AutoTransferHandler {
    
    @NotNull
    @Override
    public Result handle(@NotNull Context context) {
        if (!(context.getRecipe() instanceof TransferRecipeDisplay))
            return Result.createNotApplicable();
        TransferRecipeDisplay recipe = (TransferRecipeDisplay) context.getRecipe();
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        AbstractContainerMenu container = context.getContainer();
        ContainerInfo<AbstractContainerMenu> containerInfo = (ContainerInfo<AbstractContainerMenu>) ContainerInfoHandler.getContainerInfo(recipe.getRecipeCategory(), container.getClass());
        if (containerInfo == null)
            return Result.createNotApplicable();
        if (recipe.getHeight() > containerInfo.getCraftingHeight(container) || recipe.getWidth() > containerInfo.getCraftingWidth(container))
            return Result.createFailed(I18n.get("error.rei.transfer.too_small", containerInfo.getCraftingWidth(container), containerInfo.getCraftingHeight(container)));
        List<List<EntryStack>> input = recipe.getOrganisedInputEntries(containerInfo, container);
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
        buf.writeResourceLocation(recipe.getRecipeCategory());
        buf.writeBoolean(Screen.hasShiftDown());
        
        buf.writeInt(input.size());
        for (List<EntryStack> stacks : input) {
            buf.writeInt(stacks.size());
            for (EntryStack stack : stacks) {
                if (stack.getItemStack() != null)
                    buf.writeItem(stack.getItemStack());
                else
                    buf.writeItem(ItemStack.EMPTY);
            }
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    public IntList hasItems(AbstractContainerMenu container, ContainerInfo<AbstractContainerMenu> containerInfo, List<List<EntryStack>> inputs) {
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
            List<EntryStack> possibleStacks = inputs.get(i);
            boolean done = possibleStacks.isEmpty();
            for (EntryStack possibleStack : possibleStacks) {
                if (!done) {
                    if (possibleStack.getType() == EntryStack.Type.ITEM) {
                        int invRequiredCount = possibleStack.getAmount();
                        int key = RecipeFinder.getItemId(possibleStack.getItemStack());
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
