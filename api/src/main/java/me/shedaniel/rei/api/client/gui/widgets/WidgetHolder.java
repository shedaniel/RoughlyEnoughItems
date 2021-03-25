package me.shedaniel.rei.api.client.gui.widgets;

import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;

public interface WidgetHolder {
    List<? extends GuiEventListener> children();
}
