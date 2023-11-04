/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonNull;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigManager;
import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.api.client.config.entry.EntryStackProvider;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRule;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleType;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.gui.config.CheatingMode;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.impl.client.config.addon.ConfigAddonRegistryImpl;
import me.shedaniel.rei.impl.client.config.collapsible.CollapsibleConfigManager;
import me.shedaniel.rei.impl.client.config.entries.ConfigAddonsEntry;
import me.shedaniel.rei.impl.client.gui.config.REIConfigScreen;
import me.shedaniel.rei.impl.common.InternalLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ConfigManagerImpl implements ConfigManager {
    private boolean craftableOnly = false;
    private final Gson gson = new GsonBuilder().create();
    private ConfigObjectImpl object;
    
    public ConfigManagerImpl() {
        AutoConfig.register(ConfigObjectImpl.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass, buildJankson(Jankson.builder())));
        InternalLogger.getInstance().info("Config loaded");
        saveConfig();
        FavoritesConfigManager.getInstance().syncFrom(this);
        CollapsibleConfigManager.getInstance().syncFrom(this);
    }
    
    public static Jankson buildJankson(Jankson.Builder builder) {
        // ResourceLocation
        builder.registerSerializer(ResourceLocation.class, (location, marshaller) -> {
            return new JsonPrimitive(location == null ? null : location.toString());
        });
        builder.registerDeserializer(String.class, ResourceLocation.class, (value, marshaller) -> {
            return value == null ? null : new ResourceLocation(value);
        });
        
        // CheatingMode
        builder.registerSerializer(CheatingMode.class, (value, marshaller) -> {
            if (value == CheatingMode.WHEN_CREATIVE) {
                return new JsonPrimitive("WHEN_CREATIVE");
            } else {
                return new JsonPrimitive(value == CheatingMode.ON);
            }
        });
        builder.registerDeserializer(Boolean.class, CheatingMode.class, (value, unmarshaller) -> {
            return value ? CheatingMode.ON : CheatingMode.OFF;
        });
        builder.registerDeserializer(String.class, CheatingMode.class, (value, unmarshaller) -> {
            return CheatingMode.valueOf(value.toUpperCase(Locale.ROOT));
        });
        
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
        
        // CompoundTag
        builder.registerSerializer(CompoundTag.class, (value, marshaller) -> {
            return marshaller.serialize(value.toString());
        });
        builder.registerDeserializer(String.class, CompoundTag.class, (value, marshaller) -> {
            try {
                return TagParser.parseTag(value);
            } catch (CommandSyntaxException e) {
                throw new DeserializationException(e);
            }
        });
        
        // EntryStackProvider
        builder.registerSerializer(EntryStackProvider.class, (stack, marshaller) -> {
            try {
                return marshaller.serialize(stack.save());
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, EntryStackProvider.class, (value, marshaller) -> {
            return EntryStackProvider.defer((CompoundTag) value);
        });
        builder.registerDeserializer(String.class, EntryStackProvider.class, (value, marshaller) -> {
            try {
                return EntryStackProvider.defer(TagParser.parseTag(value));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                return EntryStackProvider.ofStack(EntryStack.empty());
            }
        });
        
        // FilteringRule
        builder.registerSerializer(FilteringRule.class, (value, marshaller) -> {
            try {
                return marshaller.serialize(FilteringRuleType.save(value, new CompoundTag()));
            } catch (Exception e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        });
        builder.registerDeserializer(Tag.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read((CompoundTag) value);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        builder.registerDeserializer(String.class, FilteringRule.class, (value, marshaller) -> {
            try {
                return FilteringRuleType.read(TagParser.parseTag(value));
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
            return FavoriteEntry.readDelegated((CompoundTag) value);
        });
        builder.registerDeserializer(String.class, FavoriteEntry.class, (value, marshaller) -> {
            try {
                CompoundTag tag = TagParser.parseTag(value);
                return FavoriteEntry.readDelegated(tag);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        
        // CategoryIdentifier
        builder.registerSerializer(CategoryIdentifier.class, (value, marshaller) -> {
            return marshaller.serialize(value.toString());
        });
        builder.registerDeserializer(String.class, CategoryIdentifier.class, (value, marshaller) -> {
            try {
                return CategoryIdentifier.of(value);
            } catch (ResourceLocationException e) {
                throw new DeserializationException(e);
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
        for (FilteringRuleType<?> type : FilteringRuleTypeRegistry.getInstance()) {
            if (type.isSingular() && getConfig().getFilteringRules().stream().noneMatch(filteringRule -> filteringRule.getType().equals(type))) {
                getConfig().getFilteringRules().add(type.createNew());
            }
        }
        AutoConfig.getConfigHolder(ConfigObjectImpl.class).registerLoadListener((configHolder, configObject) -> {
            object = configObject;
            return InteractionResult.PASS;
        });
        AutoConfig.getConfigHolder(ConfigObjectImpl.class).save();
        FavoritesConfigManager.getInstance().saveConfig();
        InternalLogger.getInstance().debug("Config saved");
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
        return craftableOnly && getConfig().isCraftableFilterEnabled();
    }
    
    @Override
    public void toggleCraftableOnly() {
        craftableOnly = !craftableOnly;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public Screen getConfigScreen(Screen parent) {
        if (true) return new REIConfigScreen(parent);
        
        try {
            ConfigScreenProvider<ConfigObjectImpl> provider = (ConfigScreenProvider<ConfigObjectImpl>) AutoConfig.getConfigScreen(ConfigObjectImpl.class, parent);
            provider.setBuildFunction(builder -> {
                ConfigAddonRegistryImpl addonRegistry = (ConfigAddonRegistryImpl) ConfigAddonRegistry.getInstance();
                if (!addonRegistry.getAddons().isEmpty()) {
                    builder.getOrCreateCategory(Component.translatable("config.roughlyenoughitems.basics")).getEntries().add(0, new ConfigAddonsEntry(220));
                }
                return null;
            });
            return provider.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
