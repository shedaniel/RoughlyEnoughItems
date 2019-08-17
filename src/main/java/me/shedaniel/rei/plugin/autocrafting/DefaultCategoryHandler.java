/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.server.ContainerInfo;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;

import java.util.List;

public class DefaultCategoryHandler implements AutoTransferHandler {
    
    @Override
    public Result handle(Context context) {
        if (!(context.getRecipe() instanceof TransferRecipeDisplay))
            return Result.createNotApplicable();
        TransferRecipeDisplay recipe = (TransferRecipeDisplay) context.getRecipe();
        if (!ContainerInfoHandler.isCategoryHandled(recipe.getRecipeCategory()))
            return Result.createNotApplicable();
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        Container container = containerScreen.getContainer();
        ContainerInfo containerInfo = ContainerInfoHandler.getContainerInfo(recipe.getRecipeCategory(), container.getClass());
        if (containerInfo == null)
            return Result.createNotApplicable();
        if (recipe.getHeight() > containerInfo.getCraftingHeight(container) || recipe.getWidth() > containerInfo.getCraftingWidth(container))
            return Result.createFailed(I18n.translate("error.rei.transfer.too_small", containerInfo.getCraftingWidth(container), containerInfo.getCraftingHeight(container)));
        if (!canUseMovePackets())
            return Result.createFailed("error.rei.not.on.server");
        List<List<ItemStack>> input = recipe.getOrganisedInput(containerInfo, container);
        IntList intList = hasItems(input);
        if (!intList.isEmpty())
            return Result.createFailed("error.rei.not.enough.materials", intList);
        if (!context.isActuallyCrafting())
            return Result.createSuccessful();
        
        context.getMinecraft().openScreen(containerScreen);
        if (containerScreen instanceof RecipeBookProvider)
            ((RecipeBookGuiHooks) ((RecipeBookProvider) containerScreen).getRecipeBookGui()).rei_getGhostSlots().reset();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(recipe.getRecipeCategory());
        buf.writeBoolean(Screen.hasShiftDown());
        
        buf.writeInt(input.size());
        for (List<ItemStack> stacks : input) {
            buf.writeInt(stacks.size());
            for (ItemStack stack : stacks) {
                buf.writeItemStack(stack);
            }
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    public boolean canUseMovePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    public IntList hasItems(List<List<ItemStack>> inputs) {
        // Create a clone of player's inventory, and count
        DefaultedList<ItemStack> copyMain = DefaultedList.create();
        for (ItemStack stack : MinecraftClient.getInstance().player.inventory.main) {
            copyMain.add(stack.copy());
        }
        IntList intList = new IntArrayList();
        for (int i = 0; i < inputs.size(); i++) {
            List<ItemStack> possibleStacks = inputs.get(i);
            boolean done = possibleStacks.isEmpty();
            for (ItemStack possibleStack : possibleStacks) {
                if (!done) {
                    int invRequiredCount = possibleStack.getCount();
                    for (ItemStack stack : copyMain) {
                        if (ItemStack.areItemsEqualIgnoreDamage(possibleStack, stack)) {
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
