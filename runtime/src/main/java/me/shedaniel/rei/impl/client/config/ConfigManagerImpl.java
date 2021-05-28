/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.config;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.architectury.hooks.screen.ScreenHooks;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonNull;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.DisplayScreenType;
import me.shedaniel.rei.api.client.gui.config.SyntaxHighlightingMode;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.REIHelperImpl;
import me.shedaniel.rei.impl.client.config.entries.*;
import me.shedaniel.rei.impl.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.impl.client.entry.filtering.rules.ManualFilteringRule;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.credits.CreditsScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ConfigManagerImpl implements ConfigManager {
    private boolean craftableOnly = false;
    private final Gson gson = new GsonBuilder().create();
    private ConfigObjectImpl object;
    
    public ConfigManagerImpl() {
        Jankson jankson = Jankson.builder().build();
        AutoConfig.register(ConfigObjectImpl.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass, buildJankson(Jankson.builder())));
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(ConfigObjectImpl.class);
        guiRegistry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
            if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class))
                return Collections.emptyList();
            KeyCodeEntry entry = ConfigEntryBuilder.create().startModifierKeyCodeField(new TranslatableComponent(i13n), getUnsafely(field, config, ModifierKeyCode.unknown())).setModifierDefaultValue(() -> getUnsafely(field, defaults)).setModifierSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build();
            return Collections.singletonList(entry);
        }, field -> field.getType() == ModifierKeyCode.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> {
            ConfigObjectImpl.UsePercentage bounds = field.getAnnotation(ConfigObjectImpl.UsePercentage.class);
            return Collections.singletonList(ConfigEntryBuilder.create().startIntSlider(new TranslatableComponent(i13n), Mth.ceil(Utils.getUnsafely(field, config, 0.0) * 100), Mth.ceil(bounds.min() * 100), Mth.ceil(bounds.max() * 100)).setDefaultValue(() -> Mth.ceil((double) Utils.getUnsafely(field, defaults) * 100)).setSaveConsumer((newValue) -> {
                setUnsafely(field, config, newValue / 100d);
            }).setTextGetter(integer -> new TextComponent(bounds.prefix() + String.format("%d%%", integer))).build());
        }, (field) -> field.getType() == Double.TYPE || field.getType() == Double.class, ConfigObjectImpl.UsePercentage.class);
        
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        Collections.singletonList(new RecipeScreenTypeEntry(220, new TranslatableComponent(i13n), getUnsafely(field, config, DisplayScreenType.UNSET), getUnsafely(field, defaults), type -> setUnsafely(field, config, type)))
                , (field) -> field.getType() == DisplayScreenType.class, ConfigObjectImpl.UseSpecialRecipeTypeScreen.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        Collections.singletonList(new SearchFilterSyntaxHighlightingEntry(new TranslatableComponent(i13n), getUnsafely(field, config, SyntaxHighlightingMode.COLORFUL), getUnsafely(field, defaults), type -> setUnsafely(field, config, type)))
                , (field) -> field.getType() == SyntaxHighlightingMode.class, ConfigObjectImpl.UseSpecialSearchFilterSyntaxHighlightingScreen.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        REIHelper.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null ?
                                Collections.singletonList(new NoFilteringEntry(220, getUnsafely(field, config, new ArrayList<>()), getUnsafely(field, defaults), list -> setUnsafely(field, config, list)))
                                :
                                Collections.singletonList(new FilteringEntry(220, getUnsafely(field, config, new ArrayList<>()), ((ConfigObjectImpl.Advanced.Filtering) config).filteringRules, getUnsafely(field, defaults), list -> setUnsafely(field, config, list), list -> ((ConfigObjectImpl.Advanced.Filtering) config).filteringRules = Lists.newArrayList(list)))
                , (field) -> field.getType() == List.class, ConfigObjectImpl.UseFilteringScreen.class);
        saveConfig();
        RoughlyEnoughItemsCore.LOGGER.info("Config loaded.");
    }
    
    private static Jankson buildJankson(Jankson.Builder builder) {
        // InputConstants.Key
        builder.registerSerializer(InputConstants.Key.class, (value, marshaller) -> {
            return new JsonPrimitive(value.getName());
        });
        builder.registerDeserializer(String.class, InputConstants.Key.class, (value, marshaller) -> {
            return InputConstants.getKey(value);
        });
        
        // ModifierKeyCode
        builder.registerSerializer(ModifierKeyCode.class, (value, marshaller) -> {
            JsonObject object = new JsonObject();
            object.put("keyCode", new JsonPrimitive(value.getKeyCode().getName()));
            object.put("modifier", new JsonPrimitive(value.getModifier().getValue()));
            return object;
        });
        builder.registerDeserializer(JsonObject.class, ModifierKeyCode.class, (value, marshaller) -> {
            String code = value.get(String.class, "keyCode");
            if (code.endsWith(".unknown")) {
                return ModifierKeyCode.unknown();
            } else {
                InputConstants.Key keyCode = InputConstants.getKey(code);
                Modifier modifier = Modifier.of(value.getShort("modifier", (short) 0));
                return ModifierKeyCode.of(keyCode, modifier);
            }
        });
        
        // Tag
        builder.registerSerializer(Tag.class, (value, marshaller) -> {
            return marshaller.serialize(value.toString());
        });
        builder.registerDeserializer(String.class, Tag.class, (value, marshaller) -> {
            try {
                return TagParser.parseTag(value);
            } catch (CommandSyntaxException e) {
                throw new DeserializationException(e);
            }
        });
        
        // EntryStack
        builder.registerSerializer(EntryStack.class, (stack, marshaller) -> {
            try {
                return marshaller.serialize(stack.save());
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, EntryStack.class, (value, marshaller) -> {
            try {
                return EntryStack.read((CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return EntryStack.empty();
            }
        });
        builder.registerDeserializer(String.class, EntryStack.class, (value, marshaller) -> {
            try {
                return EntryStack.read(TagParser.parseTag(value));
            } catch (Exception e) {
                e.printStackTrace();
                return EntryStack.empty();
            }
        });
        
        // FilteringRule
        builder.registerSerializer(FilteringRule.class, (value, marshaller) -> {
            try {
                return marshaller.serialize(FilteringRule.save(value, new CompoundTag()));
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRule.read((CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        builder.registerDeserializer(String.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRule.read(TagParser.parseTag(value));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        
        // FavoriteEntry
        builder.registerSerializer(FavoriteEntry.class, (value, marshaller) -> {
            try {
                return marshaller.serialize(value.save(new CompoundTag()));
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, FavoriteEntry.class, (value, marshaller) -> {
            try {
                return FavoriteEntry.delegate(() -> FavoriteEntry.read((CompoundTag) value), () -> (CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        builder.registerDeserializer(String.class, FavoriteEntry.class, (value, marshaller) -> {
            try {
                CompoundTag tag = TagParser.parseTag(value);
                return FavoriteEntry.delegate(() -> FavoriteEntry.read(tag), () -> tag);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        
        return builder.build();
    }
    
    @Override
    public void startReload() {
    }
    
    public static ConfigManagerImpl getInstance() {
        return (ConfigManagerImpl) ConfigManager.getInstance();
    }
    
    @Override
    public void saveConfig() {
        if (getConfig().getFavoriteEntries() != null) {
            getConfig().getFavoriteEntries().removeIf(Objects::isNull);
        }
        if (getConfig().getFilteredStacks() != null) {
            getConfig().getFilteredStacks().removeIf(EntryStack::isEmpty);
            List<EntryStack<?>> normalizedFilteredStacks = CollectionUtils.map(getConfig().getFilteredStacks(), EntryStack::normalize);
            getConfig().getFilteredStacks().clear();
            getConfig().getFilteredStacks().addAll(normalizedFilteredStacks);
        }
        if (getConfig().getFilteringRules().stream().noneMatch(filteringRule -> filteringRule instanceof ManualFilteringRule)) {
            getConfig().getFilteringRules().add(new ManualFilteringRule());
        }
        AutoConfig.getConfigHolder(ConfigObjectImpl.class).registerLoadListener((configHolder, configObject) -> {
            object = configObject;
            return InteractionResult.PASS;
        });
        AutoConfig.getConfigHolder(ConfigObjectImpl.class).save();
    }
    
    @Override
    public ConfigObjectImpl getConfig() {
        if (object == null) {
            object = AutoConfig.getConfigHolder(ConfigObjectImpl.class).getConfig();
        }
        return object;
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
                builder.setGlobalized(true);
                builder.setGlobalizedExpanded(false);
                if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().getRecipeManager() != null) {
                    builder.getOrCreateCategory(new TranslatableComponent("config.roughlyenoughitems.advanced")).getEntries().add(0, new ReloadPluginsEntry(220));
                }
                return builder.setAfterInitConsumer(screen -> {
                    ScreenHooks.addRenderableWidget(screen, new Button(screen.width - 104, 4, 100, 20, new TranslatableComponent("text.rei.credits"), button -> {
                        MutableLong current = new MutableLong(0);
                        CreditsScreen creditsScreen = new CreditsScreen(screen);
                        Minecraft.getInstance().setScreen(creditsScreen);
                    }));
                }).setSavingRunnable(() -> {
                    saveConfig();
                    EntryRegistry.getInstance().refilter();
                    REIHelper.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
                    if (REIHelperImpl.getSearchField() != null) {
                        ScreenOverlayImpl.getEntryListWidget().updateSearch(REIHelperImpl.getSearchField().getText(), true);
                    }
                }).build();
            });
            return provider.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
