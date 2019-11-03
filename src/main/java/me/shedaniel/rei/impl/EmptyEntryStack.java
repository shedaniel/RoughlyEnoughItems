/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.Entry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Optional;

@Deprecated
public class EmptyEntryStack extends AbstractEntryStack {
    
    @Deprecated
    public static final EntryStack EMPTY = new EmptyEntryStack();
    
    private EmptyEntryStack() {
    }
    
    @Override
    public Optional<Identifier> getIdentifier() {
        return Optional.empty();
    }
    
    @Override
    public Type getType() {
        return Type.EMPTY;
    }
    
    @Override
    public int getAmount() {
        return 0;
    }
    
    @Override
    public void setAmount(int amount) {
    
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public Entry toEntry() {
        return null;
    }
    
    @Override
    public EntryStack copy() {
        return this;
    }
    
    @Override
    public Object getObject() {
        return null;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        return stack.getType() == getType();
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        return stack.getType() == getType();
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        return stack.getType() == getType();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        return stack.getType() == getType();
    }
    
    @Override
    @Nullable
    public QueuedTooltip getTooltip(int mouseX, int mouseY) {
        return null;
    }
    
    @Override
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
    
    }
}
