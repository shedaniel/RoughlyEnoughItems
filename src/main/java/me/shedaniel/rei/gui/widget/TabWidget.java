/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.ChatFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TabWidget extends HighlightableWidget {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final Identifier CHEST_GUI_TEXTURE_DARK = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer_dark.png");
    
    private final Consumer<QueuedTooltip> tooltipRenderer;
    private final List<ItemStack> slots;
    public boolean shown = false, selected = false;
    public Renderer renderer;
    public int id;
    public String categoryName;
    public Rectangle bounds;
    public RecipeCategory category;
    
    public TabWidget(int id, Rectangle bounds) {
        this.id = id;
        this.bounds = bounds;
        this.slots = Lists.newArrayList();
        this.tooltipRenderer = tooltip -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int specWidth = MathHelper.clamp(slots.size(), 1, 9) * 18 + 10;
            int specHeight = Math.max(1, MathHelper.ceil(slots.size() / 9)) * 18 + 10;
            List<String> lines = tooltip.getText();
            TextRenderer font = client.textRenderer;
            int width = Math.max(lines.stream().map(font::getStringWidth).max(Integer::compareTo).get(), specWidth);
            int tooltipHeight = lines.size() <= 1 ? 8 : lines.size() * 10;
            int height = (lines.isEmpty() ? 0 : (tooltipHeight + 10)) + specHeight;
            ScreenHelper.drawHoveringWidget(tooltip.getX(), tooltip.getY(), (x, y, aFloat) -> {
                int currentY = y;
                if (!lines.isEmpty()) {
                    GlStateManager.disableRescaleNormal();
                    GuiLighting.disable();
                    GlStateManager.disableLighting();
                    this.blitOffset = 1000;
                    this.fillGradient(x - 3, y - 4, x + width + 3, y - 3, -267386864, -267386864);
                    this.fillGradient(x - 3, y + tooltipHeight + 3, x + width + 3, y + tooltipHeight + 4, -267386864, -267386864);
                    this.fillGradient(x - 3, y - 3, x + width + 3, y + tooltipHeight + 3, -267386864, -267386864);
                    this.fillGradient(x - 4, y - 3, x - 3, y + tooltipHeight + 3, -267386864, -267386864);
                    this.fillGradient(x + width + 3, y - 3, x + width + 4, y + tooltipHeight + 3, -267386864, -267386864);
                    this.fillGradient(x - 3, y - 3 + 1, x - 3 + 1, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
                    this.fillGradient(x + width + 2, y - 3 + 1, x + width + 3, y + tooltipHeight + 3 - 1, 1347420415, 1344798847);
                    this.fillGradient(x - 3, y - 3, x + width + 3, y - 3 + 1, 1347420415, 1347420415);
                    this.fillGradient(x - 3, y + tooltipHeight + 2, x + width + 3, y + tooltipHeight + 3, 1344798847, 1344798847);
                    for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                        GlStateManager.disableDepthTest();
                        font.drawWithShadow(lines.get(lineIndex), x, currentY, -1);
                        GlStateManager.enableDepthTest();
                        currentY += lineIndex == 0 ? 12 : 10;
                    }
                    this.blitOffset = 0;
                    GlStateManager.enableLighting();
                    GuiLighting.enable();
                    GlStateManager.enableRescaleNormal();
                    currentY += 6;
                }
                List<Pair<Point, ItemStack>> pairs = Lists.newArrayList();
                GlStateManager.pushMatrix();
                GlStateManager.translatef(x, currentY, 1000f);
                new CategoryBaseWidget(new Rectangle(specWidth, specHeight)).render();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.translatef(x, currentY, 0f);
                int currentX = 5;
                int currentYY = 5;
                int i = 0;
                this.blitOffset = 1000;
                for(ItemStack itemStack : slots) {
                    i++;
                    minecraft.getTextureManager().bindTexture(RoughlyEnoughItemsCore.getConfigManager().getConfig().darkTheme ? SlotWidget.RECIPE_GUI_DARK : SlotWidget.RECIPE_GUI);
                    blit(currentX, currentYY, 0, 222, 18, 18);
                    pairs.add(new Pair<>(new Point(x + currentX + 1, currentY + currentYY + 1), itemStack));
                    currentX += 18;
                    if (i > 9) {
                        i = 1;
                        currentX = 5;
                        currentYY += 18;
                    }
                }
                this.blitOffset = 0;
                GlStateManager.popMatrix();
                ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                pairs.forEach(pair -> {
                    ItemStack stack = pair.getRight();
                    itemRenderer.zOffset = 1300;
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GuiLighting.enableForItems();
                    int x1 = pair.getLeft().x;
                    int y1 = pair.getLeft().y;
                    blitOffset = 1300;
                    GlStateManager.disableDepthTest();
                    fillGradient(x1, y1, x1 + 16, y1 + 16, 0xFFFFFFFF, 0xFFFFFFFF);
                    GlStateManager.enableDepthTest();
                    blitOffset = 0;
                    itemRenderer.renderGuiItem(stack, x1, y1);
                    itemRenderer.renderGuiItemOverlay(font, stack, x1, y1);
                    itemRenderer.zOffset = 0;
                });
            }, width, height, 0);
        };
    }
    
    public void setRenderer(RecipeCategory category, Renderer renderable, String categoryName, boolean selected) {
        slots.clear();
        if (renderable == null) {
            shown = false;
            this.renderer = null;
        } else {
            shown = true;
            this.renderer = renderable;
            this.slots.addAll(RoughlyEnoughItemsCore.getRecipeHelper().getWorkingStations(category.getIdentifier()));
        }
        this.category = category;
        this.selected = selected;
        this.categoryName = categoryName;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isShown() {
        return shown;
    }
    
    public Renderer getRenderer() {
        return renderer;
    }
    
    @Override
    public List<Widget> children() {
        return Collections.emptyList();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (shown) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiLighting.disable();
            minecraft.getTextureManager().bindTexture(RoughlyEnoughItemsCore.getConfigManager().getConfig().darkTheme ? CHEST_GUI_TEXTURE_DARK : CHEST_GUI_TEXTURE);
            this.blit(bounds.x, bounds.y + 2, selected ? 28 : 0, 192, 28, (selected ? 30 : 27));
            renderer.setBlitOffset(100);
            renderer.render((int) bounds.getCenterX(), (int) bounds.getCenterY(), mouseX, mouseY, delta);
            if (isHighlighted(mouseX, mouseY)) {
                drawTooltip();
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private void drawTooltip() {
        if (this.minecraft.options.advancedItemTooltips)
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ChatFormat.DARK_GRAY.toString() + category.getIdentifier().toString(), ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())).setSpecialRenderer(tooltipRenderer));
        else
            ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(categoryName, ClientHelper.getInstance().getFormattedModFromIdentifier(category.getIdentifier())).setSpecialRenderer(tooltipRenderer));
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
}
