package me.shedaniel.listeners;

import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.listenerdefinitions.MinecraftResize;

/**
 * Created by James on 7/28/2018.
 */
public class ResizeListener implements MinecraftResize {
    @Override
    public void resize(int scaledWidth, int scaledHeight) {
        REIRenderHelper.resize(scaledWidth, scaledHeight);
    }
}
