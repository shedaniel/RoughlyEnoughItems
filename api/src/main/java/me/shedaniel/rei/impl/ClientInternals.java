/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class ClientInternals {
    private static Supplier<ClientHelper> clientHelper = ClientInternals::throwNotSetup;
    private static Supplier<WidgetsProvider> widgetsProvider = ClientInternals::throwNotSetup;
    private static Supplier<ViewSearchBuilder> viewSearchBuilder = ClientInternals::throwNotSetup;
    private static Supplier<PluginManager<REIClientPlugin>> clientPluginManager = ClientInternals::throwNotSetup;
    private static Supplier<EntryRenderer<?>> emptyEntryRenderer = ClientInternals::throwNotSetup;
    private static BiFunction<Supplier<FavoriteEntry>, Supplier<CompoundTag>, FavoriteEntry> delegateFavoriteEntry = (supplier, toJson) -> throwNotSetup();
    private static Function<CompoundTag, FavoriteEntry> favoriteEntryFromJson = (object) -> throwNotSetup();
    private static Function<Boolean, ClickArea.Result> clickAreaHandlerResult = (result) -> throwNotSetup();
    private static BiFunction<@Nullable Point, Collection<Component>, Tooltip> tooltipProvider = (point, texts) -> throwNotSetup();
    private static Supplier<List<String>> jeiCompatMods = ClientInternals::throwNotSetup;
    private static Supplier<Object> builtinClientPlugin = ClientInternals::throwNotSetup;
    
    private static <T> T throwNotSetup() {
        throw new AssertionError("REI Internals have not been initialized!");
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstance(T instance, Class<T> clazz) {
        attachInstanceSupplier(instance, clazz.getSimpleName());
    }
    
    @ApiStatus.Internal
    public static <T> void attachInstanceSupplier(T instance, String name) {
        attachInstance((Supplier<T>) () -> instance, name);
    }
    
    public static <T> void attachInstance(T instance, String name) {
        try {
            for (Field field : ClientInternals.class.getDeclaredFields()) {
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
    
    public static ClientHelper getClientHelper() {
        return clientHelper.get();
    }
    
    public static WidgetsProvider getWidgetsProvider() {
        return widgetsProvider.get();
    }
    
    public static ViewSearchBuilder createViewSearchBuilder() {
        return viewSearchBuilder.get();
    }
    
    public static Object getBuiltinPlugin() {
        return builtinClientPlugin.get();
    }
    
    public static ClickArea.Result createClickAreaHandlerResult(boolean applicable) {
        return clickAreaHandlerResult.apply(applicable);
    }
    
    public static Tooltip createTooltip(@Nullable Point point, Collection<Component> texts) {
        return tooltipProvider.apply(point, texts);
    }
    
    public static FavoriteEntry delegateFavoriteEntry(Supplier<FavoriteEntry> supplier, Supplier<CompoundTag> toJoin) {
        return delegateFavoriteEntry.apply(supplier, toJoin);
    }
    
    public static FavoriteEntry favoriteEntryFromJson(CompoundTag tag) {
        return favoriteEntryFromJson.apply(tag);
    }
    
    public static <T> EntryRenderer<T> getEmptyEntryRenderer() {
        return emptyEntryRenderer.get().cast();
    }
    
    public static List<String> getJeiCompatMods() {
        return jeiCompatMods.get();
    }
    
    public static PluginManager<REIClientPlugin> getPluginManager() {
        return clientPluginManager.get();
    }
    
    @Environment(EnvType.CLIENT)
    public interface WidgetsProvider {
        boolean isRenderingPanel(Panel panel);
        
        Widget createDrawableWidget(DrawableConsumer drawable);
        
        Slot createSlot(Point point);
        
        Slot createSlot(Rectangle bounds);
        
        Button createButton(Rectangle bounds, Component text);
        
        Panel createPanelWidget(Rectangle bounds);
        
        Label createLabel(Point point, FormattedText text);
        
        Arrow createArrow(Rectangle rectangle);
        
        BurningFire createBurningFire(Rectangle rectangle);
        
        DrawableConsumer createTexturedConsumer(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight);
        
        DrawableConsumer createFillRectangleConsumer(Rectangle rectangle, int color);
    }
}
