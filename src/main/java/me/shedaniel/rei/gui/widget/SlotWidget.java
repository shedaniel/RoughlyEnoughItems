/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.compat.RenderHelper;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Entry;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.api.annotations.ToBeRemoved;
import me.shedaniel.rei.gui.renderers.FluidRenderer;
import me.shedaniel.rei.gui.renderers.ItemStackRenderer;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class SlotWidget extends WidgetWithBounds {
    
    public static final Identifier RECIPE_GUI = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Identifier RECIPE_GUI_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    private Rectangle bounds;
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
        this.bounds = new Rectangle(x - 1, y - 1, 18, 18);
        this.clickToMoreRecipes = false;
        this.drawHighlightedBackground = true;
    }
    
    public SlotWidget(int x, int y, List<Renderer> itemList, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        this(x, y, itemList, drawBackground, showToolTips);
        this.clickToMoreRecipes = clickToMoreRecipes;
    }
    
    /**
     * @see WidgetWithBounds#getBounds()
     */
    @ToBeRemoved
    @Deprecated
    public int getX() {
        return bounds.x + 1;
    }
    
    /**
     * @see WidgetWithBounds#getBounds()
     */
    @ToBeRemoved
    @Deprecated
    public void setX(int x) {
        bounds.x = x - 1;
    }
    
    /**
     * @see WidgetWithBounds#getBounds()
     */
    @ToBeRemoved
    @Deprecated
    public int getY() {
        return bounds.y + 1;
    }
    
    /**
     * @see WidgetWithBounds#getBounds()
     */
    @ToBeRemoved
    @Deprecated
    public void setY(int y) {
        bounds.y = y - 1;
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
            blit(bounds.x, bounds.y, 0, 222, bounds.width, bounds.height);
        }
        boolean highlighted = containsMouse(mouseX, mouseY);
        renderer.setBlitOffset(200);
        renderer.render(bounds.x + 9, bounds.y + 7, mouseX, mouseY, delta);
        if (highlighted && showToolTips) {
            QueuedTooltip queuedTooltip = renderer.getQueuedTooltip(delta);
            if (queuedTooltip != null) {
                ScreenHelper.getLastOverlay().addTooltip(queuedTooltip);
            }
        }
        if (drawHighlightedBackground && highlighted) {
            RenderHelper.disableLighting();
            RenderHelper.disableDepthTest();
            RenderHelper.colorMask(true, true, true, false);
            int color = darkTheme ? -1877929711 : -2130706433;
            blitOffset = 300;
            fillGradient(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, color, color);
            blitOffset = 0;
            RenderHelper.colorMask(true, true, true, true);
            RenderHelper.enableLighting();
            RenderHelper.enableDepthTest();
        }
    }
    
    @ToBeRemoved
    @Deprecated
    public int getBlitOffset() {
        return getZ();
    }
    
    @ToBeRemoved
    @Deprecated
    public void setBlitOffset(int offset) {
        setZ(offset);
    }
    
    /**
     * @deprecated Not used anymore, see {@link Renderer#getQueuedTooltip(float)}
     */
    @ToBeRemoved
    @Deprecated
    protected void queueTooltip(Fluid fluid, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(fluid)));
    }
    
    /**
     * @deprecated Not used anymore, see {@link Renderer#getQueuedTooltip(float)}
     */
    @ToBeRemoved
    @Deprecated
    private List<String> getTooltip(Fluid fluid) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetFluidName(fluid));
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
    
    /**
     * @deprecated Not used anymore, see {@link Renderer#getQueuedTooltip(float)}
     */
    @ToBeRemoved
    @Deprecated
    protected void queueTooltip(ItemStack itemStack, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(itemStack)));
    }
    
    /**
     * @deprecated Not used anymore, see {@link Renderer#getQueuedTooltip(float)}
     */
    @ToBeRemoved
    @Deprecated
    protected List<String> getTooltip(ItemStack itemStack) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetItemStackToolTip(itemStack, true));
        toolTip.addAll(getExtraItemToolTips(itemStack));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromItem(itemStack.getItem());
            String s1 = ClientHelper.getInstance().getModFromItem(itemStack.getItem()).toLowerCase(Locale.ROOT);
            if (!modString.isEmpty()) {
                toolTip.removeIf(s -> !s.equals(toolTip.get(0)) && Formatting.strip(s).equalsIgnoreCase(s1));
                toolTip.add(modString);
            }
        }
        return toolTip;
    }
    
    /**
     * @deprecated See {@link ItemStackRenderer#getExtraToolTips(ItemStack)}
     */
    @ToBeRemoved
    @Deprecated
    protected List<String> getExtraItemToolTips(ItemStack stack) {
        return Collections.emptyList();
    }
    
    /**
     * @deprecated See {@link FluidRenderer#getExtraToolTips(Fluid)}
     */
    @ToBeRemoved
    @Deprecated
    protected List<String> getExtraFluidToolTips(Fluid fluid) {
        return Collections.emptyList();
    }
    
    @ToBeRemoved
    @Deprecated
    public ItemStack getCurrentItemStack() {
        if (getCurrentRenderer() instanceof ItemStackRenderer)
            return ((ItemStackRenderer) getCurrentRenderer()).getItemStack();
        return ItemStack.EMPTY;
    }
    
    public Renderer getCurrentRenderer() {
        if (renderers.isEmpty())
            return Renderer.empty();
        return renderers.get(MathHelper.floor((System.currentTimeMillis() / 500 % (double) renderers.size()) / 1f));
    }
    
    /**
     * @param itemList the list of items
     * @deprecated Use {@link SlotWidget#setRenderers(List)}
     */
    @ToBeRemoved
    @Deprecated
    public void setItemList(List<ItemStack> itemList) {
        this.setRenderers(itemList.stream().map(Renderer::fromItemStack).collect(Collectors.toList()));
    }
    
    public void setRenderers(List<Renderer> renderers) {
        this.renderers = renderers;
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clickToMoreRecipes)
            return false;
        Optional<Entry> entry = getCurrentRenderer().getEntry();
        if (entry.isPresent() && getBounds().contains(mouseX, mouseY))
            if (button == 0)
                return ClientHelper.getInstance().executeRecipeKeyBind(entry.get());
            else if (button == 1)
                return ClientHelper.getInstance().executeUsageKeyBind(entry.get());
        return false;
    }
    
    @ToBeRemoved
    @Deprecated
    public boolean isCurrentRendererItem() {
        return getCurrentRenderer() instanceof ItemStackRenderer;
    }
    
    @ToBeRemoved
    @Deprecated
    public boolean isCurrentRendererFluid() {
        return getCurrentRenderer() instanceof FluidRenderer;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (!clickToMoreRecipes)
            return false;
        Optional<Entry> entry = getCurrentRenderer().getEntry();
        if (entry.isPresent() && getBounds().contains(PointHelper.fromMouse()))
            if (ClientHelper.getInstance().getRecipeKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeRecipeKeyBind(entry.get());
            else if (ClientHelper.getInstance().getUsageKeyBinding().matchesKey(int_1, int_2))
                return ClientHelper.getInstance().executeUsageKeyBind(entry.get());
        return false;
    }
    
}
