/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.impl.client.forge;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ErrorDisplayerImpl {
    private static final List<UnaryOperator<Screen>> CONSUMERS = new ArrayList<>();
    
    static {
        MinecraftForge.EVENT_BUS.addListener(ErrorDisplayerImpl::onGuiOpen);
    }
    
    public static void registerGuiInit(UnaryOperator<Screen> consumer) {
        CONSUMERS.add(consumer);
    }
    
    public static void onGuiOpen(GuiOpenEvent event) {
        for (UnaryOperator<Screen> consumer : CONSUMERS) {
            Screen screen = consumer.apply(event.getGui());
            if (screen != null) {
                event.setGui(screen);
            }
        }
    }
}
