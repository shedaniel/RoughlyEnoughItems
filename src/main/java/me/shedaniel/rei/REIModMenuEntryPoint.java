/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei;

import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.Screen;

import java.util.Optional;
import java.util.function.Supplier;

public class REIModMenuEntryPoint implements ModMenuApi {
    
    @Override
    public String getModId() {
        return "roughlyenoughitems";
    }
    
    @Override
    public Optional<Supplier<Screen>> getConfigScreen(Screen screen) {
        return Optional.of(() -> getScreen(screen));
    }
    
    public Screen getScreen(Screen parent) {
        return RoughlyEnoughItemsCore.getConfigManager().getConfigScreen(parent);
    }
    
}
