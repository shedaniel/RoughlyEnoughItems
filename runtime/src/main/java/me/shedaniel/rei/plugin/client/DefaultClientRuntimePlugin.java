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

package me.shedaniel.rei.plugin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitorWidget;
import me.shedaniel.rei.api.client.gui.widgets.Panel;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.gui.ContainerScreenOverlay;
import me.shedaniel.rei.impl.client.gui.screen.AbstractDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.widget.FavoritesListWidget;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;
import me.shedaniel.rei.plugin.client.entry.ItemEntryDefinition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class DefaultClientRuntimePlugin implements REIClientPlugin {
    public static final ResourceLocation PLUGIN = new ResourceLocation("roughlyenoughitems", "default_runtime_plugin");
    
    @Override
    public void registerEntryTypes(EntryTypeRegistry registry) {
        registry.register(VanillaEntryTypes.ITEM, new ItemEntryDefinition());
        registry.register(VanillaEntryTypes.FLUID, new FluidEntryDefinition());
        
        registry.registerBridge(VanillaEntryTypes.ITEM, VanillaEntryTypes.FLUID, input -> {
            Optional<Stream<EntryStack<FluidStack>>> stream = FluidSupportProvider.getInstance().itemToFluids(input);
            if (!stream.isPresent()) {
                return InteractionResultHolder.pass(Stream.empty());
            }
            return InteractionResultHolder.success(stream.get());
        });
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (ClientHelperImpl.getInstance().isAprilFools.get()) {
            registry.addEntry(ClientEntryStacks.of(new AbstractRenderer() {
                private ResourceLocation id = new ResourceLocation("roughlyenoughitems", "textures/gui/kirb.png");
                
                @Override
                public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                    Minecraft.getInstance().getTextureManager().bind(id);
                    innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getBlitOffset(), 0, 1, 0, 1);
                }
    
                @Override
                @Nullable
                public Tooltip getTooltip(Point point) {
                    return Tooltip.create(new TextComponent("Kirby"), ClientHelper.getInstance().getFormattedModFromModId("Dream Land"));
                }
            }));
        }
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
            FavoritesListWidget widget = ContainerScreenOverlay.getFavoritesListWidget();
            if (widget != null) {
                if (widget.favoritePanelButton.isVisible()) {
                    return Collections.singletonList(widget.favoritePanelButton.bounds);
                }
            }
            return Collections.emptyList();
        });
        registry.registerDecider(new DisplayBoundsProvider<AbstractDisplayViewingScreen>() {
            @Override
            public Rectangle getScreenBounds(AbstractDisplayViewingScreen screen) {
                return screen.getBounds();
            }
    
            @Override
            public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
                return  AbstractDisplayViewingScreen.class.isAssignableFrom(screen);
            }
    
            @Override
            public InteractionResult shouldScreenBeOverlaid(Class<?> screen) {
                return InteractionResult.SUCCESS;
            }
        });
        registry.registerDraggableStackProvider(DraggableStackProviderWidget.from(context -> {
            if (RoughlyEnoughItemsCore.shouldReturn(context.getScreen()) || !REIHelper.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIHelper.getInstance().getOverlay().get().children(), DraggableStackProviderWidget.class::isInstance);
        }));
        registry.registerDraggableStackVisitor(DraggableStackVisitorWidget.from(context -> {
            if (RoughlyEnoughItemsCore.shouldReturn(context.getScreen()) || !REIHelper.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIHelper.getInstance().getOverlay().get().children(), DraggableStackVisitorWidget.class::isInstance);
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
        public EntryStackFavoriteEntry read(CompoundTag object) {
            return new EntryStackFavoriteEntry(EntryStack.read(object.getCompound(key)));
        }
        
        @Override
        public EntryStackFavoriteEntry fromArgs(Object... args) {
            return new EntryStackFavoriteEntry((EntryStack<?>) args[0]);
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
        private final long hashIgnoreAmount;
        
        public EntryStackFavoriteEntry(EntryStack<?> stack) {
            this.stack = stack.normalize();
            this.hashIgnoreAmount = EntryStacks.hashExact(this.stack);
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
            if (!ClientHelper.getInstance().isCheating()) return false;
            EntryStack<?> entry = stack.copy();
            if (!entry.isEmpty()) {
                if (entry.getValueType() == FluidStack.class) {
                    Item bucketItem = ((FluidStack) entry.getValue()).getFluid().getBucket();
                    if (bucketItem != null) {
                        entry = EntryStacks.of(bucketItem);
                    }
                }
                if (entry.getType() == VanillaEntryTypes.ITEM)
                    entry.<ItemStack>cast().getValue().setCount(button != 1 && !Screen.hasShiftDown() ? 1 : ((ItemStack) entry.getValue()).getMaxStackSize());
                return ClientHelper.getInstance().tryCheatingEntry(entry);
            }
            
            return false;
        }
        
        @Override
        public long hashIgnoreAmount() {
            return hashIgnoreAmount;
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
            if (!(other instanceof EntryStackFavoriteEntry)) return false;
            EntryStackFavoriteEntry that = (EntryStackFavoriteEntry) other;
            return EntryStacks.equalsExact(stack, that.stack);
        }
    }
}
