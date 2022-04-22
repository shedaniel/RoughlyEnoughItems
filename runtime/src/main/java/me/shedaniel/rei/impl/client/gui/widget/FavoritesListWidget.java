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

package me.shedaniel.rei.impl.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.SystemFavoriteEntryProvider;
import me.shedaniel.rei.api.client.gui.AbstractContainerEventHandler;
import me.shedaniel.rei.api.client.gui.drag.*;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.favorites.FavoriteEntryTypeRegistryImpl;
import me.shedaniel.rei.impl.client.gui.widget.region.RealRegionEntry;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionDraggableStack;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionEntryListEntry;
import me.shedaniel.rei.impl.client.gui.widget.region.RegionListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.client.gui.widget.EntryListWidget.entrySize;
import static me.shedaniel.rei.impl.client.gui.widget.EntryListWidget.notSteppingOnExclusionZones;

@ApiStatus.Internal
public class FavoritesListWidget extends WidgetWithBounds implements DraggableStackProviderWidget, DraggableStackVisitorWidget, OverlayListWidget, RegionListener<FavoriteEntry> {
    private Rectangle fullBounds;
    private EntryStacksRegionWidget<FavoriteEntry> systemRegion = new EntryStacksRegionWidget<>(new RegionListener<FavoriteEntry>() {
        @Override
        @Nullable
        public FavoriteEntry convertDraggableStack(DraggingContext<Screen> context, DraggableStack stack) {
            return FavoriteEntry.fromEntryStack(stack.getStack().copy());
        }
        
        @Override
        public boolean canAcceptDrop(RealRegionEntry<FavoriteEntry> entry) {
            return false;
        }
        
        @Override
        @Nullable
        public FavoriteEntry asFavorite(RealRegionEntry<FavoriteEntry> entry) {
            return null;
        }
        
        @Override
        public boolean canBeDragged(RealRegionEntry<FavoriteEntry> entry) {
            return RegionListener.super.canBeDragged(entry);
        }
        
        @Override
        public boolean removeOnDrag() {
            return false;
        }
    });
    private EntryStacksRegionWidget<FavoriteEntry> region = new EntryStacksRegionWidget<>(this);
    private List<FavoriteEntry> lastSystemEntries = new ArrayList<>();
    
