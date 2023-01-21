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

package me.shedaniel.rei.impl.client.entry.type.types;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.entry.type.BuiltinClientEntryTypes;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryDefinition;
import me.shedaniel.rei.impl.common.entry.type.types.BuiltinEntryDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RenderingEntryDefinition {
    public static final EntryDefinition<Renderer> RENDERING = new BuiltinEntryDefinition<Renderer>(Renderer.class, BuiltinClientEntryTypes.RENDERING, false, RenderingEntryDefinition::throwRendering, () -> () -> DeferredRenderer.INSTANCE) {
        @Override
        public Component asFormattedText(EntryStack<Renderer> entry, Renderer value) {
            Tooltip tooltip = value.getTooltip(TooltipContext.ofMouse());
            if (tooltip != null) {
                for (Tooltip.Entry e : tooltip.entries()) {
                    if (e.isText()) {
                        return e.getAsText();
                    }
                }
            }
            return super.asFormattedText(entry, value);
        }
    
        @Override
        public Component asFormattedText(EntryStack<Renderer> entry, Renderer value, TooltipContext context) {
            Tooltip tooltip = value.getTooltip(context);
            if (tooltip != null) {
                for (Tooltip.Entry e : tooltip.entries()) {
                    if (e.isText()) {
                        return e.getAsText();
                    }
                }
            }
            return super.asFormattedText(entry, value, context);
        }
    };
    
    private static <T> T throwRendering() {
        throw new IllegalStateException("Can not create rendering type from NBT tag!");
    }
    
    public enum DeferredRenderer implements EntryRenderer<Renderer> {
        INSTANCE;
        
        @Override
        public void render(EntryStack<Renderer> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            entry.getValue().render(matrices, bounds, mouseX, mouseY, delta);
        }
        
        @Override
        @Nullable
        public Tooltip getTooltip(EntryStack<Renderer> entry, TooltipContext context) {
            return entry.getValue().getTooltip(context);
        }
    }
}
