package me.shedaniel.rei.listeners;

import net.minecraft.client.MinecraftClient;

public interface ClientTick extends IListener {
    
    public void onTick(MinecraftClient minecraftClient);
    
}
