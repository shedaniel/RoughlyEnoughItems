package me.shedaniel.mixins;

import me.shedaniel.listenerdefinitions.GuiKeyDown;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.MathHelper;
import org.dimdev.riftloader.RiftLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Objects;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends InventoryEffectRenderer {
    
    @Shadow
    private static int selectedTabIndex = ItemGroup.BUILDING_BLOCKS.getIndex();
    @Shadow
    private GuiTextField searchField;
    
    @Shadow
    protected abstract boolean hasTmpInventory(@Nullable Slot p_208018_1_);
    
    @Shadow
    protected abstract void setCurrentCreativeTab(ItemGroup tab);
    
    @Shadow
    protected abstract void updateCreativeSearch();
    
    @Shadow
    private boolean field_195377_F;
    
    @Shadow
    protected abstract boolean needsScrollBars();
    
    @Shadow
    private float currentScroll;
    
    public MixinGuiContainerCreative(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }
    
    @Overwrite
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        this.field_195377_F = false;
        if (selectedTabIndex != ItemGroup.SEARCH.getIndex()) {
            if (selectedTabIndex != ItemGroup.INVENTORY.getIndex()) {
                if (this.mc.gameSettings.keyBindChat.matchesKey(p_keyPressed_1_, p_keyPressed_2_)) {
                    this.field_195377_F = true;
                    this.setCurrentCreativeTab(ItemGroup.SEARCH);
                    return true;
                }
            } else for(GuiKeyDown listener : RiftLoader.instance.getListeners(GuiKeyDown.class))
                if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
                    return true;
        } else {
            boolean flag = !this.hasTmpInventory(this.hoveredSlot) || this.hoveredSlot != null && this.hoveredSlot.getHasStack();
            
            if (flag && this.func_195363_d(p_keyPressed_1_, p_keyPressed_2_)) {
                this.field_195377_F = true;
                return true;
            } else {
                String s = this.searchField.getText();
                
                if (this.searchField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
                    if (!Objects.equals(s, this.searchField.getText()))
                        this.updateCreativeSearch();
                    return true;
                }
            }
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Overwrite
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        if (!this.needsScrollBars()) {
            return super.mouseScrolled(p_mouseScrolled_1_);
        } else {
            int i = (((GuiContainerCreative.ContainerCreative) this.inventorySlots).itemList.size() + 9 - 1) / 9 - 5;
            this.currentScroll = (float) ((double) this.currentScroll - p_mouseScrolled_1_ / (double) i);
            this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
            ((GuiContainerCreative.ContainerCreative) this.inventorySlots).scrollTo(this.currentScroll);
            return true;
        }
    }
    
}
