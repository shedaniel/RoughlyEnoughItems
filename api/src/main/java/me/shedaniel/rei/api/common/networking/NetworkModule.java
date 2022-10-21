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

package me.shedaniel.rei.api.common.networking;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public interface NetworkModule<T> {
    NetworkModuleKey<Unit> DELETE_ITEM = new NetworkModuleKey<>() {};
    NetworkModuleKey<ItemStack> CHEAT_GIVE = new NetworkModuleKey<>() {};
    NetworkModuleKey<Map.Entry<ItemStack, Integer>> CHEAT_HOTBAR = new NetworkModuleKey<>() {};
    NetworkModuleKey<ItemStack> CHEAT_GRAB = new NetworkModuleKey<>() {};
    NetworkModuleKey<Map.Entry<ItemStack, String>> CHEAT_STATUS_REPLY = new NetworkModuleKey<>() {};
    NetworkModuleKey<List<List<ItemStack>>> NOT_ENOUGH_ITEMS = new NetworkModuleKey<>() {};
    NetworkModuleKey<TransferData> TRANSFER = new NetworkModuleKey<>() {};
    
    NetworkModuleKey<T> getKey();
    
    boolean canUse(Object target);
    
    void onInitialize();
    
    void send(Object target, T data);
    
    record TransferData(CategoryIdentifier<?> categoryIdentifier, boolean stacked, CompoundTag displayTag) {}
}
