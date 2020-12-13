package me.shedaniel.rei.api.entry;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Tooltip;
import org.jetbrains.annotations.Nullable;

public interface EntryRenderer<T> {
    void render(EntryStack<T> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    @Nullable
    Tooltip getTooltip(EntryStack<T> entry, Point mouse);
}