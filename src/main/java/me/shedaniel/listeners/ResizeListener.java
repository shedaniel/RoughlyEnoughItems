package me.shedaniel.listeners;

import me.shedaniel.gui.AEIRenderHelper;
import me.shedaniel.listenerdefinitions.MinecraftResize;

/**
 * Created by James on 7/28/2018.
 */
public class ResizeListener implements MinecraftResize {
    @Override
    public void resize() {
        AEIRenderHelper.resize();
    }
}
