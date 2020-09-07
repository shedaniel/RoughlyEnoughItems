package me.shedaniel.rei;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.ApiStatus;

@Mod("roughlyenoughitems")
@ApiStatus.Internal
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoughlyEnoughItems {
    @SubscribeEvent
    public static void onSetup(FMLCommonSetupEvent event) {
        new RoughlyEnoughItemsInit();
    }
}
