/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.gui.ConfigScreenProvider;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.annotations.Internal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;

import java.util.List;

@Deprecated
@Internal
public class ConfigManagerImpl implements ConfigManager {
    
    private boolean craftableOnly;
    
    public ConfigManagerImpl() {
        this.craftableOnly = false;
        AutoConfig.register(ConfigObjectImpl.class, JanksonConfigSerializer::new);
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Config is loaded.");
    }
    
    @Override
    public void saveConfig() {
        ((me.sargunvohra.mcmods.autoconfig1u.ConfigManager<ConfigObjectImpl>) AutoConfig.getConfigHolder(ConfigObjectImpl.class)).save();
    }
    
    @Override
    public ConfigObject getConfig() {
        return AutoConfig.getConfigHolder(ConfigObjectImpl.class).getConfig();
    }
    
    @Override
    public boolean isCraftableOnlyEnabled() {
        return craftableOnly;
    }
    
    @Override
    public void toggleCraftableOnly() {
        craftableOnly = !craftableOnly;
    }
    
    @Override
    public void openConfigScreen(Screen parent) {
        MinecraftClient.getInstance().openScreen(getConfigScreen(parent));
    }
    
    @Override
    public Screen getConfigScreen(Screen parent) {
        try {
            ConfigScreenProvider<ConfigObjectImpl> provider = (ConfigScreenProvider<ConfigObjectImpl>) AutoConfig.getConfigScreen(ConfigObjectImpl.class, parent);
            provider.setI13nFunction(manager -> "config.roughlyenoughitems");
            provider.setOptionFunction((baseI13n, field) -> field.isAnnotationPresent(ConfigObject.DontApplyFieldName.class) ? baseI13n : String.format("%s.%s", baseI13n, field.getName()));
            provider.setCategoryFunction((baseI13n, categoryName) -> String.format("%s.%s", baseI13n, categoryName));
            return provider.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Screen(new LiteralText("")) {
            @Override
            public void render(int int_1, int int_2, float float_1) {
                renderDirtBackground(0);
                List<String> list = minecraft.textRenderer.wrapStringToWidthAsList(I18n.translate("text.rei.config_api_failed"), width - 100);
                int y = (int) (height / 2 - minecraft.textRenderer.fontHeight * 1.3f / 2 * list.size());
                for (int i = 0; i < list.size(); i++) {
                    String s = list.get(i);
                    drawCenteredString(minecraft.textRenderer, s, width / 2, y, -1);
                    y += minecraft.textRenderer.fontHeight;
                }
                super.render(int_1, int_2, float_1);
            }
            
            @Override
            protected void init() {
                super.init();
                addButton(new net.minecraft.client.gui.widget.ButtonWidget(width / 2 - 100, height - 26, 200, 20, I18n.translate("text.rei.back"), buttonWidget -> {
                    this.minecraft.openScreen(parent);
                }));
            }
            
            @Override
            public boolean keyPressed(int int_1, int int_2, int int_3) {
                if (int_1 == 256 && this.shouldCloseOnEsc()) {
                    this.minecraft.openScreen(parent);
                    return true;
                }
                return super.keyPressed(int_1, int_2, int_3);
            }
        };
    }
    
}
