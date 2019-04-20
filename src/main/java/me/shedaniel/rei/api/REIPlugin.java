package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.util.Identifier;

public interface REIPlugin extends REIPluginEntry {
    @Override
    default Identifier getPluginIdentifier() {
        return RoughlyEnoughItemsCore.getPluginIdentifier(this).orElse(Identifier.create("null"));
    }
}
