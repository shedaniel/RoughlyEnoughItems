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

package me.shedaniel.rei.impl.client.gui.forge;

import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScreenOverlayImplForge extends ScreenOverlayImpl {
    @Override
    public void renderTooltipInner(Screen screen, GuiGraphics graphics, Tooltip tooltip, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        EntryStack<?> stack = tooltip.getContextStack();
        ItemStack itemStack = stack.getType() == VanillaEntryTypes.ITEM ? stack.castValue() : ItemStack.EMPTY;
        List<Component> texts = CollectionUtils.filterAndMap(tooltip.entries(), Tooltip.Entry::isText, Tooltip.Entry::getAsText);
        List<ClientTooltipComponent> components = ForgeHooksClient.gatherTooltipComponents(itemStack, texts, Optional.empty(), mouseX, screen.width, screen.height, screen.getMinecraft().font);
        components = new ArrayList<>(components);
        for (Tooltip.Entry entry : tooltip.entries()) {
            if (!entry.isText()) {
                TooltipComponent component = entry.getAsTooltipComponent();
                
                if (component instanceof ClientTooltipComponent client) {
                    components.add(client);
                    continue;
                }
                
                components.add(1, ClientTooltipComponent.create(component));
            }
        }
        Font font = Minecraft.getInstance().font;
        if (!itemStack.isEmpty()) {
            font = ForgeHooksClient.getTooltipFont(itemStack, font);
        }
        graphics.tooltipStack = itemStack;
        graphics.renderTooltipInternal(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
        graphics.tooltipStack = ItemStack.EMPTY;
        graphics.pose().popPose();
    }
}
