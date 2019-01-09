package me.shedaniel.rei.gui;

import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.LabelWidget;
import me.shedaniel.rei.mixin.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerGuiOverlay extends Gui {
    
    private Rectangle rectangle;
    private IMixinContainerGui containerGui;
    private Window window;
    private int page;
    private List<IWidget> widgets;
    
    public ContainerGuiOverlay(ContainerGui containerGui) {
        this.containerGui = (IMixinContainerGui) containerGui;
    }
    
    @Override
    protected void onInitialized() {
        //Update Variables
        this.widgets = new ArrayList<>();
        this.window = MinecraftClient.getInstance().window;
        if (MinecraftClient.getInstance().currentGui instanceof ContainerGui)
            this.containerGui = (IMixinContainerGui) MinecraftClient.getInstance().currentGui;
        this.page = 0;
        
        rectangle = calculateBoundary();
        addButton(new ButtonWidget(-1, rectangle.x, rectangle.y + 3, 16, 20, "<") {
            @Override
            public void onPressed(double double_1, double double_2) {
                //Left Page
            }
        });
        addButton(new ButtonWidget(-1, rectangle.x + rectangle.width - 18, rectangle.y + 3, 16, 20, ">") {
            @Override
            public void onPressed(double double_1, double double_2) {
                //Right Page
            }
        });
        widgets.add(new LabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, String.format("%s/%s", page + 1, getTotalPage() + 1)));
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        widgets.forEach(widget -> widget.draw(int_1, int_2, float_1));
    }
    
    private Rectangle calculateBoundary() {
        int startX = containerGui.getContainerLeft() + containerGui.getContainerWidth() + 10;
        int width = window.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, window.getScaledHeight());
    }
    
    private int getTotalPage() {
        return 10;
    }
    
}
