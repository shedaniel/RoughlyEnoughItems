package me.shedaniel.gui.widget;

import me.shedaniel.ClientListener;
import me.shedaniel.library.KeyBindFunction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class KeyBindButton extends GuiButton {
    
    private int currentKey;
    private Consumer<Integer> onEditKeyBind;
    private boolean editMode;
    
    public KeyBindButton(int buttonId, int x, int y, int widthIn, int heightIn, int currentKey, Consumer<Integer> onEditKeyBind) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.currentKey = currentKey;
        this.onEditKeyBind = onEditKeyBind;
    }
    
    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if (editMode) {
            currentKey = KeyEvent.getExtendedKeyCodeForChar(p_charTyped_1_);
            onEditKeyBind.accept(currentKey);
            editMode = false;
            return true;
        }
        return false;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.displayString = editMode ? I18n.format("text.rei.listeningkey") : KeyEvent.getKeyText(currentKey);
        if (!editMode && ClientListener.keyBinds.stream().map(KeyBindFunction::getKey).filter(integer -> integer == currentKey).count() > 1)
            this.displayString = TextFormatting.RED + this.displayString;
        super.render(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void focusChanged(boolean focused) {
        if (focused == false)
            editMode = focused;
    }
    
    @Override
    public void onClick(double mouseX, double mouseY) {
        editMode = !editMode;
    }
    
    @Override
    public boolean canFocus() {
        return true;
    }
    
}
