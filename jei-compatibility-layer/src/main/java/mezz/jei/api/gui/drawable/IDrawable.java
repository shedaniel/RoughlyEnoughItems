package mezz.jei.api.gui.drawable;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;

/**
 * Represents something to be drawn on screen.
 * Useful for drawing miscellaneous things in {@link IRecipeCategory#draw(Object, PoseStack, double, double)}.
 * {@link IGuiHelper} has many functions to create IDrawables.
 *
 * @see IDrawableAnimated
 * @see IDrawableStatic
 */
public interface IDrawable {
    
    int getWidth();
    
    int getHeight();
    
    default void draw(PoseStack matrixStack) {
        draw(matrixStack, 0, 0);
    }
    
    void draw(PoseStack matrixStack, int xOffset, int yOffset);
    
}
