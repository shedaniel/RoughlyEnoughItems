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

package me.shedaniel.rei.impl.client.gui.config;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.utils.value.IntValue;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import me.shedaniel.rei.impl.client.REIRuntimeImpl;
import me.shedaniel.rei.impl.client.config.ConfigManagerImpl;
import me.shedaniel.rei.impl.client.config.ConfigObjectImpl;
import me.shedaniel.rei.impl.client.gui.ScreenOverlayImpl;
import me.shedaniel.rei.impl.client.gui.config.components.ConfigCategoriesListWidget;
import me.shedaniel.rei.impl.client.gui.config.components.ConfigEntriesListWidget;
import me.shedaniel.rei.impl.client.gui.config.components.ConfigSearchListWidget;
import me.shedaniel.rei.impl.client.gui.config.options.*;
import me.shedaniel.rei.impl.client.gui.modules.Menu;
import me.shedaniel.rei.impl.client.gui.widget.HoleWidget;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.literal;
import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.translatable;

public class REIConfigScreen extends Screen implements ConfigAccess {
    private final Screen parent;
    private final List<OptionCategory> categories;
    private final List<Widget> widgets = new ArrayList<>();
    private final Map<String, ?> defaultOptions = new HashMap<>();
    private final Map<String, ?> options = new HashMap<>();
    private OptionCategory activeCategory;
    private boolean searching;
    @Nullable
    private Menu menu;
    @Nullable
    private CompositeOption<ModifierKeyCode> focusedKeycodeOption = null;
    private ModifierKeyCode partialKeycode = null;
    
    public REIConfigScreen(Screen parent) {
        this(parent, AllREIConfigCategories.CATEGORIES);
    }
    
    public REIConfigScreen(Screen parent, List<OptionCategory> categories) {
        super(ConfigUtils.translatable("config.roughlyenoughitems.title"));
        this.parent = parent;
        this.categories = CollectionUtils.map(categories, OptionCategory::copy);
        this.cleanRequiresLevel();
        Preconditions.checkArgument(!this.categories.isEmpty(), "Categories cannot be empty!");
        this.activeCategory = this.categories.get(0);
        
        ConfigObjectImpl defaultConfig = new ConfigObjectImpl();
        ConfigObjectImpl config = ConfigManagerImpl.getInstance().getConfig();
        for (OptionCategory category : this.categories) {
            for (OptionGroup group : category.getGroups()) {
                for (CompositeOption<?> option : group.getOptions()) {
                    ((Map<String, Object>) this.defaultOptions).put(option.getId(), option.getBind().apply(defaultConfig));
                    ((Map<String, Object>) this.options).put(option.getId(), option.getBind().apply(config));
                }
            }
        }
    }
    
    private void cleanRequiresLevel() {
        if (!(REIRuntime.getInstance().getPreviousContainerScreen() == null || Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().getConnection().getRecipeManager() == null)) {
            return;
        }
        
        for (OptionCategory category : this.categories) {
            for (OptionGroup group : category.getGroups()) {
                group.getOptions().replaceAll(option -> {
                    if (option.isRequiresLevel()) {
                        return new CompositeOption<>(option.getId(), option.getName(), option.getDescription(), i -> 0, (i, v) -> new Object())
                                .entry(value -> translatable("config.rei.texts.requires_level").withStyle(ChatFormatting.RED))
                                .defaultValue(() -> 1);
                    } else {
                        return option;
                    }
                });
            }
        }
    }
    
