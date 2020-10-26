/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.shedaniel.autoconfig1u.AutoConfig;
import me.shedaniel.autoconfig1u.annotation.ConfigEntry;
import me.shedaniel.autoconfig1u.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig1u.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig1u.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig1u.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.autoconfig1u.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.autoconfig1u.shadowed.blue.endless.jankson.JsonPrimitive;
import me.shedaniel.autoconfig1u.util.Utils;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.api.Modifier;
import me.shedaniel.clothconfig2.forge.api.ModifierKeyCode;
import me.shedaniel.clothconfig2.forge.gui.entries.KeyCodeEntry;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ConfigManager;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.gui.config.RecipeScreenType;
import me.shedaniel.rei.gui.config.entry.FilteringEntry;
import me.shedaniel.rei.gui.config.entry.NoFilteringEntry;
import me.shedaniel.rei.gui.config.entry.RecipeScreenTypeEntry;
import me.shedaniel.rei.gui.config.entry.ReloadPluginsEntry;
import me.shedaniel.rei.gui.credits.CreditsScreen;
import me.shedaniel.rei.impl.filtering.FilteringRule;
import me.shedaniel.rei.impl.filtering.rules.ManualFilteringRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.shedaniel.autoconfig1u.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig1u.util.Utils.setUnsafely;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class ConfigManagerImpl implements ConfigManager {
    
    private boolean craftableOnly;
    private final Gson gson = new GsonBuilder().create();
    
    public ConfigManagerImpl() {
        this.craftableOnly = false;
        Jankson jankson = Jankson.builder().build();
        AutoConfig.register(ConfigObjectImpl.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass, Jankson.builder().registerPrimitiveTypeAdapter(InputMappings.Input.class, it -> {
            return it instanceof String ? InputMappings.getKey((String) it) : null;
        }).registerSerializer(InputMappings.Input.class, (it, marshaller) -> new JsonPrimitive(it.getName())).registerTypeAdapter(ModifierKeyCode.class, o -> {
            String code = ((JsonPrimitive) o.get("keyCode")).asString();
            if (code.endsWith(".unknown")) return ModifierKeyCode.unknown();
            InputMappings.Input keyCode = InputMappings.getKey(code);
            Modifier modifier = Modifier.of(((Number) ((JsonPrimitive) o.get("modifier")).getValue()).shortValue());
            return ModifierKeyCode.of(keyCode, modifier);
        }).registerSerializer(ModifierKeyCode.class, (keyCode, marshaller) -> {
            JsonObject object = new JsonObject();
            object.put("keyCode", new JsonPrimitive(keyCode.getKeyCode().getName()));
            object.put("modifier", new JsonPrimitive(keyCode.getModifier().getValue()));
            return object;
        }).registerSerializer(EntryStack.class, (stack, marshaller) -> {
            try {
                return jankson.load(gson.toJson(stack.toJson()));
            } catch (SyntaxError syntaxError) {
                syntaxError.printStackTrace();
                return JsonNull.INSTANCE;
            }
        }).registerPrimitiveTypeAdapter(EntryStack.class, it -> {
            return it instanceof String ? EntryStack.readFromJson(gson.fromJson((String) it, JsonElement.class)) : null;
        }).registerTypeAdapter(EntryStack.class, it -> {
            return EntryStack.readFromJson(gson.fromJson(it.toString(), JsonElement.class));
        }).registerSerializer(FavoriteEntry.class, (favoriteEntry, marshaller) -> {
            try {
                return jankson.load(favoriteEntry.toJson(new com.google.gson.JsonObject()).toString());
            } catch (SyntaxError syntaxError) {
                syntaxError.printStackTrace();
                return JsonNull.INSTANCE;
            }
        }).registerTypeAdapter(FavoriteEntry.class, it -> {
            com.google.gson.JsonObject object = gson.fromJson(it.toString(), com.google.gson.JsonObject.class);
            return FavoriteEntry.delegate(() -> FavoriteEntry.fromJson(object), () -> object);
        }).registerPrimitiveTypeAdapter(FavoriteEntry.class, it -> {
            com.google.gson.JsonObject object = gson.fromJson(it.toString(), com.google.gson.JsonObject.class);
            return FavoriteEntry.delegate(() -> FavoriteEntry.fromJson(object), () -> object);
        }).registerSerializer(FilteringRule.class, (rule, marshaller) -> {
            return new JsonPrimitive(FilteringRule.toTag(rule, new CompoundNBT()).toString());
        }).registerPrimitiveTypeAdapter(FilteringRule.class, it -> {
            try {
                return it instanceof String ? FilteringRule.fromTag(JsonToNBT.parseTag((String) it)) : null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).build()));
        GuiRegistry guiRegistry = AutoConfig.getGuiRegistry(ConfigObjectImpl.class);
        guiRegistry.registerPredicateProvider((i13n, field, config, defaults, guiProvider) -> {
            if (field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class))
                return Collections.emptyList();
            KeyCodeEntry entry = ConfigEntryBuilder.create().startModifierKeyCodeField(new TranslationTextComponent(i13n), getUnsafely(field, config, ModifierKeyCode.unknown())).setModifierDefaultValue(() -> getUnsafely(field, defaults)).setModifierSaveConsumer(newValue -> setUnsafely(field, config, newValue)).build();
            return Collections.singletonList(entry);
        }, field -> field.getType() == ModifierKeyCode.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) -> {
            ConfigObjectImpl.UsePercentage bounds = field.getAnnotation(ConfigObjectImpl.UsePercentage.class);
            return Collections.singletonList(ConfigEntryBuilder.create().startIntSlider(new TranslationTextComponent(i13n), MathHelper.ceil(Utils.getUnsafely(field, config, 0.0) * 100), MathHelper.ceil(bounds.min() * 100), MathHelper.ceil(bounds.max() * 100)).setDefaultValue(() -> MathHelper.ceil((double) Utils.getUnsafely(field, defaults) * 100)).setSaveConsumer((newValue) -> {
                Utils.setUnsafely(field, config, newValue / 100d);
            }).setTextGetter(integer -> new StringTextComponent(String.format("Size: %d%%", integer))).build());
        }, (field) -> field.getType() == Double.TYPE || field.getType() == Double.class, ConfigObjectImpl.UsePercentage.class);
        
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        Collections.singletonList(new RecipeScreenTypeEntry(220, new TranslationTextComponent(i13n), getUnsafely(field, config, RecipeScreenType.UNSET), getUnsafely(field, defaults), type -> setUnsafely(field, config, type)))
                , (field) -> field.getType() == RecipeScreenType.class, ConfigObjectImpl.UseSpecialRecipeTypeScreen.class);
        guiRegistry.registerAnnotationProvider((i13n, field, config, defaults, guiProvider) ->
                        REIHelper.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null ?
                                Collections.singletonList(new NoFilteringEntry(220, getUnsafely(field, config, new ArrayList<>()), getUnsafely(field, defaults), list -> setUnsafely(field, config, list)))
                                :
                                Collections.singletonList(new FilteringEntry(220, getUnsafely(field, config, new ArrayList<>()), ((ConfigObjectImpl.Advanced.Filtering) config).filteringRules, getUnsafely(field, defaults), list -> setUnsafely(field, config, list), list -> ((ConfigObjectImpl.Advanced.Filtering) config).filteringRules = Lists.newArrayList(list)))
                , (field) -> field.getType() == List.class, ConfigObjectImpl.UseFilteringScreen.class);
        saveConfig();
        RoughlyEnoughItemsCore.LOGGER.info("Config loaded.");
        
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (minecraft, screen) -> getConfigScreen(screen));
    }
    
    @Override
    public void saveConfig() {
        if (getConfig().getFavoriteEntries() != null)
            getConfig().getFavoriteEntries().removeIf(Objects::isNull);
        if (getConfig().getFilteredStacks() != null) {
            getConfig().getFilteredStacks().removeIf(EntryStack::isEmpty);
            for (EntryStack stack : getConfig().getFilteredStacks()) {
                stack.setting(EntryStack.Settings.CHECK_AMOUNT, EntryStack.Settings.FALSE).setting(EntryStack.Settings.RENDER_COUNTS, EntryStack.Settings.FALSE).setting(EntryStack.Settings.CHECK_TAGS, EntryStack.Settings.TRUE);
            }
        }
        if (getConfig().getFilteringRules().stream().noneMatch(filteringRule -> filteringRule instanceof ManualFilteringRule)) {
            getConfig().getFilteringRules().add(new ManualFilteringRule());
        }
        ((me.shedaniel.autoconfig1u.ConfigManager<ConfigObjectImpl>) AutoConfig.getConfigHolder(ConfigObjectImpl.class)).save();
    }
    
    @Override
    public ConfigObjectImpl getConfig() {
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
                builder.setGlobalized(true);
                builder.setGlobalizedExpanded(true);
                if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().getConnection().getRecipeManager() != null) {
                    builder.getOrCreateCategory(new TranslationTextComponent("config.roughlyenoughitems.advanced")).getEntries().add(0, new ReloadPluginsEntry(220));
                }
                return builder.setAfterInitConsumer(screen -> {
                    Button creditsButton = new Button(screen.width - 104, 4, 100, 20, new TranslationTextComponent("text.rei.credits"), button -> {
                        Minecraft.getInstance().setScreen(new CreditsScreen(screen));
                    });
                    screen.buttons.add(creditsButton);
                    ((List<IGuiEventListener>) screen.children()).add(creditsButton);
                }).setSavingRunnable(() -> {
                    saveConfig();
                    EntryRegistry.getInstance().refilter();
                    if (ScreenHelper.getSearchField() != null)
                        ContainerScreenOverlay.getEntryListWidget().updateSearch(ScreenHelper.getSearchField().getText(), true);
                }).build();
            });
            return provider.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static class ConfigErrorScreen extends Screen {
        private final ITextComponent message;
        private final Screen parent;
        
        public ConfigErrorScreen(Screen parent, ITextComponent title, ITextComponent message) {
            super(title);
            this.parent = parent;
            this.message = message;
        }
        
        @Override
        protected void init() {
            super.init();
            this.addButton(new Button(this.width / 2 - 100, 140, 200, 20, DialogTexts.GUI_CANCEL, button -> this.minecraft.setScreen(parent)));
        }
        
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            drawCenteredString(matrices, this.font, this.title, this.width / 2, 90, 16777215);
            drawCenteredString(matrices, this.font, this.message, this.width / 2, 110, 16777215);
            super.render(matrices, mouseX, mouseY, delta);
        }
        
        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }
    }
    
}
