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

package me.shedaniel.rei.plugin.common.runtime;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.stack.PlayerInventorySlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor;
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessorRegistry;
import me.shedaniel.rei.api.common.transfer.info.stack.VanillaSlotAccessor;
import me.shedaniel.rei.plugin.client.entry.FluidEntryDefinition;
import me.shedaniel.rei.plugin.client.entry.ItemEntryDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Internal
public class DefaultRuntimePlugin implements REIServerPlugin {
    public static final ResourceLocation PLUGIN = new ResourceLocation("roughlyenoughitems", "default_runtime_plugin");
    
    @Override
    public void registerEntryTypes(EntryTypeRegistry registry) {
        registry.register(VanillaEntryTypes.ITEM, new ItemEntryDefinition());
        registry.register(VanillaEntryTypes.FLUID, new FluidEntryDefinition());
        
        registry.registerBridge(VanillaEntryTypes.ITEM, VanillaEntryTypes.FLUID, input -> {
            Optional<Stream<EntryStack<FluidStack>>> stream = FluidSupportProvider.getInstance().itemToFluids(input);
            if (!stream.isPresent()) {
                return CompoundEventResult.pass();
            }
            return CompoundEventResult.interruptTrue(stream.get());
        });
    }
    
    @Override
    public void registerSlotAccessors(SlotAccessorRegistry registry) {
        registry.register(new ResourceLocation("roughlyenoughitems", "vanilla"),
                slotAccessor -> slotAccessor instanceof VanillaSlotAccessor,
                new SlotAccessorRegistry.Serializer() {
                    @Override
                    public SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag) {
                        int slot = tag.getInt("Slot");
                        return new VanillaSlotAccessor(menu.slots.get(slot));
                    }
                    
                    @Override
                    @Nullable
                    public CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor) {
                        if (!(accessor instanceof VanillaSlotAccessor)) {
                            throw new IllegalArgumentException("Cannot save non-vanilla slot accessor!");
                        }
                        CompoundTag tag = new CompoundTag();
                        tag.putInt("Slot", ((VanillaSlotAccessor) accessor).getSlot().index);
                        return tag;
                    }
                });
        registry.register(new ResourceLocation("roughlyenoughitems", "player"),
                slotAccessor -> slotAccessor instanceof PlayerInventorySlotAccessor,
                new SlotAccessorRegistry.Serializer() {
                    @Override
                    public SlotAccessor read(AbstractContainerMenu menu, Player player, CompoundTag tag) {
                        int slot = tag.getInt("Slot");
                        return new PlayerInventorySlotAccessor(player, slot);
                    }
                    
                    @Override
                    @Nullable
                    public CompoundTag save(AbstractContainerMenu menu, Player player, SlotAccessor accessor) {
                        if (!(accessor instanceof PlayerInventorySlotAccessor)) {
                            throw new IllegalArgumentException("Cannot save non-player slot accessor!");
                        }
                        CompoundTag tag = new CompoundTag();
                        tag.putInt("Slot", ((PlayerInventorySlotAccessor) accessor).getIndex());
                        return tag;
                    }
                });
    }
}
