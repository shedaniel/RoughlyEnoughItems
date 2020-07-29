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

package me.shedaniel.rei.impl;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.fractions.Fraction;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.widgets.*;
import me.shedaniel.rei.gui.widget.Widget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class Internals {
    private static Supplier<ConfigManager> configManager = Internals::throwNotSetup;
    private static Supplier<ClientHelper> clientHelper = Internals::throwNotSetup;
    private static Supplier<RecipeHelper> recipeHelper = Internals::throwNotSetup;
    private static Supplier<REIHelper> reiHelper = Internals::throwNotSetup;
    private static Supplier<FluidSupportProvider> fluidSupportProvider = Internals::throwNotSetup;
    private static Supplier<EntryStackProvider> entryStackProvider = Internals::throwNotSetup;
    private static Supplier<SubsetsRegistry> subsetsRegistry = Internals::throwNotSetup;
    private static Supplier<EntryRegistry> entryRegistry = Internals::throwNotSetup;
    private static Supplier<DisplayHelper> displayHelper = Internals::throwNotSetup;
    private static Supplier<WidgetsProvider> widgetsProvider = Internals::throwNotSetup;
    private static Supplier<ClientHelper.ViewSearchBuilder> viewSearchBuilder = Internals::throwNotSetup;
    private static BiFunction<@Nullable Point, Collection<Text>, Tooltip> tooltipProvider = (point, texts) -> throwNotSetup();
    
    private static <T> T throwNotSetup() {
        throw new AssertionError("REI Internals have not been initialized!");
    }
    
    @NotNull
    public static ConfigManager getConfigManager() {
        return configManager.get();
    }
    
    @NotNull
    public static ClientHelper getClientHelper() {
        return clientHelper.get();
    }
    
    @NotNull
    public static RecipeHelper getRecipeHelper() {
        return recipeHelper.get();
    }
    
    @NotNull
    public static REIHelper getREIHelper() {
        return reiHelper.get();
    }
    
    @NotNull
    @ApiStatus.Experimental
    public static FluidSupportProvider getFluidSupportProvider() {
        return fluidSupportProvider.get();
    }
    
    @NotNull
    public static EntryStackProvider getEntryStackProvider() {
        return entryStackProvider.get();
    }
    
    @NotNull
    public static SubsetsRegistry getSubsetsRegistry() {
        return subsetsRegistry.get();
    }
    
    @NotNull
    public static EntryRegistry getEntryRegistry() {
        return entryRegistry.get();
    }
    
    @NotNull
    public static DisplayHelper getDisplayHelper() {
        return displayHelper.get();
    }
    
    @NotNull
    public static WidgetsProvider getWidgetsProvider() {
        return widgetsProvider.get();
    }
    
    @NotNull
    public static ClientHelper.ViewSearchBuilder createViewSearchBuilder() {
        return viewSearchBuilder.get();
    }
    
    @NotNull
    public static Tooltip createTooltip(@Nullable Point point, Collection<Text> texts) {
        return tooltipProvider.apply(point, texts);
    }
    
    public interface EntryStackProvider {
        EntryStack empty();
        
        EntryStack fluid(Fluid fluid);
        
        EntryStack fluid(Fluid fluid, Fraction amount);
        
        EntryStack item(ItemStack stack);
    }
    
    @Environment(EnvType.CLIENT)
    public interface WidgetsProvider {
        boolean isRenderingPanel(Panel panel);
        
        Widget createDrawableWidget(DrawableConsumer drawable);
        
        Slot createSlot(Point point);
        
        Button createButton(Rectangle bounds, Text text);
        
        Panel createPanelWidget(Rectangle bounds);
        
        Label createLabel(Point point, StringRenderable text);
        
        Arrow createArrow(Rectangle rectangle);
        
        BurningFire createBurningFire(Rectangle rectangle);
        
        DrawableConsumer createTexturedConsumer(Identifier texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight);
        
        DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color);
    }
}
