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
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.renderers.FluidRenderer;
import me.shedaniel.rei.gui.renderers.ItemStackRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SlotWidget extends WidgetWithBounds {
    
    public static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Identifier RECIPE_GUI_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    protected int x, y;
    private List<Renderer> renderers = new LinkedList<>();
    private boolean drawBackground, showToolTips, clickToMoreRecipes, drawHighlightedBackground;
    
    public SlotWidget(int x, int y, Renderer renderer, boolean drawBackground, boolean showToolTips) {
        this(x, y, Collections.singletonList(renderer), drawBackground, showToolTips);
    }
    
    public SlotWidget(int x, int y, Renderer renderer, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        this(x, y, Collections.singletonList(renderer), drawBackground, showToolTips, clickToMoreRecipes);
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
    
    public SlotWidget(int x, int y, List<Renderer> itemList, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        this(x, y, itemList, drawBackground, showToolTips);
        this.clickToMoreRecipes = clickToMoreRecipes;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
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
        boolean darkTheme = ScreenHelper.isDarkModeEnabled();
        if (drawBackground) {
            minecraft.getTextureManager().bindTexture(darkTheme ? RECIPE_GUI_DARK : RECIPE_GUI);
            blit(this.x - 1, this.y - 1, 0, 222, 18, 18);
        }
        boolean highlighted = containsMouse(mouseX, mouseY);
        if (drawHighlightedBackground && highlighted) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.colorMask(true, true, true, false);
            int color = darkTheme ? 0xFF5E5E5E : -2130706433;
            fillGradient(x, y, x + 16, y + 16, color, color);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepthTest();
        }
        if (isCurrentRendererItem() && !getCurrentItemStack().isEmpty()) {
            renderer.setBlitOffset(200);
            renderer.render(x + 8, y + 6, mouseX, mouseY, delta);
            if (!getCurrentItemStack().isEmpty() && highlighted && showToolTips)
                queueTooltip(getCurrentItemStack(), delta);
        } else if (isCurrentRendererFluid()) {
            renderer.setBlitOffset(200);
            renderer.render(x + 8, y + 6, mouseX, mouseY, delta);
            if (((FluidRenderer) renderer).getFluid() != null && highlighted && showToolTips)
                queueTooltip(((FluidRenderer) renderer).getFluid(), delta);
        } else {
            renderer.setBlitOffset(200);
            renderer.render(x + 8, y + 6, mouseX, mouseY, delta);
        }
    }
    
    public int getBlitOffset() {
        return this.blitOffset;
    }
    
    public void setBlitOffset(int offset) {
        this.blitOffset = offset;
    }
    
    protected void queueTooltip(Fluid fluid, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(fluid)));
    }
    
    private List<String> getTooltip(Fluid fluid) {
        List<String> toolTip = Lists.newArrayList(EntryListOverlay.tryGetFluidName(fluid));
        toolTip.addAll(getExtraFluidToolTips(fluid));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromIdentifier(Registry.FLUID.getId(fluid));
            boolean alreadyHasMod = false;
            for (String s : toolTip)
                if (s.equalsIgnoreCase(modString)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(modString);
        }
        return toolTip;
    }
    
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    protected List<String> getTooltip(ItemStack itemStack) {
        List<String> toolTip = Lists.newArrayList(EntryListOverlay.tryGetItemStackToolTip(itemStack, true));
        toolTip.addAll(getExtraItemToolTips(itemStack));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
            String s1 = ClientHelper.getInstance().getModFromItem(itemStack.getItem()).toLowerCase(Locale.ROOT);
            if (!modString.isEmpty()) {
                toolTip.removeIf(s -> s.toLowerCase(Locale.ROOT).contains(s1));
                toolTip.add(modString);
            }
        }
        return toolTip;
    }
    
    protected List<String> getExtraItemToolTips(ItemStack stack) {
        return Collections.emptyList();
    }
    
    protected List<String> getExtraFluidToolTips(Fluid fluid) {
        return Collections.emptyList();
    }
    
    public ItemStack getCurrentItemStack() {
        if (getCurrentRenderer() instanceof ItemStackRenderer)
            return ((ItemStackRenderer) getCurrentRenderer()).getItemStack();
        return ItemStack.EMPTY;
    }
    
    public Renderer getCurrentRenderer() {
        if (renderers.size() == 0)
            return Renderer.empty();
        return renderers.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) renderers.size()) / 1f));
    }
    
    public void setItemList(List<ItemStack> itemList) {
        this.setRenderers(itemList.stream().map(Renderer::fromItemStack).collect(Collectors.toList()));
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
    
    public boolean isCurrentRendererFluid() {
        return getCurrentRenderer() instanceof FluidRenderer;
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
