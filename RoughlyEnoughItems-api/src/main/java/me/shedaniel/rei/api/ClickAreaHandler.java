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

package me.shedaniel.rei.api;

import me.shedaniel.math.Point;
import me.shedaniel.rei.impl.Internals;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.stream.Stream;

@FunctionalInterface
public interface ClickAreaHandler<T extends Screen> {
    Result handle(ClickAreaContext<T> context);
    
    @ApiStatus.NonExtendable
    interface ClickAreaContext<T extends Screen> {
        T getScreen();
        
        Point getMousePosition();
    }
    
    @ApiStatus.NonExtendable
    interface Result {
        static Result success() {
            return Internals.createClickAreaHandlerResult(true);
        }
        
        static Result fail() {
            return Internals.createClickAreaHandlerResult(false);
        }
        
        Result category(ResourceLocation category);
        
        default Result categories(Iterable<ResourceLocation> categories) {
            for (ResourceLocation category : categories) {
                category(category);
            }
            return this;
        }
        
        boolean isSuccessful();
        
        Stream<ResourceLocation> getCategories();
    }
}
