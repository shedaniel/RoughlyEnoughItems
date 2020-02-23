/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.rei.api.ConfigManager;

public class REIModMenuEntryPoint implements ModMenuApi {
    
    @Override
    public String getModId() {
        return "roughlyenoughitems";
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ConfigManager.getInstance().getConfigScreen(parent);
    }
}
