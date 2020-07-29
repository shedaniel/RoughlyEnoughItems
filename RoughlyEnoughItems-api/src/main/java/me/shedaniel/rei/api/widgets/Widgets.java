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

package me.shedaniel.rei.api.widgets;

import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.DrawableConsumer;
import me.shedaniel.rei.gui.widget.Widget;
import me.shedaniel.rei.impl.Internals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public final class Widgets {
    private Widgets() {}
    
    @NotNull
    public static Widget createDrawableWidget(@NotNull DrawableConsumer drawable) {
        return Internals.getWidgetsProvider().createDrawableWidget(drawable);
    }
    
    @NotNull
    public static Widget wrapVanillaWidget(@NotNull Element element) {
        return new VanillaWrappedWidget(element);
    }
    
    private static class VanillaWrappedWidget extends Widget {
        private Element element;
        
        public VanillaWrappedWidget(Element element) {
            this.element = Objects.requireNonNull(element);
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (element instanceof DrawableHelper)
                ((DrawableHelper) element).setZOffset(getZ());
            if (element instanceof Drawable)
                ((Drawable) element).render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(element);
        }
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, @NotNull Rectangle bounds) {
        return createTexturedWidget(identifier, bounds, 0, 0);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, int x, int y, int width, int height) {
        return createTexturedWidget(identifier, x, y, 0, 0, width, height);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, @NotNull Rectangle bounds, float u, float v) {
        return createTexturedWidget(identifier, bounds, u, v, 256, 256);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, int x, int y, float u, float v, int width, int height) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, 256, 256);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, @NotNull Rectangle bounds, float u, float v, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, bounds.width, bounds.height, textureWidth, textureHeight);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, @NotNull Rectangle bounds, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, uWidth, vHeight, textureWidth, textureHeight);
    }
    
    @NotNull
    public static Widget createTexturedWidget(@NotNull Identifier identifier, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createDrawableWidget(Internals.getWidgetsProvider().createTexturedConsumer(identifier, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight));
    }
    
    @NotNull
    public static Widget createFilledRectangle(@NotNull Rectangle rectangle, int color) {
        return createDrawableWidget(Internals.getWidgetsProvider().createFillRectangleConsumer(rectangle, color));
    }
    
    @NotNull
    public static Label createLabel(@NotNull Point point, @NotNull Text text) {
        return Internals.getWidgetsProvider().createLabel(point, text);
    }
    
    @NotNull
    public static Label createClickableLabel(@NotNull Point point, @NotNull Text text, @Nullable Consumer<Label> onClick) {
        return createLabel(point, text).clickable().onClick(onClick);
    }
    
    @NotNull
    public static Arrow createArrow(@NotNull Point point) {
        return Internals.getWidgetsProvider().createArrow(new Rectangle(point, new Dimension(24, 17)));
    }
    
    @NotNull
    public static BurningFire createBurningFire(@NotNull Point point) {
        return Internals.getWidgetsProvider().createBurningFire(new Rectangle(point, new Dimension(14, 14)));
    }
    
    @NotNull
    public static Widget createSlotBackground(@NotNull Point point) {
        return createSlotBase(new Rectangle(point.x - 1, point.y - 1, 18, 18));
    }
    
    @NotNull
    public static Widget createResultSlotBackground(@NotNull Point point) {
        return createSlotBase(new Rectangle(point.x - 5, point.y - 5, 26, 26));
    }
    
    @NotNull
    public static Panel createRecipeBase(@NotNull Rectangle rectangle) {
        return Internals.getWidgetsProvider().createPanelWidget(rectangle).yTextureOffset(ConfigObject.getInstance().getRecipeBorderType().getYOffset()).rendering(Widgets::shouldRecipeBaseRender);
    }
    
    @NotNull
    public static Panel createCategoryBase(@NotNull Rectangle rectangle) {
        return Internals.getWidgetsProvider().createPanelWidget(rectangle).yTextureOffset(ConfigObject.getInstance().getRecipeBorderType().getYOffset()).rendering(Widgets::shouldSlotBaseRender);
    }
    
    private static boolean shouldRecipeBaseRender(@NotNull Panel panel) {
        return ConfigObject.getInstance().getRecipeBorderType().isRendering() && Internals.getWidgetsProvider().isRenderingPanel(panel);
    }
    
    @NotNull
    public static Panel createRecipeBase(@NotNull Rectangle rectangle, int color) {
        return createRecipeBase(rectangle).color(color);
    }
    
    @NotNull
    public static Panel createCategoryBase(@NotNull Rectangle rectangle, int color) {
        return createCategoryBase(rectangle).color(color);
    }
    
    @NotNull
    public static Panel createSlotBase(@NotNull Rectangle rectangle) {
        return Internals.getWidgetsProvider().createPanelWidget(rectangle).yTextureOffset(-66).rendering(Widgets::shouldSlotBaseRender);
    }
    
    private static boolean shouldSlotBaseRender(@NotNull Panel panel) {
        return true;
    }
    
    @NotNull
    public static Panel createSlotBase(@NotNull Rectangle rectangle, int color) {
        return createSlotBase(rectangle).color(color);
    }
    
    @NotNull
    public static Slot createSlot(@NotNull Point point) {
        return Internals.getWidgetsProvider().createSlot(point);
    }
    
    @NotNull
    public static Button createButton(@NotNull Rectangle bounds, @NotNull Text text) {
        return Internals.getWidgetsProvider().createButton(bounds, text);
    }
    
    public static void produceClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
