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

package me.shedaniel.rei.api.fractions;

import com.google.common.math.LongMath;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class Fraction extends Number implements Comparable<Fraction> {
    private static final Fraction EMPTY = ofWhole(0);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.###");
    private final long numerator;
    private final long denominator;
    private final boolean integer;
    private boolean simplified;
    
    private Fraction(long numerator, long denominator) {
        if (denominator > 0) {
            this.numerator = numerator;
            this.denominator = denominator;
        } else if (denominator < 0) {
            this.numerator = -numerator;
            this.denominator = -denominator;
        } else {
            throw new ArithmeticException("/ by zero");
        }
        this.integer = intValue() == doubleValue();
        this.simplified = (this.numerator >= -1 && this.numerator <= 1) || this.denominator == 1;
    }
    
    public static Fraction empty() {
        return EMPTY;
    }
    
    public static Fraction ofWhole(long whole) {
        return new Fraction(whole, 1);
    }
    
    public static Fraction of(long numerator, long denominator) {
        return new Fraction(numerator, denominator);
    }
    
    public static Fraction of(long whole, long numerator, long denominator) {
        return of(numerator + whole * denominator, denominator);
    }
    
    public static Fraction from(double value) {
        int whole = (int) value;
        double part = value - whole;
        int i = 1;
        
        while (true) {
            double tem = part / (1D / i);
            long numerator = Math.round(tem);
            if (Math.abs(tem - numerator) < 0.00001) {
                return of(whole, numerator, i);
            }
            i++;
        }
    }
    
    public long getNumerator() {
        return numerator;
    }
    
    public long getDenominator() {
        return denominator;
    }
    
    public Fraction add(Fraction other) {
        if (other.numerator == 0) return this;
        return of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator);
    }
    
    public Fraction minus(Fraction other) {
        if (other.numerator == 0) return this;
        return of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator);
    }
    
    public Fraction multiply(Fraction other) {
        if (other.numerator == other.denominator) return this;
        return of(numerator * other.numerator, denominator * other.denominator);
    }
    
    public Fraction divide(Fraction other) {
        if (other.numerator == other.denominator) return this;
        return of(numerator * other.denominator, denominator * other.numerator);
    }
    
    public Fraction inverse() {
        if (numerator == denominator)
            return this;
        Fraction fraction = of(denominator, numerator);
        fraction.simplified = fraction.simplified && simplified;
        return fraction;
    }
    
    public Fraction simplify() {
        if (simplified)
            return this;
        if (numerator == 0)
            return ofWhole(0);
        long gcd = LongMath.gcd(Math.abs(numerator), denominator);
        Fraction fraction = of(numerator / gcd, denominator / gcd);
        fraction.simplified = true;
        return fraction;
    }
    
    public boolean isGreaterThan(Fraction fraction) {
        return compareTo(fraction) > 0;
    }
    
    public boolean isLessThan(Fraction fraction) {
        return compareTo(fraction) < 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fraction fraction = (Fraction) o;
        return numerator * fraction.denominator == denominator * fraction.numerator;
    }
    
    @Override
    public int hashCode() {
        return Double.hashCode(doubleValue());
    }
    
    @Override
    public int compareTo(@NotNull Fraction fraction) {
        return Long.compare(numerator * fraction.denominator, denominator * fraction.numerator);
    }
    
    @Override
    public int intValue() {
        return (int) longValue();
    }
    
    @Override
    public long longValue() {
        return numerator / denominator;
    }
    
    @Override
    public float floatValue() {
        return (float) numerator / denominator;
    }
    
    @Override
    public double doubleValue() {
        return (double) numerator / denominator;
    }
    
    public String toDecimalString() {
        return DECIMAL_FORMAT.format(doubleValue());
    }
    
    @Override
    public String toString() {
        if (integer) return toDecimalString();
        return String.format("%s (%d/%d)", toDecimalString(), numerator, denominator);
    }
}
