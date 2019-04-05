package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.CreativePlayerInventoryScreenHooks;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativePlayerInventoryScreen.class)
public abstract class MixinCreativePlayerInventoryScreen implements CreativePlayerInventoryScreenHooks {
    
    @Shadow
    private static int selectedTab;
    @Shadow
    private boolean field_2888;
    
    @Shadow
    protected abstract boolean doRenderScrollBar();
    
    @Override
    public int rei_getSelectedTab() {
        return selectedTab;
    }
    
    @Override
    public boolean rei_doRenderScrollBar() {
        return doRenderScrollBar();
    }
    
    @Override
    public boolean rei_getField2888() {
        return field_2888;
    }
    
}
