/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.impl.client.networking.modules;

import com.google.common.collect.Lists;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.transformers.SplitPacketTransformer;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.api.common.networking.NetworkModule;
import me.shedaniel.rei.api.common.networking.NetworkModuleKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;

public class NotEnoughItemsNetworkModule implements NetworkModule<List<List<ItemStack>>> {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "og_not_enough");
    
    @Override
    public NetworkModuleKey<List<List<ItemStack>>> getKey() {
        return NetworkModule.NOT_ENOUGH_ITEMS;
    }
    
    @Override
    public boolean canUse(Object target) {
        return NetworkManager.canPlayerReceive((ServerPlayer) target, NotEnoughItemsNetworkModule.ID);
    }
    
    @Override
    public void onInitialize() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> {
            NetworkManager.registerReceiver(NetworkManager.c2s(), ID, Collections.singletonList(new SplitPacketTransformer()), (buf, context) -> {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof CraftingScreen craftingScreen) {
                    RecipeBookComponent recipeBookGui = craftingScreen.getRecipeBookComponent();
                    GhostRecipe ghostSlots = recipeBookGui.ghostRecipe;
                    ghostSlots.clear();
                    
                    List<List<ItemStack>> input = Lists.newArrayList();
                    int mapSize = buf.readInt();
                    for (int i = 0; i < mapSize; i++) {
                        List<ItemStack> list = Lists.newArrayList();
                        int count = buf.readInt();
                        for (int j = 0; j < count; j++) {
                            list.add(buf.readItem());
                        }
                        input.add(list);
                    }
                    
                    ghostSlots.addIngredient(Ingredient.of(Items.STONE), 381203812, 12738291);
                    CraftingMenu container = craftingScreen.getMenu();
                    for (int i = 0; i < input.size(); i++) {
                        List<ItemStack> stacks = input.get(i);
                        if (!stacks.isEmpty()) {
                            Slot slot = container.getSlot(i + container.getResultSlotIndex() + 1);
                            ghostSlots.addIngredient(Ingredient.of(stacks.toArray(new ItemStack[0])), slot.x, slot.y);
                        }
                    }
                }
            });
        });
    }
    
    @Override
    public void send(Object target, List<List<ItemStack>> data) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(data.size());
        for (List<ItemStack> stacks : data) {
            buf.writeInt(stacks.size());
            for (ItemStack stack : stacks) {
                buf.writeItem(stack);
            }
        }
        NetworkManager.sendToServer(NotEnoughItemsNetworkModule.ID, buf);
    }
}
