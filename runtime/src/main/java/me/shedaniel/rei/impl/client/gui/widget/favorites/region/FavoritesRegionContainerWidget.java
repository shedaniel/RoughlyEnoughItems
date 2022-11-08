package me.shedaniel.rei.impl.client.gui.widget.favorites.region;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.element.FavoritesListElement;
import me.shedaniel.rei.impl.client.gui.widget.favorites.listeners.FavoritesRegionListener;
import me.shedaniel.rei.impl.client.gui.widget.region.EntryStacksRegionWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static me.shedaniel.rei.impl.client.gui.widget.entrylist.EntryListWidget.entrySize;

public class FavoritesRegionContainerWidget extends WidgetWithBounds implements FavoritesListElement {
    private final Rectangle bounds = new Rectangle();
    private final List<Region> regions = new ArrayList<>();
    
    public FavoritesRegionContainerWidget() {
        this.regions.add(new Region());
    }
    
    private Stream<EntryStacksRegionWidget<FavoriteEntry>> getRegions() {
        return children().stream();
    }
    
    @Override
    public List<EntryStacksRegionWidget<FavoriteEntry>> children() {
        return Lists.transform(regions, Region::getRegion);
    }
    
    @Override
    public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
        final boolean isMouseOver = REIRuntime.getInstance().getOverlay().orElseThrow().getFavoritesList().orElseThrow().containsMouse(new Point(mouseX, mouseY));
        int i = 0;
        
