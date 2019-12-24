/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.List;

public class RecipeArrowWidget extends WidgetWithBounds {

    private int x, y;
    private boolean animated;

    public RecipeArrowWidget(int x, int y, boolean animated) {
        this.x = x;
        this.y = y;
        this.animated = animated;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 24, 17);
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
        blit(x, y, 106, 91, 24, 17);
        if (animated) {
            int width = MathHelper.ceil((System.currentTimeMillis() / 250 % 24d) / 1f);
            blit(x, y, 82, 91, width, 17);
        }
    }

    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
}
