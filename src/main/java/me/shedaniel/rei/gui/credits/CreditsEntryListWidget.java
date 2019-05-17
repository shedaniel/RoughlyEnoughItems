/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.credits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.network.chat.Component;

public class CreditsEntryListWidget extends AlwaysSelectedEntryListWidget<CreditsEntryListWidget.CreditsItem> {
    
    public CreditsEntryListWidget(MinecraftClient client, int width, int height, int startY, int endY, int entryHeight) {
        super(client, width, height, startY, endY, entryHeight);
    }
    
    public void creditsClearEntries() {
        clearEntries();
    }
    
    private CreditsItem rei_getEntry(int int_1) {
        return this.children().get(int_1);
    }
    
    public void creditsAddEntry(CreditsItem entry) {
        addEntry(entry);
    }
    
    @Override
    public int getRowWidth() {
        return width - 80;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return width - 40;
    }
    
    public static class CreditsItem extends AlwaysSelectedEntryListWidget.Entry<CreditsItem> {
        private String text;
        
        public CreditsItem(Component textComponent) {
            this(textComponent.getFormattedText());
        }
        
        public CreditsItem(String text) {
            this.text = text;
        }
        
        @Override
        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow(text, x + 5, y + 5, -1);
        }
    }
    
}
