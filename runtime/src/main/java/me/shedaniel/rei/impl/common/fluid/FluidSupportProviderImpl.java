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

package me.shedaniel.rei.impl.common.fluid;

import com.google.common.collect.Lists;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.REIPlugin;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FluidSupportProviderImpl implements FluidSupportProvider {
    private final List<Provider> providers = Lists.newCopyOnWriteArrayList();
    
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
    public Optional<Stream<EntryStack<FluidStack>>> itemToFluids(EntryStack<? extends ItemStack> itemStack) {
        if (itemStack.isEmpty()) return Optional.empty();
        for (Provider provider : providers) {
            InteractionResultHolder<@Nullable Stream<EntryStack<FluidStack>>> resultHolder = Objects.requireNonNull(provider.itemToFluid(itemStack));
            Stream<EntryStack<FluidStack>> stream = resultHolder.getObject();
            if (stream != null) {
                if (resultHolder.getResult().consumesAction()) {
                    return Optional.of(stream);
                } else if (resultHolder.getResult() == InteractionResult.FAIL) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }
}
