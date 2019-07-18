package me.shedaniel.reiclothconfig2.api;

import net.minecraft.client.Minecraft;

import java.awt.*;

public interface MouseUtils {
    
    static Minecraft client = Minecraft.getInstance();
    
    static Point getMouseLocation() {
        return new Point((int) getMouseX(), (int) getMouseY());
    }
    
    static double getMouseX() {
        return client.mouseHelper.getMouseX() * (double) client.mainWindow.getScaledWidth() / (double) client.mainWindow.getWidth();
    }
    
    static double getMouseY() {
        return client.mouseHelper.getMouseY() * (double) client.mainWindow.getScaledWidth() / (double) client.mainWindow.getWidth();
    }
    
}