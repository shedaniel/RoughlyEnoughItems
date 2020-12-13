package me.shedaniel.rei.api.entry;

import me.shedaniel.rei.api.Renderer;
import net.minecraft.client.gui.GuiComponent;

public abstract class AbstractRenderer extends GuiComponent implements Renderer {
    @Override
    public int getZ() {
        return getBlitOffset();
    }
    
    @Override
    public void setZ(int z) {
        setBlitOffset(z);
    }
}
