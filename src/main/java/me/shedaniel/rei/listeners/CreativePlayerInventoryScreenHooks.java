package me.shedaniel.rei.listeners;

import net.minecraft.client.gui.ingame.CreativePlayerInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativePlayerInventoryScreen.class)
public interface CreativePlayerInventoryScreenHooks {
    @Accessor("selectedTab")
    int rei_getSelectedTab();
}
