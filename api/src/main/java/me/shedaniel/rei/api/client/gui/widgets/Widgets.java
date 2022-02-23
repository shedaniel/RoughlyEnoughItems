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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
    
    public static Widget wrapVanillaWidget(GuiEventListener element) {
        return new VanillaWrappedWidget(element);
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, double x, double y, double z) {
        return withTranslate(widget, Matrix4f.createTranslateMatrix((float) x, (float) y, (float) z));
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, Matrix4f translate) {
        WidgetWithBounds widgetWithBounds = wrapWidgetWithBounds(widget);
        return new WidgetWithBoundsWithTranslate(widgetWithBounds, () -> translate);
    }
    
    public static <T extends Widget> WidgetWithBounds withTranslate(T widget, Function<T, Matrix4f> translate) {
        WidgetWithBounds widgetWithBounds = wrapWidgetWithBounds(widget);
        return new WidgetWithBoundsWithTranslate(widgetWithBounds, () -> translate.apply(widget));
    }
    
    public static WidgetWithBounds withTranslate(Widget widget, Supplier<Matrix4f> translate) {
        WidgetWithBounds widgetWithBounds = wrapWidgetWithBounds(widget);
        return new WidgetWithBoundsWithTranslate(widgetWithBounds, translate);
    }
    
    private static class WidgetWithBoundsWithTranslate extends DelegateWidget {
        private final Supplier<Matrix4f> translate;
        
        private WidgetWithBoundsWithTranslate(WidgetWithBounds widget, Supplier<Matrix4f> translate) {
            super(widget);
            this.translate = translate;
        }
        
        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            poseStack.pushPose();
            poseStack.last().pose().multiply(translate.get());
            Vector4f mouse = transformMouse(i, j);
            super.render(poseStack, (int) mouse.x(), (int) mouse.y(), f);
            poseStack.popPose();
        }
        
        private Vector4f transformMouse(double mouseX, double mouseY) {
            Vector4f mouse = new Vector4f((float) mouseX, (float) mouseY, 0, 1);
            mouse.transform(translate.get());
            return mouse;
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            Vector4f mouse = transformMouse(mouseX, mouseY);
            return super.containsMouse(mouse.x(), mouse.y());
        }
        
        @Override
        public boolean mouseClicked(double d, double e, int i) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseClicked(mouse.x(), mouse.y(), i);
        }
        
        @Override
        public boolean mouseReleased(double d, double e, int i) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseReleased(mouse.x(), mouse.y(), i);
        }
        
        @Override
        public boolean mouseDragged(double d, double e, int i, double f, double g) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseDragged(mouse.x(), mouse.y(), i, f, g);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double f) {
            Vector4f mouse = transformMouse(d, e);
            return super.mouseScrolled(mouse.x(), mouse.y(), f);
        }
    }
    
    private static class VanillaWrappedWidget extends Widget {
        private GuiEventListener element;
        
        public VanillaWrappedWidget(GuiEventListener element) {
            this.element = Objects.requireNonNull(element);
            setFocused(element);
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (element instanceof GuiComponent component)
                component.setBlitOffset(getZ());
            if (element instanceof net.minecraft.client.gui.components.Widget widget)
                widget.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.singletonList(element);
        }
        
        @Nullable
        @Override
        public GuiEventListener getFocused() {
            return element;
        }
        
        @Override
        public void setFocused(@Nullable GuiEventListener guiEventListener) {
            if (guiEventListener == element) {
                super.setFocused(element);
            } else if (element instanceof ContainerEventHandler handler) {
                handler.setFocused(guiEventListener);
            }
        }
        
        @Override
        public boolean isDragging() {
            return true;
        }
        
        @Override
        public boolean containsMouse(double mouseX, double mouseY) {
            return element.isMouseOver(mouseX, mouseY);
        }
    }
    
    public static WidgetWithBounds wrapRenderer(Rectangle bounds, Renderer renderer) {
        return wrapRenderer(() -> bounds, renderer);
    }
    
    public static WidgetWithBounds wrapRenderer(Supplier<Rectangle> bounds, Renderer renderer) {
        if (renderer instanceof Widget widget)
            return wrapWidgetWithBoundsSupplier(widget, bounds);
        return new RendererWrappedWidget(renderer, bounds);
    }
    
    public static WidgetWithBounds wrapWidgetWithBounds(Widget widget) {
        return wrapWidgetWithBounds(widget, null);
    }
    
    public static WidgetWithBounds wrapWidgetWithBounds(Widget widget, Rectangle bounds) {
        return wrapWidgetWithBoundsSupplier(widget, bounds == null ? null : () -> bounds);
    }
    
    public static WidgetWithBounds wrapWidgetWithBoundsSupplier(Widget widget, Supplier<Rectangle> bounds) {
        if (widget instanceof WidgetWithBounds withBounds)
            return withBounds;
        if (bounds == null)
            return new DelegateWidget(widget);
        return new DelegateWidgetWithBounds(widget, bounds);
    }
    
    private static class RendererWrappedWidget extends WidgetWithBounds {
        private final Renderer renderer;
        private final Supplier<Rectangle> bounds;
        
        public RendererWrappedWidget(Renderer renderer, Supplier<Rectangle> bounds) {
            this.renderer = Objects.requireNonNull(renderer);
            this.bounds = Objects.requireNonNull(bounds);
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            renderer.render(matrices, getBounds(), mouseX, mouseY, delta);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            if (renderer instanceof GuiEventListener listener)
                return Collections.singletonList(listener);
            return Collections.emptyList();
        }
        
        @Override
        public void setZ(int z) {
            renderer.setZ(z);
        }
        
        @Override
        public int getZ() {
            return renderer.getZ();
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds.get();
        }
    }
    
    private static class DelegateWidgetWithBounds extends DelegateWidget {
        private final Supplier<Rectangle> bounds;
        
        public DelegateWidgetWithBounds(Widget widget, Supplier<Rectangle> bounds) {
            super(widget);
            this.bounds = bounds;
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds.get();
        }
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
    
    public static <T> Iterable<T> walk(Iterable<? extends GuiEventListener> listeners, Predicate<GuiEventListener> predicate) {
        return () -> new AbstractIterator<T>() {
            Stack<Iterator<? extends GuiEventListener>> stack;
            
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
                    if (predicate.test(listener)) {
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
