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

import com.google.gson.JsonObject;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.widgets.*;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    private static Supplier<FavoriteEntryType.Registry> favoriteEntryTypeRegistry = Internals::throwNotSetup;
    private static BiFunction<Supplier<FavoriteEntry>, Supplier<JsonObject>, FavoriteEntry> delegateFavoriteEntry = (supplier, toJson) -> throwNotSetup();
    private static Function<JsonObject, FavoriteEntry> favoriteEntryFromJson = (object) -> throwNotSetup();
    private static Function<@NotNull Boolean, ClickAreaHandler.Result> clickAreaHandlerResult = (result) -> throwNotSetup();
    private static BiFunction<@Nullable Point, Collection<ITextComponent>, Tooltip> tooltipProvider = (point, texts) -> throwNotSetup();
    private static Supplier<BuiltinPlugin> builtinPlugin = Internals::throwNotSetup;
    
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
    
    public static FavoriteEntryType.Registry getFavoriteEntryTypeRegistry() {
        return favoriteEntryTypeRegistry.get();
    }
    
    @NotNull
    public static ClickAreaHandler.Result createClickAreaHandlerResult(boolean applicable) {
        return clickAreaHandlerResult.apply(applicable);
    }
    
    @NotNull
    public static Tooltip createTooltip(@Nullable Point point, Collection<ITextComponent> texts) {
        return tooltipProvider.apply(point, texts);
    }
    
    @NotNull
    public static BuiltinPlugin getBuiltinPlugin() {
        return builtinPlugin.get();
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, Class<T> clazz) {
        attachInstance((Supplier<T>) () -> instance, clazz.getSimpleName());
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, String name) {
        try {
            for (Field field : Internals.class.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(name)) {
                    field.setAccessible(true);
                    field.set(null, instance);
                    return;
                }
            }
            throw new RuntimeException("Failed to attach " + instance + " with field name: " + name);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static FavoriteEntry delegateFavoriteEntry(Supplier<FavoriteEntry> supplier, Supplier<JsonObject> toJoin) {
        return delegateFavoriteEntry.apply(supplier, toJoin);
    }
    
    public static FavoriteEntry favoriteEntryFromJson(JsonObject object) {
        return favoriteEntryFromJson.apply(object);
    }
    
    public interface EntryStackProvider {
        EntryStack empty();
        
        EntryStack fluid(FluidStack stack);
        
        EntryStack item(ItemStack stack);
    }
    
    @OnlyIn(Dist.CLIENT)
    public interface WidgetsProvider {
        boolean isRenderingPanel(Panel panel);
        
        Widget createDrawableWidget(DrawableConsumer drawable);
        
        Slot createSlot(Point point);
        
        Button createButton(Rectangle bounds, ITextComponent text);
        
        Panel createPanelWidget(Rectangle bounds);
        
        Label createLabel(Point point, ITextProperties text);
        
        Arrow createArrow(Rectangle rectangle);
        
        BurningFire createBurningFire(Rectangle rectangle);
        
        DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight);
        
        DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color);
    }
}
