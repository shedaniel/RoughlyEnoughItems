package me.shedaniel.gui.widget;

import me.shedaniel.ClientListener;
import me.shedaniel.library.KeyBindFunction;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextFormat;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class KeyBindButton extends ButtonWidget {
    
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
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.text = editMode ? I18n.translate("text.rei.listeningkey") : KeyEvent.getKeyText(currentKey);
        if (!editMode && ClientListener.keyBinds.stream().map(KeyBindFunction::getKey).filter(integer -> integer == currentKey).count() > 1)
            this.text = TextFormat.RED + this.text;
        super.draw(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void setHasFocus(boolean boolean_1) {
        if (boolean_1 == false)
            editMode = boolean_1;
    }
    
    @Override
    public boolean hasFocus() {
        return true;
    }
    
    @Override
    public void onPressed(double double_1, double double_2) {
        editMode = !editMode;
    }
    
}
