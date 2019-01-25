package me.shedaniel.rei.mixin;

import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.container.CraftingTableGui;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingTableGui.class)
public abstract class MixinCraftingTableGui extends ContainerGui {
    
    @Shadow
    @Final
    private RecipeBookGui recipeBookGui;
    
    public MixinCraftingTableGui(Container container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }
    
    @Override
    public GuiEventListener getFocused() {
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
