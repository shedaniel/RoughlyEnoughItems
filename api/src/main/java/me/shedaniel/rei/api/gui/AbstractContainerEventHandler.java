package me.shedaniel.rei.api.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public abstract class AbstractContainerEventHandler extends GuiComponent implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;
    
    @Override
    public boolean isDragging() {
        return this.isDragging;
    }
    
    @Override
    public void setDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }
    
    @Override
    @Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        this.focused = focused;
    }
}