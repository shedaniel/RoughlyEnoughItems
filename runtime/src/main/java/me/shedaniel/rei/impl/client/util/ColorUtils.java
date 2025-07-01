package me.shedaniel.rei.impl.client.util;

public class ColorUtils {
    public static int withAlpha(int color, double alpha) {
        return withAlpha(color, (float) alpha);
    }
    
    public static int withAlpha(int color, float alpha) {
        boolean colorHasAlpha = (color >>> 24) != 0;
        if (colorHasAlpha) {
            int colorAlpha = color >>> 24 & 0xFF;
            int newAlpha = Math.round(colorAlpha * alpha);
            return (color & 0x00FFFFFF) | (newAlpha << 24);
        } else {
            int newAlpha = Math.round(0xFF * alpha);
            return (color & 0x00FFFFFF) | (newAlpha << 24);
        }
    }
}
