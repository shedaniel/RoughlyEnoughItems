package me.shedaniel.rei.api.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collections;
import java.util.List;

public class NoOpWidget extends Widget {
    public static final NoOpWidget INSTANCE = new NoOpWidget();
    
    private NoOpWidget() {
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }
}