    public final AddFavoritePanel favoritePanel = new AddFavoritePanel(this);
    private final NumberAnimator<Double> trashBoundsHeight = ValueAnimator.ofDouble().withConvention(() -> {
        if (DraggingContext.getInstance().isDraggingStack() && fullBounds.contains(DraggingContext.getInstance().getCurrentPosition())) {
            return Math.min(60D, fullBounds.height * 0.15D);
        }
        return 0D;
    }, ValueAnimator.typicalTransitionTime());
    private final Rectangle trashBounds = new Rectangle();
    public final ToggleAddFavoritePanelButton favoritePanelButton = new ToggleAddFavoritePanelButton(this);
    private List<Widget> children = ImmutableList.of(favoritePanel, favoritePanelButton, systemRegion, region);
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (fullBounds.contains(mouseX, mouseY)) {
            if (Screen.hasControlDown()) {
                ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
                if (config.setEntrySize(config.getEntrySize() + amount * 0.075)) {
                    ConfigManager.getInstance().saveConfig();
                    REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    return true;
                }
            } else if (favoritePanel.mouseScrolled(mouseX, mouseY, amount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public Rectangle getBounds() {
        return fullBounds;
    }
    
    public EntryStacksRegionWidget<FavoriteEntry> getRegion() {
        return region;
    }
    
    public EntryStacksRegionWidget<FavoriteEntry> getSystemRegion() {
        return systemRegion;
    }
    
    @Override
    public void onDrop(Stream<FavoriteEntry> entries) {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            List<FavoriteEntry> favorites = ConfigObject.getInstance().getFavoriteEntries();
            favorites.clear();
            entries.forEach(entry -> {
                favorites.add(entry.copy());
            });
            
            ConfigManager.getInstance().saveConfig();
        }
    }
    
    @Override
    public void onRemove(RealRegionEntry<FavoriteEntry> entry) {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            List<FavoriteEntry> favorites = ConfigObject.getInstance().getFavoriteEntries();
            favorites.removeIf(favoriteEntry -> Objects.equals(entry.getEntry(), favoriteEntry));
            ConfigManager.getInstance().saveConfig();
        }
    }
    
    @Override
    public void onConsumed(RealRegionEntry<FavoriteEntry> entry) {
        setSystemRegionEntries(entry);
    }
    
    @Override
    @Nullable
    public FavoriteEntry convertDraggableStack(DraggingContext<Screen> context, DraggableStack stack) {
        return FavoriteEntry.fromEntryStack(stack.getStack().copy());
    }
    
    @Override
    @Nullable
    public DraggableStack getHoveredStack(DraggingContext<Screen> context, double mouseX, double mouseY) {
        DraggableStack stack = region.getHoveredStack(context, mouseX, mouseY);
        if (stack != null) return stack;
        stack = systemRegion.getHoveredStack(context, mouseX, mouseY);
        if (stack != null) return stack;
        if (favoritePanel.bounds.contains(mouseX, mouseY)) {
            for (AddFavoritePanel.Row row : favoritePanel.rows.get()) {
                if (row instanceof AddFavoritePanel.SectionEntriesRow entriesRow) {
                    for (AddFavoritePanel.SectionEntriesRow.SectionFavoriteWidget widget : entriesRow.widgets) {
                        if (widget.containsMouse(mouseX, mouseY + favoritePanel.scroller.scrollAmount())) {
                            RealRegionEntry<FavoriteEntry> entry = new RealRegionEntry<>(region, widget.entry.copy(), entrySize());
                            entry.size.setAs(entrySize() * 100);
                            return new RegionDraggableStack<>(entry, widget);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        if (favoritePanel.bounds.contains(context.getCurrentPosition()) || trashBounds.contains(context.getCurrentPosition())) {
            context.renderToVoid(stack);
            return DraggedAcceptorResult.CONSUMED;
        }
        return Stream.of(region, systemRegion)
                .map(visitor -> visitor.acceptDraggedStack(context, stack))
                .filter(result -> result != DraggedAcceptorResult.PASS)
                .findFirst()
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    @Override
    public EntryStack<?> getFocusedStack() {
        Point mouse = PointHelper.ofMouse();
        EntryStack<?> stack = region.getFocusedStack();
        if (stack != null && !stack.isEmpty()) return stack;
        stack = systemRegion.getFocusedStack();
        if (stack != null && !stack.isEmpty()) return stack;
        if (favoritePanel.bounds.contains(mouse)) {
            for (AddFavoritePanel.Row row : favoritePanel.rows.get()) {
                if (row instanceof AddFavoritePanel.SectionEntriesRow entriesRow) {
                    for (AddFavoritePanel.SectionEntriesRow.SectionFavoriteWidget widget : entriesRow.widgets) {
                        if (widget.containsMouse(mouse)) {
                            return ClientEntryStacks.of(widget.entry.getRenderer(false)).copy();
                        }
                    }
                }
            }
        }
        return EntryStack.empty();
    }
    
    @Override
    public Stream<EntryStack<?>> getEntries() {
        return region.getEntries();
    }
    
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (fullBounds.isEmpty())
            return;
        
        this.trashBoundsHeight.update(delta);
        double trashBoundsHeight = this.trashBoundsHeight.value();
        if (Math.round(trashBoundsHeight) > 0) {
            double trashBoundsHeightTarget = Math.min(150D, fullBounds.height * 0.15D);
            double progress = Math.pow(Mth.clamp(trashBoundsHeight / trashBoundsHeightTarget, 0, 1), 7);
            int y = this.fullBounds.getMaxY() - 4 - favoritePanel.getBounds().height;
            trashBounds.setBounds(this.fullBounds.x + 4, (int) Math.round(y - trashBoundsHeight), this.fullBounds.width - 8, (int) Math.round(trashBoundsHeight - 4));
            int alpha = 0x12 + (int) (0x22 * progress * (Mth.cos((float) (System.currentTimeMillis() % 2000 / 1000F * Math.PI)) + 1) / 2);
            fillGradient(matrices, this.trashBounds.x, this.trashBounds.y, this.trashBounds.getMaxX(), this.trashBounds.getMaxY(), 0xFFFFFF | (alpha << 24), 0xFFFFFF | (alpha << 24));
            int lineColor = (int) (0x60 * progress) << 24 | 0xFFFFFF;
            fillGradient(matrices, this.trashBounds.x, this.trashBounds.y, this.trashBounds.getMaxX(), this.trashBounds.y + 1, lineColor, lineColor);
            fillGradient(matrices, this.trashBounds.x, this.trashBounds.getMaxY() - 1, this.trashBounds.getMaxX(), this.trashBounds.getMaxY(), lineColor, lineColor);
            
            fillGradient(matrices, this.trashBounds.x, this.trashBounds.y + 1, this.trashBounds.x + 1, this.trashBounds.getMaxY() - 1, lineColor, lineColor);
            fillGradient(matrices, this.trashBounds.getMaxX() - 1, this.trashBounds.y + 1, this.trashBounds.getMaxX(), this.trashBounds.getMaxY() - 1, lineColor, lineColor);
            
            Component text = Component.translatable("text.rei.dispose_here");
            if (0xAA * progress > 0x4) {
                font.draw(matrices, text, this.trashBounds.getCenterX() - font.width(text) / 2, this.trashBounds.getCenterY() - 4F, (int) (0xAA * progress) << 24 | 0xFFFFFF);
            }
        } else {
            trashBounds.setBounds(0, 0, 0, 0);
        }
        if (!PluginManager.areAnyReloading()) {
            updateSystemRegion();
        }
//        systemRegion.getBounds().setBounds(this.fullBounds.x + 1, this.fullBounds.y - 1 + 14, this.fullBounds.width - 1, Math.max(1, systemRegion.scrolling.getMaxScrollHeight()));
        systemRegion.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + 1, this.fullBounds.width, Math.max(1, systemRegion.scrolling.getMaxScrollHeight()));
        int systemHeight = systemRegion.getBounds().getHeight();
        if (systemHeight > 1 && !region.isEmpty()) {
            Rectangle innerBounds = systemRegion.getInnerBounds();
//            font.draw(matrices, Component.translatable("System Favorites").withStyle(ChatFormatting.UNDERLINE), innerBounds.x - 1 + 4, fullBounds.y - 1 + 4, 0xFFFFFFFF);
            fillGradient(matrices, innerBounds.x + 1, this.fullBounds.y + systemHeight + 2, innerBounds.getMaxX() - 1, this.fullBounds.y + systemHeight + 3, 0xFF777777, 0xFF777777);
//            fillGradient(matrices, innerBounds.x - 2, this.fullBounds.y - 1, innerBounds.getMaxX() + 2, this.fullBounds.y + systemHeight + 1 + 14, 0x34FFFFFF, 0x34FFFFFF);
//            systemHeight += 4 + 14;
            systemHeight += 4;
        }
        if (favoritePanel.getBounds().height > 20)
            region.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + systemHeight, this.fullBounds.width, this.fullBounds.height - systemHeight - (this.fullBounds.getMaxY() - this.favoritePanel.bounds.y) - 4 - (Math.round(trashBoundsHeight) <= 0 ? 0 : trashBoundsHeight));
        else
            region.getBounds().setBounds(this.fullBounds.x, this.fullBounds.y + systemHeight, this.fullBounds.width, this.fullBounds.height - systemHeight - (Math.round(trashBoundsHeight) <= 0 ? 0 : trashBoundsHeight + 24));
        systemRegion.render(matrices, mouseX, mouseY, delta);
        region.render(matrices, mouseX, mouseY, delta);
        renderAddFavorite(matrices, mouseX, mouseY, delta);
    }
    
    private void updateSystemRegion() {
        boolean updated = false;
        List<Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>>> providers = ((FavoriteEntryTypeRegistryImpl) FavoriteEntryType.registry()).getSystemProviders();
        
        for (Triple<SystemFavoriteEntryProvider<?>, MutableLong, List<FavoriteEntry>> pair : providers) {
            SystemFavoriteEntryProvider<?> provider = pair.getLeft();
            MutableLong mutableLong = pair.getMiddle();
            List<FavoriteEntry> entries = pair.getRight();
            
            if (mutableLong.getValue() == -1 || mutableLong.getValue() < System.currentTimeMillis()) {
                mutableLong.setValue(System.currentTimeMillis() + provider.updateInterval());
                List<FavoriteEntry> provide = (List<FavoriteEntry>) provider.provide();
                if (!provide.equals(entries)) {
                    entries.clear();
                    entries.addAll(provide);
                    updated = true;
                }
            }
        }
        
        if (updated) {
            lastSystemEntries = CollectionUtils.flatMap(providers, Triple::getRight);
            setSystemRegionEntries(null);
        }
    }
    
    private void setSystemRegionEntries(@Nullable RealRegionEntry<FavoriteEntry> removed) {
        systemRegion.setEntries(CollectionUtils.filterToList(lastSystemEntries, entry -> {
            if (region.has(entry)) return false;
            if (DraggingContext.getInstance().isDraggingStack()) {
                DraggableStack currentStack = DraggingContext.getInstance().getCurrentStack();
                if (currentStack instanceof RegionDraggableStack) {
                    RegionDraggableStack<?> stack = (RegionDraggableStack<?>) currentStack;
                    
                    if (removed != null && stack.getEntry() == removed) return true;
                    return stack.getEntry().region != region || !Objects.equals(stack.getEntry().getEntry(), entry);
                }
            }
            return true;
        }), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
    }
    
    @Override
    public void onSetNewEntries(List<RegionEntryListEntry<FavoriteEntry>> regionEntryListEntries) {
        setSystemRegionEntries(null);
    }
    
    private void renderAddFavorite(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.favoritePanel.render(matrices, mouseX, mouseY, delta);
        this.favoritePanelButton.render(matrices, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(PointHelper.ofMouse()))
            for (Widget widget : children())
                if (widget.keyPressed(keyCode, scanCode, modifiers))
                    return true;
        return false;
    }
    
    public void updateFavoritesBounds(@Nullable String searchTerm) {
        this.fullBounds = REIRuntime.getInstance().calculateFavoritesListArea();
    }
    
    public void updateSearch() {
        if (ConfigObject.getInstance().isFavoritesEnabled()) {
            region.setEntries(CollectionUtils.map(ConfigObject.getInstance().getFavoriteEntries(), FavoriteEntry::copy), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
        } else region.setEntries(Collections.emptyList(), EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
    }
    
    @Override
    public List<? extends Widget> children() {
        return children;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (systemRegion.mouseClicked(mouseX, mouseY, button) || region.mouseClicked(mouseX, mouseY, button))
            return true;
        for (Widget widget : children())
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY)) {
            for (Widget widget : children())
                if (widget.mouseReleased(mouseX, mouseY, button))
                    return true;
        }
        return false;
    }
    
    public static class ToggleAddFavoritePanelButton extends FadingFavoritePanelButton {
        public ToggleAddFavoritePanelButton(FavoritesListWidget widget) {
            super(widget);
        }
        
        @Override
        protected void onClick() {
            widget.favoritePanel.expendState.setTo(!widget.favoritePanel.expendState.target(), 1500);
            widget.favoritePanel.resetRows();
        }
        
        @Override
        protected void queueTooltip() {
            Tooltip.create(Component.translatable("text.rei.add_favorite_widget")).queue();
        }
        
        @Override
        protected Rectangle updateArea(Rectangle fullArea) {
            return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - 16 - 4, 16, 16);
        }
        
        @Override
        protected boolean isAvailable(int mouseX, int mouseY) {
            boolean expended = widget.favoritePanel.expendState.value();
            return widget.fullBounds.contains(mouseX, mouseY) || REIRuntime.getInstance().getOverlay().orElseThrow().getEntryList().containsMouse(new Point(mouseX, mouseY)) || expended;
        }
        
        @Override
        protected void renderButtonText(PoseStack matrices, MultiBufferSource.BufferSource bufferSource) {
            float expendProgress = (float) widget.favoritePanel.expendState.progress();
            if (expendProgress < .9f) {
                int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * (1 - expendProgress)) << 24);
                font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, matrices.last().pose(), bufferSource, false, 0, 15728880);
            }
            if (expendProgress > .1f) {
                int textColor = 0xFFFFFF | (Math.round(0xFF * alpha.floatValue() * expendProgress) << 24);
                font.drawInBatch("+", bounds.getCenterX() - 2.5f, bounds.getCenterY() - 3, textColor, false, matrices.last().pose(), bufferSource, false, 0, 15728880);
            }
        }
    }
    
    public abstract static class FadingFavoritePanelButton extends WidgetWithBounds {
        protected final FavoritesListWidget widget;
        public boolean wasClicked = false;
        public final NumberAnimator<Double> alpha = ValueAnimator.ofDouble(0);
        
        public final Rectangle bounds = new Rectangle();
        
        public FadingFavoritePanelButton(FavoritesListWidget widget) {
            this.widget = widget;
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            this.bounds.setBounds(updateArea(widget.fullBounds));
            boolean hovered = containsMouse(mouseX, mouseY);
            switch (ConfigObject.getInstance().getFavoriteAddWidgetMode()) {
                case ALWAYS_INVISIBLE:
                    this.alpha.setAs(0);
                    break;
                case AUTO_HIDE:
                    this.alpha.setTo(hovered ? 1f : isAvailable(mouseX, mouseY) ? 0.5f : 0f, 260);
                    break;
                case ALWAYS_VISIBLE:
                    this.alpha.setAs(hovered ? 1f : 0.5f);
                    break;
            }
            this.alpha.update(delta);
            int buttonColor = 0xFFFFFF | (Math.round(0x74 * alpha.floatValue()) << 24);
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
            if (isVisible()) {
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                renderButtonText(matrices, bufferSource);
                bufferSource.endBatch();
            }
            if (hovered) {
                queueTooltip();
            }
        }
        
        protected abstract boolean isAvailable(int mouseX, int mouseY);
        
        protected abstract void renderButtonText(PoseStack matrices, MultiBufferSource.BufferSource bufferSource);
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        public boolean isVisible() {
            return Math.round(0x12 * alpha.floatValue()) > 0;
        }
        
        protected boolean wasClicked() {
            boolean tmp = this.wasClicked;
            this.wasClicked = false;
            return tmp;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isVisible() && containsMouse(mouseX, mouseY)) {
                this.wasClicked = true;
            }
            return false;
        }
        
        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (wasClicked() && isVisible() && containsMouse(mouseX, mouseY)) {
                onClick();
                return true;
            }
            return false;
        }
        
