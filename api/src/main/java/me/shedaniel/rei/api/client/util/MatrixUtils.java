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

package me.shedaniel.rei.api.client.util;

import com.mojang.math.Transformation;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@ApiStatus.Experimental
public class MatrixUtils {
    public static Matrix4f inverse(Matrix4f matrix) {
        Transformation transformation = new Transformation(matrix);
        Transformation inverse = transformation.inverse();
        if (inverse != null) inverse.getScale(); // This has a side effect
        return inverse == null ? Transformation.identity().getMatrix() : inverse.getMatrix();
    }
    
    public static Rectangle transform(Matrix4f matrix, Rectangle rectangle) {
        Vector4f vec1 = new Vector4f((float) rectangle.x, (float) rectangle.y, 0, 1);
        matrix.transform(vec1);
        Vector4f vec2 = new Vector4f((float) rectangle.getMaxX(), (float) rectangle.getMaxY(), 0, 1);
        matrix.transform(vec2);
        int x1 = Math.round(vec1.x());
        int x2 = Math.round(vec2.x());
        int y1 = Math.round(vec1.y());
        int y2 = Math.round(vec2.y());
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
    
    public static Point transform(Matrix4f matrix, Point point) {
        Vector4f mouse = new Vector4f((float) point.x, (float) point.y, 0, 1);
        matrix.transform(mouse);
        return new Point(mouse.x(), mouse.y());
    }
}
