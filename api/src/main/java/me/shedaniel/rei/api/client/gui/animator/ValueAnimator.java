/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
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

package me.shedaniel.rei.api.client.gui.animator;

import me.shedaniel.math.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An animator that can animate a value.
 *
 * @param <T> the type of the value
 */
@ApiStatus.Experimental
public interface ValueAnimator<T> extends ValueProvider<T> {
    static long typicalTransitionTime() {
        return 700;
    }
    
    static NumberAnimator<Double> ofDouble() {
        return new DoubleValueAnimatorImpl(0);
    }
    
    static NumberAnimator<Float> ofFloat() {
        return ofDouble().asFloat();
    }
    
    static NumberAnimator<Integer> ofInt() {
        return ofDouble().asInt();
    }
    
    static NumberAnimator<Long> ofLong() {
        return ofDouble().asLong();
    }
    
    static ProgressValueAnimator<Boolean> ofBoolean() {
        return ofBoolean(50D);
    }
    
    static ProgressValueAnimator<Boolean> ofBoolean(double switchPoint) {
        return ProgressValueAnimator.mapProgress(ofDouble(), d -> d > (switchPoint / 100), b -> b ? 100D : 0D);
    }
    
    static ValueAnimator<Color> ofColor() {
        return RecordValueAnimator.of(ofInt(), ofInt(), ofInt(), ofInt(), Color::ofRGBA, (color, r, g, b, a) -> {
            r.set(color.getRed());
            g.set(color.getGreen());
            b.set(color.getBlue());
            a.set(color.getAlpha());
        });
    }
    
    static ValueAnimator<Rectangle> ofRectangle() {
        return RecordValueAnimator.of(ofInt(), ofInt(), ofInt(), ofInt(), Rectangle::new, (rectangle, x, y, width, height) -> {
            x.set(rectangle.x);
            y.set(rectangle.y);
            width.set(rectangle.width);
            height.set(rectangle.height);
        });
    }
    
    static ValueAnimator<Dimension> ofDimension() {
        return RecordValueAnimator.of(ofInt(), ofInt(), Dimension::new, (dimension, width, height) -> {
            width.set(dimension.width);
            height.set(dimension.height);
        });
    }
    
    static ValueAnimator<Point> ofPoint() {
        return RecordValueAnimator.of(ofInt(), ofInt(), Point::new, (dimension, width, height) -> {
            width.set(dimension.x);
            height.set(dimension.y);
        });
    }
    
    static ValueAnimator<FloatingRectangle> ofFloatingRectangle() {
        return RecordValueAnimator.of(ofDouble(), ofDouble(), ofDouble(), ofDouble(), FloatingRectangle::new, (rectangle, x, y, width, height) -> {
            x.set(rectangle.x);
            y.set(rectangle.y);
            width.set(rectangle.width);
            height.set(rectangle.height);
        });
    }
    
    static ValueAnimator<FloatingDimension> ofFloatingDimension() {
        return RecordValueAnimator.of(ofDouble(), ofDouble(), FloatingDimension::new, (dimension, width, height) -> {
            width.set(dimension.width);
            height.set(dimension.height);
        });
    }
    
    static ValueAnimator<FloatingPoint> ofFloatingPoint() {
        return RecordValueAnimator.of(ofDouble(), ofDouble(), FloatingPoint::new, (dimension, width, height) -> {
            width.set(dimension.x);
            height.set(dimension.y);
        });
    }
    
    static NumberAnimator<Double> ofDouble(double initialValue) {
        return new DoubleValueAnimatorImpl(initialValue);
    }
    
    static NumberAnimator<Float> ofFloat(float initialValue) {
        return ofFloat().setAs(initialValue);
    }
    
    static NumberAnimator<Integer> ofInt(int initialValue) {
        return ofInt().setAs(initialValue);
    }
    
    static NumberAnimator<Long> ofLong(long initialValue) {
        return ofLong().setAs(initialValue);
    }
    
    static ProgressValueAnimator<Boolean> ofBoolean(boolean initialValue) {
        return ofBoolean().setAs(initialValue);
    }
    
    static ProgressValueAnimator<Boolean> ofBoolean(double switchPoint, boolean initialValue) {
        return ofBoolean(switchPoint).setAs(initialValue);
    }
    
    static ValueAnimator<Color> ofColor(Color initialValue) {
        return ofColor().setAs(initialValue);
    }
    
    static ValueAnimator<Rectangle> ofRectangle(Rectangle initialValue) {
        return ofRectangle().setAs(initialValue);
    }
    
    static ValueAnimator<Dimension> ofDimension(Dimension initialValue) {
        return ofDimension().setAs(initialValue);
    }
    
    static ValueAnimator<Point> ofDimension(Point initialValue) {
        return ofPoint().setAs(initialValue);
    }
    
    static ValueAnimator<FloatingRectangle> ofFloatingRectangle(FloatingRectangle initialValue) {
        return ofFloatingRectangle().setAs(initialValue);
    }
    
    static ValueAnimator<FloatingDimension> ofFloatingDimension(FloatingDimension initialValue) {
        return ofFloatingDimension().setAs(initialValue);
    }
    
    static ValueAnimator<FloatingPoint> ofFloatingDimension(FloatingPoint initialValue) {
        return ofFloatingPoint().setAs(initialValue);
    }
    
    default <R> ValueAnimator<R> map(Function<T, R> converter, Function<R, T> backwardsConverter) {
        return new MappingValueAnimator<>(this, converter, backwardsConverter);
    }
    
    default ValueAnimator<T> setAs(T value) {
        return setTo(value, -1);
    }
    
    ValueAnimator<T> setTo(T value, long duration);
    
    default ValueAnimator<T> withConvention(Supplier<T> convention, long duration) {
        return new ConventionValueAnimator<>(this, convention, duration);
    }
    
    @Override
    default void completeImmediately() {
        setAs(target());
    }
}
