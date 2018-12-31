package me.shedaniel.listenerdefinitions;

import net.minecraft.client.settings.KeyBinding;

import java.util.List;

public interface PreLoadOptions extends IEvent {
    public List<KeyBinding> loadOptions();
}
