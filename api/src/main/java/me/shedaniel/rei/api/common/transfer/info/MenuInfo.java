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

package me.shedaniel.rei.api.common.transfer.info;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.RecipeFinder;
import me.shedaniel.rei.api.common.transfer.RecipeFinderPopulator;
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler;
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;

/**
 * Provider of information for {@link AbstractContainerMenu}, for the default REI {@link me.shedaniel.rei.api.client.registry.transfer.TransferHandler}.
 * Allows custom slots by the interface {@link SlotAccessor}, populates and syncs the {@link Display} to the server.
 *
 * @param <T> the type of the menu
 * @param <D> the type of display
 * @see MenuInfoRegistry
 * @see me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo
 * @see me.shedaniel.rei.api.common.transfer.info.simple.SimpleGridMenuInfo
 */
public interface MenuInfo<T extends AbstractContainerMenu, D extends Display> {
    /**
     * Returns a {@link RecipeFinderPopulator}, used to populate a {@link RecipeFinder} with
     * available ingredients.
     *
     * @return a {@link RecipeFinderPopulator}
     */
    RecipeFinderPopulator<T, D> getRecipeFinderPopulator();
    
    /**
     * Returns an {@link InputCleanHandler} that cleans the input grid.
     *
     * @return an {@link InputCleanHandler} that cleans the input grid
     * @see SimplePlayerInventoryMenuInfo#getInputCleanHandler()
     */
    InputCleanHandler<T, D> getInputCleanHandler();
    
    /**
     * Returns an {@link Iterable} of {@link SlotAccessor}, of the slots that houses the inputs of the transfer.
     *
     * @param context the context of the transfer
     * @return an {@link Iterable} of the input slots.
     */
    Iterable<SlotAccessor> getInputSlots(MenuInfoContext<T, ?, D> context);
    
    /**
     * Returns an {@link Iterable} of {@link SlotAccessor}, of the slots that provides ingredients.
     *
     * @param context the context of the transfer
     * @return an {@link Iterable} of the inventory slots.
     */
    Iterable<SlotAccessor> getInventorySlots(MenuInfoContext<T, ?, D> context);
    
    /**
     * Marks the transfer as dirty, as in something has changed.
     * This denotes that an update should be sent to the {@link ServerPlayer}.
     *
     * @param context the context of the transfer
     */
    default void markDirty(MenuInfoContext<T, ? extends ServerPlayer, D> context) {
        context.getPlayerEntity().getInventory().setChanged();
        context.getMenu().sendAllDataToRemote();
    }
    
    /**
     * Validates the transfer, throws {@link MenuTransferException} if something is wrong.
     *
     * @param context the context of the transfer
     * @throws MenuTransferException the exception to throw if something is wrong,
     *                               this exception should be caught by the invoker
     */
    @Environment(EnvType.CLIENT)
    default void validate(MenuInfoContext<T, ?, D> context) throws MenuTransferException {
    }
    
    /**
     * Returns the inputs of the {@link Display}. The nested lists are possible stacks for that specific slot.
     *
     * @param context the context of the transfer
     * @param fill    whether this call is for a fill or not, if it is for a fill, the returned list should be aligned for the menu,
     *                otherwise it should be aligned for the display category
     * @return the list of lists of items
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default List<List<ItemStack>> getInputs(MenuInfoContext<T, ?, D> context, boolean fill) {
        if (context.getDisplay() == null) return Collections.emptyList();
        return CollectionUtils.map(context.getDisplay().getInputIngredients(context, this, fill), inputEntry ->
                CollectionUtils.<EntryStack<?>, ItemStack>filterAndMap(inputEntry.get(),
                        stack -> stack.getType() == VanillaEntryTypes.ITEM, EntryStack::castValue));
    }
    
    /**
     * Returns the inputs of the {@link Display}. The nested lists are possible stacks for that specific slot.
     *
     * @param context the context of the transfer
     * @param fill    whether this call is for a fill or not, if it is for a fill, the returned list should be aligned for the menu,
     *                otherwise it should be aligned for the display category
     * @return the list of lists of items
     */
    default List<InputIngredient<ItemStack>> getInputsIndexed(MenuInfoContext<T, ?, D> context, boolean fill) {
        if (context.getDisplay() == null) return Collections.emptyList();
        return CollectionUtils.map(context.getDisplay().getInputIngredients(context, this, fill), entry ->
                InputIngredient.withType(entry, VanillaEntryTypes.ITEM));
    }
    
    /**
     * Serializes the {@link Display} as {@link CompoundTag}, sent to the server for further info for the transfer.
     *
     * @param context the context of the transfer
     * @param display the display to serialize
     * @return the {@link CompoundTag} serialized
     */
    default CompoundTag save(MenuSerializationContext<T, ?, D> context, D display) {
        return DisplaySerializerRegistry.getInstance().save(display, new CompoundTag());
    }
    
    /**
     * Returns the {@link Display} for this menu info.
     *
     * @return the {@link Display}
     */
    D getDisplay();
    
    /**
     * Renders the missing ingredients of the transfer.
     * The indices of the missing stacks are provided, this aligns with the list returned by {@link #getInputs(MenuInfoContext, boolean)}.
     *
     * @param context        the context of the transfer
     * @param inputs         the list of inputs
     * @param missing        the list of missing stacks
     * @param missingIndices the indices of the missing stacks
     * @param matrices       the rendering transforming matrices
     * @param mouseX         the mouse x position
     * @param mouseY         the mouse y position
     * @param delta          the delta frame time
     * @param widgets        the widgets set-up by the category
     * @param bounds         the bounds of the display
     */
    @Environment(EnvType.CLIENT)
    default void renderMissingInput(MenuInfoContext<T, ?, D> context, List<InputIngredient<ItemStack>> inputs, List<InputIngredient<ItemStack>> missing, IntSet missingIndices, PoseStack matrices, int mouseX, int mouseY,
            float delta, List<Widget> widgets, Rectangle bounds) {
        int i = 0;
        for (Widget widget : widgets) {
            if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
                if (missingIndices.contains(i++)) {
                    matrices.pushPose();
                    matrices.translate(0, 0, 50);
                    Rectangle innerBounds = ((Slot) widget).getInnerBounds();
                    GuiComponent.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x40ff0000);
                    matrices.popPose();
                }
            }
        }
    }
    
    /**
     * Renders the missing ingredients of the transfer.
     * The indices of the missing stacks are provided, this aligns with the list returned by {@link #getInputs(MenuInfoContext, boolean)}.
     *
     * @param context        the context of the transfer
     * @param inputs         the list of inputs
     * @param missingIndices the indices of the missing stacks
     * @param matrices       the rendering transforming matrices
     * @param mouseX         the mouse x position
     * @param mouseY         the mouse y position
     * @param delta          the delta frame time
     * @param widgets        the widgets set-up by the category
     * @param bounds         the bounds of the display
     */
    @Environment(EnvType.CLIENT)
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    default void renderMissingInput(MenuInfoContext<T, ?, D> context, List<List<ItemStack>> inputs, IntList missingIndices, PoseStack matrices, int mouseX, int mouseY,
            float delta, List<Widget> widgets, Rectangle bounds) {
    }
}
