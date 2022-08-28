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

package me.shedaniel.rei.impl.client;

import com.google.common.base.Suppliers;
import com.mojang.serialization.DataResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.TooltipQueue;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.PreFilteredEntryList;
import me.shedaniel.rei.api.client.registry.screen.ClickArea;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.impl.client.provider.*;
import me.shedaniel.rei.impl.common.Internals;
import net.minecraft.ReportedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.*;

@ApiStatus.Internal
public final class ClientInternals {
    private static final ClientHelper CLIENT_HELPER = resolveService(ClientHelper.class);
    private static final WidgetsProvider WIDGETS_PROVIDER = resolveService(WidgetsProvider.class);
    private static final ViewSearchBuilder VIEW_SEARCH_BUILDER = resolveService(ViewSearchBuilder.class);
    private static final PluginManager<REIClientPlugin> CLIENT_PLUGIN_MANAGER = Internals.createPluginManager(
            REIClientPlugin.class,
            UnaryOperator.identity());
    private static final DelegatingFavoriteEntryProvider DELEGATE_FAVORITE_ENTRY = resolveService(DelegatingFavoriteEntryProvider.class);
    private static final FavoritesEntriesListProvider FAVORITES_ENTRIES_LIST = resolveService(FavoritesEntriesListProvider.class);
    private static final List<OverlayTicker> OVERLAY_TICKERS = resolveServices(OverlayTicker.class);
    private static final AutoCraftingEvaluator AUTO_CRAFTING_EVALUATOR = resolveService(AutoCraftingEvaluator.class);
    private static final TooltipQueue TOOLTIP_QUEUE = resolveService(TooltipQueue.class);
    private static final TooltipRenderer TOOLTIP_RENDERER = resolveService(TooltipRenderer.class);
    private static final OverlayProvider SCREEN_OVERLAY_PROVIDER = resolveService(OverlayProvider.class);
    private static Function<CompoundTag, DataResult<FavoriteEntry>> favoriteEntryFromJson = (object) -> throwNotSetup();
    private static Function<Boolean, ClickArea.Result> clickAreaHandlerResult = (result) -> throwNotSetup();
    private static BiFunction<@Nullable Point, Collection<Tooltip.Entry>, Tooltip> tooltipProvider = (point, texts) -> throwNotSetup();
    private static TriFunction<Point, @Nullable TooltipFlag, Boolean, TooltipContext> tooltipContextProvider = (point, texts, search) -> throwNotSetup();
    private static Function<Object, Tooltip.Entry> tooltipEntryProvider = (component) -> throwNotSetup();
    private static Supplier<List<String>> jeiCompatMods = ClientInternals::throwNotSetup;
    private static Supplier<Object> builtinClientPlugin = ClientInternals::throwNotSetup;
    private static final MissingStacksTooltipProvider MISSING_TOOLTIP = resolveService(MissingStacksTooltipProvider.class);
    private static BiConsumer<ReportedException, String> crashHandler = (exception, component) -> throwNotSetup();
    private static Supplier<PreFilteredEntryList> preFilteredEntryList = ClientInternals::throwNotSetup;
    
    public static <T> T resolveService(Class<T> serviceClass) {
        return Internals.resolveService(serviceClass);
    }
    
    public static <T> List<T> resolveServices(Class<T> serviceClass) {
        return Internals.resolveServices(serviceClass);
    }
    
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
        return CLIENT_HELPER;
    }
    
    public static WidgetsProvider getWidgetsProvider() {
        return WIDGETS_PROVIDER;
    }
    
    public static ViewSearchBuilder createViewSearchBuilder() {
        return VIEW_SEARCH_BUILDER;
    }
    
    public static Object getBuiltinPlugin() {
        return builtinClientPlugin.get();
    }
    
    public static ClickArea.Result createClickAreaHandlerResult(boolean applicable) {
        return clickAreaHandlerResult.apply(applicable);
    }
    
    public static Tooltip createTooltip(@Nullable Point point, Collection<Tooltip.Entry> texts) {
        return tooltipProvider.apply(point, texts);
    }
    
    public static TooltipRenderer getTooltipRenderer() {
        return TOOLTIP_RENDERER;
    }
    
    public static ScreenOverlay getNewOverlay() {
        return SCREEN_OVERLAY_PROVIDER.provide();
    }
    
    public static TooltipContext createTooltipContext(Point point, @Nullable TooltipFlag flag, boolean isSearch) {
        return tooltipContextProvider.apply(point, flag, isSearch);
    }
    
    public static Tooltip.Entry createTooltipEntry(Object component) {
        return tooltipEntryProvider.apply(component);
    }
    
    public static FavoriteEntry delegateFavoriteEntry(Supplier<DataResult<FavoriteEntry>> supplier, Supplier<CompoundTag> toJoin) {
        return DELEGATE_FAVORITE_ENTRY.delegate(supplier, toJoin);
    }
    
    public static List<FavoriteEntry> getFavoritesEntriesList() {
        return FAVORITES_ENTRIES_LIST.get();
    }
    
    public static List<OverlayTicker> getOverlayTickers() {
        return OVERLAY_TICKERS;
    }
    
    public static AutoCraftingEvaluator.Builder getAutoCraftingEvaluator(Display display) {
        return AUTO_CRAFTING_EVALUATOR.builder(display);
    }
    
    public static TooltipQueue getTooltipQueue() {
        return TOOLTIP_QUEUE;
    }
    
    public static DataResult<FavoriteEntry> favoriteEntryFromJson(CompoundTag tag) {
        return favoriteEntryFromJson.apply(tag);
    }
    
    public static List<String> getJeiCompatMods() {
        return jeiCompatMods.get();
    }
    
    public static PluginManager<REIClientPlugin> getPluginManager() {
        return CLIENT_PLUGIN_MANAGER;
    }
    
    public static TooltipComponent createMissingTooltip(List<EntryIngredient> stacks) {
        return MISSING_TOOLTIP.provide(stacks);
    }
    
    public static PreFilteredEntryList getPreFilteredEntryList() {
        return preFilteredEntryList.get();
    }
    
    public static void crash(ReportedException exception, String component) {
        crashHandler.accept(exception, component);
    }
}
