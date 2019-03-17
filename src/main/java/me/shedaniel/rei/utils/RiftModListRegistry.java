package me.shedaniel.rei.utils;

import me.shedaniel.api.ConfigRegistry;
import me.shedaniel.rei.client.ClientHelper;
import net.minecraft.client.Minecraft;

public class RiftModListRegistry {
    
    public static void register() {
        ConfigRegistry.registerConfig("roughlyenoughitems", () -> ClientHelper.openConfigWindow(Minecraft.getInstance().currentScreen, false));
    }
    
}
