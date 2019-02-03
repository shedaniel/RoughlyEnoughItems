package me.shedaniel.rei.mixin;

import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.listeners.IMixinGuiContainer;
import me.shedaniel.rei.listeners.IMixinTabGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class MixinGuiContainer extends GuiScreen implements IMixinGuiContainer {
    
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;
    @Shadow
    protected int xSize;
    @Shadow
    protected int ySize;
    @Shadow
    protected Slot hoveredSlot;
    @Shadow
    private ItemStack draggedStack;
    
    @Override
    public int getContainerLeft() {
        return guiLeft;
    }
    
    @Override
    public int getContainerTop() {
        return guiTop;
    }
    
    @Override
    public int getContainerWidth() {
        return xSize;
    }
    
    @Override
    public int getContainerHeight() {
        return ySize;
    }
    
    @Override
    public void setOverlay(ContainerGuiOverlay overlay) {
        GuiHelper.setOverlay(overlay);
    }
    
    @Inject(method = "initGui()V", at = @At("RETURN"))
    protected void initGui(CallbackInfo info) {
        GuiHelper.setLastGuiContainer((GuiContainer) (Object) this);
        GuiHelper.setLastMixinGuiContainer((IMixinGuiContainer) this);
        GuiHelper.setOverlay(new ContainerGuiOverlay());
        this.eventListeners.add(GuiHelper.getLastOverlay());
    }
    
    @Inject(method = "render(IIF)V", at = @At("RETURN"))
    public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (Minecraft.getInstance().currentScreen instanceof GuiContainerCreative) {
            IMixinTabGetter tabGetter = (IMixinTabGetter) Minecraft.getInstance().currentScreen;
            if (tabGetter.getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        GuiHelper.getLastOverlay().renderOverlay(int_1, int_2, float_1);
    }
    
    @Override
    public ItemStack getDraggedStack() {
        return this.draggedStack;
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (GuiHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
            ci.cancel();
            ci.setReturnValue(true);
        }
    }
    
    @Override
    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
    
    @Override
    public boolean mouseScrolled(double double_1) {
        ContainerGuiOverlay overlay = GuiHelper.getLastOverlay();
        if (GuiHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
            for(IGuiEventListener entry : this.getChildren())
                if (entry.mouseScrolled(double_1))
                    return true;
        return super.mouseScrolled(double_1);
    }
    
}
