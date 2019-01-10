package me.shedaniel.rei.gui;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.ItemListOverlay;
import me.shedaniel.rei.gui.widget.LabelWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerGuiOverlay extends Gui {
    
    private Rectangle rectangle;
    private IMixinContainerGui containerGui;
    private Window window;
    private static int page = 0;
    private final List<IWidget> widgets;
    private ItemListOverlay itemListOverlay;
    private ButtonWidget buttonLeft, buttonRight;
    private final List<QueuedTooltip> queuedTooltips;
    
    public ContainerGuiOverlay(ContainerGui containerGui) {
        this.queuedTooltips = new ArrayList<>();
        this.containerGui = (IMixinContainerGui) containerGui;
        this.widgets = new ArrayList<>();
    }
    
    public void onInitialized() {
        //Update Variables
        this.widgets.clear();
        this.window = MinecraftClient.getInstance().window;
        if (MinecraftClient.getInstance().currentGui instanceof ContainerGui)
            this.containerGui = (IMixinContainerGui) MinecraftClient.getInstance().currentGui;
        this.rectangle = calculateBoundary();
        widgets.add(this.itemListOverlay = new ItemListOverlay(this, containerGui, page));
        
        this.itemListOverlay.updateList(getItemListArea(), page);
        addButton(buttonLeft = new ButtonWidget(-1, rectangle.x, rectangle.y + 3, 16, 20, "<") {
            @Override
            public void onPressed(double double_1, double double_2) {
                page--;
                if (page < 0)
                    page = getTotalPage();
                itemListOverlay.updateList(getItemListArea(), page);
            }
        });
        addButton(buttonRight = new ButtonWidget(-1, rectangle.x + rectangle.width - 18, rectangle.y + 3, 16, 20, ">") {
            @Override
            public void onPressed(double double_1, double double_2) {
                page++;
                if (page > getTotalPage())
                    page = 0;
                itemListOverlay.updateList(getItemListArea(), page);
            }
        });
        this.widgets.add(new LabelWidget(rectangle.x + (rectangle.width / 2), rectangle.y + 10, "") {
            @Override
            public void draw(int mouseX, int mouseY, float partialTicks) {
                this.text = String.format("%s/%s", page + 1, getTotalPage() + 1);
                super.draw(mouseX, mouseY, partialTicks);
            }
        });
        
        this.listeners.addAll(widgets);
    }
    
    private Rectangle getItemListArea() {
        return new Rectangle(rectangle.x + 2, rectangle.y + 24, rectangle.width - 4, rectangle.height - 27);
    }
    
    public Rectangle getRectangle() {
        return rectangle;
    }
    
    public void render(int mouseX, int mouseY, float partialTicks) {
        draw(mouseX, mouseY, partialTicks);
        queuedTooltips.forEach(queuedTooltip -> containerGui.getContainerGui().drawTooltip(queuedTooltip.text, queuedTooltip.mouse.x, queuedTooltip.mouse.y));
        queuedTooltips.clear();
    }
    
    public void addTooltip(QueuedTooltip queuedTooltip) {
        queuedTooltips.add(queuedTooltip);
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        widgets.forEach(widget -> widget.draw(int_1, int_2, float_1));
        itemListOverlay.draw(int_1, int_2, float_1);
        super.draw(int_1, int_2, float_1);
    }
    
    private Rectangle calculateBoundary() {
        int startX = containerGui.getContainerLeft() + containerGui.getContainerWidth() + 10;
        int width = window.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, window.getScaledHeight());
    }
    
    private int getTotalPage() {
        return MathHelper.ceil(ClientHelper.getItemList().size() / itemListOverlay.getTotalSlotsPerPage());
    }
    
    @Override
    public boolean mouseScrolled(double amount) {
        if (rectangle.contains(ClientHelper.getMouseLocation())) {
            if (amount > 0 && buttonLeft.enabled)
                buttonLeft.onPressed(0, 0);
            else if (amount < 0 && buttonRight.enabled)
                buttonRight.onPressed(0, 0);
            else return false;
            return true;
        }
        for(IWidget widget : widgets)
            if (widget.mouseScrolled(amount))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        for(IWidget widget : widgets)
            if (widget.mouseClicked(double_1, double_2, int_1))
                return true;
        return false;
    }
}
