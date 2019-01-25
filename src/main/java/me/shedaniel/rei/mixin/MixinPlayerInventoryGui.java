package me.shedaniel.rei.mixin;

import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryGui;
import net.minecraft.client.gui.ingame.PlayerInventoryGui;
import net.minecraft.client.gui.ingame.RecipeBookProvider;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.container.PlayerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventoryGui.class)
public abstract class MixinPlayerInventoryGui extends AbstractPlayerInventoryGui<PlayerContainer> implements RecipeBookProvider {
    
    @Shadow
    @Final
    private RecipeBookGui recipeBook;
    
    public MixinPlayerInventoryGui(PlayerContainer container_1, PlayerInventory playerInventory_1, TextComponent textComponent_1) {
        super(container_1, playerInventory_1, textComponent_1);
    }
    
    @Override
    public GuiEventListener getFocused() {
        return super.getFocused();
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (recipeBook.mouseClicked(mouseX, mouseY, button)) {
            focusOn(recipeBook);
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
    
}
