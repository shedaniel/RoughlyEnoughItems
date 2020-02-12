/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.prospector.modmenu.ModMenu;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.gui.ConfigScreenProvider;
import me.sargunvohra.mcmods.autoconfig1u.gui.registry.GuiRegistry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.JsonObject;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.JsonPrimitive;
import me.sargunvohra.mcmods.autoconfig1u.util.Utils;
import me.shedaniel.cloth.hooks.ScreenHooks;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.gui.ConfigReloadingScreen;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.entry.FilteringEntry;
import me.shedaniel.rei.gui.config.entry.NoFilteringEntry;
import me.shedaniel.rei.gui.config.entry.RecipeScreenTypeEntry;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import me.shedaniel.rei.gui.widget.ReloadConfigButtonWidget;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static me.sargunvohra.mcmods.autoconfig1u.util.Utils.getUnsafely;
import static me.sargunvohra.mcmods.autoconfig1u.util.Utils.setUnsafely;

@ApiStatus.Internal
public class ConfigManagerImpl implements ConfigManager {
    
    private boolean craftableOnly;
    private Gson gson = new GsonBuilder().create();
    
    public ConfigManagerImpl() {
        this.craftableOnly = false;
        AutoConfig.register(ConfigObjectImpl.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass, Jankson.builder().registerPrimitiveTypeAdapter(InputUtil.KeyCode.class, it -> {
            return it instanceof String ? InputUtil.fromName((String) it) : null;
        }).registerSerializer(InputUtil.KeyCode.class, (it, marshaller) -> new JsonPrimitive(it.getName())).registerTypeAdapter(ModifierKeyCode.class, o -> {
            InputUtil.KeyCode keyCode = InputUtil.fromName(((JsonPrimitive) o.get("keyCode")).asString());
            Modifier modifier = Modifier.of(((Number) ((JsonPrimitive) o.get("modifier")).getValue()).shortValue());
            return ModifierKeyCode.of(keyCode, modifier);
        }).registerSerializer(ModifierKeyCode.class, (keyCode, marshaller) -> {
            JsonObject object = new JsonObject();
            object.put("keyCode", new JsonPrimitive(keyCode.getKeyCode().getName()));
            object.put("modifier", new JsonPrimitive(keyCode.getModifier().getValue()));
            return object;
        }).registerSerializer(EntryStack.class, (stack, marshaller) -> {
            return new JsonPrimitive(gson.toJson(stack.toJson()));
        }).registerPrimitiveTypeAdapter(EntryStack.class, it -> {
            return it instanceof String ? EntryStack.readFromJson(gson.fromJson((String) it, JsonElement.class)) : EntryStack.empty();
        }).build()));
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(ConfigObjectImpl.class);
        //noinspection rawtypes
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> Collections.singletonList(ConfigEntryBuilder.create().startEnumSelector(i13n, (Class) field.getType(), getUnsafely(field, config, null)).setDefaultValue(() -> getUnsafely(field, defaults)).setSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build()), field -> field.getType().isEnum(), ConfigObjectImpl.UseEnumSelectorInstead.class);
        guiRegistry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
            if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class))
                return Collections.emptyList();
            KeyCodeEntry entry = ConfigEntryBuilder.create().startModifierKeyCodeField(i13n, getUnsafely(field, config, ModifierKeyCode.unknown())).setModifierDefaultValue(() -> getUnsafely(field, defaults)).setModifierSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build();
            entry.setAllowMouse(false);
            return Collections.singletonList(entry);
        }, field -> field.getType() == ModifierKeyCode.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> {
            ConfigObjectImpl.UsePercentage bounds = field.getAnnotation(ConfigObjectImpl.UsePercentage.class);
            return Collections.singletonList(ConfigEntryBuilder.create().startIntSlider(i13n, MathHelper.ceil(Utils.getUnsafely(field, config, 0.0) * 100), MathHelper.ceil(bounds.min() * 100), MathHelper.ceil(bounds.max() * 100)).setDefaultValue(() -> MathHelper.ceil((double) Utils.getUnsafely(field, defaults) * 100)).setSaveConsumer((newValue) -> {
                Utils.setUnsafely(field, config, newValue / 100d);
            }).setTextGetter(integer -> String.format("Size: %d%%", integer)).build());
        }, (field) -> field.getType() == Double.TYPE || field.getType() == Double.class, ConfigObjectImpl.UsePercentage.class);
        
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        Collections.singletonList(new RecipeScreenTypeEntry(220, i13n, getUnsafely(field, config, RecipeScreenType.UNSET), getUnsafely(field, defaults), type -> setUnsafely(field, config, type)))
                , (field) -> field.getType() == RecipeScreenType.class, ConfigObjectImpl.UseSpecialRecipeTypeScreen.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        ScreenHelper.getLastContainerScreenHooks() == null || MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().getNetworkHandler().getRecipeManager() == null ?
                                Collections.singletonList(new NoFilteringEntry(getUnsafely(field, config, new ArrayList<>()), getUnsafely(field, defaults), list -> setUnsafely(field, config, list)))
                                :
                                Collections.singletonList(new FilteringEntry(getUnsafely(field, config, new ArrayList<>()), getUnsafely(field, defaults), list -> setUnsafely(field, config, list)))
                , (field) -> field.getType() == List.class, ConfigObjectImpl.UseFilteringScreen.class);
        saveConfig();
        RoughlyEnoughItemsCore.LOGGER.info("[REI] Config is loaded.");
    }
    
    @Override
    public List<EntryStack> getFavorites() {
        return getConfig().getFavorites();
    }
    
    @Override
    public void saveConfig() {
        if (getFavorites() != null)
            getFavorites().removeIf(EntryStack::isEmpty);
        if (getConfig().getFilteredStacks() != null)
            getConfig().getFilteredStacks().removeIf(EntryStack::isEmpty);
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
    
    @SuppressWarnings("deprecation")
    @Override
    public Screen getConfigScreen(Screen parent) {
        try {
            ConfigScreenProvider<ConfigObjectImpl> provider = (ConfigScreenProvider<ConfigObjectImpl>) AutoConfig.getConfigScreen(ConfigObjectImpl.class, parent);
            provider.setI13nFunction(manager -> "config.roughlyenoughitems");
            provider.setOptionFunction((baseI13n, field) -> field.isAnnotationPresent(ConfigObjectImpl.DontApplyFieldName.class) ? baseI13n : String.format("%s.%s", baseI13n, field.getName()));
            provider.setCategoryFunction((baseI13n, categoryName) -> String.format("%s.%s", baseI13n, categoryName));
            provider.setBuildFunction(builder -> {
                if (FabricLoader.getInstance().isModLoaded("modmenu")) {
                    builder.getOrCreateCategory("config.roughlyenoughitems.!general").addEntry(new TooltipListEntry<Object>(I18n.translate("config.roughlyenoughitems.smooth_scrolling"), null) {
                        int width = 220;
                        private AbstractButtonWidget buttonWidget = new AbstractPressableButtonWidget(0, 0, 0, 20, this.getFieldName()) {
                            public void onPress() {
                                Screen screen = ModMenu.getConfigScreen("cloth-config2", parent);
                                if (screen != null) {
                                    MinecraftClient.getInstance().openScreen(screen);
                                } else
                                    ModMenu.openConfigScreen("cloth-config2");
                            }
                        };
                        private List<Element> children = ImmutableList.of(this.buttonWidget);
                        
                        public Object getValue() {
                            return null;
                        }
                        
                        public Optional<Object> getDefaultValue() {
                            return Optional.empty();
                        }
                        
                        public void save() {
                        }
                        
                        public List<? extends Element> children() {
                            return this.children;
                        }
                        
                        public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
                            super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
                            Window window = MinecraftClient.getInstance().getWindow();
                            this.buttonWidget.active = this.isEditable();
                            this.buttonWidget.y = y;
                            this.buttonWidget.x = x + entryWidth / 2 - this.width / 2;
                            this.buttonWidget.setWidth(this.width);
                            this.buttonWidget.render(mouseX, mouseY, delta);
                        }
                    });
                }
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
                    ((ScreenHooks) screen).cloth_addButton(new ButtonWidget(screen.width - 104, 4, 100, 20, I18n.translate("text.rei.credits"), button -> {
                        MinecraftClient.getInstance().openScreen(new CreditsScreen(screen));
                    }));
                }).setSavingRunnable(() -> {
                    saveConfig();
                    if (ScreenHelper.getSearchField() != null)
                        ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText());
                }).build();
            });
            return provider.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
