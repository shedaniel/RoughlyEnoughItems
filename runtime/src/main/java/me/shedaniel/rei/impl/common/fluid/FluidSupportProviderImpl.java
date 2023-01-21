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

package me.shedaniel.rei.impl.common.fluid;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FluidSupportProviderImpl extends ForwardingList<FluidSupportProvider.Provider> implements FluidSupportProvider {
    private final List<Provider> providers = Lists.newCopyOnWriteArrayList();
    private final List<Provider> immutable = Collections.unmodifiableList(providers);
    
    @Override
    public ReloadStage getStage() {
        return ReloadStage.START;
    }
    
    @Override
    public void acceptPlugin(REIPlugin<?> plugin) {
        plugin.registerFluidSupport(this);
    }
    
    @Override
    public void startReload() {
        providers.clear();
    }
    
    @Override
    public void register(Provider provider) {
        providers.add(Objects.requireNonNull(provider, "Registered provider is null!"));
    }
    
    @Override
    public Optional<Stream<EntryStack<FluidStack>>> itemToFluids(EntryStack<? extends ItemStack> stack) {
        if (stack.isEmpty()) return Optional.empty();
        for (Provider provider : providers) {
            CompoundEventResult<@Nullable Stream<EntryStack<FluidStack>>> resultHolder = Objects.requireNonNull(provider.itemToFluid(stack));
            if (resultHolder.interruptsFurtherEvaluation()) {
                if (resultHolder.isTrue()) {
                    return Optional.of(resultHolder.object());
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    protected List<Provider> delegate() {
        return immutable;
    }
}
