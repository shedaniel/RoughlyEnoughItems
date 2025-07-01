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

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3x2f;
import org.joml.Vector3f;

@ApiStatus.Experimental
public class MatrixUtils {
    public static Matrix3x2f inverse(Matrix3x2f matrix) {
        float m00 = matrix.m00;
        float m01 = matrix.m01;
        float m10 = matrix.m10;
        float m11 = matrix.m11;
        float m20 = matrix.m20; // Translation X
        float m21 = matrix.m21; // Translation Y
        
        // Calculate determinant of the 2x2 rotation/scale part
        float det = m00 * m11 - m10 * m01;
        
        if (Math.abs(det) < 0.00001f) { // Check for singularity (e.g., zero scale)
            System.err.println("Warning: Matrix is singular (determinant near zero), cannot be accurately inverted.");
            return null;
        }
        
        float invDet = 1.0f / det;
        float invM00 = m11 * invDet;
        float invM01 = -m01 * invDet;
        float invM10 = -m10 * invDet;
        float invM11 = m00 * invDet;
        
        // Apply inverse 2x2 part to translation
        // T_inv = -M_inv * T
        float invM20 = -(invM00 * m20 + invM10 * m21);
        float invM21 = -(invM01 * m20 + invM11 * m21);
        
        // Create the inverse matrix
        return new Matrix3x2f(
                invM00, invM01, // First column (x-axis transformation)
                invM10, invM11, // Second column (y-axis transformation)
                invM20, invM21  // Third column (translation)
        );
    }
    
    public static Rectangle transform(Matrix3x2f matrix, Rectangle rectangle) {
        Vector3f vec1 = new Vector3f((float) rectangle.x, (float) rectangle.y, 0);
        matrix.transform(vec1);
        Vector3f vec2 = new Vector3f((float) rectangle.getMaxX(), (float) rectangle.getMaxY(), 0);
        matrix.transform(vec2);
        int x1 = Math.round(vec1.x());
        int x2 = Math.round(vec2.x());
        int y1 = Math.round(vec1.y());
        int y2 = Math.round(vec2.y());
        return new Rectangle(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }
    
    public static Point transform(Matrix3x2f matrix, Point point) {
        Vector3f mouse = new Vector3f((float) point.x, (float) point.y, 0);
        matrix.transform(mouse);
        return new Point(mouse.x(), mouse.y());
    }
}
