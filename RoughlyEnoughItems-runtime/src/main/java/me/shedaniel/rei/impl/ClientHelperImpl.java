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
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.PreRecipeViewingScreen;
import me.shedaniel.rei.gui.RecipeScreen;
import me.shedaniel.rei.gui.RecipeViewingScreen;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientHelperImpl implements ClientHelper, ClientModInitializer {
    
    private static ClientHelperImpl instance;
    @ApiStatus.Internal public final Lazy<Boolean> isYog = new Lazy<>(() -> {
        try {
            if (MinecraftClient.getInstance().getSession().getProfile().getId().equals(UUID.fromString("f9546389-9415-4358-9c29-2c26b25bff5b")))
                return true;
        } catch (Throwable ignored) {
        }
        return false;
    });
    @ApiStatus.Internal public final Lazy<Boolean> isAprilFools = new Lazy<>(() -> {
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
    public Text getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.isEmpty())
            return NarratorManager.EMPTY;
        return new LiteralText(mod).formatted(Formatting.BLUE, Formatting.ITALIC);
    }
    
    @Override
    public Text getFormattedModFromIdentifier(Identifier identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.isEmpty())
            return NarratorManager.EMPTY;
        return new LiteralText(mod).formatted(Formatting.BLUE, Formatting.ITALIC);
    }
    
    @Override
    public String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(Registry.ITEM.getId(item));
    }
    
    @Override
    public String getModFromModId(String modid) {
        if (modid == null)
            return "";
        String any = modNameCache.getOrDefault(modid, null);
        if (any != null)
            return any;
        String s = FabricLoader.getInstance().getModContainer(modid).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(modid);
        modNameCache.put(modid, s);
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
        if (MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen) {
            MinecraftClient.getInstance().player.inventory.setCursorStack(ItemStack.EMPTY);
            ((CreativeInventoryScreen) MinecraftClient.getInstance().currentScreen).isCursorDragging = false;
            return;
        }
        ClientSidePacketRegistry.INSTANCE.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()));
        if (MinecraftClient.getInstance().currentScreen instanceof ContainerScreen) {
            ((ContainerScreen<?>) MinecraftClient.getInstance().currentScreen).isCursorDragging = false;
        }
    }
    
    @Override
    public boolean tryCheatingEntry(EntryStack entry) {
        if (entry.getType() != EntryStack.Type.ITEM)
            return false;
        if (MinecraftClient.getInstance().player == null) return false;
        if (MinecraftClient.getInstance().player.inventory == null) return false;
        ItemStack cheatedStack = entry.getItemStack().copy();
        if (ConfigObject.getInstance().isGrabbingItems() && MinecraftClient.getInstance().currentScreen instanceof CreativeInventoryScreen) {
            PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
            EntryStack stack = entry.copy();
            if (!inventory.getCursorStack().isEmpty() && EntryStack.create(inventory.getCursorStack()).equalsIgnoreAmount(stack)) {
                stack.setAmount(MathHelper.clamp(stack.getAmount() + inventory.getCursorStack().getCount(), 1, stack.getItemStack().getMaxCount()));
            } else if (!inventory.getCursorStack().isEmpty()) {
                return false;
            }
            inventory.setCursorStack(stack.getItemStack().copy());
            return true;
        } else if (RoughlyEnoughItemsCore.canUsePackets()) {
            PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
            EntryStack stack = entry.copy();
            if (!inventory.getCursorStack().isEmpty() && !EntryStack.create(inventory.getCursorStack()).equalsIgnoreAmount(stack)) {
                return false;
            }
            try {
                ClientSidePacketRegistry.INSTANCE.sendToServer(ConfigObject.getInstance().isGrabbingItems() ? RoughlyEnoughItemsNetwork.CREATE_ITEMS_GRAB_PACKET : RoughlyEnoughItemsNetwork.CREATE_ITEMS_PACKET, new PacketByteBuf(Unpooled.buffer()).writeItemStack(cheatedStack));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Identifier identifier = entry.getIdentifier().orElse(null);
            if (identifier == null)
                return false;
            String tagMessage = cheatedStack.copy().getTag() != null && !cheatedStack.copy().getTag().isEmpty() ? cheatedStack.copy().getTag().asString() : "";
            String og = cheatedStack.getCount() == 1 ? ConfigObject.getInstance().getGiveCommand().replaceAll(" \\{count}", "") : ConfigObject.getInstance().getGiveCommand();
            String madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", tagMessage).replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
            if (madeUpCommand.length() > 256) {
                madeUpCommand = og.replaceAll("\\{player_name}", MinecraftClient.getInstance().player.getEntityName()).replaceAll("\\{item_name}", identifier.getPath()).replaceAll("\\{item_identifier}", identifier.toString()).replaceAll("\\{nbt}", "").replaceAll("\\{count}", String.valueOf(cheatedStack.getCount()));
                MinecraftClient.getInstance().player.addMessage(new TranslatableText("text.rei.too_long_nbt"), false);
            }
            MinecraftClient.getInstance().player.sendChatMessage(madeUpCommand);
            return true;
        }
    }
    
    @Override
    public List<ItemStack> getInventoryItemsTypes() {
        List<ItemStack> inventoryStacks = new ArrayList<>(MinecraftClient.getInstance().player.inventory.main);
        inventoryStacks.addAll(MinecraftClient.getInstance().player.inventory.armor);
        inventoryStacks.addAll(MinecraftClient.getInstance().player.inventory.offHand);
        return inventoryStacks;
    }
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @Override
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
        openRecipeViewingScreen(map, null, null, null);
    }
    
    @ApiStatus.ScheduledForRemoval
    @Deprecated
    @ApiStatus.Internal
    public void openRecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> map, @Nullable Identifier category, @Nullable EntryStack ingredientNotice, @Nullable EntryStack resultNotice) {
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
        if (MinecraftClient.getInstance().currentScreen instanceof RecipeScreen)
            ScreenHelper.storeRecipeScreen((RecipeScreen) MinecraftClient.getInstance().currentScreen);
        MinecraftClient.getInstance().openScreen(screen);
        return true;
    }
    
    @Override
    public boolean canUseMovePackets() {
        return ClientSidePacketRegistry.INSTANCE.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
    
    @Override
    public void onInitializeClient() {
        ClientHelperImpl.instance = this;
        RoughlyEnoughItemsCore.attachInstance(instance, ClientHelper.class);
        RoughlyEnoughItemsCore.attachInstance((Supplier<ClientHelper.ViewSearchBuilder>) ViewSearchBuilder::new, "viewSearchBuilder");
        modNameCache.put("minecraft", "Minecraft");
        modNameCache.put("c", "Global");
        modNameCache.put("global", "Global");
    }
    
    public static final class ViewSearchBuilder implements ClientHelper.ViewSearchBuilder {
        @NotNull private final Set<Identifier> categories = new HashSet<>();
        @NotNull private final List<EntryStack> recipesFor = new ArrayList<>();
        @NotNull private final List<EntryStack> usagesFor = new ArrayList<>();
        @Nullable private Identifier preferredOpenedCategory = null;
        @Nullable private EntryStack inputNotice;
        @Nullable private EntryStack outputNotice;
        @NotNull private final Lazy<Map<RecipeCategory<?>, List<RecipeDisplay>>> map = new Lazy<>(() -> RecipeHelper.getInstance().buildMapFor(this));
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategory(Identifier category) {
            this.categories.add(category);
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategories(Collection<Identifier> categories) {
            this.categories.addAll(categories);
            return this;
        }
        
        @Override
        @NotNull
        public Set<Identifier> getCategories() {
            return categories;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addRecipesFor(EntryStack stack) {
            this.recipesFor.add(stack);
            return this;
        }
        
        @Override
        @NotNull
        public List<EntryStack> getRecipesFor() {
            return recipesFor;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addUsagesFor(EntryStack stack) {
            this.usagesFor.add(stack);
            return this;
        }
        
        @Override
        @NotNull
        public List<EntryStack> getUsagesFor() {
            return usagesFor;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setPreferredOpenedCategory(@Nullable Identifier category) {
            this.preferredOpenedCategory = category;
            return this;
        }
        
        @Nullable
        @Override
        public Identifier getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder fillPreferredOpenedCategory() {
            if (getPreferredOpenedCategory() == null) {
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                if (currentScreen instanceof RecipeScreen) {
                    setPreferredOpenedCategory(((RecipeScreen) currentScreen).getCurrentCategory());
                }
            }
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setInputNotice(@Nullable EntryStack stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setOutputNotice(@Nullable EntryStack stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack getOutputNotice() {
            return outputNotice;
        }
        
        @NotNull
        @Override
        public Map<RecipeCategory<?>, List<RecipeDisplay>> buildMap() {
            return this.map.get();
        }
    }
    
    public static final class LegacyWrapperViewSearchBuilder implements ClientHelper.ViewSearchBuilder {
        @NotNull private final Map<RecipeCategory<?>, List<RecipeDisplay>> map;
        @Nullable private Identifier preferredOpenedCategory = null;
        @Nullable private EntryStack inputNotice;
        @Nullable private EntryStack outputNotice;
        
        public LegacyWrapperViewSearchBuilder(@NotNull Map<RecipeCategory<?>, List<RecipeDisplay>> map) {
            this.map = map;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategory(Identifier category) {
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addCategories(Collection<Identifier> categories) {
            return this;
        }
        
        @Override
        public @NotNull Set<Identifier> getCategories() {
            return Collections.emptySet();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addRecipesFor(EntryStack stack) {
            return this;
        }
        
        @Override
        public @NotNull List<EntryStack> getRecipesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder addUsagesFor(EntryStack stack) {
            return this;
        }
        
        @Override
        public @NotNull List<EntryStack> getUsagesFor() {
            return Collections.emptyList();
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setPreferredOpenedCategory(@Nullable Identifier category) {
            this.preferredOpenedCategory = category;
            return this;
        }
        
        @Nullable
        @Override
        public Identifier getPreferredOpenedCategory() {
            return this.preferredOpenedCategory;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder fillPreferredOpenedCategory() {
            if (getPreferredOpenedCategory() == null) {
                Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                if (currentScreen instanceof RecipeScreen) {
                    setPreferredOpenedCategory(((RecipeScreen) currentScreen).getCurrentCategory());
                }
            }
            return this;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setInputNotice(@Nullable EntryStack stack) {
            this.inputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack getInputNotice() {
            return inputNotice;
        }
        
        @Override
        public ClientHelper.ViewSearchBuilder setOutputNotice(@Nullable EntryStack stack) {
            this.outputNotice = stack;
            return this;
        }
        
        @Nullable
        @Override
        public EntryStack getOutputNotice() {
            return outputNotice;
        }
        
        @Override
        public @NotNull Map<RecipeCategory<?>, List<RecipeDisplay>> buildMap() {
            return this.map;
        }
    }
}
