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

package me.shedaniel.rei.api.client.gui.drag;

import me.shedaniel.math.Rectangle;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.StreamSupport;

@FunctionalInterface
public interface DraggableBoundsProvider {
    static VoxelShape fromRectangle(Rectangle bounds) {
        return Shapes.box(bounds.x, bounds.y, 0, bounds.getMaxX(), bounds.getMaxY(), 0.1);
    }
    
    static DraggableBoundsProvider ofRectangle(Rectangle bounds) {
        return ofShape(fromRectangle(bounds));
    }
    
    static DraggableBoundsProvider ofRectangles(Iterable<Rectangle> bounds) {
        VoxelShape shape = StreamSupport.stream(bounds.spliterator(), false)
                .map(DraggableBoundsProvider::fromRectangle)
                .reduce(Shapes.empty(), Shapes::or);
        return ofShape(shape);
    }
    
    static DraggableBoundsProvider ofShape(VoxelShape shape) {
        return () -> shape;
    }
    
    static DraggableBoundsProvider ofShapes(Iterable<VoxelShape> shapes) {
        VoxelShape shape = StreamSupport.stream(shapes.spliterator(), false)
                .reduce(Shapes.empty(), Shapes::or);
        return ofShape(shape);
    }
    
    static DraggableBoundsProvider empty() {
        return Shapes::empty;
    }
    
    static DraggableBoundsProvider concat(Iterable<DraggableBoundsProvider> providers) {
        return () -> StreamSupport.stream(providers.spliterator(), false)
                .map(DraggableBoundsProvider::bounds)
                .reduce(Shapes.empty(), Shapes::or);
    }
    
    VoxelShape bounds();
}
