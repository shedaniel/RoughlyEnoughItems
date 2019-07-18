package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.GuiEventHandlerHooks;
import net.minecraft.client.gui.GuiEventHandler;
import net.minecraft.client.gui.IGuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(GuiEventHandler.class)
public abstract class MixinGuiEventHandler implements GuiEventHandlerHooks {
    
    @Shadow
    protected abstract void setFocused(@Nullable IGuiEventListener p_195073_1_);
    
    @Shadow
    protected abstract void setDragging(boolean p_195072_1_);
    
    @Override
    public void rei_setFocused(IGuiEventListener listener) {
        setFocused(listener);
    }
    
    @Override
    public void rei_setDragging(boolean dragging) {
        setDragging(dragging);
    }
}
