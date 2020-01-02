/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.annotations.Internal;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.util.Util;

@Deprecated
@Internal
public class ConfigReloadingScreen extends Screen {
    
    private Screen parent;
    
    public ConfigReloadingScreen(Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        if (!RecipeHelper.getInstance().arePluginsLoading())
            minecraft.openScreen(parent);
        this.drawCenteredString(this.font, I18n.translate("text.rei.config.is.reloading"), this.width / 2, this.height / 2 - 50, 16777215);
        String string_3;
        switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
            case 0:
            default:
                string_3 = "O o o";
                break;
            case 1:
            case 3:
                string_3 = "o O o";
                break;
            case 2:
                string_3 = "o o O";
        }
        this.drawCenteredString(this.font, string_3, this.width / 2, this.height / 2 - 41, 8421504);
        super.render(int_1, int_2, float_1);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
