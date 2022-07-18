/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.widget.entrylist;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitorWidget;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.impl.client.ClientHelperImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionRenderingDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@ApiStatus.Internal
public abstract class EntryListWidget extends WidgetWithBounds implements OverlayListWidget, DraggableStackVisitorWidget {
    private static final int SIZE = 18;
    protected final RegionRenderingDebugger debugger = new RegionRenderingDebugger();
    protected Rectangle bounds, innerBounds;
    public final NumberAnimator<Double> scaleIndicator = ValueAnimator.ofDouble(0.0D)
            .withConvention(() -> 0.0D, 8000);
    
    public static int entrySize() {
        return Mth.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
    
    public static boolean notSteppingOnExclusionZones(int left, int top, int width, int height) {
        Minecraft instance = Minecraft.getInstance();
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(instance.screen)) {
            InteractionResult fit = canItemSlotWidgetFit(left, top, width, height, decider);
            if (fit != InteractionResult.PASS)
                return fit == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    private static InteractionResult canItemSlotWidgetFit(int left, int top, int width, int height, OverlayDecider decider) {
        InteractionResult fit;
        fit = decider.isInZone(left, top);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left + width, top);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left, top + height);
        if (fit != InteractionResult.PASS)
            return fit;
        fit = decider.isInZone(left + width, top + height);
        return fit;
    }
    
    private boolean containsChecked(Point point, boolean inner) {
        return containsChecked(point.x, point.y, inner);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return hasSpace() && super.containsMouse(mouseX, mouseY);
    }
    
    public boolean innerContainsMouse(double mouseX, double mouseY) {
        return hasSpace() && innerBounds.contains(mouseX, mouseY);
    }
    
    public boolean containsChecked(double x, double y, boolean inner) {
        if (inner) {
            if (!innerContainsMouse(x, y)) return false;
        } else {
            if (!containsMouse(x, y)) return false;
        }
        Minecraft instance = Minecraft.getInstance();
        for (OverlayDecider decider : ScreenRegistry.getInstance().getDeciders(instance.screen)) {
            InteractionResult result = decider.isInZone(x, y);
            if (result != InteractionResult.PASS)
                return result == InteractionResult.SUCCESS;
        }
        return true;
    }
    
