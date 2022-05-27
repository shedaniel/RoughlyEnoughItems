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

package me.shedaniel.rei.plugin.client.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultFuelDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DefaultFuelCategory implements DisplayCategory<DefaultFuelDisplay> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    
    @Override
    public CategoryIdentifier<? extends DefaultFuelDisplay> getCategoryIdentifier() {
        return BuiltinPlugin.FUEL;
    }
    
    @Override
    public Component getTitle() {
        return Component.translatable("category.rei.fuel");
    }
    
    @Override
    public int getDisplayHeight() {
        return 49;
    }
    
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.COAL);
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultFuelDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 17);
        String burnItems = DECIMAL_FORMAT.format(display.getFuelTime() / 200d);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createLabel(new Point(bounds.x + 26, bounds.getMaxY() - 15), Component.translatable("category.rei.fuel.time.items", burnItems))
                .color(0xFF404040, 0xFFBBBBBB).noShadow().leftAligned());
        widgets.add(Widgets.createBurningFire(new Point(bounds.x + 6, startPoint.y + 1)).animationDurationTicks(display.getFuelTime()));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 6, startPoint.y + 18)).entries(display.getInputEntries().get(0)).markInput());
        return widgets;
    }
    
    @Override
    public DisplayRenderer getDisplayRenderer(DefaultFuelDisplay display) {
        Slot slot = Widgets.createSlot(new Point(0, 0)).entries(display.getInputEntries().get(0)).disableBackground().disableHighlight();
        String burnItems = DECIMAL_FORMAT.format(display.getFuelTime() / 200d);
        return new DisplayRenderer() {
            private Component text = Component.translatable("category.rei.fuel.time_short.items", burnItems);
            
            @Override
            public int getHeight() {
                return 22;
            }
            
            @Nullable
            @Override
            public Tooltip getTooltip(TooltipContext context) {
                if (slot.containsMouse(context.getPoint()))
                    return slot.getCurrentTooltip(context);
                return null;
            }
            
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                slot.setZ(getZ() + 50);
                slot.getBounds().setLocation(bounds.x + 4, bounds.y + 2);
                slot.render(matrices, mouseX, mouseY, delta);
                Minecraft.getInstance().font.drawShadow(matrices, text.getVisualOrderText(), bounds.x + 25, bounds.y + 8, -1);
            }
        };
    }
}
