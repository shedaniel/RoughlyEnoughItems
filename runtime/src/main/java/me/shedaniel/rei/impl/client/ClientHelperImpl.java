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

package me.shedaniel.rei.impl.client;

import io.netty.buffer.Unpooled;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.platform.Platform;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.client.view.Views;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.ClientInternals;
import me.shedaniel.rei.impl.client.gui.screen.CompositeDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.UncertainDisplayViewingScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientHelperImpl implements ClientHelper {
    @ApiStatus.Internal
    public final LazyLoadedValue<Boolean> isYog = new LazyLoadedValue<>(() -> {
        try {
            if (Minecraft.getInstance().getUser().getGameProfile().getId().equals(UUID.fromString("f9546389-9415-4358-9c29-2c26b25bff5b")))
                return true;
        } catch (Throwable ignored) {
        }
        return false;
    });
    @ApiStatus.Internal
    public final LazyLoadedValue<Boolean> isAprilFools = new LazyLoadedValue<>(() -> {
        try {
            LocalDateTime now = LocalDateTime.now();
            return now.getMonthValue() == 4 && now.getDayOfMonth() == 1;
        } catch (Throwable ignored) {
        }
        return false;
    });
    private final Map<String, String> modNameCache = new HashMap<String, String>() {{
        put("minecraft", "Minecraft");
        put("c", "Global");
        put("global", "Global");
    }};
    
    /**
     * @return the instance of {@link ClientHelperImpl}
     * @see ClientHelper#getInstance()
     */
    @ApiStatus.Internal
    public static ClientHelperImpl getInstance() {
        return (ClientHelperImpl) ClientHelper.getInstance();
    }
    
    public  boolean hasPermissionToUsePackets() {
        try {
            Minecraft.getInstance().getConnection().getSuggestionsProvider().hasPermission(0);
            return hasOperatorPermission() && canUsePackets();
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public boolean hasOperatorPermission() {
        try {
            return Minecraft.getInstance().getConnection().getSuggestionsProvider().hasPermission(1);
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public boolean canUsePackets() {
        return NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET) && NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET) && NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET);
    }
    
    public boolean canDeleteItems() {
        return hasPermissionToUsePackets() || Minecraft.getInstance().gameMode.hasInfiniteItems();
    }
    
    @Override
    public String getModFromModId(String modId) {
        if (modId == null)
            return "";
        String any = modNameCache.getOrDefault(modId, null);
        if (any != null)
            return any;
        if (Platform.isModLoaded(modId)) {
            String modName = Platform.getMod(modId).getName();
            modNameCache.put(modId, modName);
            return modName;
        }
        return modId;
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
            Minecraft.getInstance().player.inventory.setCarried(ItemStack.EMPTY);
            ((CreativeModeInventoryScreen) Minecraft.getInstance().screen).isQuickCrafting = false;
            return;
        }
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new FriendlyByteBuf(Unpooled.buffer()));
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
        if (Minecraft.getInstance().player.inventory == null) return false;
        ItemStack cheatedStack = entry.getValue().copy();
        if (ConfigObject.getInstance().isGrabbingItems() && Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            Inventory inventory = Minecraft.getInstance().player.inventory;
            EntryStack<ItemStack> stack = entry.copy();
            if (!inventory.getCarried().isEmpty() && EntryStacks.equalsExact(EntryStacks.of(inventory.getCarried()), stack)) {
                stack.getValue().setCount(Mth.clamp(stack.getValue().getCount() + inventory.getCarried().getCount(), 1, stack.getValue().getMaxStackSize()));
            } else if (!inventory.getCarried().isEmpty()) {
                return false;
            }
            inventory.setCarried(stack.getValue().copy());
            return true;
        } else if (ClientHelperImpl.getInstance().canUsePackets()) {
            Inventory inventory = Minecraft.getInstance().player.inventory;
            EntryStack<ItemStack> stack = entry.copy();
            if (!inventory.getCarried().isEmpty() && !EntryStacks.equalsExact(EntryStacks.of(inventory.getCarried()), stack)) {
                return false;
            }
            try {
                NetworkManager.sendToServer(ConfigObject.getInstance().isGrabbingItems() ? RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET : RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(cheatedStack));
                return true;
            } catch (Exception exception) {
                return false;
            }
        } else {
            ResourceLocation identifier = entry.getIdentifier();
            if (identifier == null) {
                return false;
            }
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
        return Minecraft.getInstance().player.inventory.compartments.stream()
                .flatMap(Collection::stream)
                .map(EntryStacks::of)
                .collect(Collectors.toSet());
    }
    
    @ApiStatus.Internal
    public void openRecipeViewingScreen(Map<DisplayCategory<?>, List<Display>> map, @Nullable CategoryIdentifier<?> category, @Nullable EntryStack<?> ingredientNotice, @Nullable EntryStack<?> resultNotice) {
        openView(new LegacyWrapperViewSearchBuilder(map).setPreferredOpenedCategory(category).setInputNotice(ingredientNotice).setOutputNotice(resultNotice).fillPreferredOpenedCategory());
    }
    
    @Override
    public boolean openView(ViewSearchBuilder builder) {
        Map<DisplayCategory<?>, List<Display>> map = builder.buildMap();
        if (map.isEmpty()) return false;
        Screen screen;
        if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.COMPOSITE) {
            screen = new CompositeDisplayViewingScreen(map, builder.getPreferredOpenedCategory());
        } else if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.UNSET) {
            screen = new UncertainDisplayViewingScreen(REIHelper.getInstance().getPreviousScreen(), DisplayScreenType.UNSET, true, original -> {
                ConfigObject.getInstance().setRecipeScreenType(original ? DisplayScreenType.ORIGINAL : DisplayScreenType.COMPOSITE);
                ConfigManager.getInstance().saveConfig();
                openView(builder);
            });
        } else {
            screen = new DefaultDisplayViewingScreen(map, builder.getPreferredOpenedCategory());
        }
        if (screen instanceof DisplayScreen) {
            if (builder.getInputNotice() != null) {
                ((DisplayScreen) screen).setIngredientStackToNotice(builder.getInputNotice());
            }
            if (builder.getOutputNotice() != null) {
                ((DisplayScreen) screen).setResultStackToNotice(builder.getOutputNotice());
            }
        }
        if (Minecraft.getInstance().screen instanceof DisplayScreen) {
            REIHelperImpl.getInstance().storeDisplayScreen((DisplayScreen) Minecraft.getInstance().screen);
        }
        Minecraft.getInstance().setScreen(screen);
        return true;
    }
    
    @Override
    public boolean canUseMovePackets() {
        return NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    public void onInitializeClient() {
        ClientInternals.attachInstance(this, ClientHelper.class);
        ClientInternals.attachInstance((Supplier<ViewSearchBuilder>) ViewSearchBuilderImpl::new, "viewSearchBuilder");
    }
    
    private static abstract class AbstractViewSearchBuilder implements ViewSearchBuilder {
        @Override
        public ViewSearchBuilder fillPreferredOpenedCategory() {
            if (getPreferredOpenedCategory() == null) {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof DisplayScreen) {
                    setPreferredOpenedCategory(((DisplayScreen) currentScreen).getCurrentCategoryId());
                }
            }
            return this;
        }
    }
    
    public static final class ViewSearchBuilderImpl extends AbstractViewSearchBuilder {
        private final Set<CategoryIdentifier<?>> categories = new HashSet<>();
        private final List<EntryStack<?>> recipesFor = new ArrayList<>();
        private final List<EntryStack<?>> usagesFor = new ArrayList<>();
        @Nullable private CategoryIdentifier<?> preferredOpenedCategory = null;
        @Nullable private EntryStack<?> inputNotice;
        @Nullable private EntryStack<?> outputNotice;
        private final LazyLoadedValue<Map<DisplayCategory<?>, List<Display>>> map = new LazyLoadedValue<>(() -> Views.getInstance().buildMapFor(this));
        
        @Override
        public ViewSearchBuilder addCategory(CategoryIdentifier<?> category) {
            this.categories.add(category);
            return this;
        }
        
        @Override
        public ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories) {
            this.categories.addAll(categories);
            return this;
        }
        
        @Override
        public Set<CategoryIdentifier<?>> getCategories() {
            return categories;
        }
        
        @Override
        public <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack) {
            this.recipesFor.add(stack);
            return this;
        }
        
        @Override
        public List<EntryStack<?>> getRecipesFor() {
            return recipesFor;
        }
        
        @Override
        public <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack) {
            this.usagesFor.add(stack);
            return this;
        }
        
        @Override
        public List<EntryStack<?>> getUsagesFor() {
            return usagesFor;
        }
        
        @Override
        public ViewSearchBuilder setPreferredOpenedCategory(@Nullable CategoryIdentifier<?> category) {
            this.preferredOpenedCategory = category;
            return this;
        }
        
        @Override
        @Nullable
        public CategoryIdentifier<?> getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public <T> ViewSearchBuilder setInputNotice(@Nullable EntryStack<T> stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public <T> ViewSearchBuilder setOutputNotice(@Nullable EntryStack<T> stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getOutputNotice() {
            return outputNotice;
        }
        
        @Override
        public Map<DisplayCategory<?>, List<Display>> buildMap() {
            return this.map.get();
        }
    }
    
    public static final class LegacyWrapperViewSearchBuilder extends AbstractViewSearchBuilder {
        private final Map<DisplayCategory<?>, List<Display>> map;
        @Nullable private CategoryIdentifier<?> preferredOpenedCategory = null;
        @Nullable private EntryStack<?> inputNotice;
        @Nullable private EntryStack<?> outputNotice;
        
        public LegacyWrapperViewSearchBuilder(Map<DisplayCategory<?>, List<Display>> map) {
            this.map = map;
        }
        
        @Override
        public ViewSearchBuilder addCategory(CategoryIdentifier<?> category) {
            return this;
        }
        
        @Override
        public ViewSearchBuilder addCategories(Collection<CategoryIdentifier<?>> categories) {
            return this;
        }
        
        @Override
        public Set<CategoryIdentifier<?>> getCategories() {
            return Collections.emptySet();
        }
        
        @Override
        public <T> ViewSearchBuilder addRecipesFor(EntryStack<T> stack) {
            return this;
        }
        
        @Override
        public List<EntryStack<?>> getRecipesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public <T> ViewSearchBuilder addUsagesFor(EntryStack<T> stack) {
            return this;
        }
        
        @Override
        public List<EntryStack<?>> getUsagesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public ViewSearchBuilder setPreferredOpenedCategory(@Nullable CategoryIdentifier<?> category) {
            this.preferredOpenedCategory = category;
            return this;
        }
    
        @Override
        @Nullable
        public CategoryIdentifier<?> getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public <T> ViewSearchBuilder setInputNotice(@Nullable EntryStack<T> stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public <T> ViewSearchBuilder setOutputNotice(@Nullable EntryStack<T> stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack<?> getOutputNotice() {
            return outputNotice;
        }
        
        @Override
        public Map<DisplayCategory<?>, List<Display>> buildMap() {
            return this.map;
        }
    }
}
