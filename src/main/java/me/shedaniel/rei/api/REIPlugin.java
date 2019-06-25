/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.util.Identifier;

/**
 * Get base class of a REI plugin.
 * This class has been replaced by {@link REIPluginEntry}
 */
@Deprecated
public interface REIPlugin extends REIPluginEntry {
    @Override
    default Identifier getPluginIdentifier() {
        return RoughlyEnoughItemsCore.getPluginIdentifier(this).orElse(Identifier.tryParse("null"));
    }
}
