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

package me.shedaniel.rei.impl.client.gui.forge;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class REIGuiGraphicsCompat {
    private static final Field SCISSOR_STACK_FIELD = findField("scissorStack");
    private static final Method INNER_TEXTURED_BLIT_METHOD = findMethod("innerBlit",
            RenderPipeline.class, GpuTextureView.class, GpuSampler.class,
            int.class, int.class, int.class, int.class,
            float.class, float.class, float.class, float.class, int.class);

    private REIGuiGraphicsCompat() {
    }

    public static void innerBlit(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier location,
                                 int xStart, int xEnd, int yStart, int yEnd,
                                 float u0, float u1, float v0, float v1, int color) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        invoke(INNER_TEXTURED_BLIT_METHOD, graphics, pipeline, texture.getTextureView(), texture.getSampler(),
                xStart, yStart, xEnd, yEnd, u0, u1, v0, v1, color);
    }

    public static void withFreshScissorStack(GuiGraphicsExtractor graphics, Runnable runnable) {
        Object previous = readField(SCISSOR_STACK_FIELD, graphics);
        try {
            Constructor<?> constructor = SCISSOR_STACK_FIELD.getType().getDeclaredConstructor();
            constructor.setAccessible(true);
            SCISSOR_STACK_FIELD.set(graphics, constructor.newInstance());
            runnable.run();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to swap GuiGraphicsExtractor scissor stack", e);
        } finally {
            try {
                SCISSOR_STACK_FIELD.set(graphics, previous);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to restore GuiGraphicsExtractor scissor stack", e);
            }
        }
    }

    private static Field findField(String name) {
        try {
            Field field = GuiGraphicsExtractor.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve GuiGraphicsExtractor field: " + name, e);
        }
    }

    private static Method findMethod(String name, Class<?>... parameterTypes) {
        try {
            Method method = GuiGraphicsExtractor.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve GuiGraphicsExtractor method: " + name, e);
        }
    }

    private static Object readField(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read GuiGraphicsExtractor field: " + field.getName(), e);
        }
    }

    private static void invoke(Method method, Object instance, Object... args) {
        try {
            method.invoke(instance, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke GuiGraphicsExtractor method: " + method.getName(), e);
        }
    }
}
