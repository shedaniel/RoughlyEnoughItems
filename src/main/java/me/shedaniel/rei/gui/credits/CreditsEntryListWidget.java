/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.credits;

import me.shedaniel.reiclothconfig2.gui.widget.DynamicSmoothScrollingEntryListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.ITextComponent;

public class CreditsEntryListWidget extends DynamicSmoothScrollingEntryListWidget<CreditsEntryListWidget.CreditsItem> {
    
    private boolean inFocus;
    
    public CreditsEntryListWidget(Minecraft client, int width, int height, int startY, int endY) {
        super(client, width, height, startY, endY, Gui.OPTIONS_BACKGROUND);
    }
    
    @Override
    public void focusChanged(boolean boolean_1) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return;
        } else {
            this.inFocus = !this.inFocus;
            if (this.inFocus && this.getFocused() == null && this.getItemCount() > 0) {
                this.moveSelection(1);
            } else if (this.inFocus && this.getFocused() != null) {
                this.moveSelection(0);
            }
            
            return;
        }
    }
    
    public void creditsClearEntries() {
        clearItems();
    }
    
    private CreditsItem rei_getEntry(int int_1) {
        return this.getChildren().get(int_1);
    }
    
    public void creditsAddEntry(CreditsItem entry) {
        addItem(entry);
    }
    
    @Override
    public int getItemWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
    public static class CreditsItem extends DynamicSmoothScrollingEntryListWidget.Entry<CreditsItem> {
        private String text;
        
        public CreditsItem(ITextComponent textComponent) {
            this(textComponent.getFormattedText());
        }
        
        public CreditsItem(String text) {
            this.text = text;
        }
        
        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(text, x + 5, y + 5, -1);
        }
        
        @Override
        public int getItemHeight() {
            return 12;
        }
    }
    
}
