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

package me.shedaniel.rei.plugin.common.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Optional;

public class DefaultPluginImpl {
    public static Fluid getFluidFromBucket(BucketItem item) {
        Field field = getContentField();
        if (field == null) return null;
        try {
            return (Fluid) field.get(item);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Optional<Field> field = null;
    
    @NotNull
    private static Field getContentField() {
        if (field == null) {
            try {
                Field field = BucketItem.class.getDeclaredField(FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", "net.minecraft.class_1755", "field_7905", "Lnet/minecraft/class_3611;"));
                field.setAccessible(true);
                DefaultPluginImpl.field = Optional.of(field);
                return field;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                DefaultPluginImpl.field = Optional.empty();
                return null;
            }
        }
        
        return field.orElse(null);
    }
}
