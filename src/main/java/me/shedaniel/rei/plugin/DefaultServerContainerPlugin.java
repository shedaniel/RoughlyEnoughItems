/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.plugin;

import me.shedaniel.rei.plugin.containers.CraftingContainerInfoWrapper;
import me.shedaniel.rei.server.ContainerInfoHandler;
import net.minecraft.container.*;
import net.minecraft.util.Identifier;

public class DefaultServerContainerPlugin implements Runnable {
    @Override
    public void run() {
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/crafting"), CraftingContainerInfoWrapper.create(CraftingTableContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/crafting"), CraftingContainerInfoWrapper.create(PlayerContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/smelting"), CraftingContainerInfoWrapper.create(FurnaceContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/smoking"), CraftingContainerInfoWrapper.create(SmokerContainer.class));
        ContainerInfoHandler.registerContainerInfo(new Identifier("minecraft", "plugins/blasting"), CraftingContainerInfoWrapper.create(BlastFurnaceContainer.class));
    }
}
