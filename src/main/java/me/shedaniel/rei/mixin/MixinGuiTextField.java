package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.GuiTextFieldHooks;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiTextField.class)
public class MixinGuiTextField implements GuiTextFieldHooks {
    
    @Shadow @Final @Mutable private int width;
    
    @Override
    public void rei_setWidth(int width) {
        this.width = width;
    }
    
}
