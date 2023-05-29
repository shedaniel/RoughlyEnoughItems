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

package me.shedaniel.rei.impl.client.gui.hints;

import me.shedaniel.math.Color;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface HintProvider {
    List<Component> provide();
    
    @Nullable
    Tooltip provideTooltip(Point mouse);
    
    @Nullable
    default Double getProgress() {
        return null;
    }
    
    Color getColor();
    
    List<HintButton> getButtons();
    
    final class HintButton {
        private final Component name;
        private final Consumer<Rectangle> action;
        
        public HintButton(Component name, Consumer<Rectangle> action) {
            this.name = name;
            this.action = action;
        }
        
        public Component name() {
            return name;
        }
        
        public Consumer<Rectangle> action() {
            return action;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            HintButton that = (HintButton) obj;
            return Objects.equals(this.name, that.name) &&
                    Objects.equals(this.action, that.action);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, action);
        }
        
        @Override
        public String toString() {
            return "HintButton[" +
                    "name=" + name + ", " +
                    "action=" + action + ']';
        }
    }
}
