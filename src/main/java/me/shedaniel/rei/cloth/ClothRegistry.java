package me.shedaniel.rei.cloth;

import me.shedaniel.cloth.api.EventPriority;
import me.shedaniel.cloth.hooks.ClothModMenuHooks;
import me.shedaniel.rei.client.ClientHelper;
import net.minecraft.client.MinecraftClient;

public class ClothRegistry {
    
    public static void register() {
        Runnable configRunnable = () -> ClientHelper.openConfigWindow(MinecraftClient.getInstance().currentScreen, false);
        ClothModMenuHooks.CONFIG_BUTTON_EVENT.registerListener(event -> {
            if (event.getModContainer() != null && event.getModContainer().getMetadata().getId().equalsIgnoreCase("roughlyenoughitems")) {
                event.setEnabled(true);
                event.setClickedRunnable(configRunnable);
                event.setCancelled(true);
            }
        }, EventPriority.LOWEST);
    }
    
}
