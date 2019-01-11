package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemSlotWidget extends Drawable implements HighlightableWidget {
    
    private static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<ItemStack> itemList = new LinkedList<>();
    private boolean drawBackground, showToolTips;
    private int x, y;
    private IMixinContainerGui containerGui;
    
    public ItemSlotWidget(int x, int y, ItemStack itemStack, boolean drawBackground, boolean showToolTips, IMixinContainerGui containerGui) {
        this(x, y, Arrays.asList(itemStack), drawBackground, showToolTips, containerGui);
    }
    
    public ItemSlotWidget(int x, int y, List<ItemStack> itemList, boolean drawBackground, boolean showToolTips, IMixinContainerGui containerGui) {
        this.itemList = itemList;
        this.drawBackground = drawBackground;
        this.showToolTips = showToolTips;
        this.x = x;
        this.y = y;
        this.containerGui = containerGui;
    }
    
    public boolean isDrawBackground() {
        return drawBackground;
    }
    
    @Override
    public List<IWidget> getListeners() {
        return new ArrayList<>();
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        final ItemStack itemStack = getCurrentStack();
        if (drawBackground) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(RECIPE_GUI);
            drawTexturedRect(this.x - 1, this.y - 1, 0, 222, 18, 18);
        }
        if (itemStack.isEmpty())
            return;
        GuiLighting.enableForItems();
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        itemRenderer.zOffset = 200.0F;
        itemRenderer.renderItemAndGlowInGui(itemStack, x, y);
        assert containerGui != null;
        if (containerGui.getDraggedStack().isEmpty())
            itemRenderer.renderItemOverlaysInGUIWithText(MinecraftClient.getInstance().fontRenderer, itemStack, x, y - 0, getItemCountOverlay(itemStack));
        else
            itemRenderer.renderItemOverlaysInGUIWithText(MinecraftClient.getInstance().fontRenderer, itemStack, x, y - 8, getItemCountOverlay(itemStack));
        itemRenderer.zOffset = 0.0F;
        if (isHighlighted(mouseX, mouseY) && showToolTips) {
            List<String> toolTip = getTooltip(itemStack);
            containerGui.getOverlay().addTooltip(new QueuedTooltip(new Point(mouseX, mouseY), toolTip));
        }
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        final String modString = "§9§o" + ClientHelper.getModFromItemStack(itemStack);
        MinecraftClient mc = MinecraftClient.getInstance();
        List<String> toolTip = Lists.newArrayList();
        if (containerGui != null)
            toolTip = containerGui.getContainerGui().getStackTooltip(itemStack).stream().filter(s -> !s.equals(modString)).collect(Collectors.toList());
        else toolTip.add(itemStack.getDisplayName().getFormattedText());
        toolTip.add("§9§o" + ClientHelper.getModFromItemStack(itemStack));
        return toolTip;
    }
    
    protected String getItemCountOverlay(ItemStack currentStack) {
        return "";
    }
    
    protected ItemStack getCurrentStack() {
        if (itemList.size() == 0)
            return new ItemStack(Items.AIR);
        return itemList.get(MathHelper.clamp((int) (System.currentTimeMillis() / 500) % itemList.size(), 0, itemList.size() - 1));
    }
    
    public void setItemList(List<ItemStack> itemList) {
        this.itemList = itemList;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.x, this.y, 18, 18);
    }
    
}
