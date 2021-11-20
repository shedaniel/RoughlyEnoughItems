package me.shedaniel.rei.api.client.registry.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.TransferDisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;

@ApiStatus.Experimental
@FunctionalInterface
public interface TransferHandlerErrorRenderer {
    void render(PoseStack matrices, int mouseX, int mouseY, float delta, List<Widget> widgets, Rectangle bounds, Display display);
    
    @ApiStatus.Internal
    static TransferHandlerErrorRenderer forRedSlots(IntList redSlots) {
        return (matrices, mouseX, mouseY, delta, widgets, bounds, display) -> {
            DisplayCategory<?> category = Objects.requireNonNull(CategoryRegistry.getInstance().get(display.getCategoryIdentifier()))
                    .getCategory();
            if (category instanceof TransferDisplayCategory) {
                ((TransferDisplayCategory<Display>) category).renderRedSlots(matrices, widgets, bounds, display, redSlots);
            }
        };
    }
}
