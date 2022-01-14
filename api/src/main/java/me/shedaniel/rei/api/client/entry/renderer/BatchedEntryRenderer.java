/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.api.client.entry.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * A batched renderer for rendering a lot of {@link EntryStack} at once with better performance.
 *
 * @param <T> the entry type
 * @param <E> the type of extra data returned in {@link #getExtraData(EntryStack)}
 */
@Environment(EnvType.CLIENT)
public interface BatchedEntryRenderer<T, E> extends EntryRenderer<T> {
    default boolean isBatched(EntryStack<T> entry) {
        return true;
    }
    
    /**
     * Returns extra data to be passed to various rendering methods.
     *
     * @param entry the stack
     * @return the extra data
     */
    E getExtraData(EntryStack<T> entry);
    
    /**
     * Returns a batch identifier, stacks with the same batch identifier will be grouped together
     * into a batch.
     *
     * @param entry     the stack
     * @param bounds    the bounds of the entry
     * @param extraData the extra data returned from {@link #getExtraData(EntryStack)}
     * @return the batch identifier
     */
    int getBatchIdentifier(EntryStack<T> entry, Rectangle bounds, E extraData);
    
    /**
     * Modifies the {@link PoseStack} passed tp various batch rendering methods.
     *
     * @param matrices the matrix stack
     * @return the modified matrix stack, could be an entirely different stack
     */
    default PoseStack batchModifyMatrices(PoseStack matrices) {
        return matrices;
    }
    
    /**
     * Starts the batch rendering, used to setup states, only called once with every batch.
     *
     * @param entry     the first entry in the batch
     * @param extraData the extra data returned from {@link #getExtraData(EntryStack)}
     * @param matrices  the matrix stack
     * @param delta     the tick delta
     */
    void startBatch(EntryStack<T> entry, E extraData, PoseStack matrices, float delta);
    
    void renderBase(EntryStack<T> entry, E extraData, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    void afterBase(EntryStack<T> entry, E extraData, PoseStack matrices, float delta);
    
    void renderOverlay(EntryStack<T> entry, E extraData, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    /**
     * Ends the batch rendering, used to setup states, only called once with every batch.
     *
     * @param entry     the first entry in the batch
     * @param extraData the extra data returned from {@link #getExtraData(EntryStack)}
     * @param matrices  the matrix stack
     * @param delta     the tick delta
     */
    void endBatch(EntryStack<T> entry, E extraData, PoseStack matrices, float delta);
    
    @Override
    default void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        matrices = batchModifyMatrices(matrices);
        E data = getExtraData(entry);
        startBatch(entry, data, matrices, delta);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        renderBase(entry, data, matrices, immediate, bounds, mouseX, mouseY, delta);
        immediate.endBatch();
        renderOverlay(entry, data, matrices, immediate, bounds, mouseX, mouseY, delta);
        immediate.endBatch();
        endBatch(entry, data, matrices, delta);
    }
}