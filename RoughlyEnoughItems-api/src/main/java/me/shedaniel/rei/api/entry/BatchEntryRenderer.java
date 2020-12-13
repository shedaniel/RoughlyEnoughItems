package me.shedaniel.rei.api.entry;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public interface BatchEntryRenderer<T> extends EntryRenderer<T> {
    static <T> int getBatchIdFrom(EntryStack<T> entry) {
        EntryRenderer<T> renderer = entry.getRenderer();
        if (renderer instanceof BatchEntryRenderer) return ((BatchEntryRenderer<T>) renderer).getBatchId(entry);
        return renderer.getClass().hashCode();
    }
    
    default int getBatchId(EntryStack<T> entry) {
        return getClass().hashCode();
    }
    
    void startBatch(EntryStack<T> entry, PoseStack matrices, float delta);
    
    void renderBase(EntryStack<T> entry, PoseStack matrices, MultiBufferSource.BufferSource immediate, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    void renderOverlay(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    void endBatch(EntryStack<T> entry, PoseStack matrices, float delta);
    
    @Deprecated
    @Override
    default void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        startBatch(entry, matrices, delta);
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        renderBase(entry, matrices, immediate, bounds, mouseX, mouseY, delta);
        immediate.endBatch();
        renderOverlay(entry, matrices, bounds, mouseX, mouseY, delta);
        endBatch(entry, matrices, delta);
    }
}