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

package me.shedaniel.rei.api.client.gui.drag;

import com.google.common.base.MoreObjects;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The context of the current dragged stack on the overlay.
 * <p>
 * Widgets should implement {@link DraggableStackProviderWidget} to submit applicable stacks to drag.
 * Widgets should implement {@link DraggableStackVisitorWidget} to accept incoming dragged stacks.
 * <p>
 * External providers should use {@link me.shedaniel.rei.api.client.registry.screen.ScreenRegistry#registerDraggableStackProvider(DraggableStackProvider)},
 * and {@link me.shedaniel.rei.api.client.registry.screen.ScreenRegistry#registerDraggableStackVisitor(DraggableStackVisitor)}.
 */
public interface DraggingContext<S extends Screen> {
    static DraggingContext<?> getInstance() {
        return REIRuntime.getInstance().getOverlay().get().getDraggingContext();
    }
    
    /**
     * Returns whether a draggable stack is present.
     *
     * @return whether a draggable stack is present
     */
    default boolean isDraggingStack() {
        return getCurrentStack() != null;
    }
    
    /**
     * Returns whether a draggable component is present.
     *
     * @return whether a draggable component is present
     */
    default boolean isDraggingComponent() {
        return getDragged() != null;
    }
    
    S getScreen();
    
    /**
     * Returns the current dragged stack, may be null.
     *
     * @return the current dragged stack, may be null
     */
    @Nullable
    DraggableStack getCurrentStack();
    
    /**
     * Returns the current dragged component, may be null.
     *
     * @return the current dragged component, may be null
     */
    @Nullable
    DraggableComponent<?> getDragged();
    
    /**
     * Returns the current position of the dragged stack, this is usually the position of the mouse pointer,
     * but you should use this regardless to account for future changes.
     *
     * @return the current position of the dragged stack
     */
    @Nullable
    Point getCurrentPosition();
    
    /**
     * Returns the current bounds of the dragged stack, this is usually on the mouse pointer,
     * but you should use this regardless to account for future changes.
     *
     * @return the current bounds of the dragged stack
     */
    @Nullable
    Rectangle getCurrentBounds();
    
    /**
     * Renders the draggable stack back to the position {@code position}.
     * This may be used to animate an unaccepted draggable stack returning to its initial position.
     *
     * @param stack           the stack to use for render
     * @param initialPosition the initial position of the stack
     * @param position        the position supplier of the destination
     */
    default void renderBackToPosition(DraggableStack stack, Point initialPosition, Supplier<Point> position) {
        renderBack(stack, initialPosition, position);
    }
    
    /**
     * Renders the draggable stack back to the bounds {@code bounds}.
     * This may be used to animate an unaccepted draggable stack returning to its initial position.
     *
     * @param stack           the stack to use for render
     * @param initialPosition the initial bounds of the stack
     * @param bounds          the boundary supplier of the destination
     */
    default void renderBackToPosition(DraggableStack stack, Rectangle initialPosition, Supplier<Rectangle> bounds) {
        renderBack(stack, initialPosition, bounds);
    }
    
    default void renderToVoid(DraggableStack stack) {
        this.renderToVoid((DraggableComponent<?>) stack);
    }
    
    /**
     * Renders the draggable component back to the position {@code position}.
     * This may be used to animate an unaccepted draggable component returning to its initial position.
     *
     * @param component       the component to use for render
     * @param initialPosition the initial position of the component
     * @param position        the position supplier of the destination
     */
    void renderBack(DraggableComponent<?> component, Point initialPosition, Supplier<Point> position);
    
    /**
     * Renders the draggable component back to the bounds {@code bounds}.
     * This may be used to animate an unaccepted draggable component returning to its initial position.
     *
     * @param component       the component to use for render
     * @param initialPosition the initial bounds of the component
     * @param bounds          the boundary supplier of the destination
     */
    void renderBack(DraggableComponent<?> component, Rectangle initialPosition, Supplier<Rectangle> bounds);
    
    default void renderToVoid(DraggableComponent<?> component) {
        Rectangle currentBounds = MoreObjects.firstNonNull(getCurrentBounds(), component.getOriginBounds(PointHelper.ofMouse()));
        Rectangle targetBounds = new Rectangle(currentBounds.getCenterX(), currentBounds.getCenterY(), 1, 1);
        int width = component.getWidth();
        int height = component.getHeight();
        renderBack(component, currentBounds, () -> targetBounds);
    }
    
    default <T extends Screen> DraggingContext<T> cast() {
        return (DraggingContext<T>) this;
    }
}
