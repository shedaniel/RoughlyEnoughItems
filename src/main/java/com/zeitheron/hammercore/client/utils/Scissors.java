package com.zeitheron.hammercore.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

/**
 * This is originally the part of Hammer Lib, repacked in REI with permission.
 * Adapted GL scissor for minecraft pixel resolution and adjusts (0;0) as left-top corner.
 *
 * @author Zeitheron
 */
public class Scissors {
    public static void begin() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    
    public static void scissor(int x, int y, int width, int height) {
        Window window = MinecraftClient.getInstance().window;
        
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
    
    public static void end() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}