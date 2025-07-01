package me.shedaniel.rei.impl.client.gui.widget.favorites.panel;

import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.LazyResettable;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.gui.drag.component.DraggableComponent;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.favorites.panel.rows.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAddPanel extends FavoritesPanel {
    private final LazyResettable<List<FavoritesPanelRow>> rows = new LazyResettable<>(() -> {
        List<FavoritesPanelRow> rows = new ArrayList<>();
        for (FavoriteEntryType.Section section : FavoriteEntryType.registry().sections()) {
            rows.add(new FavoritesPanelSectionRow(section.getText(), section.getText().copy().withStyle(style -> style.withUnderlined(true))));
            rows.add(new FavoritesPanelEntriesRow(this, CollectionUtils.map(section.getEntries(), FavoriteEntry::copy)));
            rows.add(new FavoritesPanelSeparatorRow());
        }
        if (!rows.isEmpty()) rows.remove(rows.size() - 1);
        rows.add(new FavoritesPanelEmptyRow(4));
        return rows;
    });
    private final ScrollingContainer scroller = new ScrollingContainer() {
        @Override
        public Rectangle getBounds() {
            return innerBounds;
        }
        
        @Override
        public int getMaxScrollHeight() {
            return Math.max(1, rows.get().stream().mapToInt(FavoritesPanelRow::getRowHeight).sum());
        }
    };
    
    public FavoritesAddPanel(FavoritesListWidget parent) {
        super(parent);
    }
    
    public void resetRows() {
        this.rows.reset();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.innerBounds.setBounds(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 20);
        
        int buttonColor = 0xFFFFFF | (Math.round(0x34 * Math.min((float) expendState.progress() * 2, 1)) << 24);
        graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
        scroller.updatePosition(delta);
        
        if (expendState.progress() > 0.05f) {
            graphics.enableScissor(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY());
            graphics.pose().pushPose();
            graphics.pose().translate(0, -scroller.scrollAmount(), 0);
            int y = innerBounds.y;
            for (FavoritesPanelRow row : rows.get()) {
                row.render(graphics, innerBounds, innerBounds.x, y, innerBounds.width, row.getRowHeight(), mouseX, mouseY + scroller.scrollAmountInt(), delta, (float) expendState.progress());
                y += row.getRowHeight();
            }
            graphics.pose().popPose();
            graphics.disableScissor();
        }
    }
    
    @Override
    protected Rectangle getButtonArea() {
        return this.parent.togglePanelButton.getBounds();
    }
    
    @Override
    protected Rectangle getTargetArea(Rectangle fullArea) {
        return new Rectangle(
                fullArea.x + 4,
                fullArea.getMaxY() - 4 - fullArea.height * 0.4f,
                fullArea.width - 8,
                fullArea.height * 0.4f
        );
    }
    
    @Override
    public boolean mouseScrolled(double d, double e, double amountX, double amountY) {
        if (innerBounds.contains(d, e) && amountY != 0) {
            scroller.offset(ClothConfigInitializer.getScrollStep() * -amountY, true);
            return true;
        }
        return super.mouseScrolled(d, e, amountX, amountY);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return rows.get();
    }
    
    public double getScrolledAmount() {
        return scroller.scrollAmount();
    }
    
    public int getScrolledAmountInt() {
        return scroller.scrollAmountInt();
    }
    
    @Nullable
    public DraggableComponent<?> getHoveredStack(double mouseX, double mouseY) {
        for (FavoritesPanelRow row : rows.get()) {
            if (row instanceof FavoritesPanelEntriesRow entriesRow) {
                DraggableComponent<?> hoveredStack = entriesRow.getHoveredStack(mouseX, mouseY);
                
                if (hoveredStack != null) {
                    return hoveredStack;
                }
            }
        }
        
        return null;
    }
    
    @Nullable
    public EntryStack<?> getFocusedStack(Point mouse) {
        for (FavoritesPanelRow row : rows.get()) {
            if (row instanceof FavoritesPanelEntriesRow entriesRow) {
                EntryStack<?> focusedStack = entriesRow.getFocusedStack(mouse);
                
                if (focusedStack != null) {
                    return focusedStack;
                }
            }
        }
        
        return null;
    }
}
