package me.shedaniel.rei.mixin;

import me.shedaniel.rei.api.TabGetter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiContainerCreative.class)
public class MixinTabGetter implements TabGetter {
    
    @Shadow
    private static int selectedTabIndex;
    
    @Override
    public int rei_getSelectedTab() {
        return selectedTabIndex;
    }
    
}
