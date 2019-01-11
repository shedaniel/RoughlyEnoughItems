package me.shedaniel.rei.mixin;

import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerGui.class)
public class MixinContainerGui extends Gui implements IMixinContainerGui {
    
    @Shadow
    protected int left;
    @Shadow
    protected int top;
    @Shadow
    protected int containerWidth;
    @Shadow
    protected int containerHeight;
    
    private ContainerGuiOverlay overlay;
    private ContainerGui lastGui;
    
    @Shadow
    private ItemStack field_2782;
    
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
        if (this.overlay == null)
            this.overlay = new ContainerGuiOverlay(lastGui);
        return this.overlay;
    }
    
    @Inject(method = "onInitialized()V", at = @At("RETURN"))
    protected void onInitialized(CallbackInfo info) {
        this.overlay = null;
        this.listeners.add(getOverlay());
        getOverlay().onInitialized();
    }
    
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    public void draw(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentGui instanceof ContainerGui)
            this.lastGui = (ContainerGui) MinecraftClient.getInstance().currentGui;
        getOverlay().render(int_1, int_2, float_1);
    }
    
    @Override
    public ItemStack getDraggedStack() {
        return this.field_2782;
    }
    
    @Override
    public ContainerGui getContainerGui() {
        return lastGui;
    }
    
    // TODO into an inject
    @Override
    public boolean mouseScrolled(double double_1) {
        for(GuiEventListener entry : this.getEntries())
            if (entry.mouseScrolled(double_1))
                return true;
        return false;
    }
    
    // TODO into an inject
    @Override
    public boolean charTyped(char char_1, int int_1) {
        for(GuiEventListener entry : this.getEntries())
            if (entry.charTyped(char_1, int_1))
                return true;
        return false;
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        for(GuiEventListener entry : this.getEntries())
            if (entry.keyPressed(int_1, int_2, int_3)) {
                ci.cancel();
                ci.setReturnValue(true);
            }
    }
    
}
