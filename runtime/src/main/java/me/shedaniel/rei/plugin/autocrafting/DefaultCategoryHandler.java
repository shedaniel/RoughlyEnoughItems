/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuTransferException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultCategoryHandler implements TransferHandler {
    @Override
    public Result handle(Context context) {
        if (!(context.getDisplay() instanceof SimpleGridMenuDisplay))
            return Result.createNotApplicable();
        SimpleGridMenuDisplay display = (SimpleGridMenuDisplay) context.getDisplay();
        AbstractContainerScreen<?> containerScreen = context.getContainerScreen();
        if (containerScreen == null) {
            return Result.createNotApplicable();
        }
        AbstractContainerMenu menu = context.getMenu();
        MenuInfo<AbstractContainerMenu, Display> menuInfo = MenuInfoRegistry.getInstance().get((CategoryIdentifier<Display>) display.getCategoryIdentifier(), (Class<AbstractContainerMenu>) menu.getClass());
        if (menuInfo == null) {
            return Result.createNotApplicable();
        }
        MenuInfoContext<AbstractContainerMenu, Player, Display> menuInfoContext = ofContext(menu, menuInfo, display);
        try {
            menuInfo.validate(menuInfoContext);
        } catch (MenuTransferException e) {
            return Result.createFailed(e.getError());
        }
        List<List<ItemStack>> input = menuInfo.getInputs(menuInfoContext);
        IntList intList = hasItems(menu, menuInfo, display, input);
        if (!intList.isEmpty()) {
            return Result.createFailed(new TranslatableComponent("error.rei.not.enough.materials"), intList);
        }
        if (!ClientHelper.getInstance().canUseMovePackets()) {
            return Result.createFailed(new TranslatableComponent("error.rei.not.on.server"));
        }
        if (!context.isActuallyCrafting()) {
            return Result.createSuccessful();
        }
        
        context.getMinecraft().setScreen(containerScreen);
        if (containerScreen instanceof RecipeUpdateListener) {
            ((RecipeUpdateListener) containerScreen).getRecipeBookComponent().ghostRecipe.clear();
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(display.getCategoryIdentifier().getIdentifier());
        buf.writeBoolean(Screen.hasShiftDown());
        
        buf.writeNbt(menuInfo.save(menuInfoContext, display));
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET, buf);
        return Result.createSuccessful();
    }
    
    @Override
    public double getPriority() {
        return -10;
    }
    
    private static MenuInfoContext<AbstractContainerMenu, Player, Display> ofContext(AbstractContainerMenu menu, MenuInfo<AbstractContainerMenu, Display> info, Display display) {
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
            public MenuInfo<AbstractContainerMenu, Display> getContainerInfo() {
                return info;
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
    
    public IntList hasItems(AbstractContainerMenu menu, MenuInfo<AbstractContainerMenu, Display> info, Display display, List<List<ItemStack>> inputs) {
        // Create a clone of player's inventory, and count
        RecipeFinder recipeFinder = new RecipeFinder();
        info.getRecipeFinderPopulator().populate(ofContext(menu, info, display), recipeFinder);
        IntList intList = new IntArrayList();
        for (int i = 0; i < inputs.size(); i++) {
            List<ItemStack> possibleStacks = inputs.get(i);
            boolean done = possibleStacks.isEmpty();
            for (ItemStack possibleStack : possibleStacks) {
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
                intList.add(i);
            }
        }
        return intList;
    }
}
