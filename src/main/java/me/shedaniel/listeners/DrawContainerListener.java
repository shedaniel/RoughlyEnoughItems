package me.shedaniel.listeners;

import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.listenerdefinitions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.ItemGroup;
import org.dimdev.rift.listener.client.ClientTickable;

/**
 * Created by James on 7/27/2018.
 */
public class DrawContainerListener implements MinecraftResize, DrawContainer, GuiCickListener, GuiKeyDown, CharInput, ClientTickable, MouseScrollListener {
    @Override
    public void draw(int x, int y, float dunno, GuiContainer gui) {
        if (!(gui instanceof GuiContainerCreative) || ((GuiContainerCreative) gui).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex()) {
            REIRenderHelper.setMouseLoc(x, y);
            REIRenderHelper.drawREI(gui);
        }
    }
    
    @Override
    public boolean onClick(int x, int y, int button) {
        GuiScreen gui = Minecraft.getInstance().currentScreen;
        if (!(gui instanceof GuiContainerCreative) || ((GuiContainerCreative) gui).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            return REIRenderHelper.mouseClick(x, y, button);
        return false;
    }
    
    @Override
    public boolean keyDown(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        GuiScreen gui = Minecraft.getInstance().currentScreen;
        if (!(gui instanceof GuiContainerCreative) || ((GuiContainerCreative) gui).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            return REIRenderHelper.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        return false;
    }
    
    @Override
    public boolean charInput(long p_onCharEvent_1_, int p_onCharEvent_3_, int p_onCharEvent_4_) {
        GuiScreen gui = Minecraft.getInstance().currentScreen;
        if (!(gui instanceof GuiContainerCreative) || ((GuiContainerCreative) gui).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            return REIRenderHelper.charInput(p_onCharEvent_1_, p_onCharEvent_3_, p_onCharEvent_4_);
        return false;
    }
    
    @Override
    public void clientTick(final Minecraft minecraft) {
        REIRenderHelper.tick();
    }
    
    @Override
    public boolean mouseScrolled(double direction) {
        GuiScreen gui = Minecraft.getInstance().currentScreen;
        if (!(gui instanceof GuiContainerCreative) || ((GuiContainerCreative) gui).getSelectedTabIndex() == ItemGroup.INVENTORY.getIndex())
            return REIRenderHelper.mouseScrolled(direction);
        return false;
    }
    
    @Override
    public void resize(int scaledWidth, int scaledHeight) {
        REIRenderHelper.resize(scaledWidth, scaledHeight);
    }
    
}
