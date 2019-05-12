/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.cloth.api.ClientUtils;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Renderable;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.renderables.ItemStackRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SlotWidget extends HighlightableWidget {
    
    private static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<Renderer> renderers = new LinkedList<>();
    private boolean drawBackground, showToolTips, clickToMoreRecipes, drawHighlightedBackground;
    private int x, y;
    private static final ItemStackRenderer TROPICAL_FISH_RENDERABLE = Renderable.fromItemStack(Items.TROPICAL_FISH.getDefaultStack());
    
    public SlotWidget(int x, int y, ItemStack itemStack, boolean drawBackground, boolean showToolTips) {
        this(x, y, Collections.singletonList(itemStack), drawBackground, showToolTips);
    }
    
    public SlotWidget(int x, int y, Collection<ItemStack> itemList, boolean drawBackground, boolean showToolTips) {
        this(x, y, itemList.stream().map(Renderable::fromItemStack).collect(Collectors.toList()), drawBackground, showToolTips);
    }
    
    public SlotWidget(int x, int y, List<Renderer> renderers, boolean drawBackground, boolean showToolTips) {
        this.renderers = renderers;
        this.drawBackground = drawBackground;
        this.showToolTips = showToolTips;
        this.x = x;
        this.y = y;
        this.clickToMoreRecipes = false;
        this.drawHighlightedBackground = true;
    }
    
    public SlotWidget(int x, int y, List<ItemStack> itemList, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        this(x, y, itemList, drawBackground, showToolTips);
        this.clickToMoreRecipes = clickToMoreRecipes;
    }
    
    public boolean isShowToolTips() {
        return showToolTips;
    }
    
    public void setShowToolTips(boolean showToolTips) {
        this.showToolTips = showToolTips;
    }
    
    public boolean isClickToMoreRecipes() {
        return clickToMoreRecipes;
    }
    
    public void setClickToMoreRecipes(boolean clickToMoreRecipes) {
        this.clickToMoreRecipes = clickToMoreRecipes;
    }
    
    public boolean isDrawHighlightedBackground() {
        return drawHighlightedBackground;
    }
    
    public void setDrawHighlightedBackground(boolean drawHighlightedBackground) {
        this.drawHighlightedBackground = drawHighlightedBackground;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    public boolean isDrawBackground() {
        return drawBackground;
    }
    
    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        Renderer renderer = getCurrentRenderer();
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
        if (isCurrentRendererItem() && !getCurrentItemStack().isEmpty()) {
            if (RoughlyEnoughItemsCore.getConfigManager().getConfig().aprilFoolsFish2019 && !highlighted)
                renderer = TROPICAL_FISH_RENDERABLE;
            renderer.setBlitOffset(200);
            renderer.render(x + 8, y + 6, mouseX, mouseY, delta);
            if (!getCurrentItemStack().isEmpty() && highlighted && showToolTips)
                queueTooltip(getCurrentItemStack(), delta);
        } else {
            renderer.setBlitOffset(200);
            renderer.render(x + 8, y + 6, mouseX, mouseY, delta);
        }
    }
    
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
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
    
    public ItemStack getCurrentItemStack() {
        if (getCurrentRenderer() instanceof ItemStackRenderer)
            return ((ItemStackRenderer) getCurrentRenderer()).getItemStack();
        return ItemStack.EMPTY;
    }
    
    public Renderer getCurrentRenderer() {
        if (renderers.size() == 0)
            return Renderable.empty();
        return renderers.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) renderers.size()) / 1f));
    }
    
    public void setItemList(List<ItemStack> itemList) {
        this.setRenderers(itemList.stream().map(Renderable::fromItemStack).collect(Collectors.toList()));
    }
    
    public void setRenderers(List<Renderer> renderers) {
        this.renderers = renderers;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(this.x - 1, this.y - 1, 18, 18);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clickToMoreRecipes)
            return false;
        if (isCurrentRendererItem() && getBounds().contains(mouseX, mouseY))
            if (button == 0)
                return ClientHelper.getInstance().executeRecipeKeyBind(getCurrentItemStack());
            else if (button == 1)
                return ClientHelper.getInstance().executeUsageKeyBind(getCurrentItemStack());
        return false;
    }
    
    public boolean isCurrentRendererItem() {
        return getCurrentRenderer() instanceof ItemStackRenderer;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!clickToMoreRecipes)
            return false;
        if (isCurrentRendererItem() && getBounds().contains(ClientUtils.getMouseLocation()))
            if (ClientHelper.getInstance().getRecipeKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(getCurrentItemStack());
            else if (ClientHelper.getInstance().getUsageKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(getCurrentItemStack());
        return false;
    }
    
}
