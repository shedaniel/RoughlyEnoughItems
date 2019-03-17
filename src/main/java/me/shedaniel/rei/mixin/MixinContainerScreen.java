package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.TabGetter;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.listeners.ContainerScreenHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiContainer.class)
public class MixinContainerScreen extends GuiScreen implements ContainerScreenHooks {
    
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
    
    @Override
    public int rei_getContainerLeft() {
        return guiLeft;
    }
    
    @Override
    public int rei_getContainerTop() {
        return guiTop;
    }
    
    @Override
    public int rei_getContainerWidth() {
        return xSize;
    }
    
    @Override
    public int rei_getContainerHeight() {
        return ySize;
    }
    
    @Inject(method = "initGui()V", at = @At("RETURN"))
    protected void onInitialized(CallbackInfo info) {
        if (Minecraft.getInstance().currentScreen instanceof GuiContainerCreative) {
            TabGetter tabGetter = (TabGetter) Minecraft.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        ScreenHelper.setLastContainerScreen((GuiContainer) (Object) this);
        this.children.add(ScreenHelper.getLastOverlay(true));
    }
    
    @Inject(method = "render(IIF)V", at = @At("RETURN"))
    public void draw(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (Minecraft.getInstance().currentScreen instanceof GuiContainerCreative) {
            TabGetter tabGetter = (TabGetter) Minecraft.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        ScreenHelper.getLastOverlay().drawOverlay(int_1, int_2, float_1);
    }
    
    @Override
    public Slot rei_getHoveredSlot() {
        return hoveredSlot;
    }
    
    @Override
    public boolean mouseScrolled(double double_1) {
        if (Minecraft.getInstance().currentScreen instanceof GuiContainerCreative) {
            TabGetter tabGetter = (TabGetter) Minecraft.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return super.mouseScrolled(double_1);
        }
        ContainerScreenOverlay overlay = ScreenHelper.getLastOverlay();
        if (ScreenHelper.isOverlayVisible() && overlay.getRectangle().contains(ClientHelper.getMouseLocation()))
            if (overlay.mouseScrolled(double_1))
                return true;
        return super.mouseScrolled(double_1);
    }
    
    @Inject(method = "keyPressed(III)Z", at = @At("HEAD"), cancellable = true)
    public void keyPressed(int int_1, int int_2, int int_3, CallbackInfoReturnable<Boolean> ci) {
        if (Minecraft.getInstance().currentScreen instanceof GuiContainerCreative) {
            TabGetter tabGetter = (TabGetter) Minecraft.getInstance().currentScreen;
            if (tabGetter.rei_getSelectedTab() != ItemGroup.INVENTORY.getIndex())
                return;
        }
        if (ScreenHelper.getLastOverlay().keyPressed(int_1, int_2, int_3)) {
            ci.setReturnValue(true);
            ci.cancel();
        }
    }
    
}
