/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.rei.api.ConfigManager;
import net.minecraft.client.gui.screen.Screen;

import java.util.function.Function;

public class REIModMenuEntryPoint implements ModMenuApi {
    
    @Override
    public String getModId() {
        return "roughlyenoughitems";
    }
    
    @Override
    public Function<Screen, ? extends Screen> getConfigScreenFactory() {
        return parent -> ConfigManager.getInstance().getConfigScreen(parent);
    }
    
}
