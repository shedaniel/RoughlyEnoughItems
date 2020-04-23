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

package me.shedaniel.rei.plugin.fuel;

import com.google.common.collect.Lists;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.widgets.Slot;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.api.widgets.Widgets;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class DefaultFuelCategory implements RecipeCategory<DefaultFuelDisplay> {
    
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.FUEL;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.fuel");
    }
    
    @Override
    public int getDisplayHeight() {
        return 49;
    }
    
    @Override
    public EntryStack getLogo() {
        return EntryStack.create(Items.COAL);
    }
    
    @Override
    public List<Widget> setupDisplay(DefaultFuelDisplay recipeDisplay, me.shedaniel.math.Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 17);
        String burnItems = DECIMAL_FORMAT.format(recipeDisplay.getFuelTime() / 200d);
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createLabel(new Point(bounds.x + 26, bounds.getMaxY() - 15), new TranslatableText("category.rei.fuel.time.items", burnItems))
                .color(0xFF404040, 0xFFBBBBBB).noShadow().leftAligned());
        widgets.add(Widgets.createBurningFire(new Point(bounds.x + 6, startPoint.y + 1)).animationDurationTicks(recipeDisplay.getFuelTime()));
        widgets.add(Widgets.createSlot(new Point(bounds.x + 6, startPoint.y + 18)).entries(recipeDisplay.getInputEntries().get(0)).markInput());
        return widgets;
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultFuelDisplay recipe) {
        Slot slot = Widgets.createSlot(new Point(0, 0)).entries(recipe.getInputEntries().get(0)).disableBackground().disableHighlight();
        String burnItems = DECIMAL_FORMAT.format(recipe.getFuelTime() / 200d);
        return new RecipeEntry() {
            @Override
            public int getHeight() {
                return 22;
            }
            
            @Nullable
            @Override
            public Tooltip getTooltip(Point point) {
                if (slot.containsMouse(point))
                    return slot.getCurrentTooltip(point);
                return null;
            }
            
            @Override
            public void render(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                slot.setZ(getZ() + 50);
                slot.getBounds().setLocation(bounds.x + 4, bounds.y + 2);
                slot.render(matrices, mouseX, mouseY, delta);
                MinecraftClient.getInstance().textRenderer.method_27517(matrices, new TranslatableText("category.rei.fuel.time_short.items", burnItems), bounds.x + 25, bounds.y + 8, -1);
            }
        };
    }
}
