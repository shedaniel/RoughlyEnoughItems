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

package me.shedaniel.rei.jeicompat;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.plugins.REIServerPlugin;
import me.shedaniel.rei.api.common.transfer.info.MenuInfo;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoProvider;
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry;
import me.shedaniel.rei.api.common.transfer.info.MenuSerializationContext;
import me.shedaniel.rei.forge.REIPlugin;
import me.shedaniel.rei.jeicompat.transfer.JEIRecipeTransferData;
import me.shedaniel.rei.jeicompat.transfer.JEITransferMenuInfo;
import mezz.jei.api.JeiPlugin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@REIPlugin(Dist.DEDICATED_SERVER)
public class JEIExtraPlugin implements REIServerPlugin {
    @Override
    public void registerMenuInfo(MenuInfoRegistry registry) {
        if (Platform.getEnvironment() == Env.SERVER) {
            registry.registerGeneric(id -> true, new MenuInfoProvider<AbstractContainerMenu, Display>() {
                @Override
                @OnlyIn(Dist.CLIENT)
                public Optional<MenuInfo<AbstractContainerMenu, Display>> provideClient(Display display, MenuSerializationContext<AbstractContainerMenu, ?, Display> context, AbstractContainerMenu menu) {
                    throw new UnsupportedOperationException();
                }
                
                @Override
                public Optional<MenuInfo<AbstractContainerMenu, Display>> provide(CategoryIdentifier<Display> category, AbstractContainerMenu menu, MenuSerializationContext<AbstractContainerMenu, ?, Display> context, CompoundTag networkTag) {
                    Display display = read(category, menu, context, networkTag);
                    if (display == null) return Optional.empty();
                    JEIRecipeTransferData<AbstractContainerMenu, Display> data = JEIRecipeTransferData.read(context.getMenu(), networkTag.getCompound(JEITransferMenuInfo.KEY));
                    return Optional.of(new JEITransferMenuInfo<>(display, data));
                }
            });
        }
    }
    
    private static <D extends Display, T extends AbstractContainerMenu> D read(CategoryIdentifier<D> category, T menu, MenuSerializationContext<T, ?, D> context, CompoundTag networkTag) {
        if (DisplaySerializerRegistry.getInstance().hasSerializer(category)) {
            return DisplaySerializerRegistry.getInstance().read(category, networkTag);
        } else {
            return null;
        }
    }
}
