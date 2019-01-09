package me.shedaniel.rei.mixin;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerGui.class)
public class MixinContainerGui extends Gui implements IMixinContainerGui {
    
    private ContainerGuiOverlay overlay;
    
    @Shadow
    protected int left;
    
    @Shadow
    protected int top;
    
    @Shadow
    protected int containerWidth;
    
    @Shadow
    protected int containerHeight;
    
    @Override
    public int getContainerLeft() {
        return left;
    }
    
    @Override
    public int getContainerTop() {
        return top;
    }
    
    @Override
    public int getContainerWidth() {
        return containerWidth;
    }
    
    @Override
    public int getContainerHeight() {
        return containerHeight;
    }
    
    @Override
    public ContainerGuiOverlay getOverlay() {
        if (overlay == null)
            overlay = new ContainerGuiOverlay((ContainerGui) MinecraftClient.getInstance().currentGui);
        return overlay;
    }
    
    @Inject(method = "onInitialized()V", at = @At("RETURN"))
    protected void onInitialized(CallbackInfo info) {
        this.listeners.add(getOverlay());
    }
    
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    public void draw(int int_1, int int_2, float float_1, CallbackInfo info) {
        getOverlay().draw(int_1, int_2, float_1);
    }
    
}
