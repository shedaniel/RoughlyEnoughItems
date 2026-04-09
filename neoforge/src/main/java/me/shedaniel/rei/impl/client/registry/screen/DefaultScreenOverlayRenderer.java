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

package me.shedaniel.rei.impl.client.registry.screen;

import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.registry.screen.OverlayRendererProvider;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.resetFocused;
import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.shouldReturn;

public enum DefaultScreenOverlayRenderer implements OverlayRendererProvider {
    INSTANCE;

    @Nullable
    private Sink sink;
    private int rendered;

    DefaultScreenOverlayRenderer() {
        NeoForge.EVENT_BUS.addListener(this::onScreenRenderPre);
        NeoForge.EVENT_BUS.addListener(this::onScreenRenderBackground);
        NeoForge.EVENT_BUS.addListener(this::onContainerScreenRenderForeground);
        NeoForge.EVENT_BUS.addListener(this::onScreenRenderPost);
    }

    private void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!shouldReturn(event.getScreen())) {
            rendered = 0;
        }
    }

    private void onScreenRenderBackground(ScreenEvent.Render.Background event) {
        Screen screen = event.getScreen();
        Sink sink = this.sink;
        if (sink == null || shouldReturn(screen) || !(screen instanceof AbstractContainerScreen) || screen instanceof DisplayScreen) {
            return;
        }

        rendered = 1;
        resetFocused(screen);
        sink.render(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
        resetFocused(screen);
    }

    private void onContainerScreenRenderForeground(ContainerScreenEvent.Render.Foreground event) {
        Screen screen = event.getContainerScreen();
        if (!shouldReturn(screen)) {
            rendered = 2;
            resetFocused(screen);
        }
    }

    private void onScreenRenderPost(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        Sink sink = this.sink;
        if (sink == null || shouldReturn(screen)) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();
        float delta = event.getPartialTick();

        if (screen instanceof AbstractContainerScreen && rendered < 2) {
            InternalLogger.getInstance().warn("Screen " + screen.getClass().getName() + " did not render background and foreground! This might cause rendering issues!");
        }

        resetFocused(screen);
        if (rendered == 0 && !(screen instanceof DisplayScreen) && (!(screen instanceof AbstractContainerScreen) || rendered < 2)) {
            sink.render(graphics, mouseX, mouseY, delta);
        }
        rendered = 1;
        sink.lateRender(graphics, mouseX, mouseY, delta);
        resetFocused(screen);
    }

    @Override
    public void onApplied(Sink sink) {
        this.sink = sink;
    }

    @Override
    public void onRemoved() {
        this.sink = null;
    }
}
