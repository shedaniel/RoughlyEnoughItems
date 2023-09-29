/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.transfer;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessorRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.impl.ClientInternals;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum SimpleTransferHandlerImpl implements ClientInternals.SimpleTransferHandler {
    INSTANCE;
    
    @Override
    public TransferHandler.Result handle(TransferHandler.Context context, SimpleTransferHandler.MissingInputRenderer missingInputRenderer, List<InputIngredient<ItemStack>> inputs, Iterable<SlotAccessor> inputSlots, Iterable<SlotAccessor> inventorySlots) {
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        List<InputIngredient<ItemStack>> missing = SimpleTransferHandlerImpl.hasItemsIndexed(context, inventorySlots, inputs);
        
        if (!missing.isEmpty()) {
            IntSet missingIndices = new IntLinkedOpenHashSet(missing.size());
            for (InputIngredient<ItemStack> ingredient : missing) {
                missingIndices.add(ingredient.getDisplayIndex());
            }
            return TransferHandler.Result.createFailed(Component.translatable("error.rei.not.enough.materials"))
                    .renderer((matrices, mouseX, mouseY, delta, widgets, bounds, d) -> {
                        missingInputRenderer.renderMissingInput(context, inputs, missing, missingIndices, matrices, mouseX, mouseY, delta, widgets, bounds);
                    })
                    .tooltipMissing(CollectionUtils.map(missing, ingredient -> EntryIngredients.ofItemStacks(ingredient.get())));
        }
        
        if (!ClientHelper.getInstance().canUseMovePackets()) {
            return TransferHandler.Result.createFailed(Component.translatable("error.rei.not.on.server"));
        }
        
        if (!context.isActuallyCrafting()) {
            return TransferHandler.Result.createSuccessful();
        }
        
        context.getMinecraft().setScreen(containerScreen);
        if (containerScreen instanceof RecipeUpdateListener listener) {
            listener.getRecipeBookComponent().ghostRecipe.clear();
        }
        
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(context.getDisplay().getCategoryIdentifier().getIdentifier());
        buf.writeBoolean(context.isStackedCrafting());
        
        buf.writeNbt(save(context, inputs, inputSlots, inventorySlots));
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_NEW_PACKET, buf);
        return TransferHandler.Result.createSuccessful();
    }
    
    private CompoundTag save(TransferHandler.Context context, List<InputIngredient<ItemStack>> inputs, Iterable<SlotAccessor> inputSlots, Iterable<SlotAccessor> inventorySlots) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", 1);
        tag.put("Inputs", saveInputs(inputs));
        tag.put("InventorySlots", saveSlots(context,inventorySlots));
        tag.put("InputSlots", saveSlots(context, inputSlots));
        return tag;
    }
    
    private Tag saveSlots(TransferHandler.Context context, Iterable<SlotAccessor> slots) {
        ListTag tag = new ListTag();
        
        for (SlotAccessor slot : slots) {
            tag.add(SlotAccessorRegistry.getInstance().save(context.getMenu(), context.getMinecraft().player, slot));
        }
        
        return tag;
    }
    
    private Tag saveInputs(List<InputIngredient<ItemStack>> inputs) {
        ListTag tag = new ListTag();
        
        for (InputIngredient<ItemStack> input : inputs) {
            CompoundTag innerTag = new CompoundTag();
            innerTag.put("Ingredient", EntryIngredients.ofItemStacks(input.get()).saveIngredient());
            innerTag.putInt("Index", input.getIndex());
            tag.add(innerTag);
        }
        
        return tag;
    }
    
    public static List<InputIngredient<ItemStack>> hasItemsIndexed(TransferHandler.Context context, Iterable<SlotAccessor> inventorySlots, List<InputIngredient<ItemStack>> inputs) {
        // Create a clone of player's inventory, and count
        RecipeFinder recipeFinder = new RecipeFinder();
        for (SlotAccessor slot : inventorySlots) {
            recipeFinder.addNormalItem(slot.getItemStack());
        }
        List<InputIngredient<ItemStack>> missing = new ArrayList<>();
        for (InputIngredient<ItemStack> possibleStacks : inputs) {
            boolean done = possibleStacks.get().isEmpty();
            for (ItemStack possibleStack : possibleStacks.get()) {
                if (!done) {
                    int invRequiredCount = possibleStack.getCount();
                    int key = RecipeFinder.getItemId(possibleStack);
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
            if (!done) {
                missing.add(possibleStacks);
            }
        }
        return missing;
    }
}
