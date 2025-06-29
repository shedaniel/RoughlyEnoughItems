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

import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

public class CalculatorDisplay implements Consumer<String> {
    private static final int MAX_LEN = 7;
    private static final DecimalFormat DEC;
    private static final DecimalFormat SCI;
    static {
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DEC = new DecimalFormat("0.##########", sym);
        DEC.setRoundingMode(RoundingMode.HALF_UP);
        DEC.setGroupingUsed(false);
        SCI = new DecimalFormat("0.##########E0", sym);
        SCI.setRoundingMode(RoundingMode.HALF_UP);
        SCI.setGroupingUsed(false);
    }
    
    private final OverlaySearchField searchField;
    private final NumberAnimator<Integer> width = ValueAnimator.ofDouble().asInt();
    private Component fullText = Component.empty();
    private FormattedCharSequence text = FormattedCharSequence.EMPTY;
    
    public CalculatorDisplay(OverlaySearchField searchField) {
        this.searchField = searchField;
    }
    
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int prevWidth = width.value();
        this.width.update(delta);
        if (prevWidth != width.value()) {
            // Keep the search field bounded
            this.searchField.addText("");
        }
        
        if (width.value() <= 0) return;
        Rectangle searchFieldBounds = searchField.getBounds();
        boolean contains = searchFieldBounds.contains(mouseX, mouseY) || searchField.isFocused();
        graphics.fill(searchFieldBounds.getMaxX() - width.value(), searchFieldBounds.y + 1, searchFieldBounds.getMaxX() - 1, searchFieldBounds.getMaxY() - 1, contains ? 0xBBFFFFFF : 0x80FFFFFF);
        graphics.enableScissor(searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.y, searchFieldBounds.getMaxX() - 3, searchFieldBounds.getMaxY());
        graphics.drawString(Minecraft.getInstance().font, text, searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.getCenterY() - 4, 0xFF000000, false);
        graphics.disableScissor();
        
        if (!this.fullText.getString().isEmpty() && contains && new Rectangle(searchFieldBounds.getMaxX() - width.value() + 3, searchFieldBounds.y, width.value() - 6, searchFieldBounds.getHeight()).contains(mouseX, mouseY)) {
            Tooltip.create(this.fullText).queue();
        }
    }
    
    @Override
    public void accept(String text) {
        if (!text.startsWith("=")) {
            this.width.setTo(0, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            this.text = Component.literal("NaN").getVisualOrderText();
            this.fullText = Component.empty();
            return;
        }
        
        if (TextCalculator.isValid(text)) {
            double eval = new TextCalculator(text).eval();
            this.text = Component.literal(fmt(eval)).getVisualOrderText();
            this.fullText = Component.literal(fmtAccurate(eval));
        }
        
        this.width.setTo(Minecraft.getInstance().font.width(this.text) + 6, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
    }
    
    public int width() {
        return width.value();
    }
    
    public static String fmt(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x))
            return String.valueOf(x);
        
        boolean neg = x < 0;
        double a = Math.abs(x);
        
        // 1) SMALL <1: decimals to fill WIDTH, else sci
        if (a > 0 && a < 1) {
            int used = neg ? 1 : 0;
            int avail = MAX_LEN - used;       // total chars left
            // "0" + "."  → 2 chars, rest decimals
            int dec = avail - 2;
            if (dec > 0) {
                double minShow = Math.pow(10, -dec);
                if (a >= minShow) {
                    String fmt = "%." + dec + "f";
                    String s = String.format(Locale.ROOT, fmt, a)
                            .replaceFirst("0+$", "") // drop trailing zeros
                            .replaceFirst("\\.$", ""); // drop trailing dot
                    // if we got something like ".123", prepend "0"
                    if (s.startsWith(".")) s = "0" + s;
                    return neg ? "-" + s : s;
                }
            }
            // too small → scientific
            return sciFmt(a, neg);
        }
        
        // 2) exact under threshold
        double thresh = neg ? 1_000_000 : 10_000_000;
        if (a < thresh) {
            String small = (a == Math.rint(a))
                    ? String.valueOf((long) a)
                    : String.format(Locale.ROOT, "%.2f", a)
                    .replaceFirst("\\.?0+$", "");
            if (small.length() + (neg ? 1 : 0) <= MAX_LEN)
                return neg ? "-" + small : small;
        }
        
        // 3) suffix m/b/t
        char suf;
        double v;
        if (a >= 1e12 && a < 1e15) {
            suf = 't';
            v = a / 1e12;
        } else if (a >= 1e9) {
            suf = 'b';
            v = a / 1e9;
        } else if (a >= 1e6) {
            suf = 'm';
            v = a / 1e6;
        } else {
            // small ≥1 but <1e6 (or neg ≥1e6)
            return sciFmt(a, neg);
        }
        {
            int used = (neg ? 1 : 0) + 1;      // sign + suffix
            int avail = MAX_LEN - used;
            String intP = String.valueOf((long) v);
            int ip = intP.length();
            int dec = Math.max(0, avail - ip - 1); // -1 for dot
            for (; dec >= 0; dec--) {
                String fmt = dec > 0 ? "%." + dec + "f" : "%.0f";
                String man = String.format(Locale.ROOT, fmt, v);
                if (man.length() <= avail)
                    return (neg ? "-" : "") + man + suf;
            }
        }
        
        // 4) fallback sci
        return sciFmt(a, neg);
    }
    
    private static String sciFmt(double a, boolean neg) {
        int exp = (int) Math.floor(Math.log10(a));
        double man = a / Math.pow(10, exp);
        String expS = String.valueOf(exp);
        int used = (neg ? 1 : 0) + 1 + expS.length(); // sign + 'e'+exp
        int avail = MAX_LEN - used;
        String intP = String.valueOf((long) man);
        int ip = intP.length();
        int dec = Math.max(0, avail - ip - 1);
        for (; dec >= 0; dec--) {
            String fmt = dec > 0 ? "%." + dec + "f" : "%.0f";
            String mS = String.format(Locale.ROOT, fmt, man);
            if (mS.length() <= avail)
                return (neg ? "-" : "") + mS + "e" + expS;
        }
        // worst‐case: truncate integer mantissa
        String mS = intP;
        if (mS.length() > avail) mS = mS.substring(0, avail);
        return (neg ? "-" : "") + mS + "e" + expS;
    }
    
    public static String fmtAccurate(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x))
            return String.valueOf(x);
        
        double a = Math.abs(x);
        if (a != 0 && (a < 1e-30 || a >= 1e30)) {
            // scientific
            return SCI.format(x).replace("E", "e");
        } else {
            return DEC.format(x);
        }
    }
}
