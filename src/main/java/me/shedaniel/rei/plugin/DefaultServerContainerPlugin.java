package me.shedaniel.rei.plugin;

import me.shedaniel.rei.plugin.containers.CraftingContainerInfoWrapper;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.minecraft.container.*;
import net.minecraft.util.Identifier;

public class DefaultServerContainerPlugin implements Runnable {
    @Override
    public void run() {
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/crafting"), new CraftingContainerInfoWrapper(CraftingTableContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/crafting"), new CraftingContainerInfoWrapper(PlayerContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/smelting"), new CraftingContainerInfoWrapper(FurnaceContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/smoking"), new CraftingContainerInfoWrapper(SmokerContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/blasting"), new CraftingContainerInfoWrapper(BlastFurnaceContainer.class));
    }
}
