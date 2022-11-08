package me.shedaniel.rei.impl.client.gui.widget.region;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.scroll.ScrollingContainer;
import me.shedaniel.rei.api.client.REIRuntime;

public interface RegionWidgetScroller {
    static RegionWidgetScroller ofScrollingContainer(ScrollingContainer container) {
        return new RegionWidgetScroller() {
            @Override
            public void renderExtra(PoseStack poses, int mouseX, int mouseY, float delta) {
                container.updatePosition(delta);
                container.renderScrollBar(0, 1, REIRuntime.getInstance().isDarkThemeEnabled() ? 0.8f : 1f);
            }
            
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return container.updateDraggingState(mouseX, mouseY, button);
            }
            
            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
                container.offset(ClothConfigInitializer.getScrollStep() * -amount, true);
                return true;
            }
            
            @Override
            public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                return container.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            }
    
            @Override
            public float scrollOffset() {
                return (float) container.scrollAmount();
            }
        };
    }
    
    void renderExtra(PoseStack poses, int mouseX, int mouseY, float delta);
    
    boolean mouseClicked(double mouseX, double mouseY, int button);
    
    boolean mouseScrolled(double mouseX, double mouseY, double amount);
    
    boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
    
    float scrollOffset();
    
    default int scrollOffsetInt() {
        return Math.round(scrollOffset());
    }
}
