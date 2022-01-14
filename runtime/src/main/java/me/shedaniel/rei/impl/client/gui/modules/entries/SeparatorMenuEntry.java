package me.shedaniel.rei.impl.client.gui.modules.entries;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.rei.impl.client.gui.modules.AbstractMenuEntry;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;

public class SeparatorMenuEntry extends AbstractMenuEntry {
    @Override
    public int getEntryWidth() {
        return 0;
    }
    
    @Override
    public int getEntryHeight() {
        return 5;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        fillGradient(matrices, getX() + 3, getY() + 2, getX() + getWidth() - 3, getY() + 3, -7829368, -7829368);
    }
}