        protected abstract void onClick();
        
        protected abstract void queueTooltip();
        
        protected abstract Rectangle updateArea(Rectangle fullArea);
    }
    
    public static class AddFavoritePanel extends WidgetWithBounds {
        private final FavoritesListWidget widget;
        public final ProgressValueAnimator<Boolean> expendState = ValueAnimator.ofBoolean(0.1, false);
        private final Rectangle bounds = new Rectangle();
        private final Rectangle scrollBounds = new Rectangle();
        private final LazyResettable<List<Row>> rows = new LazyResettable<>(() -> {
            List<Row> rows = new ArrayList<>();
            for (FavoriteEntryType.Section section : FavoriteEntryType.registry().sections()) {
                rows.add(new SectionRow(section.getText(), section.getText().copy().withStyle(style -> style.withUnderlined(true))));
                rows.add(new SectionEntriesRow(CollectionUtils.map(section.getEntries(), FavoriteEntry::copy)));
                rows.add(new SectionSeparatorRow());
            }
            if (!rows.isEmpty()) rows.remove(rows.size() - 1);
            rows.add(new EmptySectionRow(4));
            return rows;
        });
        private final ScrollingContainer scroller = new ScrollingContainer() {
            @Override
            public Rectangle getBounds() {
                return scrollBounds;
            }
            
            @Override
            public int getMaxScrollHeight() {
                return Math.max(1, rows.get().stream().mapToInt(Row::getRowHeight).sum());
            }
        };
        
        public AddFavoritePanel(FavoritesListWidget widget) {
            this.widget = widget;
        }
        
        public void resetRows() {
            this.rows.reset();
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            this.bounds.setBounds(updatePanelArea(widget.fullBounds));
            this.scrollBounds.setBounds(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 20);
            this.expendState.update(delta);
            int buttonColor = 0xFFFFFF | (Math.round(0x34 * Math.min((float) expendState.progress() * 2, 1)) << 24);
            fillGradient(matrices, bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
            scroller.updatePosition(delta);
            
            if (expendState.value()) {
                ScissorsHandler.INSTANCE.scissor(scrollBounds);
                matrices.pushPose();
                matrices.translate(0, -scroller.scrollAmount(), 0);
                int y = scrollBounds.y;
                for (Row row : rows.get()) {
                    row.render(matrices, scrollBounds.x, y, scrollBounds.width, row.getRowHeight(), mouseX, mouseY + scroller.scrollAmountInt(), delta);
                    y += row.getRowHeight();
                }
                matrices.popPose();
                ScissorsHandler.INSTANCE.removeLastScissor();
            }
        }
        
        private Rectangle updatePanelArea(Rectangle fullArea) {
            int currentWidth = 16 + Math.round(Math.min((float) expendState.progress(), 1) * (fullArea.getWidth() - 16 - 8));
            int currentHeight = 16 + Math.round((float) expendState.progress() * (fullArea.getHeight() * 0.4f - 16 - 8 + 4));
            return new Rectangle(fullArea.x + 4, fullArea.getMaxY() - currentHeight - 4, currentWidth, currentHeight);
        }
        
        @Override
        public boolean mouseScrolled(double d, double e, double f) {
            if (scrollBounds.contains(d, e)) {
                scroller.offset(ClothConfigInitializer.getScrollStep() * -f, true);
                return true;
            }
            return super.mouseScrolled(d, e, f);
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return rows.get();
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        private static abstract class Row extends AbstractContainerEventHandler {
            public abstract int getRowHeight();
            
            public abstract void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta);
        }
        
        private static class SectionRow extends Row {
            private final Component sectionText;
            private final Component styledText;
            
            public SectionRow(Component sectionText, Component styledText) {
                this.sectionText = sectionText;
                this.styledText = styledText;
            }
            
            @Override
            public int getRowHeight() {
                return 11;
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
                if (mouseX >= x && mouseY >= y && mouseX <= x + rowWidth && mouseY <= y + rowHeight) {
                    Tooltip.create(sectionText).queue();
                }
                Minecraft.getInstance().font.draw(matrices, styledText, x, y + 1, 0xFFFFFFFF);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
        
        private static class SectionSeparatorRow extends Row {
            @Override
            public int getRowHeight() {
                return 5;
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
                fillGradient(matrices, x, y + 2, x + rowWidth, y + 3, -571806998, -571806998);
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
        
        private static class EmptySectionRow extends Row {
            private final int height;
            
            public EmptySectionRow(int height) {
                this.height = height;
            }
            
            @Override
            public int getRowHeight() {
                return height;
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return Collections.emptyList();
            }
        }
        
        private class SectionEntriesRow extends Row {
            private final List<FavoriteEntry> entries;
            private final List<SectionFavoriteWidget> widgets;
            private int blockedCount;
            private int lastY;
            
            public SectionEntriesRow(List<FavoriteEntry> entries) {
                this.entries = entries;
                int entrySize = entrySize();
                this.widgets = CollectionUtils.map(this.entries, entry -> new SectionFavoriteWidget(new Point(0, 0), entrySize, entry));
                
                for (SectionFavoriteWidget widget : this.widgets) {
                    widget.size.setTo(entrySize * 100, 300);
                }
                
                this.lastY = scrollBounds.y;
                
                updateEntriesPosition(widget -> false);
            }
            
            @Override
            public int getRowHeight() {
                return Mth.ceil((entries.size() + blockedCount) / (scrollBounds.width / (float) entrySize())) * entrySize();
            }
            
            @Override
            public void render(PoseStack matrices, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, float delta) {
                this.lastY = y;
                int entrySize = entrySize();
                boolean fastEntryRendering = ConfigObject.getInstance().doesFastEntryRendering();
                updateEntriesPosition(entry -> true);
                for (SectionFavoriteWidget widget : widgets) {
                    widget.update(delta);
                    
                    if (widget.getBounds().getMaxY() > lastY && widget.getBounds().getY() <= lastY + rowHeight) {
                        if (widget.getCurrentEntry().isEmpty())
                            continue;
                        widget.render(matrices, mouseX, mouseY, delta);
                    }
                }
            }
            
            @Override
            public List<? extends GuiEventListener> children() {
                return widgets;
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return super.mouseClicked(mouseX, mouseY + scroller.scrollAmount(), button);
            }
            
            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                return super.mouseDragged(mouseX, mouseY + scroller.scrollAmount(), button, deltaX, deltaY);
            }
            
            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return super.mouseReleased(mouseX, mouseY + scroller.scrollAmount(), button);
            }
            
            private class SectionFavoriteWidget extends EntryListEntryWidget {
                private ValueAnimator<FloatingPoint> pos = ValueAnimator.ofFloatingPoint();
                private NumberAnimator<Double> size = ValueAnimator.ofDouble();
                private FavoriteEntry entry;
                
                protected SectionFavoriteWidget(Point point, int entrySize, FavoriteEntry entry) {
                    super(point, entrySize);
                    this.entry = entry;
                    entry(ClientEntryStacks.of(entry.getRenderer(true)));
                    noBackground();
                }
                
                public void moveTo(boolean animated, int xPos, int yPos) {
                    pos.setTo(new FloatingPoint(xPos, yPos), animated ? 200 : -1);
                }
                
                public void update(float delta) {
                    this.pos.update(delta);
                    this.size.update(delta);
                    this.getBounds().width = this.getBounds().height = (int) Math.round(this.size.doubleValue() / 100);
                    double offsetSize = (entrySize() - this.size.doubleValue() / 100) / 2;
                    this.getBounds().x = (int) Math.round(pos.value().x + offsetSize);
                    this.getBounds().y = (int) Math.round(pos.value().y + offsetSize) + lastY;
                }
                
                @Override
                @Nullable
                public Tooltip getCurrentTooltip(Point point) {
                    point = PointHelper.ofMouse();
                    if (!scrollBounds.contains(point)) return null;
                    Tooltip tooltip = super.getCurrentTooltip(point);
                    if (tooltip != null) {
                        tooltip.add(Component.empty());
                        tooltip.add(Component.translatable("tooltip.rei.drag_to_add_favorites"));
                    }
                    return tooltip;
                }
            }
            
            public void updateEntriesPosition(Predicate<SectionFavoriteWidget> animated) {
                int entrySize = entrySize();
                this.blockedCount = 0;
                int width = scrollBounds.width / entrySize;
                int currentX = 0;
                int currentY = 0;
                
                int slotIndex = 0;
                for (SectionFavoriteWidget widget : this.widgets) {
                    while (true) {
                        int xPos = currentX * entrySize + scrollBounds.x - 1;
                        int yPos = currentY * entrySize;
                        
                        currentX++;
                        if (currentX >= width) {
                            currentX = 0;
                            currentY++;
                        }
                        
                        if (notSteppingOnExclusionZones(xPos, yPos + lastY - scroller.scrollAmountInt(), entrySize, entrySize, scrollBounds)) {
                            widget.moveTo(animated.test(widget), xPos, yPos);
                            break;
                        } else {
                            blockedCount++;
                        }
                    }
                }
            }
        }
    }
}
