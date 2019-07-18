package com.zeitheron.hammercore.client.utils;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/**
 * This is originally the part of Hammer Lib, repacked in REI with permission.
 * Adapted GL scissor for minecraft pixel resolution and adjusts (0;0) as left-top corner.
 *
 * @author Zeitheron
 */
public class Scissors {
    /**
     * Starts the scissor test
     */
    public static void begin() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    
    /**
     * Setup the scissor bounds
     *
     * @param x      the top left x coordinates
     * @param y      the top left y coordinates
     * @param width  the width of the bounds
     * @param height the height of the bounds
     */
    public static void scissor(int x, int y, int width, int height) {
        MainWindow window = Minecraft.getInstance().mainWindow;
        
        int sw = window.getWidth();
        int sh = window.getHeight();
        float dw = window.getScaledWidth();
        float dh = window.getScaledHeight();
        
        x = Math.round(sw * (x / dw));
        y = Math.round(sh * (y / dh));
        
        width = Math.round(sw * (width / dw));
        height = Math.round(sh * (height / dh));
        
        GL11.glScissor(x, sh - height - y, width, height);
    }
    
    /**
     * Stops the scissor test
     */
    public static void end() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}