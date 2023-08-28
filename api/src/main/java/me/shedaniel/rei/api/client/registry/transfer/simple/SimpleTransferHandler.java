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

package me.shedaniel.rei.api.client.registry.transfer.simple;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.ClientInternals;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApiStatus.Experimental
public interface SimpleTransferHandler extends TransferHandler {
    static <C extends AbstractContainerMenu, D extends Display> SimpleTransferHandler create(Class<? extends C> containerClass,
                                                                                             CategoryIdentifier<D> categoryIdentifier,
                                                                                             IntRange inputSlots) {
        return new SimpleTransferHandler() {
            @Override
            public ApplicabilityResult checkApplicable(Context context) {
                if (!containerClass.isInstance(context.getMenu())
                        || !categoryIdentifier.equals(context.getDisplay().getCategoryIdentifier())
                        || context.getContainerScreen() == null) {
                    return ApplicabilityResult.createNotApplicable();
                } else {
                    return ApplicabilityResult.createApplicable();
                }
            }
            
            @Override
            public Iterable<SlotAccessor> getInputSlots(Context context) {
                return IntStream.range(inputSlots.min, inputSlots.maxExclusive)
                        .mapToObj(id -> SlotAccessor.fromSlot(context.getMenu().getSlot(id)))
                        .toList();
            }
            
            @Override
            public Iterable<SlotAccessor> getInventorySlots(Context context) {
                LocalPlayer player = context.getMinecraft().player;
                Inventory inventory = player.getInventory();
                return IntStream.range(0, inventory.items.size())
                        .mapToObj(index -> SlotAccessor.fromPlayerInventory(player, index))
                        .collect(Collectors.toList());
            }
        };
    }
    
    static <C extends Container, D extends Display> SimpleTransferHandler create(Class<? extends C> containerClass,
                                                                                 CategoryIdentifier<D> categoryIdentifier,
                                                                                 IntRange inputSlots,
                                                                                 IntRange inventorySlots) {
        return new SimpleTransferHandler() {
            @Override
            public ApplicabilityResult checkApplicable(Context context) {
                if (!containerClass.isInstance(context.getMenu())
                        || !categoryIdentifier.equals(context.getDisplay().getCategoryIdentifier())
                        || context.getContainerScreen() == null) {
                    return ApplicabilityResult.createNotApplicable();
                } else {
                    return ApplicabilityResult.createApplicable();
                }
            }
            
            @Override
            public Iterable<SlotAccessor> getInputSlots(Context context) {
                return IntStream.range(inputSlots.min, inputSlots.maxExclusive)
                        .mapToObj(id -> SlotAccessor.fromSlot(context.getMenu().getSlot(id)))
                        .toList();
            }
            
            @Override
            public Iterable<SlotAccessor> getInventorySlots(Context context) {
                return IntStream.range(inventorySlots.min, inventorySlots.maxExclusive)
                        .mapToObj(id -> SlotAccessor.fromSlot(context.getMenu().getSlot(id)))
                        .toList();
            }
        };
    }
    
    record IntRange(int min, int maxExclusive) {
    }
    
    /**
     * Returns an {@link Iterable} of {@link SlotAccessor}, of the slots that houses the inputs of the transfer.
     *
     * @param context the context of the transfer
     * @return an {@link Iterable} of the input slots.
     */
    Iterable<SlotAccessor> getInputSlots(Context context);
    
    /**
     * Returns an {@link Iterable} of {@link SlotAccessor}, of the slots that provides ingredients.
     *
     * @param context the context of the transfer
     * @return an {@link Iterable} of the inventory slots.
     */
    Iterable<SlotAccessor> getInventorySlots(Context context);
    
    /**
     * Returns the inputs of the {@link Display}. The nested lists are possible stacks for that specific slot.
     *
     * @param context the context of the transfer
     * @return the list of lists of items
     */
    default List<InputIngredient<ItemStack>> getInputsIndexed(Context context) {
        if (context.getDisplay() == null) return Collections.emptyList();
        return CollectionUtils.map(context.getDisplay().getInputIngredients(context.getMenu(), context.getMinecraft().player), (entry) ->
                InputIngredient.withType(entry, VanillaEntryTypes.ITEM));
    }
    
    /**
     * Renders the missing ingredients of the transfer.
     *
     * @param context        the context of the transfer
     * @param inputs         the list of inputs
     * @param missing        the list of missing stacks
     * @param missingIndices the indices of the missing stacks
     * @param matrices       the rendering transforming context
     * @param mouseX         the mouse x position
     * @param mouseY         the mouse y position
     * @param delta          the delta frame time
     * @param widgets        the widgets set-up by the category
     * @param bounds         the bounds of the display
     */
    default void renderMissingInput(Context context, List<InputIngredient<ItemStack>> inputs, List<InputIngredient<ItemStack>> missing,
                                    IntSet missingIndices, PoseStack matrices, int mouseX, int mouseY,
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
    
    default MissingInputRenderer getMissingInputRenderer() {
        return this::renderMissingInput;
    }
    
    @Override
    default Result handle(Context context) {
        return handleSimpleTransfer(context, getMissingInputRenderer(), getInputsIndexed(context), getInputSlots(context), getInventorySlots(context));
    }
    
    default Result handleSimpleTransfer(TransferHandler.Context context, MissingInputRenderer missingInputRenderer,
                                        List<InputIngredient<ItemStack>> inputs, Iterable<SlotAccessor> inputSlots,
                                        Iterable<SlotAccessor> inventorySlots) {
        return ClientInternals.getSimpleTransferHandler().handle(context, missingInputRenderer, inputs, inputSlots, inventorySlots);
    }
    
    interface MissingInputRenderer {
        void renderMissingInput(Context context, List<InputIngredient<ItemStack>> inputs, List<InputIngredient<ItemStack>> missing,
                                IntSet missingIndices, PoseStack matrices, int mouseX, int mouseY,
                                float delta, List<Widget> widgets, Rectangle bounds);
    }
}
