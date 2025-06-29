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

package me.shedaniel.rei.impl.client.gui.widget.search;

import java.util.regex.Pattern;

public class TextCalculator {
    // regex to validate
    private static final String NUM = "\\d+(?:\\.\\d+)?(?:[kKmMbBtT]|[eE]\\d+)?(?!\\d)";
    // full regex: optional “=”, then tokens (NUM or operators/paren), allowing implicit *
    private static final Pattern VALID = Pattern.compile(
            "^=?\\s*"
                    + "(?:" + NUM + "|[()+\\-*/])"
                    + "(?:\\s*(?:" + NUM + "|[()+\\-*/]))*"
                    + "\\s*$"
    );
    
    private final String s;
    private int pos = 0;
    
    public TextCalculator(String expr) {
        this.s = expr.replaceFirst("^=", "").replaceAll("\\s+", "");
    }
    
    public static boolean isValid(String expr) {
        if (!VALID.matcher(expr).matches()) return false;
        try {
            return Double.isFinite(new TextCalculator(expr).eval());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public double eval() {
        pos = 0;
        double v = parseExpression();
        if (pos != s.length()) {
            throw new IllegalArgumentException("Unexpected char at " + pos);
        }
        return v;
    }
    
    // expr = term { (+|-) term }
    private double parseExpression() {
        double v = parseTerm();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == '+') {
                pos++;
                v += parseTerm();
            } else if (c == '-') {
                pos++;
                v -= parseTerm();
            } else break;
        }
        return v;
    }
    
    // term = factor { (*|/ | implicit) factor }
    private double parseTerm() {
        double v = parseFactor();
        while (pos < s.length()) {
            char c = s.charAt(pos);
            if (c == '*') {
                pos++;
                v *= parseFactor();
            } else if (c == '/') {
                pos++;
                v /= parseFactor();
            }
            // implicit multiplication: number or ')' followed by '(' or number
            else if (isImplicitMul()) {
                v *= parseFactor();
            } else break;
        }
        return v;
    }
    
    private boolean isImplicitMul() {
        if (pos == 0) return false;
        char prev = s.charAt(pos - 1), next = s.charAt(pos);
        return (Character.isDigit(prev) || prev == ')')
                && (next == '(' || Character.isDigit(next));
    }
    
    // factor = number-with-suffix | '(' expr ')' | unary -
    private double parseFactor() {
        if (pos < s.length() && s.charAt(pos) == '-') {
            pos++;
            return -parseFactor();
        }
        if (pos < s.length() && s.charAt(pos) == '(') {
            pos++;
            double v = parseExpression();
            if (pos >= s.length() || s.charAt(pos) != ')')
                throw new IllegalArgumentException("Missing ) at " + pos);
            pos++;
            return v;
        }
        return parseNumber();
    }
    
    // number = digits[.digits][suffix]
    private double parseNumber() {
        int start = pos;
        // integer/decimal
        while (pos < s.length() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos) == '.'))
            pos++;
        if (pos == start)
            throw new IllegalArgumentException("Number expected at " + pos);
        double val = Double.parseDouble(s.substring(start, pos));
        
        // suffix?
        if (pos < s.length()) {
            char c = s.charAt(pos);
            double mul = 1;
            if (c == 'k' || c == 'K') {
                mul = 1e3;
                pos++;
            } else if (c == 'm' || c == 'M') {
                mul = 1e6;
                pos++;
            } else if (c == 'b' || c == 'B') {
                mul = 1e9;
                pos++;
            } else if (c == 't' || c == 'T') {
                mul = 1e12;
                pos++;
            } else if ((c == 'e' || c == 'E') && pos + 1 < s.length() && Character.isDigit(s.charAt(pos + 1))) {
                pos++;
                int eStart = pos;
                while (pos < s.length() && Character.isDigit(s.charAt(pos))) pos++;
                int exp = Integer.parseInt(s.substring(eStart, pos));
                mul = Math.pow(10, exp);
            }
            val *= mul;
        }
        return val;
    }
}