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

package me.shedaniel.rei.plugin.autocrafting;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.InputIngredient;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.network.chat.Component;

import java.util.List;

public class InventoryCraftingTransferHandler implements TransferHandler {
    private final SimpleTransferHandler parent;
    
    public InventoryCraftingTransferHandler(SimpleTransferHandler parent) {
        this.parent = parent;
    }
    
    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        ApplicabilityResult applicable = parent.checkApplicable(context);
        if (!applicable.isApplicable()) return applicable;
        
        DefaultCraftingDisplay<?> display = (DefaultCraftingDisplay<?>) context.getDisplay();
        if (display != null && (display.getWidth() > 2 || display.getHeight() > 2)) {
            return ApplicabilityResult.createApplicableWithError(Component.translatable("error.rei.transfer.too_small", 2, 2));
        }
        
        return applicable;
    }
    
    @Override
    public Result handle(Context context) {
        List<InputIngredient<EntryStack<?>>> inputs = ((DefaultCraftingDisplay<?>) context.getDisplay()).getInputIngredients(2, 2);
        return parent.handleSimpleTransfer(context, parent.getMissingInputRenderer(),
                CollectionUtils.map(inputs, entry -> InputIngredient.withType(entry, VanillaEntryTypes.ITEM)),
                parent.getInputSlots(context), parent.getInventorySlots(context));
    }
}