    private static Rectangle updateInnerBounds(Rectangle bounds) {
        bounds = bounds.clone();
        int heightReduction = (int) Math.round(bounds.height * (1 - ConfigObject.getInstance().getVerticalEntriesBoundariesPercentage()));
        bounds.y += heightReduction / 2;
        bounds.height -= heightReduction;
        int maxHeight = (int) Math.ceil(entrySize() * ConfigObject.getInstance().getVerticalEntriesBoundariesRows());
        if (bounds.height > maxHeight) {
            bounds.y += (bounds.height - maxHeight) / 2;
            bounds.height = maxHeight;
        }
        int entrySize = entrySize();
        if (ConfigObject.getInstance().isEntryListWidgetScrolled()) {
            int width = Math.max(Mth.floor((bounds.width - 2 - 6) / (float) entrySize), 1);
            if (ConfigObject.getInstance().isLeftHandSidePanel()) {
                return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) + 3), bounds.y, width * entrySize, bounds.height);
            }
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f) - 3), bounds.y, width * entrySize, bounds.height);
        } else {
            int width = Math.max(Mth.floor((bounds.width - 2) / (float) entrySize), 1);
            int height = Math.max(Mth.floor((bounds.height - 2) / (float) entrySize), 1);
            return new Rectangle((int) (bounds.getCenterX() - width * (entrySize / 2f)), (int) (bounds.getCenterY() - height * (entrySize / 2f)), width * entrySize, height * entrySize);
        }
    }
    
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        if (innerBounds.contains(context.getCurrentPosition())) {
            context.renderToVoid(stack);
            return DraggedAcceptorResult.CONSUMED;
        } else {
            return DraggedAcceptorResult.PASS;
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsChecked(mouseX, mouseY, false)) {
            if (Screen.hasControlDown()) {
                ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
                scaleIndicator.setAs(10.0D);
                if (config.setEntrySize(config.getEntrySize() + amount * 0.075)) {
                    ConfigManager.getInstance().saveConfig();
                    REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    return true;
                }
            }
        }
        
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!hasSpace()) return;
        
        boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
        renderEntries(fastEntryRendering, matrices, mouseX, mouseY, delta);
        
        debugger.render(matrices, bounds.x, bounds.y, delta);
        
        if (containsChecked(mouseX, mouseY, false) && ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && !minecraft.player.containerMenu.getCarried().isEmpty() && ClientHelperImpl.getInstance().canDeleteItems()) {
            EntryStack<?> stack = EntryStacks.of(minecraft.player.containerMenu.getCarried().copy());
            if (stack.getType() != VanillaEntryTypes.ITEM) {
                EntryStack<ItemStack> cheatsAs = stack.cheatsAs();
                stack = cheatsAs.isEmpty() ? stack : cheatsAs;
            }
            for (Widget child : children()) {
                if (child.containsMouse(mouseX, mouseY) && child instanceof EntryWidget widget) {
                    if (widget.cancelDeleteItems(stack)) {
                        return;
                    }
                }
            }
            Tooltip.create(Component.translatable("text.rei.delete_items")).queue();
        }
        
        scaleIndicator.update(delta);
        if (scaleIndicator.value() > 0.04) {
            matrices.pushPose();
            matrices.translate(0, 0, 500);
            Component component = Component.literal(Math.round(ConfigObject.getInstance().getEntrySize() * 100) + "%");
            int width = font.width(component);
            int backgroundColor = ((int) Math.round(0xa0 * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            int textColor = ((int) Math.round(0xdd * Mth.clamp(scaleIndicator.value(), 0.0, 1.0))) << 24;
            fillGradient(matrices, bounds.getCenterX() - width / 2 - 2, bounds.getCenterY() - 6, bounds.getCenterX() + width / 2 + 2, bounds.getCenterY() + 6, backgroundColor, backgroundColor);
            font.draw(matrices, component, bounds.getCenterX() - width / 2, bounds.getCenterY() - 4, 0xFFFFFF | textColor);
            matrices.popPose();
        }
    }
    
    protected abstract void renderEntries(boolean fastEntryRendering, PoseStack matrices, int mouseX, int mouseY, float delta);
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsChecked(mouse(), false))
            for (Widget widget : getEntryWidgets())
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        return false;
    }
    
    public void updateArea(Rectangle bounds, String searchTerm) {
        this.bounds = REIRuntime.getInstance().calculateEntryListArea(bounds);
        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
        if (favoritesListWidget != null) {
            favoritesListWidget.updateFavoritesBounds(searchTerm);
        }
        if (ConfigObject.getInstance().isFavoritesEnabled() && favoritesListWidget == null) {
            updateSearch(searchTerm, true);
        } else {
            updateEntriesPosition();
        }
    }
    
    public boolean hasSpace() {
        int entrySize = entrySize();
        int width = innerBounds.width / entrySize;
        int height = innerBounds.height / entrySize;
        return width * height > 0;
    }
    
    public void updateEntriesPosition() {
        int entrySize = entrySize();
        boolean zoomed = ConfigObject.getInstance().isFocusModeZoomed();
        this.innerBounds = updateInnerBounds(bounds);
        updateEntries(entrySize, zoomed);
        FavoritesListWidget favoritesListWidget = ScreenOverlayImpl.getFavoritesListWidget();
        if (favoritesListWidget != null) {
            favoritesListWidget.getSystemRegion().updateEntriesPosition(entry -> true);
            favoritesListWidget.getRegion().updateEntriesPosition(entry -> true);
        }
    }
    
    protected abstract void updateEntries(int entrySize, boolean zoomed);
    
    public abstract List</*EntryStack<?> | CollapsedStack*/ Object> getCollapsedStacks();
    
    protected abstract void setCollapsedStacks(List</*EntryStack<?> | CollapsedStack*/ Object> stacks);
    
    public void updateSearch(String searchTerm, boolean ignoreLastSearch) {
        EntryListSearchManager.INSTANCE.update(searchTerm, ignoreLastSearch, stacks -> {
            setCollapsedStacks(stacks);
            updateEntriesPosition();
        });
        debugger.debugTime = ConfigObject.getInstance().doDebugRenderTimeRequired();
        FavoritesListWidget favorites = ScreenOverlayImpl.getFavoritesListWidget();
        if (favorites != null) {
            favorites.updateSearch();
        }
    }
    
    @Override
    public List<? extends Widget> children() {
        return getEntryWidgets();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!hasSpace()) return false;
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsChecked(mouseX, mouseY, false)) {
            LocalPlayer player = minecraft.player;
            if (ClientHelper.getInstance().isCheating() && !(Minecraft.getInstance().screen instanceof DisplayScreen) && player != null && player.containerMenu != null && !player.containerMenu.getCarried().isEmpty() && ClientHelperImpl.getInstance().canDeleteItems()) {
                EntryStack<?> stack = EntryStacks.of(minecraft.player.containerMenu.getCarried().copy());
                if (stack.getType() != VanillaEntryTypes.ITEM) {
                    EntryStack<ItemStack> cheatsAs = stack.cheatsAs();
                    stack = cheatsAs.isEmpty() ? stack : cheatsAs;
                }
                boolean canDelete = true;
                
                for (Widget child : children()) {
                    if (child.containsMouse(mouseX, mouseY) && child instanceof EntryWidget widget) {
                        if (widget.cancelDeleteItems(stack)) {
                            canDelete = false;
                            break;
                        }
                    }
                }
                
                if (canDelete) {
                    ClientHelper.getInstance().sendDeletePacket();
                    return true;
                }
            }
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        }
        return false;
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = mouse();
        if (containsChecked(mouse, false)) {
            for (Slot entry : getEntryWidgets()) {
                EntryStack<?> currentEntry = entry.getCurrentEntry();
                if (!currentEntry.isEmpty() && entry.containsMouse(mouse)) {
                    return currentEntry.copy();
                }
            }
        }
        return EntryStack.empty();
    }
    
    protected abstract List<EntryListStackEntry> getEntryWidgets();
    
    public void init(ScreenOverlayImpl overlay) {
    }
}
