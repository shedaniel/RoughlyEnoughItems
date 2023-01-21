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

package me.shedaniel.rei.api.client.registry.screen;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClickArea<T extends Screen> {
    Result handle(ClickAreaContext<T> context);
    
    @ApiStatus.NonExtendable
    interface ClickAreaContext<T extends Screen> {
        T getScreen();
        
        Point getMousePosition();
    }
    
    @ApiStatus.NonExtendable
    interface Result {
        static Result success() {
            return ClientInternals.createClickAreaHandlerResult(true);
        }
        
        static Result fail() {
            return ClientInternals.createClickAreaHandlerResult(false);
        }
        
        /**
         * Sets a custom execute function.
         * Returns {@code true} to indicate that the click area was executed.
         * Returns {@code false} to indicate that the click area was not executed, and
         * leaves REI to handle the click area.
         *
         * @param task the task to execute
         * @return this
         */
        @ApiStatus.Experimental
        Result executor(BooleanSupplier task);
        
        Result category(CategoryIdentifier<?> category);
        
        default Result categories(Iterable<? extends CategoryIdentifier<?>> categories) {
            for (CategoryIdentifier<?> category : categories) {
                category(category);
            }
            return this;
        }
        
        @ApiStatus.Experimental
        Result tooltip(Supplier<Component @Nullable []> tooltip);
        
        boolean isSuccessful();
        
        @ApiStatus.Experimental
        boolean execute();
        
        @ApiStatus.Experimental
        Component @Nullable [] getTooltips();
        
        Stream<CategoryIdentifier<?>> getCategories();
    }
}
