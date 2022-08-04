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
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import me.shedaniel.rei.RoughlyEnoughItemsNetwork;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import me.shedaniel.rei.impl.client.gui.screen.CompositeDisplayViewingScreen;
import me.shedaniel.rei.impl.client.gui.screen.DefaultDisplayViewingScreen;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientHelperImpl implements ClientHelper {
    public final Supplier<Boolean> isAprilFools = Suppliers.memoize(() -> {
        try {
            LocalDateTime now = LocalDateTime.now();
            return now.getMonthValue() == 4 && now.getDayOfMonth() == 1;
        } catch (Throwable ignored) {
        }
        return false;
    });
    private final Map<String, String> modNameCache = new HashMap<>() {{
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
    
    public boolean hasPermissionToUsePackets() {
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
    
    public boolean canUseHotbarPackets() {
        return NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.CREATE_ITEMS_HOTBAR_PACKET);
    }
    
    public boolean canDeleteItems() {
        return hasPermissionToUsePackets() || Minecraft.getInstance().gameMode.hasInfiniteItems();
    }
    
    @Override
    public void appendModIdToTooltips(Tooltip components, String modId) {
        final String modName = ClientHelper.getInstance().getModFromModId(modId);
        Iterator<Tooltip.Entry> iterator = components.entries().iterator();
        while (iterator.hasNext()) {
            Tooltip.Entry entry = iterator.next();
            if (entry.isText() && FormattingUtils.stripFormatting(entry.getAsText().getString()).equalsIgnoreCase(modName)) {
                iterator.remove();
            }
        }
        components.add(ClientHelper.getInstance().getFormattedModFromModId(modId));
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
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen inventoryScreen) {
            Minecraft.getInstance().player.containerMenu.setCarried(ItemStack.EMPTY);
            inventoryScreen.isQuickCrafting = false;
            return;
        }
        NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.DELETE_ITEMS_PACKET, new FriendlyByteBuf(Unpooled.buffer()));
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> containerScreen) {
            containerScreen.isQuickCrafting = false;
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
            AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
            EntryStack<ItemStack> stack = entry.copy();
            if (!menu.getCarried().isEmpty() && EntryStacks.equalsExact(EntryStacks.of(menu.getCarried()), stack)) {
                stack.getValue().setCount(Mth.clamp(stack.getValue().getCount() + menu.getCarried().getCount(), 1, stack.getValue().getMaxStackSize()));
            } else if (!menu.getCarried().isEmpty()) {
                return false;
            }
            menu.setCarried(stack.getValue().copy());
            return true;
        } else if (ClientHelperImpl.getInstance().canUsePackets()) {
            AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
            EntryStack<ItemStack> stack = entry.copy();
            if (!menu.getCarried().isEmpty() && !EntryStacks.equalsExact(EntryStacks.of(menu.getCarried()), stack)) {
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
    
    @Override
    public boolean tryCheatingEntryTo(EntryStack<?> e, int hotbarSlotId) {
        if (e.getType() != VanillaEntryTypes.ITEM)
            return false;
        EntryStack<ItemStack> entry = (EntryStack<ItemStack>) e;
        if (Minecraft.getInstance().player == null) return false;
        if (Minecraft.getInstance().player.getInventory() == null) return false;
        if (Minecraft.getInstance().gameMode != null && Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
            EntryStack<ItemStack> stack = entry.copy();
            if (menu.getCarried().isEmpty()) {
                Minecraft.getInstance().player.getInventory().setItem(hotbarSlotId, stack.getValue().copy());
                Minecraft.getInstance().player.inventoryMenu.broadcastChanges();
                return true;
            }
        }
        if (ClientHelperImpl.getInstance().canUseHotbarPackets()) {
            AbstractContainerMenu menu = Minecraft.getInstance().player.containerMenu;
            EntryStack<ItemStack> stack = entry.copy();
            if (!menu.getCarried().isEmpty()) {
                return false;
            }
            try {
                NetworkManager.sendToServer(RoughlyEnoughItemsNetwork.CREATE_ITEMS_HOTBAR_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeItem(stack.getValue().copy()).writeVarInt(hotbarSlotId));
                return true;
            } catch (Exception exception) {
                return false;
            }
        } else return false;
    }
    
    @Override
    public boolean openView(ViewSearchBuilder builder) {
        return openView(builder.buildMapInternal(), builder.getPreferredOpenedCategory(), builder.getUsagesFor(), builder.getRecipesFor());
    }
    
    public boolean openView(Map<DisplayCategory<?>, List<DisplaySpec>> map, @Nullable CategoryIdentifier<?> category,
            List<EntryStack<?>> usagesFor, List<EntryStack<?>> recipesFor) {
        if (map.isEmpty()) return false;
        Screen screen;
        if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.COMPOSITE) {
            screen = new CompositeDisplayViewingScreen(map, category);
        } else if (ConfigObject.getInstance().getRecipeScreenType() == DisplayScreenType.UNSET) {
            ConfigObject.getInstance().setRecipeScreenType(DisplayScreenType.ORIGINAL);
            ConfigManager.getInstance().saveConfig();
            return openView(map, category, usagesFor, recipesFor);
//            screen = new UncertainDisplayViewingScreen(REIRuntime.getInstance().getPreviousScreen(), DisplayScreenType.UNSET, true, original -> {
//                ConfigObject.getInstance().setRecipeScreenType(original ? DisplayScreenType.ORIGINAL : DisplayScreenType.COMPOSITE);
//                ConfigManager.getInstance().saveConfig();
//                openView(builder);
//            });
        } else {
            screen = new DefaultDisplayViewingScreen(map, category);
        }
        if (screen instanceof DisplayScreen displayScreen) {
            for (EntryStack<?> stack : usagesFor) {
                displayScreen.addIngredientToNotice(stack);
            }
            for (EntryStack<?> stack : recipesFor) {
                displayScreen.addResultToNotice(stack);
            }
        }
        if (Minecraft.getInstance().screen instanceof DisplayScreen displayScreen) {
            REIRuntimeImpl.getInstance().storeDisplayScreen(displayScreen);
        }
        Minecraft.getInstance().setScreen(screen);
        return true;
    }
    
    @Override
    public boolean canUseMovePackets() {
        return NetworkManager.canServerReceive(RoughlyEnoughItemsNetwork.MOVE_ITEMS_PACKET);
    }
}
