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

import java.util.ArrayList;
import java.util.List;

import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.resetFocused;
import static me.shedaniel.rei.RoughlyEnoughItemsCoreClient.shouldReturn;

public enum DefaultScreenOverlayRenderer implements OverlayRendererProvider {
    INSTANCE;
    
    private final List<Runnable> onRemoved = new ArrayList<>();
    
    @Override
    public void onApplied(Sink sink) {
        int[] rendered = {0};
        ClientGuiEvent.ScreenRenderPre renderPre;
        ClientGuiEvent.ContainerScreenRenderBackground renderContainerBg;
        ClientGuiEvent.ContainerScreenRenderForeground renderContainerFg;
        ClientGuiEvent.ScreenRenderPost renderPost;
        ClientGuiEvent.RENDER_PRE.register(renderPre = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return EventResult.pass();
            rendered[0] = 0;
            return EventResult.pass();
        });
        ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.register(renderContainerBg = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            rendered[0] = 1;
            resetFocused(screen);
            if (!(screen instanceof DisplayScreen)) {
                sink.render(graphics, mouseX, mouseY, delta);
            }
            resetFocused(screen);
        });
        ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.register(renderContainerFg = (screen, graphics, mouseX, mouseY, delta) -> {
            if (shouldReturn(screen))
                return;
            rendered[0] = 2;
            resetFocused(screen);
            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.translate(-screen.leftPos, -screen.topPos, 0.0);
            RenderSystem.applyModelViewMatrix();
            sink.lateRender(graphics, mouseX, mouseY, delta);
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
            resetFocused(screen);
        });
        ClientGuiEvent.RENDER_POST.register(renderPost = (screen, graphics, mouseX, mouseY, delta) -> {
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
        });
        this.onRemoved.add(() -> {
            ClientGuiEvent.RENDER_PRE.unregister(renderPre);
            ClientGuiEvent.RENDER_CONTAINER_BACKGROUND.unregister(renderContainerBg);
            ClientGuiEvent.RENDER_CONTAINER_FOREGROUND.unregister(renderContainerFg);
            ClientGuiEvent.RENDER_POST.unregister(renderPost);
        });
    }
    
    @Override
    public void onRemoved() {
        this.onRemoved.forEach(Runnable::run);
        this.onRemoved.clear();
    }
}
