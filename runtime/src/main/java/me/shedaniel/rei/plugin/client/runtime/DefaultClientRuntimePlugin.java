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

package me.shedaniel.rei.plugin.client.runtime;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.config.ItemCheatingMode;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitorWidget;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.widget.FavoritesListWidget;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class DefaultClientRuntimePlugin implements REIClientPlugin {
    private final FilteredStacksVisibilityHandler filteredStacksVisibilityHandler = new FilteredStacksVisibilityHandler();
    
    @SuppressWarnings("rawtypes")
    public DefaultClientRuntimePlugin() {
        PluginStageExecutionWatcher watcher = new PluginStageExecutionWatcher();
        for (PluginManager<? extends REIPlugin<?>> instance : PluginManager.getActiveInstances()) {
            instance.registerReloadable((Reloadable) watcher.reloadable(instance));
        }
        REIRuntimeImpl.getInstance().addHintProvider(watcher);
        REIRuntimeImpl.getInstance().addHintProvider(new SearchBarHighlightWatcher());
        REIRuntimeImpl.getInstance().addHintProvider(new SearchFilterPrepareWatcher());
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (ClientHelperImpl.getInstance().isAprilFools.get()) {
            registry.addEntry(ClientEntryStacks.of(new AbstractRenderer() {
                private ResourceLocation id = new ResourceLocation("roughlyenoughitems", "textures/gui/kirb.png");
                
                @Override
                public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                    RenderSystem.setShaderTexture(0, id);
                    innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getBlitOffset(), 0, 1, 0, 1);
                }
                
                @Override
                @Nullable
                public Tooltip getTooltip(Point point) {
                    return Tooltip.create(Component.literal("Kirby"), ClientHelper.getInstance().getFormattedModFromModId("Dream Land"));
                }
            }));
        }
        
        ((EntryRegistryImpl) registry).refilterListener.add(filteredStacksVisibilityHandler::reset);
    }
    
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        filteredStacksVisibilityHandler.reset();
        registry.registerVisibilityPredicate(filteredStacksVisibilityHandler);
    }
    
    @Override
    public void registerScreens(ScreenRegistry registry) {
        ExclusionZones zones = registry.exclusionZones();
        zones.register(DefaultDisplayViewingScreen.class, screen -> {
            Panel widget = screen.getWorkingStationsBaseWidget();
            if (widget == null)
                return Collections.emptyList();
            return Collections.singletonList(widget.getBounds().clone());
        });
        zones.register(Screen.class, screen -> {
            FavoritesListWidget widget = ScreenOverlayImpl.getFavoritesListWidget();
            if (widget != null) {
                if (widget.favoritePanelButton.isVisible()) {
                    return Collections.singletonList(widget.favoritePanelButton.bounds);
                }
            }
            return Collections.emptyList();
        });
        registry.registerDraggableStackProvider(DraggableStackProviderWidget.from(context -> {
            if (RoughlyEnoughItemsCoreClient.shouldReturn(context.getScreen()) || !REIRuntime.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIRuntime.getInstance().getOverlay().get().children(), DraggableStackProviderWidget.class::isInstance);
        }));
        registry.registerDraggableStackVisitor(DraggableStackVisitorWidget.from(context -> {
            if (RoughlyEnoughItemsCoreClient.shouldReturn(context.getScreen()) || !REIRuntime.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIRuntime.getInstance().getOverlay().get().children(), DraggableStackVisitorWidget.class::isInstance);
        }));
    }
    
    @Override
    public void registerFavorites(FavoriteEntryType.Registry registry) {
        registry.register(EntryStackFavoriteType.INSTANCE.id, EntryStackFavoriteType.INSTANCE);
    }
    
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new DefaultCategoryHandler());
    }
    
    private enum EntryStackFavoriteType implements FavoriteEntryType<EntryStackFavoriteEntry> {
        INSTANCE(FavoriteEntryType.ENTRY_STACK);
        
        private final String key = "data";
        private ResourceLocation id;
        
        EntryStackFavoriteType(ResourceLocation id) {
            this.id = id;
        }
        
        @Override
        public DataResult<EntryStackFavoriteEntry> read(CompoundTag object) {
            EntryStack<?> stack;
            try {
                stack = EntryStack.read(object.getCompound(key));
            } catch (Throwable throwable) {
                return DataResult.error(throwable.getMessage());
            }
            return DataResult.success(new EntryStackFavoriteEntry(stack), Lifecycle.stable());
        }
        
        @Override
        public DataResult<EntryStackFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error("Cannot create EntryStackFavoriteEntry from empty args!");
            if (!(args[0] instanceof EntryStack<?> stack))
                return DataResult.error("Creation of EntryStackFavoriteEntry from args expected EntryStack as the first argument!");
            if (!stack.supportSaving())
                return DataResult.error("Creation of EntryStackFavoriteEntry from an unserializable stack!");
            return DataResult.success(new EntryStackFavoriteEntry(stack), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(EntryStackFavoriteEntry entry, CompoundTag tag) {
            tag.put(key, entry.stack.save());
            return tag;
        }
    }
    
    private static class EntryStackFavoriteEntry extends FavoriteEntry {
        private static final Function<EntryStack<?>, String> CANCEL_FLUID_AMOUNT = s -> null;
        private final EntryStack<?> stack;
        private final long hash;
        
        public EntryStackFavoriteEntry(EntryStack<?> stack) {
            this.stack = stack.normalize();
            this.hash = EntryStacks.hashExact(this.stack);
        }
        
        @Override
        public boolean isInvalid() {
            return this.stack.isEmpty();
        }
        
        @Override
        public Renderer getRenderer(boolean showcase) {
            return this.stack;
        }
        
        @Override
        public boolean doAction(int button) {
            if (!(ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen))) return false;
            EntryStack<?> entry = stack.copy();
            if (!entry.isEmpty()) {
                if (entry.getType() != VanillaEntryTypes.ITEM) {
                    EntryStack<ItemStack> cheatsAs = entry.cheatsAs();
                    entry = cheatsAs.isEmpty() ? entry : cheatsAs;
                }
                if (entry.getType() == VanillaEntryTypes.ITEM)
                    entry.<ItemStack>castValue().setCount(button != 1 && !Screen.hasShiftDown() == (ConfigObject.getInstance().getItemCheatingMode() == ItemCheatingMode.REI_LIKE) ? 1 : entry.<ItemStack>castValue().getMaxStackSize());
                return ClientHelper.getInstance().tryCheatingEntry(entry);
            }
            
            return false;
        }
        
        @Override
        public long hashIgnoreAmount() {
            return hash;
        }
        
        @Override
        public FavoriteEntry copy() {
            return new EntryStackFavoriteEntry(stack.normalize());
        }
        
        @Override
        public ResourceLocation getType() {
            return EntryStackFavoriteType.INSTANCE.id;
        }
        
        @Override
        public boolean isSame(FavoriteEntry other) {
            if (!(other instanceof EntryStackFavoriteEntry that)) return false;
            return EntryStacks.equalsExact(stack, that.stack);
        }
    }
}
