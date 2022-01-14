package me.shedaniel.rei.mixin.forge;

import me.shedaniel.rei.api.client.config.ConfigObject;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class MixinEffectRenderingInventoryScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    public MixinEffectRenderingInventoryScreen(AbstractContainerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }
    
    @Unique
    private boolean leftSideEffects() {
        return ConfigObject.getInstance().isLeftSideMobEffects();
    }
    
    @ModifyVariable(method = "renderEffects",
                    at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;", ordinal = 0),
                    ordinal = 2) // 3rd int
    public int modifyK(int k) {
        if (!leftSideEffects()) return k;
        boolean bl = this.leftPos >= 120;
        return bl ? this.leftPos - 120 - 4 : this.leftPos - 32 - 4;
    }
    
    @ModifyVariable(method = "renderEffects",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/client/gui/screens/inventory/EffectRenderingInventoryScreen;renderBackgrounds(Lcom/mojang/blaze3d/vertex/PoseStack;IILjava/lang/Iterable;Z)V",
                             ordinal = 0),
                    ordinal = 0) // 1st bool
    public boolean modifyBl(boolean bl) {
        if (!leftSideEffects()) return bl;
        return this.leftPos >= 120;
    }
}
