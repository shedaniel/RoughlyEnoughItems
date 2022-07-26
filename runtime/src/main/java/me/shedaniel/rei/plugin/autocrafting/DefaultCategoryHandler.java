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

package me.shedaniel.rei.plugin.autocrafting;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuTransferException;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCategoryHandler implements TransferHandler {
    @Override
    public Result handle(Context context) {
        Display display = context.getDisplay();
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        if (containerScreen == null) {
            return Result.createNotApplicable();
        }
        AbstractContainerMenu menu = context.getMenu();
        MenuInfoContext<AbstractContainerMenu, Player, Display> menuInfoContext = ofContext(menu, display);
        MenuInfo<AbstractContainerMenu, Display> menuInfo = MenuInfoRegistry.getInstance().getClient(display, menuInfoContext, menu);
        if (menuInfo == null) {
            return Result.createNotApplicable();
        }
        try {
            menuInfo.validate(menuInfoContext);
        } catch (MenuTransferException e) {
            if (e.isApplicable()) {
                return Result.createFailed(e.getError());
            } else {
                return Result.createNotApplicable();
            }
        }
        List<InputIngredient<ItemStack>> input = menuInfo.getInputsIndexed(menuInfoContext, false);
        List<InputIngredient<ItemStack>> missing = hasItemsIndexed(menuInfoContext, menu, menuInfo, display, input);
        if (!missing.isEmpty()) {
            IntList missingIndices = new IntArrayList(missing.size());
            for (InputIngredient<ItemStack> ingredient : missing) {
                missingIndices.add(ingredient.getIndex());
            }
            IntSet missingIndicesSet = new IntLinkedOpenHashSet(missingIndices);
            List<List<ItemStack>> oldInputs = CollectionUtils.map(input, InputIngredient::get);
            return Result.createFailed(Component.translatable("error.rei.not.enough.materials"))
                    .renderer((matrices, mouseX, mouseY, delta, widgets, bounds, d) -> {
                        menuInfo.renderMissingInput(menuInfoContext, oldInputs, missingIndices, matrices, mouseX, mouseY, delta, widgets, bounds);
                        menuInfo.renderMissingInput(menuInfoContext, input, missing, missingIndicesSet, matrices, mouseX, mouseY, delta, widgets, bounds);
                    })
                    .tooltipMissing(CollectionUtils.map(missing, ingredient -> EntryIngredients.ofItemStacks(ingredient.get())));
        }
        if (!ClientHelper.getInstance().canUseMovePackets()) {
            return Result.createFailed(Component.translatable("error.rei.not.on.server"));
        }
        if (!context.isActuallyCrafting()) {
            return Result.createSuccessful();
        }
        
        context.getMinecraft().setScreen(containerScreen);
        if (containerScreen instanceof RecipeUpdateListener listener) {
            listener.getRecipeBookComponent().ghostRecipe.clear();
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(display.getCategoryIdentifier().getIdentifier());
        buf.writeBoolean(context.isStackedCrafting());
        
        buf.writeNbt(menuInfo.save(menuInfoContext, display));
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    private static MenuInfoContext<AbstractContainerMenu, Player, Display> ofContext(AbstractContainerMenu menu, Display display) {
        return new MenuInfoContext<AbstractContainerMenu, Player, Display>() {
            @Override
            public AbstractContainerMenu getMenu() {
                return menu;
            }
            
            @Override
            public Player getPlayerEntity() {
                return Minecraft.getInstance().player;
            }
            
            @Override
            public CategoryIdentifier<Display> getCategoryIdentifier() {
                return (CategoryIdentifier<Display>) display.getCategoryIdentifier();
            }
            
            @Override
            public Display getDisplay() {
                return display;
            }
        };
    }
    
    public IntList hasItems(MenuInfoContext<AbstractContainerMenu, Player, Display> menuInfoContext, AbstractContainerMenu menu, MenuInfo<AbstractContainerMenu, Display> info, Display display, List<List<ItemStack>> inputs) {
        List<InputIngredient<ItemStack>> missing = hasItemsIndexed(menuInfoContext, menu, info, display,
                CollectionUtils.mapIndexed(inputs, InputIngredient::of));
        IntList ids = new IntArrayList(missing.size());
        for (InputIngredient<ItemStack> ingredient : missing) {
            ids.add(ingredient.getIndex());
        }
        return ids;
    }
    
    public List<InputIngredient<ItemStack>> hasItemsIndexed(MenuInfoContext<AbstractContainerMenu, Player, Display> menuInfoContext, AbstractContainerMenu menu, MenuInfo<AbstractContainerMenu, Display> info, Display display, List<InputIngredient<ItemStack>> inputs) {
        // Create a clone of player's inventory, and count
        RecipeFinder recipeFinder = new RecipeFinder();
        info.getRecipeFinderPopulator().populate(menuInfoContext, recipeFinder);
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
