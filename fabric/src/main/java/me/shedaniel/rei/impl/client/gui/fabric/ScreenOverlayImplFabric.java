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

package me.shedaniel.rei.impl.client.gui.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenOverlayImplFabric extends ScreenOverlayImpl {
    @Override
    public void renderTooltipInner(Screen screen, PoseStack matrices, Tooltip tooltip, int mouseX, int mouseY) {
        List<ClientTooltipComponent> lines = tooltip.entries().stream()
                .flatMap(component -> {
                    if (component.isText()) {
                        List<FormattedText> texts = Minecraft.getInstance().font.getSplitter().splitLines(component.getAsText(), 100000, Style.EMPTY);
                        Stream<FormattedCharSequence> sequenceStream = texts.isEmpty() ? Stream.of(component.getAsText().getVisualOrderText())
                                : texts.stream().map(Language.getInstance()::getVisualOrder);
                        return sequenceStream.map(ClientTooltipComponent::create);
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
        for (Tooltip.Entry entry : tooltip.entries()) {
            if (entry.isTooltipComponent()) {
                TooltipComponent component = entry.getAsTooltipComponent();
                
                if (component instanceof ClientTooltipComponent client) {
                    lines.add(client);
                    continue;
                }
                
                try {
                    ClientInternals.getClientTooltipComponent(lines, component);
                } catch (Throwable exception) {
                    throw new IllegalArgumentException("Failed to add tooltip component! " + component + ", Class: " + (component == null ? null : component.getClass().getCanonicalName()), exception);
                }
            }
        }
        renderTooltipInner(matrices, lines, tooltip.getX(), tooltip.getY());
    }
    
    public static void renderTooltipInner(PoseStack matrices, List<ClientTooltipComponent> lines, int mouseX, int mouseY) {
        if (lines.isEmpty()) {
            return;
        }
        matrices.pushPose();
        Minecraft.getInstance().screen.renderTooltipInternal(matrices, lines, mouseX, mouseY);
        matrices.popPose();
    }
}
