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

package me.shedaniel.rei.gui.plugin;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.Panel;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.ingredient.EntryStack;
import me.shedaniel.rei.api.ingredient.entry.AbstractRenderer;
import me.shedaniel.rei.api.ingredient.entry.ComparisonContext;
import me.shedaniel.rei.api.ingredient.entry.EntryTypeRegistry;
import me.shedaniel.rei.api.ingredient.entry.VanillaEntryTypes;
import me.shedaniel.rei.api.ingredient.util.EntryStacks;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.plugin.entry.FluidEntryDefinition;
import me.shedaniel.rei.gui.plugin.entry.ItemEntryDefinition;
import me.shedaniel.rei.gui.widget.FavoritesListWidget;
import me.shedaniel.rei.plugin.autocrafting.DefaultCategoryHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class DefaultRuntimePlugin implements REIPluginV0 {
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
        registry.registerEntry(EntryStacks.of(new AbstractRenderer() {
            private ResourceLocation id = new ResourceLocation("roughlyenoughitems", "textures/gui/kirb.png");
            
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                Minecraft.getInstance().getTextureManager().bind(id);
                innerBlit(matrices.last().pose(), bounds.x, bounds.getMaxX(), bounds.y, bounds.getMaxY(), getBlitOffset(), 0, 1, 0, 1);
            }
            
            @Override
            public @Nullable Tooltip getTooltip(Point point) {
                return Tooltip.create(new TextComponent("Kirby"), ClientHelper.getInstance().getFormattedModFromModId("Dream Land"));
            }
        }));
    }
    
    @Override
    public void registerBounds(DisplayBoundsRegistry registry) {
        ExclusionZones exclusionZones = ExclusionZones.getInstance();
        exclusionZones.register(RecipeViewingScreen.class, () -> {
            Panel widget = ((RecipeViewingScreen) Minecraft.getInstance().screen).getWorkingStationsBaseWidget();
            if (widget == null)
                return Collections.emptyList();
            return Collections.singletonList(widget.getBounds().clone());
        });
        exclusionZones.register(Screen.class, () -> {
            FavoritesListWidget widget = ContainerScreenOverlay.getFavoritesListWidget();
            if (widget != null) {
                if (widget.favoritePanelButton.isVisible())
                    return Collections.singletonList(widget.favoritePanelButton.bounds);
            }
            return Collections.emptyList();
        });
        registry.registerProvider(new DisplayBoundsRegistry.DisplayBoundsProvider<RecipeViewingScreen>() {
            @Override
            public Rectangle getScreenBounds(RecipeViewingScreen screen) {
                return screen.getBounds();
            }
            
            @Override
            public Class<?> getBaseSupportedClass() {
                return RecipeViewingScreen.class;
            }
            
            @Override
            public InteractionResult shouldScreenBeOverlaid(Class<?> screen) {
                return InteractionResult.SUCCESS;
            }
        });
        registry.registerProvider(new DisplayBoundsRegistry.DisplayBoundsProvider<VillagerRecipeViewingScreen>() {
            @Override
            public Rectangle getScreenBounds(VillagerRecipeViewingScreen screen) {
                return screen.bounds;
            }
            
            @Override
            public Class<?> getBaseSupportedClass() {
                return VillagerRecipeViewingScreen.class;
            }
            
            @Override
            public InteractionResult shouldScreenBeOverlaid(Class<?> screen) {
                return InteractionResult.SUCCESS;
            }
        });
    }
    
    @Override
    public void registerOthers(RecipeRegistry registry) {
        registry.registerAutoCraftingHandler(new DefaultCategoryHandler());
        FavoriteEntryType.registry().register(EntryStackFavoriteType.INSTANCE.id, EntryStackFavoriteType.INSTANCE);
    }
    
    private enum EntryStackFavoriteType implements FavoriteEntryType<EntryStackFavoriteEntry> {
        INSTANCE(FavoriteEntryType.ENTRY_STACK);
        
        private final String key = "data";
        private ResourceLocation id;
        
        EntryStackFavoriteType(ResourceLocation id) {
            this.id = id;
        }
        
        @Override
        public @NotNull EntryStackFavoriteEntry fromJson(@NotNull JsonObject object) {
            return new EntryStackFavoriteEntry(EntryStack.readFromJson(GsonHelper.getAsJsonObject(object, key)));
        }
        
        @Override
        public @NotNull EntryStackFavoriteEntry fromArgs(Object... args) {
            return new EntryStackFavoriteEntry((EntryStack<?>) args[0]);
        }
        
        @Override
        public @NotNull JsonObject toJson(@NotNull EntryStackFavoriteEntry entry, @NotNull JsonObject object) {
            object.add(key, entry.stack.toJson());
            return object;
        }
    }
    
    private static class EntryStackFavoriteEntry extends FavoriteEntry {
        private static final Function<EntryStack<?>, String> CANCEL_FLUID_AMOUNT = s -> null;
        private final EntryStack<?> stack;
        private final int hashIgnoreAmount;
        
        public EntryStackFavoriteEntry(EntryStack<?> stack) {
            this.stack = stack.copy();
            this.stack.setAmount(Fraction.ofWhole(127));
            if (this.stack.getType() == VanillaEntryTypes.ITEM)
                this.stack.setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE);
            else if (this.stack.getType() == VanillaEntryTypes.FLUID)
                this.stack.setting(EntryStack.Settings.Fluid.AMOUNT_TOOLTIP, CANCEL_FLUID_AMOUNT);
            this.hashIgnoreAmount = stack.hash(ComparisonContext.IGNORE_COUNT);
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
                    entry.setAmount(Fraction.ofWhole(button != 1 && !Screen.hasShiftDown() ? 1 : ((ItemStack) entry.getValue()).getMaxStackSize()));
                return ClientHelper.getInstance().tryCheatingEntry(entry);
            }
            
            return false;
        }
        
        @Override
        public int hashIgnoreAmount() {
            return hashIgnoreAmount;
        }
        
        @Override
        public FavoriteEntry copy() {
            return new EntryStackFavoriteEntry(stack.copy());
        }
        
        @Override
        public ResourceLocation getType() {
            return EntryStackFavoriteType.INSTANCE.id;
        }
        
        @Override
        public boolean isSame(FavoriteEntry other) {
            if (!(other instanceof EntryStackFavoriteEntry)) return false;
            EntryStackFavoriteEntry that = (EntryStackFavoriteEntry) other;
            return EntryStacks.equals(stack, that.stack, ComparisonContext.IGNORE_COUNT);
        }
    }
}