        for (Region region : this.regions) {
            region.getRegion().getBounds().setBounds(getBounds());
            region.getRegion().render(poses, mouseX, mouseY, delta);
            
            region.renderExtra(i++, poses, mouseX, mouseY, delta, isMouseOver);
        }
    }
    
    public void updateEntriesPosition() {
        getRegions().forEach(region -> region.updateEntriesPosition(stack -> true));
    }
    
    public Stream<EntryStack<?>> getEntries() {
        return this.getRegions().flatMap(EntryStacksRegionWidget::getEntries);
    }
    
    public void setBounds(Rectangle bounds) {
        this.bounds.setBounds(new Rectangle(bounds.x, bounds.y, bounds.width - 5, bounds.height));
    }
    
    public void setEntries(List<FavoriteEntry> entries) {
        this.regions.get(0).getRegion().setEntries(entries, EntryStacksRegionWidget.RemovalMode.DISAPPEAR);
    }
    
    @Override
    @Nullable
    public DraggableComponent<Object> getHovered(DraggingContext<Screen> context, double mouseX, double mouseY) {
        return this.getRegions().map(region -> region.getHovered(context, mouseX, mouseY))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public DraggedAcceptorResult acceptDragged(DraggingContext<Screen> context, DraggableComponent<?> component) {
        return this.getRegions()
                .map(visitor -> visitor.acceptDragged(context, component))
                .filter(result -> result != DraggedAcceptorResult.PASS)
                .findFirst()
                .orElse(DraggedAcceptorResult.PASS);
    }
    
    @Override
    public Rectangle getBounds() {
        return this.bounds;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : children()) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (GuiEventListener widget : children()) {
            if (widget.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (containsMouse(mouse())) {
            for (GuiEventListener widget : children()) {
                if (widget.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (containsMouse(mouseX, mouseY)) {
            for (GuiEventListener element : children()) {
                if (element.mouseScrolled(mouseX, mouseY, amount)) {
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    private class Region extends GuiComponent {
        private final EntryStacksRegionWidget<FavoriteEntry> region = new EntryStacksRegionWidget<>(new FavoritesRegionListener());
        private final Font font = Minecraft.getInstance().font;
        private final NumberAnimator<Double> alpha = ValueAnimator.ofDouble();
        private final NumberAnimator<Double> entries = ValueAnimator.ofDouble();
        private final NumberAnimator<Double> height = ValueAnimator.ofDouble();
        private final ProgressValueAnimator<Boolean> createNewGroupViaDrag = ValueAnimator.ofBoolean(0.1);
        
        public EntryStacksRegionWidget<FavoriteEntry> getRegion() {
            return this.region;
        }
        
        public void renderExtra(int index, PoseStack poses, int mouseX, int mouseY, float delta, boolean isMouseOver) {
            this.alpha.update(delta);
            this.entries.update(delta);
            this.height.update(delta);
            this.createNewGroupViaDrag.update(delta);
            
            final int fillColor = Math.round(0x12 * Math.min(this.alpha.floatValue(), 30) / 30f) << 24 | 0xFFFFFF;
            final int lineColor = Math.round(0x30 * Math.min(this.alpha.floatValue(), 30) / 30f) << 24 | 0xFFFFFF;
            final int padding = 4 + Math.round(Math.min(this.alpha.floatValue(), 30) / 30f * 0);
            
            Rectangle bounds = this.region.getInnerBounds().clone();
            bounds.height = this.height.intValue();
            bounds.x -= padding;
            bounds.y -= padding;
            bounds.width += padding * 2;
            bounds.height += padding * 2;
            
            final boolean createGroupViaDrag = regions.size() - 1 == index && isMouseOver && mouseY > bounds.getMaxY() + 24 && DraggingContext.getInstance().isDraggingStack();
    
            bounds.height += 30;
            if (createGroupViaDrag) bounds.height += entrySize() * 2 + 16;
            
            this.alpha.setTo(this.entries.doubleValue() != this.entries.target() || this.createNewGroupViaDrag.value() ? 300d : (isMouseOver && bounds.contains(mouseX, mouseY)) ? 70d : 0d, 4000);
            this.entries.setTo(region.children().size(), 400);
            this.height.setTo(region.contentHeight(), 400);
            
            bounds.height -= 30;
            if (createGroupViaDrag) bounds.height -= entrySize() * 2 + 16;
            
            if (this.alpha.floatValue() > 0.1f) {
                renderBoundsBackground(poses, fillColor, lineColor, bounds);
                
                renderButton(bounds, 0, Math.min(this.alpha.floatValue(), 30) / 30f, poses, "Del", -3.5f, -3.5f, mouseX, mouseY, new TextComponent("Remove Group"));
                renderButton(bounds, 20, Math.min(this.alpha.floatValue(), 30) / 30f, poses, "+", -2.5f, -3.5f, mouseX, mouseY, new TextComponent("Add New Group"));
                renderButton(bounds, bounds.width - 16 - 20, Math.min(this.alpha.floatValue(), 30) / 30f, poses, "✎", -3.5f, -3.5f, mouseX, mouseY, new TextComponent("Rename Group"));
                renderButton(bounds, bounds.width - 16, Math.min(this.alpha.floatValue(), 30) / 30f, poses, "Hide", -1.5f, -3.5f, mouseX, mouseY, new TextComponent("Collapse Group"));
            }
            
            this.createNewGroupViaDrag.setTo(createGroupViaDrag, 700);
            
            if (this.createNewGroupViaDrag.value()) {
                Rectangle curr = new Rectangle(bounds.x + 20, bounds.getMaxY() + 4, 16, 16);
                Rectangle target = new Rectangle(bounds.x, bounds.getMaxY() + 25, bounds.width, region.getBounds().getMaxY() - bounds.getMaxY() - 25);
                double progress = this.createNewGroupViaDrag.progress();
                double scaledProgress = Math.pow(this.createNewGroupViaDrag.progress(), 0.3);
                Rectangle nextBounds = new Rectangle(
                        curr.x + (target.x - curr.x) * progress,
                        curr.y + (target.y - curr.y) * progress,
                        curr.width + (target.width - curr.width) * progress,
                        curr.height + (target.height - curr.height) * progress
                );
                final int newFillColor = (int) Math.round((fillColor >> 24) * scaledProgress) << 24 | 0xFFFFFF;
                final int newLineColor = (int) Math.round((lineColor >> 24) * scaledProgress) << 24 | 0xFFFFFF;
                renderBoundsBackground(poses, newFillColor, newLineColor, nextBounds);
            }
        }
        
        public void renderBoundsBackground(PoseStack poses, int fillColor, int lineColor, Rectangle bounds) {
            fillGradient(poses, bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, fillColor, fillColor);
            
            fillGradient(poses, bounds.x + 1, bounds.y, bounds.getMaxX() - 1, bounds.y + 1, lineColor, lineColor);
            fillGradient(poses, bounds.x + 1, bounds.getMaxY() - 1, bounds.getMaxX() - 1, bounds.getMaxY(), lineColor, lineColor);
            
            fillGradient(poses, bounds.x, bounds.y + 1, bounds.x + 1, bounds.getMaxY() - 1, lineColor, lineColor);
            fillGradient(poses, bounds.getMaxX() - 1, bounds.y + 1, bounds.getMaxX(), bounds.getMaxY() - 1, lineColor, lineColor);
        }
        
        public void renderButton(Rectangle bounds, int xOffset, float alpha, PoseStack poses, String text, float xTextOffset, float yTextOffset, int mouseX, int mouseY, Component tooltip) {
            int a = Math.round((0x22 + 0x15 * (Mth.cos((float) (System.currentTimeMillis() % 2000 / 1000F * Math.PI)) + 1) / 2) * alpha);
            int buttonColor = a << 24 | 0xFFFFFF;
            
            fillGradient(poses, bounds.x + xOffset, bounds.getMaxY() + 4, bounds.x + xOffset + 16, bounds.getMaxY() + 20, buttonColor, buttonColor);
            if (Math.round(0xAA * alpha) > 0x4) {
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                font.drawInBatch(text, bounds.x + 8 + xOffset + xTextOffset, bounds.getMaxY() + 4 + 8 + yTextOffset, 0xFFFFFF | (Math.round(0xAA * alpha) << 24), false, poses.last().pose(), bufferSource, false, 0, 0xf000f0);
                bufferSource.endBatch();
            }
            
            if (new Rectangle(bounds.x + xOffset, bounds.getMaxY() + 4, 16, 16).contains(mouseX, mouseY)) {
                Tooltip.create(tooltip).queue();
            }
        }
    }
}
