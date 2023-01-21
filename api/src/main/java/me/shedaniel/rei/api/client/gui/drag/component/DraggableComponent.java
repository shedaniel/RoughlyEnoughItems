/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.api.client.gui.drag.component;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface DraggableComponent<T> extends Supplier<T> {
    default <A> Optional<DraggableComponent<A>> getIf(A... typeHack) {
        if (typeHack.length != 0) throw new IllegalStateException("array must be empty!");
        if (typeHack.getClass().getComponentType().isInstance(get())) {
            return Optional.of((DraggableComponent<A>) this);
        } else {
            return Optional.empty();
        }
    }
    
    default <A> boolean ifMatches(Consumer<A> consumer, A... typeHack) {
        Optional<DraggableComponent<A>> optional = getIf(typeHack);
        optional.ifPresent(comp -> consumer.accept(comp.get()));
        return optional.isPresent();
    }
    
    default <A> boolean ifMatches(Predicate<A> consumer, A... typeHack) {
        Optional<DraggableComponent<A>> optional = getIf(typeHack);
        return optional.map(comp -> consumer.test(comp.get())).orElse(false);
    }
    
    int getWidth();
    
    int getHeight();
    
    /**
     * Returns the bounds of the component. This is the bounds that the origin of the component is at.
     *
     * @param mouse the mouse position
     * @return the bounds of the component
     */
    default Rectangle getOriginBounds(Point mouse) {
        return new Rectangle(mouse.x - getWidth() / 2, mouse.y - getHeight() / 2, getWidth(), getHeight());
    }
    
    default void drag() {
    }
    
    default void release(DraggedAcceptorResult result) {
    }
    
    default void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
    }
    
    default void render(PoseStack matrices, Point position, int mouseX, int mouseY, float delta) {
        render(matrices, new Rectangle(position.x - getWidth() / 2, position.y - getHeight() / 2, getWidth(), getHeight()), mouseX, mouseY, delta);
    }
}
