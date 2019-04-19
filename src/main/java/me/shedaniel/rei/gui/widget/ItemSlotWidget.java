package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ItemSlotWidget extends HighlightableWidget {
    
    private static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<ItemStack> itemList = new LinkedList<>();
    private boolean drawBackground, showToolTips, clickToMoreRecipes, drawHighlightedBackground;
    private int x, y;
    
    public ItemSlotWidget(int x, int y, ItemStack itemStack, boolean drawBackground, boolean showToolTips) {
        this(x, y, Collections.singletonList(itemStack), drawBackground, showToolTips);
    }
    
    public ItemSlotWidget(int x, int y, List<ItemStack> itemList, boolean drawBackground, boolean showToolTips) {
        this.itemList = itemList;
        this.drawBackground = drawBackground;
        this.showToolTips = showToolTips;
        this.x = x;
        this.y = y;
        this.clickToMoreRecipes = false;
        this.drawHighlightedBackground = true;
    }
    
    public ItemSlotWidget(int x, int y, List<ItemStack> itemList, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        this(x, y, itemList, drawBackground, showToolTips);
        this.clickToMoreRecipes = clickToMoreRecipes;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    public void setDrawHighlightedBackground(boolean drawHighlightedBackground) {
        this.drawHighlightedBackground = drawHighlightedBackground;
    }
    
    public boolean isDrawBackground() {
        return drawBackground;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        ItemStack itemStack = getCurrentStack().copy();
        if (drawBackground) {
            minecraft.getTextureManager().bindTexture(RECIPE_GUI);
            blit(this.x - 1, this.y - 1, 0, 222, 18, 18);
        }
        boolean highlighted = isHighlighted(mouseX, mouseY);
        if (drawHighlightedBackground && highlighted) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.colorMask(true, true, true, false);
            fillGradient(x, y, x + 16, y + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
        if (!itemStack.isEmpty()) {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019 && !highlighted)
                itemStack = Items.TROPICAL_FISH.getDefaultStack();
            GuiLighting.enableForItems();
            ItemRenderer itemRenderer = minecraft.getItemRenderer();
            itemRenderer.zOffset = 200.0F;
            itemRenderer.renderGuiItem(itemStack, x, y);
            itemRenderer.renderGuiItemOverlay(font, itemStack, x, y, getItemCountOverlay(itemStack));
            itemRenderer.zOffset = 0.0F;
        }
        if (!itemStack.isEmpty() && highlighted && showToolTips)
            queueTooltip(itemStack, delta);
    }
    
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        final String modString = ClientHelper.getFormattedModFromItem(itemStack.getItem());
        List<String> toolTip = Lists.newArrayList(ItemListOverlay.tryGetItemStackToolTip(itemStack, true));
        toolTip.addAll(getExtraToolTips(itemStack));
        boolean alreadyHasMod = false;
        for(String s : toolTip)
            if (s.equalsIgnoreCase(modString)) {
                alreadyHasMod = true;
                break;
            }
        if (!alreadyHasMod)
            toolTip.add(modString);
        return toolTip;
    }
    
    protected List<String> getExtraToolTips(ItemStack stack) {
        return Collections.emptyList();
    }
    
    protected String getItemCountOverlay(ItemStack currentStack) {
        return "";
    }
    
    public ItemStack getCurrentStack() {
        if (itemList.size() == 0)
            return new ItemStack(Items.AIR);
        return itemList.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) itemList.size()) / 1f));
    }
    
    public void setItemList(List<ItemStack> itemList) {
        this.itemList = itemList;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.x - 1, this.y - 1, 18, 18);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clickToMoreRecipes)
            return false;
        if (getBounds().contains(mouseX, mouseY))
            if (button == 0)
                return ClientHelper.executeRecipeKeyBind(getCurrentStack());
            else if (button == 1)
                return ClientHelper.executeUsageKeyBind(getCurrentStack());
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!clickToMoreRecipes)
            return false;
        if (getBounds().contains(ClientUtils.getMouseLocation()))
            if (ClientHelper.RECIPE.matchesKey(int_1, int_2))
                return ClientHelper.executeRecipeKeyBind(getCurrentStack());
            else if (ClientHelper.USAGE.matchesKey(int_1, int_2))
                return ClientHelper.executeUsageKeyBind(getCurrentStack());
        return false;
    }
    
}
