package me.shedaniel.rei.mixin;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends InventoryEffectRenderer implements IRecipeShownListener {
    
    @Shadow @Final private GuiRecipeBook recipeBookGui;
    
    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }
    
    @Override
    public IGuiEventListener getFocused() {
        return super.getFocused();
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (recipeBookGui.mouseClicked(mouseX, mouseY, button)) {
            focusOn(recipeBookGui);
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
    
}
