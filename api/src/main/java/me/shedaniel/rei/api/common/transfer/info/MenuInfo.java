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

package me.shedaniel.rei.api.common.transfer.info;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.RecipeFinderPopulator;
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler;
import me.shedaniel.rei.api.common.transfer.info.stack.StackAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Provider of information for {@link AbstractContainerMenu}, for the default REI {@link me.shedaniel.rei.api.client.registry.transfer.TransferHandler}.
 * Allows custom slots by the interface {@link StackAccessor}, populates and syncs the {@link Display} to the server.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 * @see MenuInfoRegistry
 * @see me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo
 * @see me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo
 */
public interface MenuInfo<T extends AbstractContainerMenu, D extends Display> {
    Class<? extends T> getContainerClass();
    
    default RecipeFinderPopulator<T, D> getRecipeFinderPopulator() {
        return (context, recipeFinder) -> {
            for (StackAccessor inventoryStack : getInventoryStacks(context)) {
                recipeFinder.addNormalItem(inventoryStack.getItemStack());
            }
            populateRecipeFinder(context.getMenu(), recipeFinder);
        };
    }
    
    InputCleanHandler<T, D> getInputCleanHandler();
    
    Iterable<StackAccessor> getInputStacks(MenuInfoContext<T, ?, D> context);
    
    Iterable<StackAccessor> getInventoryStacks(MenuInfoContext<T, ?, D> context);
    
    default void markDirty(MenuInfoContext<T, ? extends ServerPlayer, D> context) {
        context.getPlayerEntity().inventory.setChanged();
        context.getMenu().broadcastChanges();
        
        AbstractContainerMenu containerMenu = context.getPlayerEntity().containerMenu;
        context.getPlayerEntity().refreshContainer(containerMenu, containerMenu.getItems());
    }
    
    default void populateRecipeFinder(T container, RecipeFinder finder) {}
    
    default void validate(MenuInfoContext<T, ?, D> context) throws MenuTransferException {}
    
    default List<List<ItemStack>> getDisplayInputs(MenuInfoContext<T, ?, D> context) {
        return CollectionUtils.map(context.getDisplay().getInputEntries(), inputEntry -> CollectionUtils.filterAndMap(inputEntry, stack -> stack.getType() == VanillaEntryTypes.ITEM,
                stack -> stack.<ItemStack>cast().getValue()));
    }
    
    default CompoundTag save(MenuSerializationContext<T, ?, D> context, D display) {
        return DisplaySerializerRegistry.getInstance().save(display.getCategoryIdentifier(), display, new CompoundTag());
    }
    
    default D read(MenuSerializationContext<T, ?, D> context, CompoundTag tag) {
        return DisplaySerializerRegistry.getInstance().read(context.getCategoryIdentifier(), tag);
    }
}
