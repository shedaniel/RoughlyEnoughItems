package me.shedaniel.rei.impl.client.gui.widget.search;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CalculatorDisplayUtils {
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
    
    private final int maxLength;
    
    public CalculatorDisplayUtils(int maxLength) {
        this.maxLength = maxLength;
    }
    
    public int maxLength() {
        return maxLength;
    }
    
    public String fmt(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x))
            return String.valueOf(x);
        
        boolean neg = x < 0;
        double a = Math.abs(x);
        
        // 1) SMALL <1: decimals to fill WIDTH, else sci
        if (a > 0 && a < 1) {
            int used = neg ? 1 : 0;
            int avail = this.maxLength - used;       // total chars left
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
        double thresh = neg ? Math.pow(10, this.maxLength - 2) : Math.pow(10, this.maxLength - 1);
        if (a < thresh) {
            String small = (a == Math.rint(a))
                    ? String.valueOf((long) a)
                    : String.format(Locale.ROOT, "%.2f", a)
                    .replaceFirst("\\.?0+$", "");
            if (small.length() + (neg ? 1 : 0) <= this.maxLength)
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
            int avail = this.maxLength - used;
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
    
    private String sciFmt(double a, boolean neg) {
        int exp = (int) Math.floor(Math.log10(a));
        double man = a / Math.pow(10, exp);
        String expS = String.valueOf(exp);
        int used = (neg ? 1 : 0) + 1 + expS.length(); // sign + 'e'+exp
        int avail = this.maxLength - used;
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
