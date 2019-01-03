package me.shedaniel.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.Core;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.listenerdefinitions.IMixinGuiContainer;
import me.shedaniel.network.CheatPacket;
import me.shedaniel.network.DeletePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by James on 7/28/2018.
 */
public class REISlot extends Control {
    private static final ResourceLocation RECIPE_GUI = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private boolean cheatable = false;
    private List<ItemStack> itemList = new LinkedList<>();
    private int itemListPointer = 0;
    private long displayCounter = 0;
    
    public boolean isDrawBackground() {
        return drawBackground;
    }
    
    private String extraTooltip;
    
    
    @Override
    public void tick() {
        if (itemList.size() > 1) {
            displayCounter++;
            if (displayCounter % 10 == 0)
                if (itemListPointer + 1 >= itemList.size())
                    itemListPointer = 0;
                else itemListPointer++;
        }
    }
    
    public void setStackList(List<ItemStack> newItemList) {
        itemList = newItemList;
        itemListPointer = 0;
        displayCounter = 0;
    }
    
    public void setExtraTooltip(String toolTip) {
        extraTooltip = toolTip;
    }
    
    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }
    
    private boolean drawBackground = false;
    private Point backgroundUV = new Point(0, 222);
    
    public REISlot(int x, int y) {
        super(x, y, 18, 18);
        this.onClick = this::onClick;
    }
    
    public ItemStack getStack() {
        if (itemList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return itemList.get(itemListPointer);
    }
    
    public void setStack(ItemStack stack) {
        itemList.clear();
        if (stack != null)
            itemList.add(stack);
        itemListPointer = 0;
    }
    
    @Override
    public void draw() {
        if (drawBackground) {
            Minecraft.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
            drawTexturedModalRect(rect.x - 1, rect.y - 1, backgroundUV.x, backgroundUV.y, rect.width, rect.height);
        }
        if (getStack().isEmpty())
            return;
        RenderHelper.enableGUIStandardItemLighting();
        
        drawStack(rect.x, rect.y);
        if (isHighlighted())
            drawTooltip();
    }
    
    protected void drawTooltip() {
        List<String> toolTip = getTooltip();
        toolTip.add("§9§o" + getMod());
        Point mouse = REIRenderHelper.getMouseLoc();
        REIRenderHelper.addToolTip(toolTip, mouse.x, mouse.y);
    }
    
    private boolean onClick(int button) {
        EntityPlayer player = Minecraft.getInstance().player;
        if (REIRenderHelper.reiGui.canCheat() && !(player.inventory.getItemStack().isEmpty())) {
            //Delete the itemstack.
            Minecraft.getInstance().getConnection().sendPacket(new DeletePacket());
            return true;
        }
        if (!player.inventory.getItemStack().isEmpty()) {
            return false;
        }
        if (REIRenderHelper.reiGui.canCheat() && this.cheatable) {
            if (getStack() != null && !getStack().isEmpty()) {
                ItemStack cheatedStack = getStack().copy();
                if (button == 0)
                    cheatedStack.setCount(1);
                if (button == 1) {
                    cheatedStack.setCount(cheatedStack.getMaxStackSize());
                }
                Core.cheatItems(cheatedStack.copy());
                return true;
            }
        } else {
            REIRenderHelper.recipeKeybind();
        }
        return false;
    }
    
    
    private void drawStack(int x, int y) {
        GuiContainer gui = REIRenderHelper.getOverlayedGui();
        REIRenderHelper.getItemRender().zLevel = 200.0F;
        REIRenderHelper.getItemRender().renderItemAndEffectIntoGUI(getStack(), x, y);
        assert gui != null;
        if (((IMixinGuiContainer) gui).getDraggedStack().isEmpty())
            REIRenderHelper.getItemRender().renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, getStack(), x, y - 0, getTextOverlay(getStack()));
        else
            REIRenderHelper.getItemRender().renderItemOverlayIntoGUI(Minecraft.getInstance().fontRenderer, getStack(), x, y - 8, getTextOverlay(getStack()));
        REIRenderHelper.getItemRender().zLevel = 0.0F;
    }
    
    public String getTextOverlay(ItemStack stack) {
        return "";
    }
    
    public String getMod() {
        if (!getStack().isEmpty()) {
            ResourceLocation location = IRegistry.ITEM.getKey(getStack().getItem());
            assert location != null;
            return REIRenderHelper.tryGettingModName(location.getNamespace());
        }
        return "";
    }
    
    protected List<String> getTooltip() {
        Minecraft mc = Minecraft.getInstance();
        GuiContainer gui = REIRenderHelper.getOverlayedGui();
        List<String> toolTip = Lists.newArrayList();
        if (gui != null) {
            toolTip = gui.getItemToolTip(getStack());
        } else {
            toolTip.add(getStack().getDisplayName().getFormattedText());
        }
        if (extraTooltip != null) {
            toolTip.add(extraTooltip);
        }
        
        return toolTip;
    }
    
    public boolean isCheatable() {
        return cheatable;
    }
    
    public void setCheatable(boolean cheatable) {
        this.cheatable = cheatable;
    }
    
    
}
