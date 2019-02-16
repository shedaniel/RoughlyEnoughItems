package me.shedaniel.rei.gui.credits;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.util.text.ITextComponent;

public class CreditsEntry extends GuiListExtended.IGuiListEntry<CreditsEntry> {
    
    private ITextComponent textComponent;
    
    public CreditsEntry(ITextComponent textComponent) {
        this.textComponent = textComponent;
    }
    
    @Override
    public void drawEntry(int entryWidth, int height, int i3, int i4, boolean isSelected, float delta) {
        int x = getX();
        int y = getY();
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(textComponent.getFormattedText(), x + 5, y + 5, -1);
    }
    
}
