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

package me.shedaniel.rei.plugin.common.displays.anvil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

/**
 * Holds the body of {@link DefaultAnvilDisplay#calculateOutput} so that the client-only
 * {@code Minecraft.getInstance().player} reference stays out of {@link DefaultAnvilDisplay}'s
 * own bytecode. Dedicated servers have no client classes at all: a client reference anywhere
 * in that class would fail class verification on the server, and
 * {@code DefaultPlugin#registerDisplaySerializer} reads its serializer on both sides - so the
 * anvil display serializer would never be registered server-side.
 *
 * This class is only loaded when {@code calculateOutput} actually runs, which only happens
 * on the client.
 */
@ApiStatus.Internal
public final class DefaultAnvilDisplayClient {
    private DefaultAnvilDisplayClient() {
    }

    @Environment(EnvType.CLIENT)
    public static Optional<Pair<ItemStack, Integer>> calculateOutput(ItemStack left, ItemStack right) {
        try {
            if (Minecraft.getInstance().player == null) return Optional.empty();
            AnvilMenu menu = new AnvilMenu(0, new Inventory(Minecraft.getInstance().player, new EntityEquipment()));
            menu.setItem(0, menu.incrementStateId(), left);
            menu.setItem(1, menu.incrementStateId(), right);
            ItemStack output = menu.getSlot(2).getItem().copy();
            if (!output.isEmpty()) {
                return Optional.of(Pair.of(output, menu.getCost()));
            } else {
                return Optional.empty();
            }
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }
}
