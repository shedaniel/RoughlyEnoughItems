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

public class SearchCalculatorUtil {
    public static double evaluate(String expr) throws Exception {
        if (expr == null || expr.trim().isEmpty()) {
            throw new Exception("Empty expression");
        }
        return parse(expr.replaceAll("\\s+", ""));
    }

    private static double parse(String expr) throws Exception {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() throws Exception {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new Exception("Unexpected character: " + (char) ch);
                return x;
            }

            double parseExpression() throws Exception {
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() throws Exception {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new Exception("Division by zero");
                        x /= divisor;
                    } else return x;
                }
            }

            double parseFactor() throws Exception {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;

                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new Exception("Missing closing parenthesis");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));

                    if (ch != -1 && Character.isLetter(ch)) {
                        char suffix = Character.toLowerCase((char) ch);
                        switch (suffix) {
                            case 'k':
                                x *= 1_000L;
                                nextChar();
                                break;
                            case 'm':
                                x *= 1_000_000L;
                                nextChar();
                                break;
                            case 'b':
                                x *= 1_000_000_000L;
                                nextChar();
                                break;
                            case 't':
                                x *= 1_000_000_000_000L;
                                nextChar();
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    throw new Exception("Unexpected character: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}