package me.shedaniel.rei.api;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.widgets.Tooltip;
import org.jetbrains.annotations.Nullable;

public interface Renderer {
    void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta);
    
    @Nullable
    default Tooltip getTooltip(Point mouse) {
        return null;
    }
    
    int getZ();
    
    void setZ(int z);
}
