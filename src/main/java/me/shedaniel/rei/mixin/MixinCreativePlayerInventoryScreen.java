package me.shedaniel.rei.mixin;

import me.shedaniel.rei.listeners.CreativePlayerInventoryScreenHooks;
import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreativePlayerInventoryScreen.class)
public class MixinCreativePlayerInventoryScreen implements CreativePlayerInventoryScreenHooks {
    @Shadow private static int selectedTab;
    
    @Override
    public int rei_getSelectedTab() {
        return selectedTab;
    }
}
