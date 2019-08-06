/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.plugin.autocrafting;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.AutoCraftingHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.RecipeBookGuiHooks;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingCategory;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;
import me.shedaniel.rei.plugin.crafting.DefaultShapedDisplay;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.screen.ingame.CraftingTableScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.container.CraftingContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.PacketByteBuf;

import java.util.List;
import java.util.function.Supplier;

public class AutoCraftingTableHandler implements AutoCraftingHandler {
    @Override
    public boolean handle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        minecraft.openScreen(parentScreen);
        ((RecipeBookGuiHooks) ((RecipeBookProvider) parentScreen).getRecipeBookGui()).rei_getGhostSlots().reset();
        DefaultCraftingDisplay display = (DefaultCraftingDisplay) displaySupplier.get();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(RoughlyEnoughItemsNetwork.CRAFTING_TABLE_MOVE);
        buf.writeBoolean(Screen.hasShiftDown());
        CraftingContainer craftingContainer = (CraftingContainer) parentScreen.getContainer();
        
        List<List<ItemStack>> ogInput = display.getInput();
        List<List<ItemStack>> input = Lists.newArrayListWithCapacity(craftingContainer.getCraftingWidth() * craftingContainer.getCraftingHeight());
        for (int i = 0; i < craftingContainer.getCraftingWidth() * craftingContainer.getCraftingHeight(); i++) {
            input.add(Lists.newArrayList());
        }
        for (int i = 0; i < ogInput.size(); i++) {
            List<ItemStack> ogStacks = ogInput.get(i);
            if (display instanceof DefaultShapedDisplay) {
                if (!ogInput.get(i).isEmpty())
                    input.set(DefaultCraftingCategory.getSlotWithSize(display, i), ogInput.get(i));
            } else if (!ogInput.get(i).isEmpty())
                input.set(i, ogInput.get(i));
        }
        
        buf.writeInt(input.size());
        for (List<ItemStack> stacks : input) {
            buf.writeInt(stacks.size());
            for (ItemStack stack : stacks) {
                buf.writeItemStack(stack);
            }
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return true;
    }
    
    @Override
    public boolean canHandle(Supplier<RecipeDisplay> displaySupplier, MinecraftClient minecraft, Screen recipeViewingScreen, AbstractContainerScreen<?> parentScreen, ContainerScreenOverlay overlay) {
        if (parentScreen instanceof CraftingTableScreen && displaySupplier.get() instanceof DefaultCraftingDisplay && canUseMovePackets()) {
            return hasItems(displaySupplier.get().getInput());
        }
        return false;
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    public boolean canUseMovePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    public boolean hasItems(List<List<ItemStack>> inputs) {
        // Create a clone of player's inventory, and count
        DefaultedList<ItemStack> copyMain = DefaultedList.create();
        for (ItemStack stack : MinecraftClient.getInstance().player.inventory.main) {
            copyMain.add(stack.copy());
        }
        for (List<ItemStack> possibleStacks : inputs) {
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
                    }
                }
            }
            if (!done)
                return false;
        }
        return true;
    }
}