    @Override
    public void init() {
        super.init();
        this.widgets.clear();
        this.widgets.add(Widgets.createLabel(new Point(width / 2, 12), this.title));
        int sideWidth = (int) Math.round(width / 4.2);
        if (this.searching) {
            this.widgets.add(Widgets.createButton(new Rectangle(8, 32, sideWidth, 20), literal("↩ ").append(translatable("gui.back")))
                    .onClick(button -> setSearching(false)));
            this.widgets.add(HoleWidget.createBackground(new Rectangle(8 + sideWidth + 4, 32, width - 16 - sideWidth - 4, 20), () -> 0, 32));
            TextFieldWidget textField = new TextFieldWidget(new Rectangle(8 + sideWidth + 4 + 6, 32 + 6, width - 16 - sideWidth - 4 - 10, 12)) {
                @Override
                protected void renderSuggestion(GuiGraphics graphics, int x, int y) {
                    int color;
                    if (containsMouse(PointHelper.ofMouse()) || isFocused()) {
                        color = 0xddeaeaea;
                    } else {
                        color = -6250336;
                    }
                    graphics.drawString(this.font, this.font.plainSubstrByWidth(this.getSuggestion(), this.getWidth()), x, y, color);
                }
            };
            textField.setHasBorder(false);
            textField.setMaxLength(9000);
            this.widgets.add(textField);
            this.widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
                textField.setSuggestion(!textField.isFocused() && textField.getText().isEmpty() ? I18n.get("config.rei.texts.search_options") : null);
            }));
            this.widgets.add(ConfigSearchListWidget.create(this, this.categories, textField, new Rectangle(8, 32 + 20 + 4, width - 16, height - 32 - (32 + 20 + 4))));
        } else {
            boolean singlePane = width - 20 - sideWidth <= 330;
            int singleSideWidth = 32 + 6 + 4;
            Mutable<Widget> list = new MutableObject<>(createEntriesList(singlePane, singleSideWidth, sideWidth));
            IntValue selectedCategory = new IntValue() {
                @Override
                public void accept(int index) {
                    REIConfigScreen.this.activeCategory = categories.get(index);
                    list.setValue(createEntriesList(singlePane, singleSideWidth, sideWidth));
                }
                
                @Override
                public int getAsInt() {
                    return categories.indexOf(activeCategory);
                }
            };
            if (!singlePane) {
                this.widgets.add(ConfigCategoriesListWidget.create(new Rectangle(8, 32, sideWidth, height - 32 - 32), categories, selectedCategory));
            } else {
                this.widgets.add(ConfigCategoriesListWidget.createTiny(new Rectangle(8, 32, singleSideWidth - 4, height - 32 - 32), categories, selectedCategory));
            }
            this.widgets.add(Widgets.delegate(list::getValue));
        }
        
        this.widgets.add(Widgets.createButton(new Rectangle(width / 2 - 150 - 10, height - 26, 150, 20), translatable("gui.cancel")).onClick(button -> {
            Minecraft.getInstance().setScreen(this.parent);
        }));
        this.widgets.add(Widgets.createButton(new Rectangle(width / 2 + 10, height - 26, 150, 20), translatable("gui.done")).onClick(button -> {
            for (OptionCategory optionCategory : this.categories) {
                for (OptionGroup group : optionCategory.getGroups()) {
                    for (CompositeOption<?> option : group.getOptions()) {
                        ((BiConsumer<ConfigObjectImpl, Object>) option.getSave()).accept(ConfigManagerImpl.getInstance().getConfig(), this.get(option));
                    }
                }
            }
            
            ConfigManagerImpl.getInstance().saveConfig();
            EntryRegistry.getInstance().refilter();
            REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
            if (REIRuntimeImpl.getSearchField() != null) {
                ScreenOverlayImpl.getEntryListWidget().updateSearch(REIRuntimeImpl.getSearchField().getText(), true);
            }
            Minecraft.getInstance().setScreen(this.parent);
        }));
    }
    
    private Widget createEntriesList(boolean singlePane, int singleSideWidth, int sideWidth) {
        return ConfigEntriesListWidget.create(this, new Rectangle(singlePane ? 8 + singleSideWidth : 12 + sideWidth, 32, singlePane ? width - 16 - singleSideWidth : width - 20 - sideWidth, height - 32 - 32), activeCategory.getGroups());
    }
    
    public Map<String, ?> getDefaultOptions() {
        return defaultOptions;
    }
    
    public Map<String, ?> getOptions() {
        return options;
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            widget.render(graphics, mouseX, mouseY, delta);
        }
        ScreenOverlayImpl.getInstance().lateRender(graphics, mouseX, mouseY, delta);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return (List<? extends GuiEventListener>) (List<?>) this.widgets;
    }
    
    @Override
    public boolean charTyped(char character, int modifiers) {
        if (menu != null && menu.charTyped(character, modifiers))
            return true;
        for (GuiEventListener listener : children())
            if (listener.charTyped(character, modifiers))
                return true;
        return super.charTyped(character, modifiers);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (menu != null && menu.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
            return true;
        for (GuiEventListener entry : children())
            if (entry.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
                return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu != null) {
            if (!menu.mouseClicked(mouseX, mouseY, button))
                closeMenu();
            return true;
        }
        
        if (this.focusedKeycodeOption != null && this.partialKeycode != null) {
            if (this.partialKeycode.isUnknown()) {
                this.partialKeycode.setKeyCode(InputConstants.Type.MOUSE.getOrCreate(button));
            } else if (this.partialKeycode.getType() == InputConstants.Type.KEYSYM) {
                Modifier modifier = this.partialKeycode.getModifier();
                int code = this.partialKeycode.getKeyCode().getValue();
                if (Minecraft.ON_OSX ? code == 343 || code == 347 : code == 341 || code == 345) {
                    this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), true, modifier.hasShift()));
                    this.partialKeycode.setKeyCode(InputConstants.Type.MOUSE.getOrCreate(button));
                    return true;
                }
                
                if (code == 344 || code == 340) {
                    this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), modifier.hasControl(), true));
                    this.partialKeycode.setKeyCode(InputConstants.Type.MOUSE.getOrCreate(button));
                    return true;
                }
                
                if (code == 342 || code == 346) {
                    this.partialKeycode.setModifier(Modifier.of(true, modifier.hasControl(), modifier.hasShift()));
                    this.partialKeycode.setKeyCode(InputConstants.Type.MOUSE.getOrCreate(button));
                    return true;
                }
            }
            
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (menu != null && menu.mouseReleased(mouseX, mouseY, button))
            return true;
        if (this.focusedKeycodeOption != null && this.partialKeycode != null && !this.partialKeycode.isUnknown()) {
            this.set(this.focusedKeycodeOption, this.partialKeycode);
            this.focusKeycode(null);
            return true;
        }
        for (GuiEventListener entry : children())
            if (entry.mouseReleased(mouseX, mouseY, button))
                return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (menu != null && menu.mouseScrolled(mouseX, mouseY, amount))
            return true;
        for (GuiEventListener listener : children())
            if (listener.mouseScrolled(mouseX, mouseY, amount))
                return true;
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.focusedKeycodeOption != null) {
            if (keyCode != 256) {
                if (this.partialKeycode.isUnknown()) {
                    this.partialKeycode.setKeyCode(InputConstants.getKey(keyCode, scanCode));
                } else {
                    Modifier modifier = this.partialKeycode.getModifier();
                    if (this.partialKeycode.getType() == InputConstants.Type.KEYSYM) {
                        int code = this.partialKeycode.getKeyCode().getValue();
                        if (Minecraft.ON_OSX ? code == 343 || code == 347 : code == 341 || code == 345) {
                            this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), true, modifier.hasShift()));
                            this.partialKeycode.setKeyCode(InputConstants.getKey(keyCode, scanCode));
                            return true;
                        }
                        
                        if (code == 344 || code == 340) {
                            this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), modifier.hasControl(), true));
                            this.partialKeycode.setKeyCode(InputConstants.getKey(keyCode, scanCode));
                            return true;
                        }
                        
                        if (code == 342 || code == 346) {
                            this.partialKeycode.setModifier(Modifier.of(true, modifier.hasControl(), modifier.hasShift()));
                            this.partialKeycode.setKeyCode(InputConstants.getKey(keyCode, scanCode));
                            return true;
                        }
                    }
                    
                    if (Minecraft.ON_OSX ? keyCode == 343 || keyCode == 347 : keyCode == 341 || keyCode == 345) {
                        this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), true, modifier.hasShift()));
                        return true;
                    }
                    
                    if (keyCode == 344 || keyCode == 340) {
                        this.partialKeycode.setModifier(Modifier.of(modifier.hasAlt(), modifier.hasControl(), true));
                        return true;
                    }
                    
                    if (keyCode == 342 || keyCode == 346) {
                        this.partialKeycode.setModifier(Modifier.of(true, modifier.hasControl(), modifier.hasShift()));
                        return true;
                    }
                }
            } else {
                this.set(this.focusedKeycodeOption, ModifierKeyCode.unknown());
                this.focusKeycode(null);
            }
            
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.focusedKeycodeOption != null && this.partialKeycode != null) {
            this.set(this.focusedKeycodeOption, this.partialKeycode);
            this.focusKeycode(null);
            return true;
        }
        
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public void openMenu(Menu menu) {
        this.menu = menu;
        this.widgets.add(menu);
    }
    
    @Override
    public void closeMenu() {
        this.widgets.remove(menu);
        this.menu = null;
    }
    
    @Override
    public <T> T get(CompositeOption<T> option) {
        return (T) getOptions().get(option.getId());
    }
    
    @Override
    public <T> void set(CompositeOption<T> option, T value) {
        ((Map<String, Object>) getOptions()).put(option.getId(), value);
    }
    
    @Override
    public <T> T getDefault(CompositeOption<T> option) {
        return (T) getDefaultOptions().get(option.getId());
    }
    
    @Override
    public void focusKeycode(CompositeOption<ModifierKeyCode> option) {
        this.focusedKeycodeOption = option;
        
        if (this.focusedKeycodeOption != null) {
            this.partialKeycode = this.get(this.focusedKeycodeOption);
            this.partialKeycode.setKeyCodeAndModifier(InputConstants.UNKNOWN, Modifier.none());
        } else {
            this.partialKeycode = null;
        }
    }
    
    @Override
    @Nullable
    public CompositeOption<ModifierKeyCode> getFocusedKeycode() {
        return this.focusedKeycodeOption;
    }
    
    public void setSearching(boolean searching) {
        this.searching = searching;
        this.init(this.minecraft, this.width, this.height);
    }
    
    public boolean isSearching() {
        return searching;
    }
}
