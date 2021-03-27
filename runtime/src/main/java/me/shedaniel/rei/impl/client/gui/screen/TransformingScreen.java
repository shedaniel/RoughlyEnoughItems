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

package me.shedaniel.rei.impl.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ScissorsScreen;
import me.shedaniel.math.Rectangle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

public class TransformingScreen extends DelegateScreen implements ScissorsScreen {
    private final DoubleSupplier xTransformer;
    private final DoubleSupplier yTransformer;
    private Screen lastScreen;
    private final BooleanSupplier finished;
    private Runnable init;
    private boolean renderingLastScreen = false;
    private boolean translatingLast;
    private boolean initAfter = false;
    
    public TransformingScreen(boolean translatingLast, Screen parent, Screen lastScreen, Runnable init, DoubleSupplier xTransformer, DoubleSupplier yTransformer, BooleanSupplier finished) {
        super(Minecraft.getInstance().level == null && parent == null ? new TitleScreen() : parent);
        this.translatingLast = translatingLast;
        this.lastScreen = lastScreen;
        this.init = init;
        this.xTransformer = xTransformer;
        this.yTransformer = yTransformer;
        this.finished = finished;
    }
    
    public void setInitAfter(boolean initAfter) {
        this.initAfter = initAfter;
    }
    
    public void setParentScreen(Screen parent) {
        this.parent = parent;
    }
    
    public void setLastScreen(Screen lastScreen) {
        this.lastScreen = lastScreen;
    }
    
    @Override
    public void init(Minecraft minecraft, int i, int j) {
        super.init(minecraft, i, j);
        if (init != null) {
            init.run();
            init = null;
            
            if (parent != null) {
                minecraft.mouseHandler.releaseMouse();
                KeyMapping.releaseAll();
            } else {
                minecraft.getSoundManager().resume();
                minecraft.mouseHandler.grabMouse();
                minecraft.screen = this;
            }
            
            minecraft.updateTitle();
        }
        if (lastScreen != null) {
            lastScreen.init(minecraft, i, j);
        }
    }
    
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!translatingLast) {
            renderingLastScreen = true;
            if (lastScreen != null) {
                RenderSystem.pushMatrix();
                RenderSystem.translated(0, 0, -400);
                lastScreen.render(poseStack, -1, -1, 0);
                RenderSystem.popMatrix();
            }
            renderingLastScreen = false;
            RenderSystem.pushMatrix();
            RenderSystem.translated(xTransformer.getAsDouble(), yTransformer.getAsDouble(), 0);
            super.render(poseStack, i, j, f);
            RenderSystem.popMatrix();
        } else {
            RenderSystem.pushMatrix();
            RenderSystem.translated(0, 0, -400);
            super.render(poseStack, i, j, f);
            RenderSystem.popMatrix();
            renderingLastScreen = true;
            if (lastScreen != null) {
                RenderSystem.pushMatrix();
                RenderSystem.translated(xTransformer.getAsDouble(), yTransformer.getAsDouble(), 0);
                lastScreen.render(poseStack, -1, -1, 0);
                RenderSystem.popMatrix();
            }
            renderingLastScreen = false;
        }
    }
    
    @Override
    public void tick() {
        if (finished.getAsBoolean()) {
            if (parent != null) {
                parent.removed();
            }
            
            Minecraft.getInstance().screen = parent;
            if (parent != null) {
                Minecraft.getInstance().noRender = false;
                if (initAfter) {
                    parent.init(Minecraft.getInstance(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
                }
            }
        } else {
            super.tick();
        }
    }
    
    @Override
    @Nullable
    public Rectangle handleScissor(@Nullable Rectangle rectangle) {
        if (renderingLastScreen == translatingLast && rectangle != null)
            rectangle.translate((int) xTransformer.getAsDouble(), (int) yTransformer.getAsDouble());
        return rectangle;
    }
}
