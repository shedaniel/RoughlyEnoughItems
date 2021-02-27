package me.shedaniel.rei.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.rei.RoughlyEnoughItemsInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("roughlyenoughitems")
public class RoughlyEnoughItemsForge {
    public RoughlyEnoughItemsForge() {
        EventBuses.registerModEventBus("roughlyenoughitems", FMLJavaModLoadingContext.get().getModEventBus());
        RoughlyEnoughItemsInitializer.onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> RoughlyEnoughItemsInitializer::onInitializeClient);
    }
}
