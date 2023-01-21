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

package me.shedaniel.rei.api.client.favorites;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.joml.Vector4f;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

@Environment(EnvType.CLIENT)
public class CompoundFavoriteRenderer extends AbstractRenderer {
    protected NumberAnimator<Double> offset = ValueAnimator.ofDouble();
    protected Rectangle scissorArea = new Rectangle();
    protected long nextSwitch = -1;
    protected IntFunction<Renderer> renderers;
    protected int count;
    protected boolean showcase;
    protected IntSupplier supplier;
    
    /**
     * Showcase
     */
    public CompoundFavoriteRenderer(List<Renderer> renderers) {
        this(true, renderers, null);
    }
    
    /**
     * Non showcase
     */
    public CompoundFavoriteRenderer(List<Renderer> renderers, IntSupplier supplier) {
        this(false, renderers, supplier);
    }
    
    protected CompoundFavoriteRenderer(boolean showcase, List<Renderer> renderers, IntSupplier supplier) {
        this(showcase, renderers.size(), renderers::get, supplier);
    }
    
    /**
     * Showcase
     */
    public CompoundFavoriteRenderer(int count, IntFunction<Renderer> renderers) {
        this(true, count, renderers, null);
    }
    
    /**
     * Non showcase
     */
    public CompoundFavoriteRenderer(int count, IntFunction<Renderer> renderers, IntSupplier supplier) {
        this(false, count, renderers, supplier);
    }
    
    public CompoundFavoriteRenderer(boolean showcase, int count, IntFunction<Renderer> renderers, IntSupplier supplier) {
        this.count = count;
        this.showcase = showcase;
        this.renderers = renderers;
        this.supplier = supplier;
    }
    
    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        updateAnimator(delta);
        Vector4f vector4f = new Vector4f(bounds.x, bounds.y, 0, 1.0F);
        matrices.last().pose().transform(vector4f);
        Vector4f vector4f2 = new Vector4f(bounds.getMaxX(), bounds.getMaxY(), 0, 1.0F);
        matrices.last().pose().transform(vector4f2);
        scissorArea.setBounds((int) vector4f.x(), (int) vector4f.y(), (int) vector4f2.x() - (int) vector4f.x(), (int) vector4f2.y() - (int) vector4f.y());
        ScissorsHandler.INSTANCE.scissor(scissorArea);
        matrices.pushPose();
        matrices.translate(0, this.offset.floatValue() * -bounds.getHeight(), 0);
        for (int i = 0; i < count; i++) {
            renderers.apply(i).render(matrices, bounds, mouseX, mouseY, delta);
            matrices.translate(0, bounds.height, 0);
        }
        matrices.popPose();
        ScissorsHandler.INSTANCE.removeLastScissor();
    }
    
    private void updateAnimator(float delta) {
        offset.update(delta);
        if (showcase) {
            if (nextSwitch == -1) {
                nextSwitch = Util.getMillis();
            }
            if (Util.getMillis() - nextSwitch > 1000) {
                nextSwitch = Util.getMillis();
                offset.setTo((offset.target().intValue() + 1) % count, 500);
            }
        } else {
            offset.setTo(supplier.getAsInt() % count, 500);
        }
    }
}
