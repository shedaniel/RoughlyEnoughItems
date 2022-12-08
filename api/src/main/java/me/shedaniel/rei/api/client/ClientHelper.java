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

package me.shedaniel.rei.api.client;

import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.FormattingUtils;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

@Environment(EnvType.CLIENT)
public interface ClientHelper {
    /**
     * @return the instance of {@link ClientHelper}
     */
    static ClientHelper getInstance() {
        return ClientInternals.getClientHelper();
    }
    
    /**
     * Returns whether cheating is enabled
     *
     * @return whether cheating is enabled
     */
    boolean isCheating();
    
    /**
     * Sets current cheating mode
     * Automatically calls {@link ConfigManager#saveConfig()}.
     *
     * @param cheating the new cheating mode
     */
    void setCheating(boolean cheating);
    
    /**
     * Tries to cheat stack using either packets or commands.
     *
     * @param stack the stack to cheat in
     * @return whether it failed
     */
    boolean tryCheatingEntry(EntryStack<?> stack);
    
    /**
     * Tries to cheat stack into the given slot.
     *
     * @param stack        the stack to cheat in
     * @param hotbarSlotId the hotbar slot id
     * @return whether it failed
     */
    boolean tryCheatingEntryTo(EntryStack<?> stack, int hotbarSlotId);
    
    /**
     * Gets the mod from an item
     *
     * @param item the item to find
     * @return the mod name
     */
    default String getModFromItem(Item item) {
        if (item.equals(Items.AIR))
            return "";
        return getModFromIdentifier(BuiltInRegistries.ITEM.getKey(item));
    }
    
    /**
     * Tries to delete the player's cursor item
     */
    void sendDeletePacket();
    
    /**
     * Returns the formatted mod from an item
     *
     * @param item the item to find
     * @return the mod name with blue and italic formatting
     */
    default Component getFormattedModFromItem(Item item) {
        String mod = getModFromItem(item);
        if (mod.isEmpty())
            return Component.empty();
        return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }
    
    /**
     * Returns the formatted mod from an identifier
     *
     * @param identifier the identifier to find
     * @return the mod name with blue and italic formatting
     */
    default Component getFormattedModFromIdentifier(ResourceLocation identifier) {
        String mod = getModFromIdentifier(identifier);
        if (mod.isEmpty())
            return Component.empty();
        return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }
    
    /**
     * Returns the mod from a modid
     *
     * @param modId the modid of the mod
     * @return the mod name with blue and italic formatting
     */
    default Component getFormattedModFromModId(String modId) {
        String mod = getModFromModId(modId);
        if (mod.isEmpty())
            return Component.empty();
        return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
    }
    
    /**
     * Appends the formatted mod to the list of tooltip components.
     *
     * @param components the list of tooltip components
     * @param modId      the modid of the mod
     * @return the list of tooltip components
     */
    default List<Component> appendModIdToTooltips(List<Component> components, String modId) {
        final String modName = ClientHelper.getInstance().getModFromModId(modId);
        boolean alreadyHasMod = false;
        for (Component s : components)
            if (FormattingUtils.stripFormatting(s.getString()).equalsIgnoreCase(modName)) {
                alreadyHasMod = true;
                break;
            }
        if (!alreadyHasMod)
            components.add(ClientHelper.getInstance().getFormattedModFromModId(modId));
        return components;
    }
    
    /**
     * Appends the formatted mod to the tooltip.
     *
     * @param components the tooltip
     * @param modId      the modid of the mod
     */
    void appendModIdToTooltips(Tooltip components, String modId);
    
    /**
     * Returns the mod from an identifier
     *
     * @param identifier the identifier to find
     * @return the mod name
     */
    default String getModFromIdentifier(ResourceLocation identifier) {
        if (identifier == null)
            return "";
        return getModFromModId(identifier.getNamespace());
    }
    
    /**
     * Returns the mod from a modid
     *
     * @param modId the modid of the mod
     * @return the mod name
     */
    String getModFromModId(String modId);
    
    /**
     * Opens the view after the search is complete.
     *
     * @param builder the view search builder
     * @return whether the view was opened
     */
    boolean openView(ViewSearchBuilder builder);
    
    /**
     * Returns whether the client can use move items packets.
     *
     * @return whether the client can use move items packets
     */
    boolean canUseMovePackets();
}
