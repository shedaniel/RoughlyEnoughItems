package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.IMixinContainerScreen;
import me.shedaniel.rei.listeners.IMixinTabGetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerScreen.class)
public class MixinContainerScreen extends Screen implements IMixinContainerScreen {
    
    @Shadow
    protected int left;
    @Shadow
    protected int top;
    @Shadow
    protected int containerWidth;
    @Shadow
    protected int containerHeight;
    @Shadow
    protected Slot focusedSlot;
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
    public void setOverlay(ContainerScreenOverlay overlay) {
        GuiHelper.setOverlay(overlay);
    }
    
    @Inject(method = "onInitialized()V", at = @At("RETURN"))
    protected void onInitialized(CallbackInfo info) {
        GuiHelper.setLastContainerScreen((ContainerScreen) (Object) this);
        GuiHelper.setLastMixinContainerScreen((IMixinContainerScreen) this);
        GuiHelper.setOverlay(new ContainerScreenOverlay());
        this.listeners.add(GuiHelper.getLastOverlay());
    }
    
    @Inject(method = "draw(IIF)V", at = @At("RETURN"))
    public void draw(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen instanceof CreativePlayerInventoryScreen) {
            IMixinTabGetter tabGetter = (IMixinTabGetter) MinecraftClient.getInstance().currentScreen;
            if (tabGetter.getSelectedTab() != ItemGroup.INVENTORY.getId())
                return;
        }
        GuiHelper.getLastOverlay().render(int_1, int_2, float_1);
    }
    
    @Override
    public ItemStack getDraggedStack() {
        return this.field_2782;
    }
    
    @Override
    public Slot getHoveredSlot() {
        return focusedSlot;
    }
    
    @Override
    public boolean mouseScrolled(double double_1) {
        ContainerScreenOverlay overlay = GuiHelper.getLastOverlay();
        if (GuiHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
            for(GuiEventListener entry : this.getEntries())
                if (entry.mouseScrolled(double_1))
                    return true;
        return super.mouseScrolled(double_1);
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (GuiHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
    
}
