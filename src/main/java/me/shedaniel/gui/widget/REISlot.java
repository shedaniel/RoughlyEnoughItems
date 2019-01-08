package me.shedaniel.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.Core;
import me.shedaniel.gui.REIRenderHelper;
import me.shedaniel.listenerdefinitions.IMixinContainerGui;
import me.shedaniel.network.DeletePacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by James on 7/28/2018.
 */
public class REISlot extends Control {
    
    private static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
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
            MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
            drawTexturedModalRect(rect.x - 1, rect.y - 1, backgroundUV.x, backgroundUV.y, rect.width, rect.height);
        }
        if (getStack().isEmpty())
            return;
        GuiLighting.enableForItems();
        
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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (REIRenderHelper.reiGui.canCheat() && !(player.inventory.getCursorStack().isEmpty())) {
            //Delete the itemstack.
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new DeletePacket());
            return true;
        }
        if (!player.inventory.getCursorStack().isEmpty()) {
            return false;
        }
        
        if (REIRenderHelper.reiGui.canCheat() && this.cheatable) {
            if (getStack() != null && !getStack().isEmpty()) {
                ItemStack cheatedStack = getStack().copy();
                if (button == 0)
                    cheatedStack.setAmount(1);
                if (button == 1) {
                    cheatedStack.setAmount(cheatedStack.getMaxAmount());
                }
                Core.cheatItems(cheatedStack);
                return true;
            }
        } else {
            if (button == 0)
                return REIRenderHelper.recipeKeyBind();
            else if (button == 1)
                return REIRenderHelper.useKeyBind();
        }
        return false;
    }
    
    
    private void drawStack(int x, int y) {
        ContainerGui gui = REIRenderHelper.getOverlayedGui();
        REIRenderHelper.getItemRender().zOffset = 200.0F;
        REIRenderHelper.getItemRender().renderItemAndGlowInGui(getStack(), x, y);
        assert gui != null;
        if (((IMixinContainerGui) gui).getDraggedStack().isEmpty())
            REIRenderHelper.getItemRender().renderItemOverlaysInGUIWithText(MinecraftClient.getInstance().fontRenderer, getStack(), x, y - 0, getTextOverlay(getStack()));
        else
            REIRenderHelper.getItemRender().renderItemOverlaysInGUIWithText(MinecraftClient.getInstance().fontRenderer, getStack(), x, y - 8, getTextOverlay(getStack()));
        REIRenderHelper.getItemRender().zOffset = 0.0F;
    }
    
    public String getTextOverlay(ItemStack stack) {
        return "";
    }
    
    public String getMod() {
        if (!getStack().isEmpty()) {
            Identifier location = Registry.ITEM.getId(getStack().getItem());
            assert location != null;
            return REIRenderHelper.tryGettingModName(location.getNamespace());
        }
        return "";
    }
    
    protected List<String> getTooltip() {
        final String modString = "§9§o" + getMod();
        MinecraftClient mc = MinecraftClient.getInstance();
        ContainerGui gui = REIRenderHelper.getOverlayedGui();
        List<String> toolTip = Lists.newArrayList();
        if (gui != null)
            toolTip = gui.getStackTooltip(getStack()).stream().filter(s -> !s.equals(modString)).collect(Collectors.toList());
        else
            toolTip.add(getStack().getDisplayName().getFormattedText());
        if (extraTooltip != null)
            toolTip.add(extraTooltip);
        return toolTip;
    }
    
    public boolean isCheatable() {
        return cheatable;
    }
    
    public void setCheatable(boolean cheatable) {
        this.cheatable = cheatable;
    }
    
    
}
