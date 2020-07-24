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
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCategoryHandler implements AutoTransferHandler {
    
    public static boolean canUseMovePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    @NotNull
    @Override
    public Result handle(@NotNull Context context) {
        if (!(context.getRecipe() instanceof TransferRecipeDisplay))
            return Result.createNotApplicable();
        TransferRecipeDisplay recipe = (TransferRecipeDisplay) context.getRecipe();
        ContainerScreen<?> containerScreen = context.getContainerScreen();
        Container container = context.getContainer();
        ContainerInfo<Container> containerInfo = (ContainerInfo<Container>) ContainerInfoHandler.getContainerInfo(recipe.getRecipeCategory(), container.getClass());
        if (containerInfo == null)
            return Result.createNotApplicable();
        if (recipe.getHeight() > containerInfo.getCraftingHeight(container) || recipe.getWidth() > containerInfo.getCraftingWidth(container))
            return Result.createFailed(I18n.translate("error.rei.transfer.too_small", containerInfo.getCraftingWidth(container), containerInfo.getCraftingHeight(container)));
        List<List<EntryStack>> input = recipe.getOrganisedInputEntries(containerInfo, container);
        IntList intList = hasItems(input);
        if (!intList.isEmpty())
            return Result.createFailed("error.rei.not.enough.materials", intList);
        if (!canUseMovePackets())
            return Result.createFailed("error.rei.not.on.server");
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        context.getMinecraft().openScreen(containerScreen);
        if (containerScreen instanceof RecipeBookProvider)
            ((RecipeBookProvider) containerScreen).getRecipeBookWidget().ghostSlots.reset();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(recipe.getRecipeCategory());
        buf.writeBoolean(Screen.hasShiftDown());
        
        buf.writeInt(input.size());
        for (List<EntryStack> stacks : input) {
            buf.writeInt(stacks.size());
            for (EntryStack stack : stacks) {
                if (stack.getItemStack() != null)
                    buf.writeItemStack(stack.getItemStack());
                else
                    buf.writeItemStack(ItemStack.EMPTY);
            }
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    public IntList hasItems(List<List<EntryStack>> inputs) {
        // Create a clone of player's inventory, and count
        DefaultedList<ItemStack> copyMain = DefaultedList.of();
        for (ItemStack stack : MinecraftClient.getInstance().player.inventory.main) {
            copyMain.add(stack.copy());
        }
        IntList intList = new IntArrayList();
        for (int i = 0; i < inputs.size(); i++) {
            List<EntryStack> possibleStacks = inputs.get(i);
            boolean done = possibleStacks.isEmpty();
            for (EntryStack possibleStack : possibleStacks) {
                if (!done) {
                    int invRequiredCount = possibleStack.getAmount();
                    for (ItemStack stack : copyMain) {
                        EntryStack entryStack = EntryStack.create(stack);
                        if (entryStack.equals(possibleStack)) {
                            while (invRequiredCount > 0 && !stack.isEmpty()) {
                                invRequiredCount--;
                                stack.decrement(1);
                            }
                        }
                    }
                    if (invRequiredCount <= 0) {
                        done = true;
                        break;
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
