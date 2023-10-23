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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.registry.screen.OverlayRendererProvider;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;

import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.resetFocused;
import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.shouldReturn;

public enum DefaultScreenOverlayRenderer implements OverlayRendererProvider {
    INSTANCE;
    
    @Nullable
    private ClientGuiEvent.ScreenRenderPre renderPre;
    @Nullable
    private ClientGuiEvent.ContainerScreenRenderBackground renderContainerBg;
    @Nullable
    private ClientGuiEvent.ContainerScreenRenderForeground renderContainerFg;
    @Nullable
    private ClientGuiEvent.ScreenRenderPost renderPost;
    
    {
        ClientGuiEvent.RENDER_PRE.register((screen, graphics, mouseX, mouseY, delta) -> {
            if (renderPre != null) {
                return renderPre.render(screen, graphics, mouseX, mouseY, delta);
            } else {
                return EventResult.pass();
            }
        });
        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register((screen, graphics, mouseX, mouseY, delta) -> {
            if (renderContainerBg != null) {
                renderContainerBg.render(screen, graphics, mouseX, mouseY, delta);
            }
        });
        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register((screen, graphics, mouseX, mouseY, delta) -> {
            if (renderContainerFg != null) {
                renderContainerFg.render(screen, graphics, mouseX, mouseY, delta);
            }
        });
        ClientGuiEvent.RENDER_POST.register((screen, graphics, mouseX, mouseY, delta) -> {
            if (renderPost != null) {
                renderPost.render(screen, graphics, mouseX, mouseY, delta);
            }
        });
    }
    
    @Override
    public void onApplied(Sink sink) {
        int[] rendered = {0};
        this.renderPre = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            rendered[0] = 0;
            return EventResult.pass();
        };
        this.renderContainerBg = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            rendered[0] = 1;
            resetFocused(screen);
            if (!(screen instanceof DisplayScreen)) {
                sink.render(graphics, mouseX, mouseY, delta);
            }
            resetFocused(screen);
        };
        this.renderContainerFg = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            rendered[0] = 2;
            resetFocused(screen);
            graphics.pose().pushPose();
            graphics.pose().translate(-screen.leftPos, -screen.topPos, 0.0);
            sink.lateRender(graphics, mouseX, mouseY, delta);
            graphics.pose().popPose();
            resetFocused(screen);
        };
        this.renderPost = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen) || rendered[0] == 2)
                return;
            if (screen instanceof AbstractContainerScreen) {
                InternalLogger.getInstance().warn("Screen " + screen.getClass().getName() + " did not render background and foreground! This might cause rendering issues!");
            }
            resetFocused(screen);
            if (rendered[0] == 0 && !(screen instanceof DisplayScreen)) {
                sink.render(graphics, mouseX, mouseY, delta);
            }
            rendered[0] = 1;
            if (rendered[0] == 1) {
                sink.lateRender(graphics, mouseX, mouseY, delta);
            }
            resetFocused(screen);
        };
    }
    
    @Override
    public void onRemoved() {
        this.renderPre = null;
        this.renderContainerBg = null;
        this.renderContainerFg = null;
        this.renderPost = null;
    }
}
