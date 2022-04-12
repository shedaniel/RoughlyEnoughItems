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

package me.shedaniel.rei.impl.client.gui.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.List;

public class ScreenOverlayImplImpl {
    public static void renderTooltipInner(Screen screen, PoseStack matrices, Tooltip tooltip, int mouseX, int mouseY) {
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
//        modelViewStack.translate(0, 0, 500);
        RenderSystem.applyModelViewMatrix();
        matrices.pushPose();
        EntryStack<?> stack = tooltip.getContextStack();
        ItemStack itemStack = stack.getType() == VanillaEntryTypes.ITEM ? stack.castValue() : ItemStack.EMPTY;
        List<Component> texts = CollectionUtils.filterAndMap(tooltip.entries(), Tooltip.Entry::isText, Tooltip.Entry::getAsText);
        List<ClientTooltipComponent> components = ForgeHooksClient.gatherTooltipComponents(itemStack, texts, mouseX, screen.width, screen.height, null, screen.getMinecraft().font);
        for (Tooltip.Entry entry : tooltip.entries()) {
            if (!entry.isText()) {
                components.add(1, entry.getAsComponent());
            }
        }
        screen.tooltipStack = itemStack;
        screen.renderTooltipInternal(matrices, components, mouseX, mouseY);
        screen.tooltipStack = ItemStack.EMPTY;
        matrices.popPose();
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
