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

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.entry.EntryStacks;
import me.shedaniel.rei.api.entry.VanillaEntryTypes;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.shedaniel.rei.impl.Internals.attachInstance;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientHelperImpl implements ClientHelper, ClientModInitializer {
    private static ClientHelperImpl instance;
    @ApiStatus.Internal public final LazyLoadedValue<Boolean> isYog = new LazyLoadedValue<>(() -> {
        try {
            if (Minecraft.getInstance().getUser().getGameProfile().getId().equals(UUID.fromString("f9546389-9415-4358-9c29-2c26b25bff5b")))
                return true;
        } catch (Throwable ignored) {
        }
        return false;
    });
    @ApiStatus.Internal public final LazyLoadedValue<Boolean> isAprilFools = new LazyLoadedValue<>(() -> {
        try {
            LocalDateTime now = LocalDateTime.now();
            return now.getMonthValue() == 4 && now.getDayOfMonth() == 1;
        } catch (Throwable ignored) {
        }
        return false;
    });
    private final Map<String, String> modNameCache = Maps.newHashMap();
    
    /**
     * @return the instance of {@link ClientHelperImpl}
     * @see ClientHelper#getInstance()
     */
    @ApiStatus.Internal
    public static ClientHelperImpl getInstance() {
        return instance;
    }
    
    @Override
    public String getModFromModId(String modId) {
        if (modId == null)
            return "";
        String any = modNameCache.getOrDefault(modId, null);
        if (any != null)
            return any;
        String s = FabricLoader.getInstance().getModContainer(modId).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modId);
        modNameCache.put(modId, s);
        return s;
    }
    
    @Override
    public boolean isCheating() {
        return ConfigObject.getInstance().isCheating();
    }
    
    @Override
    public void setCheating(boolean cheating) {
        ConfigObject.getInstance().setCheating(cheating);
        ConfigManager.getInstance().saveConfig();
    }
    
    @Override
    public void sendDeletePacket() {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            Minecraft.getInstance().player.getInventory().setCarried(ItemStack.EMPTY);
            ((CreativeModeInventoryScreen) Minecraft.getInstance().screen).isQuickCrafting = false;
            return;
        }
        ClientPlayNetworking.send(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new FriendlyByteBuf(Unpooled.buffer()));
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen) {
            ((AbstractContainerScreen<?>) Minecraft.getInstance().screen).isQuickCrafting = false;
        }
    }
    
    @Override
    public boolean tryCheatingEntry(EntryStack<?> e) {
        if (e.getType() != VanillaEntryTypes.ITEM)
            return false;
        EntryStack<ItemStack> entry = (EntryStack<ItemStack>) e;
        if (Minecraft.getInstance().player == null) return false;
        if (Minecraft.getInstance().player.getInventory() == null) return false;
        ItemStack cheatedStack = entry.getValue().copy();
        if (ConfigObject.getInstance().isGrabbingItems() && Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            Inventory inventory = Minecraft.getInstance().player.getInventory();
            EntryStack<ItemStack> stack = entry.copy();
            if (!inventory.getCarried().isEmpty() && EntryStacks.equalsIgnoreCount(EntryStacks.of(inventory.getCarried()), stack)) {
                stack.setAmount(Fraction.ofWhole(Mth.clamp(stack.getAmount().intValue() + inventory.getCarried().getCount(), 1, stack.getValue().getMaxStackSize())));
            } else if (!inventory.getCarried().isEmpty()) {
                return false;
            }
            inventory.setCarried(stack.getValue().copy());
            return true;
        } else if (RoughlyEnoughItemsCore.canUsePackets()) {
            Inventory inventory = Minecraft.getInstance().player.getInventory();
            EntryStack<ItemStack> stack = entry.copy();
            if (!inventory.getCarried().isEmpty() && !EntryStacks.equalsIgnoreCount(EntryStacks.of(inventory.getCarried()), stack)) {
                return false;
            }
            try {
                ClientPlayNetworking.send(ConfigObject.getInstance().isGrabbingItems() ? RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET : RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(cheatedStack));
                return true;
            } catch (Exception exception) {
                return false;
            }
        } else {
            ResourceLocation identifier = entry.getIdentifier().orElse(null);
            if (identifier == null)
                return false;
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().getAsString() : "";
            String og = cheatedStack.getCount() == 1 ? ConfigObject.getInstance().getGiveCommand().replaceAll(" \\{count}", "") : ConfigObject.getInstance().getGiveCommand();
            String madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", Minecraft.getInstance().player.getScoreboardName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("text.rei.too_long_nbt"), false);
            }
            Minecraft.getInstance().player.chat(madeUpCommand);
            return true;
        }
    }
    
    @ApiStatus.Internal
    public Set<EntryStack<?>> _getInventoryItemsTypes() {
        return Minecraft.getInstance().player.getInventory().compartments.stream()
                .flatMap(Collection::stream)
                .map(EntryStacks::of)
                .collect(Collectors.toSet());
    }
    
    @ApiStatus.Internal
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map, @Nullable ResourceLocation category, @Nullable EntryStack<?> ingredientNotice, @Nullable EntryStack<?> resultNotice) {
        openView(new LegacyWrapperViewSearchBuilder(map).setPreferredOpenedCategory(category).setInputNotice(ingredientNotice).setOutputNotice(resultNotice).fillPreferredOpenedCategory());
    }
    
    @Override
    public boolean openView(ClientHelper.ViewSearchBuilder builder) {
        Map<RecipeCategory<?>, List<RecipeDisplay>> map = builder.buildMap();
        if (map.isEmpty()) return false;
        Screen screen;
        if (ConfigObject.getInstance().getRecipeScreenType() == RecipeScreenType.VILLAGER) {
            screen = new VillagerRecipeViewingScreen(map, builder.getPreferredOpenedCategory());
        } else if (ConfigObject.getInstance().getRecipeScreenType() == RecipeScreenType.UNSET) {
            screen = new PreRecipeViewingScreen(REIHelper.getInstance().getPreviousContainerScreen(), RecipeScreenType.UNSET, true, original -> {
                ConfigObject.getInstance().setRecipeScreenType(original ? RecipeScreenType.ORIGINAL : RecipeScreenType.VILLAGER);
                ConfigManager.getInstance().saveConfig();
                openView(builder);
            });
        } else {
            screen = new RecipeViewingScreen(map, builder.getPreferredOpenedCategory());
        }
        if (screen instanceof RecipeScreen) {
            if (builder.getInputNotice() != null)
                ((RecipeScreen) screen).addIngredientStackToNotice(builder.getInputNotice());
            if (builder.getOutputNotice() != null)
                ((RecipeScreen) screen).addResultStackToNotice(builder.getOutputNotice());
        }
        if (Minecraft.getInstance().screen instanceof RecipeScreen)
            ScreenHelper.storeRecipeScreen((RecipeScreen) Minecraft.getInstance().screen);
        Minecraft.getInstance().setScreen(screen);
        return true;
    }
    
    @Override
    public boolean canUseMovePackets() {
        return ClientPlayNetworking.canSend(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    @Override
    public void onInitializeClient() {
        ClientHelperImpl.instance = this;
        attachInstance(instance, ClientHelper.class);
        attachInstance((Supplier<ClientHelper.ViewSearchBuilder>) ViewSearchBuilder::new, "viewSearchBuilder");
        modNameCache.put("minecraft", "Minecraft");
        modNameCache.put("c", "Global");
        modNameCache.put("global", "Global");
    }
    
    private static abstract class AbstractViewSearchBuilder implements ClientHelper.ViewSearchBuilder {
        @Override
        public ClientHelper.ViewSearchBuilder fillPreferredOpenedCategory() {
            if (getPreferredOpenedCategory() == null) {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof RecipeScreen) {
                    setPreferredOpenedCategory(((RecipeScreen) currentScreen).getCurrentCategory());
                }
            }
            return this;
        }
    }
    
    public static final class ViewSearchBuilder extends AbstractViewSearchBuilder {
        @NotNull private final Set<ResourceLocation> categories = new HashSet<>();
        @NotNull private final List<EntryStack<?>> recipesFor = new ArrayList<>();
        @NotNull private final List<EntryStack<?>> usagesFor = new ArrayList<>();
        @Nullable private ResourceLocation preferredOpenedCategory = null;
        @Nullable private EntryStack<?> inputNotice;
        @Nullable private EntryStack<?> outputNotice;
        @NotNull
        private final LazyLoadedValue<Map<RecipeCategory<?>, List<RecipeDisplay>>> map = new LazyLoadedValue<>(() -> RecipeHelper.getInstance().buildMapFor(this));
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategory(ResourceLocation category) {
            this.categories.add(category);
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategories(Collection<ResourceLocation> categories) {
            this.categories.addAll(categories);
            return this;
        }
        
        @Override
        @NotNull
        public Set<ResourceLocation> getCategories() {
            return categories;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addRecipesFor(EntryStack<?> stack) {
            this.recipesFor.add(stack);
            return this;
        }
        
        @Override
        @NotNull
        public List<EntryStack<?>> getRecipesFor() {
            return recipesFor;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addUsagesFor(EntryStack<?> stack) {
            this.usagesFor.add(stack);
            return this;
        }
        
        @Override
        @NotNull
        public List<EntryStack<?>> getUsagesFor() {
            return usagesFor;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setPreferredOpenedCategory(@Nullable ResourceLocation category) {
            this.preferredOpenedCategory = category;
            return this;
        }
        
        @Nullable
        @Override
        public ResourceLocation getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setInputNotice(@Nullable EntryStack<?> stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setOutputNotice(@Nullable EntryStack<?> stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getOutputNotice() {
            return outputNotice;
        }
        
        @NotNull
        @Override
        public Map<RecipeCategory<?>, List<RecipeDisplay>> buildMap() {
            return this.map.get();
        }
    }
    
    public static final class LegacyWrapperViewSearchBuilder extends AbstractViewSearchBuilder {
        @NotNull private final Map<RecipeCategory<?>, List<RecipeDisplay>> map;
        @Nullable private ResourceLocation preferredOpenedCategory = null;
        @Nullable private EntryStack<?> inputNotice;
        @Nullable private EntryStack<?> outputNotice;
        
        public LegacyWrapperViewSearchBuilder(@NotNull Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
            this.map = map;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategory(ResourceLocation category) {
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategories(Collection<ResourceLocation> categories) {
            return this;
        }
        
        @Override
        public @NotNull Set<ResourceLocation> getCategories() {
            return Collections.emptySet();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addRecipesFor(EntryStack<?> stack) {
            return this;
        }
        
        @Override
        public @NotNull List<EntryStack<?>> getRecipesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addUsagesFor(EntryStack<?> stack) {
            return this;
        }
        
        @Override
        public @NotNull List<EntryStack<?>> getUsagesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setPreferredOpenedCategory(@Nullable ResourceLocation category) {
            this.preferredOpenedCategory = category;
            return this;
        }
        
        @Nullable
        @Override
        public ResourceLocation getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setInputNotice(@Nullable EntryStack<?> stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setOutputNotice(@Nullable EntryStack<?> stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getOutputNotice() {
            return outputNotice;
        }
        
        @Override
        public @NotNull Map<RecipeCategory<?>, List<RecipeDisplay>> buildMap() {
            return this.map;
        }
    }
}
