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

package me.shedaniel.rei.plugin.client.runtime;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import dev.architectury.platform.Platform;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCoreClient;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.config.RecipeBorderType;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentProviderWidget;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponentVisitorWidget;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.widget.AutoCraftingEvaluator;
import me.shedaniel.rei.impl.client.gui.widget.DisplayCompositeWidget;
import me.shedaniel.rei.impl.client.gui.widget.DisplayTooltipComponent;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.search.method.DefaultInputMethod;
import me.shedaniel.rei.impl.client.search.method.unihan.*;
import me.shedaniel.rei.impl.common.InternalLogger;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryListener;
import me.shedaniel.rei.impl.common.util.HNEntryStackWrapper;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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
        REIRuntimeImpl.getInstance().addHintProvider(new SearchFilterWatcher());
        REIRuntimeImpl.getInstance().addHintProvider(new InputMethodWatcher());
    }
    
    @Override
    public void registerEntries(EntryRegistry registry) {
        if (ClientHelperImpl.getInstance().isAprilFools.get()) {
            registry.addEntry(ClientEntryStacks.of(new AbstractRenderer() {
                private final ResourceLocation id = new ResourceLocation("roughlyenoughitems", "textures/gui/kirb.png");
                
                @Override
                public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                    RenderSystem.setShaderTexture(0, id);
                    innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getZ(), 0, 1, 0, 1);
                }
                
                @Override
                @Nullable
                public Tooltip getTooltip(TooltipContext context) {
                    return Tooltip.create(context.getPoint(), Component.literal("Kirby"), ClientHelper.getInstance().getFormattedModFromModId("Dream Land"));
                }
            }));
        }
        
        ((EntryRegistryImpl) registry).listeners.add(new EntryRegistryListener() {
            @Override
            public void onReFilter(List<HNEntryStackWrapper> stacks) {
                filteredStacksVisibilityHandler.reset();
            }
        });
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
                if (widget.togglePanelButton.isVisible()) {
                    return Collections.singletonList(widget.togglePanelButton.bounds);
                }
            }
            return Collections.emptyList();
        });
        registry.registerDraggableComponentProvider(DraggableComponentProviderWidget.from(context -> {
            if (RoughlyEnoughItemsCoreClient.shouldReturn(context.getScreen()) || !REIRuntime.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIRuntime.getInstance().getOverlay().get().children(), DraggableComponentProviderWidget.class::isInstance);
        }));
        registry.registerDraggableComponentVisitor(DraggableComponentVisitorWidget.from(context -> {
            if (RoughlyEnoughItemsCoreClient.shouldReturn(context.getScreen()) || !REIRuntime.getInstance().isOverlayVisible()) return Collections.emptyList();
            return Widgets.walk(REIRuntime.getInstance().getOverlay().get().children(), DraggableComponentVisitorWidget.class::isInstance);
        }));
    }
    
    @Override
    public void registerFavorites(FavoriteEntryType.Registry registry) {
        registry.register(EntryStackFavoriteType.INSTANCE.id, EntryStackFavoriteType.INSTANCE);
        registry.register(DisplayFavoriteType.INSTANCE.id, DisplayFavoriteType.INSTANCE);
    }
    
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new DefaultCategoryHandler());
    }
    
    @Override
    public void registerInputMethods(InputMethodRegistry registry) {
        registry.add(DefaultInputMethod.ID, DefaultInputMethod.INSTANCE);
        UniHanManager manager = new UniHanManager(Platform.getConfigFolder().resolve("roughlyenoughitems/unihan.zip"));
        registry.add(new ResourceLocation("rei:pinyin"), new PinyinInputMethod(manager));
        registry.add(new ResourceLocation("rei:jyutping"), new JyutpingInputMethod(manager));
        registry.add(new ResourceLocation("rei:bomopofo"), new BomopofoInputMethod(manager));
        registry.add(new ResourceLocation("rei:double_pinyin"), new DoublePinyinInputMethod(manager));
    }
    
    private enum EntryStackFavoriteType implements FavoriteEntryType<EntryStackFavoriteEntry> {
        INSTANCE(FavoriteEntryType.ENTRY_STACK);
        
        private final String key = "data";
        private final ResourceLocation id;
        
        EntryStackFavoriteType(ResourceLocation id) {
            this.id = id;
        }
        
        @Override
        public DataResult<EntryStackFavoriteEntry> read(CompoundTag object) {
            EntryStack<?> stack;
            try {
                stack = EntryStack.read(object.getCompound(key));
            } catch (Throwable throwable) {
                return DataResult.error(throwable::getMessage);
            }
            return DataResult.success(new EntryStackFavoriteEntry(stack), Lifecycle.stable());
        }
        
        @Override
        public DataResult<EntryStackFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error(() -> "Cannot create EntryStackFavoriteEntry from empty args!");
            if (!(args[0] instanceof EntryStack<?> stack))
                return DataResult.error(() -> "Creation of EntryStackFavoriteEntry from args expected EntryStack as the first argument!");
            if (!stack.supportSaving())
                return DataResult.error(() -> "Creation of EntryStackFavoriteEntry from an unserializable stack!");
            return DataResult.success(new EntryStackFavoriteEntry(stack), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(EntryStackFavoriteEntry entry, CompoundTag tag) {
            tag.put(key, entry.stack.saveStack());
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
    
    private enum DisplayFavoriteType implements FavoriteEntryType<DisplayFavoriteEntry> {
        INSTANCE(FavoriteEntryType.DISPLAY);
        
        private final String key = "data";
        private final ResourceLocation id;
        
        DisplayFavoriteType(ResourceLocation id) {
            this.id = id;
        }
        
        @Override
        public DataResult<DisplayFavoriteEntry> read(CompoundTag object) {
            try {
                if (object.contains("Data")) {
                    Display display = DisplaySerializerRegistry.getInstance().read(CategoryIdentifier.of(object.getString("CategoryID")), object.getCompound("Data"));
                    return DataResult.success(new DisplayFavoriteEntry(display, UUID.fromString(object.getString("UUID"))), Lifecycle.stable());
                } else {
                    return DataResult.success(new DisplayFavoriteEntry(null, UUID.fromString(object.getString("UUID"))), Lifecycle.stable());
                }
            } catch (Throwable throwable) {
                return DataResult.error(throwable::getMessage);
            }
        }
        
        @Override
        public DataResult<DisplayFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error(() -> "Cannot create DisplayFavoriteEntry from empty args!");
            if (!(args[0] instanceof Display display))
                return DataResult.error(() -> "Creation of DisplayFavoriteEntry from args expected Display as the first argument!");
            return DataResult.success(new DisplayFavoriteEntry(display, UUID.randomUUID()), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(DisplayFavoriteEntry entry, CompoundTag tag) {
            boolean hasSerializer = DisplaySerializerRegistry.getInstance().hasSerializer(entry.display.getCategoryIdentifier());
            tag.putString("CategoryID", entry.display.getCategoryIdentifier().toString());
            tag.putString("UUID", entry.uuid.toString());
            
            if (hasSerializer) {
                try {
                    tag.put("Data", DisplaySerializerRegistry.getInstance().save(entry.display, new CompoundTag()));
                } catch (Exception e) {
                    InternalLogger.getInstance().warn("Failed to save display favorite entry", e);
                }
            }
            
            return tag;
        }
    }
    
    private static class DisplayFavoriteEntry extends FavoriteEntry {
        private static final Function<EntryStack<?>, String> CANCEL_FLUID_AMOUNT = s -> null;
        private final Supplier<DisplayTooltipComponent> tooltipComponent;
        private final Display display;
        private final UUID uuid;
        private final long hash;
        
        public DisplayFavoriteEntry(Display display, UUID uuid) {
            this.display = display;
            this.uuid = uuid;
            this.hash = uuid.hashCode();
            this.tooltipComponent = Suppliers.memoize(() -> new DisplayTooltipComponent(display));
        }
        
        @Override
        public UUID getUuid() {
            return uuid;
        }
        
        @Override
        public boolean isInvalid() {
            return this.display == null;
        }
        
        @Override
        public Renderer getRenderer(boolean showcase) {
            Panel panel = Widgets.createRecipeBase(new Rectangle(0, 0, 18, 18))
                    .yTextureOffset(RecipeBorderType.LIGHTER.getYOffset());
            Slot slot = Widgets.createSlot(new Rectangle())
                    .disableBackground()
                    .disableHighlight()
                    .disableTooltips();
            for (EntryIngredient ingredient : display.getOutputEntries()) {
                slot.entries(ingredient);
            }
            return new AbstractRenderer() {
                @Override
                public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                    matrices.pushPose();
                    matrices.translate(bounds.getX(), bounds.getY(), 1);
                    matrices.scale(bounds.width / (float) panel.getBounds().getWidth(), bounds.height / (float) panel.getBounds().getHeight(), 1);
                    panel.render(matrices, mouseX, mouseY, delta);
                    matrices.popPose();
                    if (bounds.width > 4 && bounds.height > 4) {
                        matrices.pushPose();
                        matrices.translate(0, 0.5, 0);
                        slot.getBounds().setBounds(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
                        slot.render(matrices, mouseX, mouseY, delta);
                        matrices.popPose();
                    }
                }
                
                @Override
                @Nullable
                public Tooltip getTooltip(TooltipContext context) {
                    Tooltip tooltip = Tooltip.create(context.getPoint());
                    tooltip.add(tooltipComponent.get());
                    tooltip.add(Component.translatable("text.auto_craft.move_items.tooltip").withStyle(ChatFormatting.YELLOW));
                    return tooltip;
                }
            };
        }
        
        @Override
        public boolean doAction(int button) {
            Widgets.produceClickSound();
            
            if (!(Minecraft.getInstance().screen instanceof DisplayScreen) && Screen.hasControlDown()) {
                AutoCraftingEvaluator.evaluateAutoCrafting(true, Screen.hasShiftDown(), display, Collections::emptyList);
                return true;
            }
            
            ClientHelperImpl.getInstance()
                    .openDisplayViewingScreen(Map.of(CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory(), List.of(display)),
                            null, List.of(), List.of());
            return true;
        }
        
        @Override
        public long hashIgnoreAmount() {
            return hash;
        }
        
        @Override
        public FavoriteEntry copy() {
            return new DisplayFavoriteEntry(this.display, this.uuid);
        }
        
        @Override
        public ResourceLocation getType() {
            return DisplayFavoriteType.INSTANCE.id;
        }
        
        @Override
        public boolean isSame(FavoriteEntry other) {
            if (!(other instanceof DisplayFavoriteEntry that)) return false;
            return Objects.equals(this.uuid, that.uuid);
        }
        
        @Override
        @Nullable
        public DraggableComponent<?> asDraggableComponent(Slot slot) {
            CategoryRegistry.CategoryConfiguration<Display> configuration = CategoryRegistry.getInstance().get((CategoryIdentifier<Display>) display.getCategoryIdentifier());
            DisplayCategory<Display> category = configuration.getCategory();
            Rectangle displayBounds = new Rectangle(0, 0, category.getDisplayWidth(display), category.getDisplayHeight());
            List<Widget> widgets = configuration.getView(display).setupDisplay(display, displayBounds);
            DisplayCompositeWidget.DisplayDraggableComponent component = new DisplayCompositeWidget.DisplayDraggableComponent(
                    Widgets.concat(CollectionUtils.filterToList(widgets, w -> !(w instanceof Panel))),
                    display, slot.getInnerBounds(), displayBounds);
            component.onFavoritesRegion = true;
            return component;
        }
    }
}
