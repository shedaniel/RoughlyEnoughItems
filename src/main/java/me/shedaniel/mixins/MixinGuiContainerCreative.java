package me.shedaniel.mixins;

import me.shedaniel.Core;
import me.shedaniel.listenerdefinitions.GuiKeyDown;
import net.minecraft.client.gui.ingame.AbstractPlayerInventoryGui;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(CreativePlayerInventoryGui.class)
public abstract class MixinGuiContainerCreative extends AbstractPlayerInventoryGui {
    
    @Shadow
    private boolean field_2888;
    
    @Shadow
    public abstract int method_2469();
    
    @Shadow
    protected abstract void setSelectedTab(ItemGroup itemGroup_1);
    
    @Shadow
    protected abstract boolean method_2470(Slot slot_1);
    
    @Shadow
    private TextFieldWidget searchBox;
    
    @Shadow
    protected abstract void method_2464();
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    @Shadow
    private float scrollPosition;
    
    public MixinGuiContainerCreative(Container container_1) {
        super(container_1);
    }
    
    @Overwrite
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        this.field_2888 = false;
        if (method_2469() != ItemGroup.SEARCH.getId()) {
            if (method_2469() != ItemGroup.INVENTORY.getId()) {
                if (this.client.options.keyChat.matches(p_keyPressed_1_, p_keyPressed_2_)) {
                    this.field_2888 = true;
                    this.setSelectedTab(ItemGroup.SEARCH);
                    return true;
                }
            } else for(GuiKeyDown listener : Core.getListeners(GuiKeyDown.class))
                if (listener.keyDown(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
                    return true;
        } else {
            boolean flag = !this.method_2470(this.focusedSlot) || this.focusedSlot != null && this.focusedSlot.hasStack();
            if (flag && this.handleHotbarKeyPressed(p_keyPressed_1_, p_keyPressed_2_)) {
                this.field_2888 = true;
                return true;
            } else {
                String s = this.searchBox.getText();
                
                if (this.searchBox.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
                    if (!Objects.equals(s, this.searchBox.getText()))
                        this.method_2464();
                    return true;
                }
            }
        }
        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }
    
    @Overwrite
    public boolean mouseScrolled(double p_mouseScrolled_1_) {
        if (!this.doRenderScrollBar()) {
            return super.mouseScrolled(p_mouseScrolled_1_);
        } else {
            int i = (((CreativePlayerInventoryGui.CreativeContainer) this.container).itemList.size() + 9 - 1) / 9 - 5;
            this.scrollPosition = (float) ((double) this.scrollPosition - p_mouseScrolled_1_ / (double) i);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            ((CreativePlayerInventoryGui.CreativeContainer) this.container).method_2473(this.scrollPosition);
            return true;
        }
    }
    
}
