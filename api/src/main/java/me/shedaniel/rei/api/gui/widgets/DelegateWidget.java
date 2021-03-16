package me.shedaniel.rei.api.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DelegateWidget extends WidgetWithBounds {
    private static final Rectangle EMPTY = new Rectangle();
    protected final Widget widget;
    private final List<Widget> children;
    
    public DelegateWidget(Widget widget) {
        this.widget = widget;
        this.children = Collections.singletonList(widget);
    }
    
    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        widget.render(poseStack, i, j, f);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public @NotNull Rectangle getBounds() {
        return widget instanceof WidgetWithBounds ? ((WidgetWithBounds) widget).getBounds() : EMPTY;
    }
    
    @Override
    public void setZ(int z) {
        widget.setZ(z);
    }
    
    @Override
    public int getZ() {
        return widget.getZ();
    }
    
    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return widget;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener guiEventListener) {
        widget.setFocused(guiEventListener);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return widget.containsMouse(mouseX, mouseY);
    }
}
