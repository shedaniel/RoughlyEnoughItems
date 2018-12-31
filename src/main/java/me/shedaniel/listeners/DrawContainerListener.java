package me.shedaniel.listeners;

import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.listenerdefinitions.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryGui;
import net.minecraft.item.ItemGroup;

/**
 * Created by James on 7/27/2018.
 */
public class DrawContainerListener implements DrawContainer, GuiCickListener, GuiKeyDown, CharInput, ClientTickable, MouseScrollListener {
    
    @Override
    public void draw(int x, int y, float dunno, ContainerGui gui) {
        if (!(gui instanceof CreativePlayerInventoryGui) || ((CreativePlayerInventoryGui) gui).method_2469() == ItemGroup.INVENTORY.getId()) {
            REIRenderHelper.setMouseLoc(x, y);
            REIRenderHelper.drawREI(gui);
        }
    }
    
    @Override
    public boolean onClick(int x, int y, int button) {
        Gui gui = MinecraftClient.getInstance().currentGui;
        if (!(gui instanceof CreativePlayerInventoryGui) || ((CreativePlayerInventoryGui) gui).method_2469() == ItemGroup.INVENTORY.getId())
            return REIRenderHelper.mouseClick(x, y, button);
        return false;
    }
    
    @Override
    public boolean keyDown(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        Gui gui = MinecraftClient.getInstance().currentGui;
        if (!(gui instanceof CreativePlayerInventoryGui) || ((CreativePlayerInventoryGui) gui).method_2469() == ItemGroup.INVENTORY.getId())
            return REIRenderHelper.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return false;
    }
    
    @Override
    public boolean charInput(long p_onCharEvent_1_, int p_onCharEvent_3_, int p_onCharEvent_4_) {
        Gui gui = MinecraftClient.getInstance().currentGui;
        if (!(gui instanceof CreativePlayerInventoryGui) || ((CreativePlayerInventoryGui) gui).method_2469() == ItemGroup.INVENTORY.getId())
            return REIRenderHelper.charInput(p_onCharEvent_1_, p_onCharEvent_3_, p_onCharEvent_4_);
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double direction) {
        Gui gui = MinecraftClient.getInstance().currentGui;
        if (!(gui instanceof CreativePlayerInventoryGui) || ((CreativePlayerInventoryGui) gui).method_2469() == ItemGroup.INVENTORY.getId())
            return REIRenderHelper.mouseScrolled(direction);
        return false;
    }
    
    @Override
    public void clientTick() {
        REIRenderHelper.tick();
    }
}
