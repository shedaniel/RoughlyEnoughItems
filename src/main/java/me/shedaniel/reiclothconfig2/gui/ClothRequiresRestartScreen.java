package me.shedaniel.reiclothconfig2.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;

public class ClothRequiresRestartScreen extends GuiYesNo {
    
    public ClothRequiresRestartScreen(GuiScreen parent) {
        super((t, i) -> {
            if (t)
                Minecraft.getInstance().shutdown();
            else
                Minecraft.getInstance().displayGuiScreen(parent);
        }, I18n.format("text.cloth-config.restart_required"), I18n.format("text.cloth-config.restart_required_sub"), I18n.format("text.cloth-config.exit_minecraft"), I18n.format("text.cloth-config.ignore_restart"), 318391381);
    }
    
}
