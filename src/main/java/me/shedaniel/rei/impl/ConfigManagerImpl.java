/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.gui.ConfigScreenProvider;
import me.sargunvohra.mcmods.autoconfig1u.gui.registry.GuiRegistry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.JsonObject;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.JsonPrimitive;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.impl.SyntaxError;
import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.api.annotations.Internal;
import me.shedaniel.rei.gui.ConfigReloadingScreen;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import me.shedaniel.rei.gui.widget.ReloadConfigButtonWidget;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.sargunvohra.mcmods.autoconfig1u.util.Utils.getUnsafely;
import static me.sargunvohra.mcmods.autoconfig1u.util.Utils.setUnsafely;

@Deprecated
@Internal
public class ConfigManagerImpl implements ConfigManager {
    
    private boolean craftableOnly;
    private List<EntryStack> favorites = new ArrayList<>();
    
    public ConfigManagerImpl() {
        this.craftableOnly = false;
        AutoConfig.register(ConfigObjectImpl.class, (definition, configClass) -> {
            return new JanksonConfigSerializer<ConfigObjectImpl>(definition, configClass, Jankson.builder().registerPrimitiveTypeAdapter(InputUtil.KeyCode.class, it -> {
                return it instanceof String ? InputUtil.fromName((String) it) : null;
            }).registerSerializer(InputUtil.KeyCode.class, (it, marshaller) -> new JsonPrimitive(it.getName())).build());
        });
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(ConfigObjectImpl.class);
        //noinspection rawtypes
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> Collections.singletonList(ConfigEntryBuilder.create().startEnumSelector(i13n, (Class) field.getType(), getUnsafely(field, config, null)).setDefaultValue(() -> getUnsafely(field, defaults)).setSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build()), field -> field.getType().isEnum(), ConfigObject.UseEnumSelectorInstead.class);
        loadFavoredEntries();
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> {
            @SuppressWarnings("rawtypes") List<AbstractConfigListEntry> entries = new ArrayList<>();
            for(FabricKeyBinding binding : ClientHelper.getInstance().getREIKeyBindings()) {
                entries.add(ConfigEntryBuilder.create().fillKeybindingField(I18n.translate(binding.getId()) + ":", binding).build());
            }
            KeyCodeEntry entry = ConfigEntryBuilder.create().startKeyCodeField(i13n, getUnsafely(field, config, InputUtil.UNKNOWN_KEYCODE)).setDefaultValue(() -> getUnsafely(field, defaults)).setSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build();
            entry.setAllowMouse(false);
            entries.add(entry);
            return entries;
        }, field -> field.getType() == InputUtil.KeyCode.class, ConfigObject.AddInFrontKeyCode.class);
        guiRegistry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
            KeyCodeEntry entry = ConfigEntryBuilder.create().startKeyCodeField(i13n, getUnsafely(field, config, InputUtil.UNKNOWN_KEYCODE)).setDefaultValue(() -> getUnsafely(field, defaults)).setSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build();
            entry.setAllowMouse(false);
            return Collections.singletonList(entry);
        }, field -> field.getType() == InputUtil.KeyCode.class);
        loadFavoredEntries();
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Config is loaded.");
    }
    
    @Override
    public List<EntryStack> getFavorites() {
        return favorites;
    }
    
    public void loadFavoredEntries() {
        favorites.clear();
        Gson gson = new GsonBuilder().create();
        for (String entry : ((ConfigObjectImpl) getConfig()).general.favorites) {
            EntryStack stack = EntryStack.readFromJson(gson.fromJson(entry, JsonElement.class));
            if (!stack.isEmpty()) favorites.add(stack);
        }
        saveConfig();
    }
    
    @Override
    public void saveConfig() {
        Gson gson = new GsonBuilder().create();
        ConfigObjectImpl object = (ConfigObjectImpl) getConfig();
        object.general.favorites.clear();
        for (EntryStack stack : favorites) {
            JsonElement element = stack.toJson();
            if (element != null) object.general.favorites.add(gson.toJson(element));
        }
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
            provider.setBuildFunction(builder -> {
                return builder.setAfterInitConsumer(screen -> {
                    if (MinecraftClient.getInstance().getNetworkHandler() != null && MinecraftClient.getInstance().getNetworkHandler().getRecipeManager() != null) {
                        ((ScreenHooks) screen).cloth_addButton(new ReloadConfigButtonWidget(4, 4, 100, 20, I18n.translate("text.rei.reload_config"), buttonWidget -> {
                            RoughlyEnoughItemsCore.syncRecipes(null);
                        }) {
                            @Override
                            public void render(int int_1, int int_2, float float_1) {
                                if (RecipeHelper.getInstance().arePluginsLoading()) {
                                    MinecraftClient.getInstance().openScreen(new ConfigReloadingScreen(MinecraftClient.getInstance().currentScreen));
                                } else
                                    super.render(int_1, int_2, float_1);
                            }
                        });
                    }
                    ((ScreenHooks) screen).cloth_addButton(new AbstractPressableButtonWidget(screen.width - 104, 4, 100, 20, I18n.translate("text.rei.credits")) {
                        @Override
                        public void onPress() {
                            MinecraftClient.getInstance().openScreen(new CreditsScreen(screen));
                        }
                    });
                }).setSavingRunnable(() -> {
                    saveConfig();
                    ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText());
                }).build();
            });
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
                for(int i = 0; i < list.size(); i++) {
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
