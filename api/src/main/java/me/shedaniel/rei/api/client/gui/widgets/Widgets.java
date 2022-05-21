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

package me.shedaniel.rei.api.client.gui.widgets;

import com.google.common.collect.AbstractIterator;
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Dimension;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.DrawableConsumer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.impl.ClientInternals;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class Widgets {
    private Widgets() {}
    
    public static Widget createDrawableWidget(DrawableConsumer drawable) {
        return ClientInternals.getWidgetsProvider().createDrawableWidget(drawable);
    }
    
    public static WidgetWithBounds withTooltip(WidgetWithBounds widget, Component... texts) {
        return withTooltip(widget, Arrays.asList(texts));
    }
    
    public static WidgetWithBounds withTooltip(WidgetWithBounds widget, Collection<Component> texts) {
        return withBounds(concat(
                widget,
                createTooltip(widget::getBounds, texts)
        ), widget::getBounds);
    }
    
    public static Widget createTooltip(Rectangle bounds, Component... texts) {
        return createTooltip(() -> bounds, Arrays.asList(texts));
    }
    
    public static Widget createTooltip(Rectangle bounds, Collection<Component> texts) {
        return createTooltip(() -> bounds, texts);
    }
    
    public static Widget createTooltip(Supplier<Rectangle> bounds, Component... texts) {
        return createTooltip(bounds, Arrays.asList(texts));
    }
    
    public static Widget createTooltip(Supplier<Rectangle> bounds, Collection<Component> texts) {
        return createTooltip(mouse -> {
            if (bounds.get().contains(mouse)) {
                return Tooltip.create(mouse, texts);
            } else {
                return null;
            }
        });
    }
    
    public static Widget createTooltip(Function<Point, @Nullable Tooltip> tooltipSupplier) {
        return createDrawableWidget((helper, matrices, mouseX, mouseY, delta) -> {
            Point mouse = new Point(mouseX, mouseY);
            Tooltip tooltip = tooltipSupplier.apply(mouse);
            if (tooltip != null) {
                tooltip.queue();
            }
        });
    }
    
    public static Widget wrapVanillaWidget(GuiEventListener element) {
        return ClientInternals.getWidgetsProvider().wrapVanillaWidget(element);
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, double x, double y, double z) {
        return withTranslate(widget, Matrix4f.createTranslateMatrix((float) x, (float) y, (float) z));
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, Matrix4f translate) {
        return withTranslate(widget, () -> translate);
    }
    
    public static <T extends Widget> WidgetWithBounds withTranslate(T widget, Function<T, Matrix4f> translate) {
        return withTranslate(widget, () -> translate.apply(widget));
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, Supplier<Matrix4f> translate) {
        WidgetWithBounds widgetWithBounds = wrapWidgetWithBounds(widget);
        return ClientInternals.getWidgetsProvider().withTranslate(widgetWithBounds, translate);
    }
    
    public static WidgetWithBounds wrapRenderer(Rectangle bounds, Renderer renderer) {
        return wrapRenderer(() -> bounds, renderer);
    }
    
    public static WidgetWithBounds wrapRenderer(Supplier<Rectangle> bounds, Renderer renderer) {
        if (renderer instanceof Widget widget)
            return wrapWidgetWithBoundsSupplier(widget, bounds);
        return ClientInternals.getWidgetsProvider().wrapRenderer(bounds, renderer);
    }
    
    /**
     * @deprecated Use {@link #withBounds(Widget)} instead.
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval
    public static WidgetWithBounds wrapWidgetWithBounds(Widget widget) {
        return withBounds(widget);
    }
    
    /**
     * @deprecated Use {@link #withBounds(Widget, Rectangle)} instead.
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval
    public static WidgetWithBounds wrapWidgetWithBounds(Widget widget, Rectangle bounds) {
        return withBounds(widget, bounds);
    }
    
    /**
     * @deprecated Use {@link #withBounds(Widget, Supplier)} instead.
     */
    @Deprecated(forRemoval = true)
    @ApiStatus.ScheduledForRemoval
    public static WidgetWithBounds wrapWidgetWithBoundsSupplier(Widget widget, Supplier<Rectangle> bounds) {
        return withBounds(widget, bounds);
    }
    
    public static WidgetWithBounds withBounds(Widget widget) {
        return wrapWidgetWithBounds(widget, null);
    }
    
    public static WidgetWithBounds withBounds(Widget widget, Rectangle bounds) {
        return wrapWidgetWithBoundsSupplier(widget, bounds == null ? null : () -> bounds);
    }
    
    public static WidgetWithBounds withBounds(Widget widget, Supplier<Rectangle> bounds) {
        if (widget instanceof WidgetWithBounds withBounds)
            return withBounds;
        if (bounds == null)
            return new DelegateWidget(widget);
        return new DelegateWidgetWithBounds(widget, bounds);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, Rectangle bounds) {
        return createTexturedWidget(identifier, bounds, 0, 0);
    }
    
    
    public static Widget createTexturedWidget(ResourceLocation identifier, int x, int y, int width, int height) {
        return createTexturedWidget(identifier, x, y, 0, 0, width, height);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, Rectangle bounds, float u, float v) {
        return createTexturedWidget(identifier, bounds, u, v, 256, 256);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, int x, int y, float u, float v, int width, int height) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, 256, 256);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, Rectangle bounds, float u, float v, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, bounds.width, bounds.height, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, x, y, u, v, width, height, width, height, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, Rectangle bounds, float u, float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createTexturedWidget(identifier, bounds.x, bounds.y, u, v, bounds.width, bounds.height, uWidth, vHeight, textureWidth, textureHeight);
    }
    
    public static Widget createTexturedWidget(ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        return createDrawableWidget(ClientInternals.getWidgetsProvider().createTexturedConsumer(identifier, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight));
    }
    
    public static Widget createFilledRectangle(Rectangle rectangle, int color) {
        return createDrawableWidget(ClientInternals.getWidgetsProvider().createFillRectangleConsumer(rectangle, color));
    }
    
    public static Label createLabel(Point point, Component text) {
        return ClientInternals.getWidgetsProvider().createLabel(point, text);
    }
    
    public static Label createClickableLabel(Point point, Component text, @Nullable Consumer<Label> onClick) {
        return createLabel(point, text).clickable().onClick(onClick);
    }
    
    public static Arrow createArrow(Point point) {
        return ClientInternals.getWidgetsProvider().createArrow(new Rectangle(point, new Dimension(24, 17)));
    }
    
    public static BurningFire createBurningFire(Point point) {
        return ClientInternals.getWidgetsProvider().createBurningFire(new Rectangle(point, new Dimension(14, 14)));
    }
    
    public static Widget createSlotBackground(Point point) {
        return createSlotBase(new Rectangle(point.x - 1, point.y - 1, 18, 18));
    }
    
    public static Widget createResultSlotBackground(Point point) {
        return createSlotBase(new Rectangle(point.x - 5, point.y - 5, 26, 26));
    }
    
    public static Panel createCategoryBase(Rectangle rectangle) {
        return ClientInternals.getWidgetsProvider().createPanelWidget(rectangle);
    }
    
    public static Panel createCategoryBase(Rectangle rectangle, int color) {
        return createCategoryBase(rectangle).color(color);
    }
    
    public static Panel createRecipeBase(Rectangle rectangle) {
        return ClientInternals.getWidgetsProvider().createPanelWidget(rectangle)
                .yTextureOffset(ConfigObject.getInstance().getRecipeBorderType().getYOffset())
                .rendering(Widgets::shouldRecipeBaseRender);
    }
    
    public static Panel createRecipeBase(Rectangle rectangle, int color) {
        return createRecipeBase(rectangle).color(color);
    }
    
    private static boolean shouldRecipeBaseRender(Panel panel) {
        return ConfigObject.getInstance().getRecipeBorderType().isRendering() && ClientInternals.getWidgetsProvider().isRenderingPanel(panel);
    }
    
    
    public static Panel createSlotBase(Rectangle rectangle) {
        return ClientInternals.getWidgetsProvider().createPanelWidget(rectangle).yTextureOffset(-66).rendering(Widgets::shouldSlotBaseRender);
    }
    
    private static boolean shouldSlotBaseRender(Panel panel) {
        return true;
    }
    
    public static Panel createSlotBase(Rectangle rectangle, int color) {
        return createSlotBase(rectangle).color(color);
    }
    
    public static Widget createShapelessIcon(Rectangle rectangle) {
        return createShapelessIcon(new Point(rectangle.getMaxX() - 4, rectangle.y + 4));
    }
    
    public static Widget createShapelessIcon(Point topRightPos) {
        return ClientInternals.getWidgetsProvider().createShapelessIcon(topRightPos);
    }
    
    public static Slot createSlot(Point point) {
        return ClientInternals.getWidgetsProvider().createSlot(point);
    }
    
    public static Slot createSlot(Rectangle bounds) {
        return ClientInternals.getWidgetsProvider().createSlot(bounds);
    }
    
    public static Button createButton(Rectangle bounds, Component text) {
        return ClientInternals.getWidgetsProvider().createButton(bounds, text);
    }
    
    public static void produceClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
    
    public static Widget concat(Widget... widgets) {
        return concat(Arrays.asList(widgets));
    }
    
    public static Widget concat(List<Widget> widgets) {
        return ClientInternals.getWidgetsProvider().concatWidgets(widgets);
    }
    
    public static <T> Iterable<T> walk(Iterable<? extends GuiEventListener> listeners, Predicate<GuiEventListener> predicate) {
        return () -> new AbstractIterator<T>() {
            Stack<Iterator<? extends GuiEventListener>> stack;
            Set<T> visited = new HashSet<>();
            
            {
                stack = new Stack<>();
                stack.push(listeners.iterator());
            }
            
            @Override
            protected T computeNext() {
                while (!stack.empty()) {
                    Iterator<? extends GuiEventListener> peek = stack.peek();
                    if (!peek.hasNext())
                        stack.pop();
                    if (stack.isEmpty())
                        break;
                    GuiEventListener listener = peek.next();
                    if (!peek.hasNext())
                        stack.pop();
                    if (predicate.test(listener) && visited.add((T) listener)) {
                        return (T) listener;
                    }
                    if (listener instanceof ContainerEventHandler handler) {
                        List<? extends GuiEventListener> children = handler.children();
                        if (!children.isEmpty()) {
                            stack.push(children.iterator());
                        }
                    } else if (listener instanceof WidgetHolder holder) {
                        List<? extends GuiEventListener> children = holder.children();
                        if (!children.isEmpty()) {
                            stack.push(children.iterator());
                        }
                    }
                }
                return endOfData();
            }
        };
    }
}
