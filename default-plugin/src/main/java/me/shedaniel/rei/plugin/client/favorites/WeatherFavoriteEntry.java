/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022 shedaniel
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

package me.shedaniel.rei.plugin.client.favorites;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.CompoundFavoriteRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WeatherFavoriteEntry extends FavoriteEntry {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "weather");
    public static final String TRANSLATION_KEY = "favorite.section.weather";
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final String KEY = "weather";
    @Nullable
    private final Weather weather;
    
    public WeatherFavoriteEntry(@Nullable Weather weather) {
        this.weather = weather;
    }
    
    @Override
    public boolean isInvalid() {
        return false;
    }
    
    @Override
    public Renderer getRenderer(boolean showcase) {
        if (weather == null) {
            List<Renderer> renderers = IntStream.range(0, 3).mapToObj(WeatherFavoriteEntry::getRenderer).collect(Collectors.toList());
            return new CompoundFavoriteRenderer(showcase, renderers, () -> getCurrentWeather().getId()) {
                @Override
                @Nullable
                public Tooltip getTooltip(TooltipContext context) {
                    return Tooltip.create(context.getPoint(), Component.translatable("text.rei.weather_button.tooltip.dropdown"));
                }
                
                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    return hashCode() == o.hashCode();
                }
                
                @Override
                public int hashCode() {
                    return Objects.hash(getClass(), showcase);
                }
            };
        }
        return getRenderer(weather.getId());
    }
    
    private static Weather getCurrentWeather() {
        ClientLevel world = Minecraft.getInstance().level;
        if (world.isThundering())
            return Weather.THUNDER;
        if (world.getLevelData().isRaining())
            return Weather.RAIN;
        return Weather.CLEAR;
    }
    
    private static Renderer getRenderer(int id) {
        Weather weather = Weather.byId(id);
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                int color = bounds.contains(mouseX, mouseY) ? 0xFFEEEEEE : 0xFFAAAAAA;
                if (bounds.width > 4 && bounds.height > 4) {
                    matrices.pushPose();
                    matrices.translate(bounds.getCenterX(), bounds.getCenterY(), 0);
                    matrices.scale(bounds.getWidth() / 18f, bounds.getHeight() / 18f, 1);
                    renderWeatherIcon(matrices, weather, 0, 0, color);
                    matrices.popPose();
                }
            }
            
            private void renderWeatherIcon(PoseStack matrices, Weather type, int centerX, int centerY, int color) {
                RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
                blit(matrices, centerX - 7, centerY - 7, type.getId() * 14, 14, 14, 14, 256, 256);
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(TooltipContext context) {
                return Tooltip.create(context.getPoint(), Component.translatable("text.rei.weather_button.tooltip.entry", Component.translatable(weather.getTranslateKey())));
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return hashCode() == o.hashCode();
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(getClass(), false, weather);
            }
        };
    }
    
    @Override
    public boolean doAction(int button) {
        if (button == 0) {
            if (weather != null) {
                Minecraft.getInstance().player.connection.sendCommand(StringUtils.removeStart(ConfigObject.getInstance().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)), "/"));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return false;
    }
    
    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        if (weather == null)
            return Optional.of(this::_getMenuEntries);
        return Optional.empty();
    }
    
    private Collection<FavoriteMenuEntry> _getMenuEntries() {
        return CollectionUtils.map(Weather.values(), WeatherMenuEntry::new);
    }
    
    @Override
    public long hashIgnoreAmount() {
        return weather == null ? 31290831290L : weather.ordinal();
    }
    
    @Override
    public FavoriteEntry copy() {
        return this;
    }
    
    @Override
    public ResourceLocation getType() {
        return ID;
    }
    
    @Override
    public boolean isSame(FavoriteEntry other) {
        if (!(other instanceof WeatherFavoriteEntry that)) return false;
        return Objects.equals(weather, that.weather);
    }
    
    public enum Type implements FavoriteEntryType<WeatherFavoriteEntry> {
        INSTANCE;
        
        @Override
        public DataResult<WeatherFavoriteEntry> read(CompoundTag object) {
            String stringValue = object.getString(KEY);
            Weather weather = stringValue.equals("NOT_SET") ? null : Weather.valueOf(stringValue);
            return DataResult.success(new WeatherFavoriteEntry(weather), Lifecycle.stable());
        }
        
        @Override
        public DataResult<WeatherFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error("Cannot create WeatherFavoriteEntry from empty args!");
            if (!(args[0] instanceof Weather weather))
                return DataResult.error("Creation of WeatherFavoriteEntry from args expected Weather as the first argument!");
            return DataResult.success(new WeatherFavoriteEntry(weather), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(WeatherFavoriteEntry entry, CompoundTag tag) {
            tag.putString(KEY, entry.weather == null ? "NOT_SET" : entry.weather.name());
            return tag;
        }
    }
    
    @ApiStatus.Internal
    public enum Weather {
        CLEAR(0, "text.rei.weather.clear"),
        RAIN(1, "text.rei.weather.rain"),
        THUNDER(2, "text.rei.weather.thunder");
        
        private final int id;
        private final String translateKey;
        
        Weather(int id, String translateKey) {
            this.id = id;
            this.translateKey = translateKey;
        }
        
        public static Weather byId(int id) {
            return byId(id, CLEAR);
        }
        
        public static Weather byId(int id, Weather defaultWeather) {
            for (Weather weather : values()) {
                if (weather.id == id)
                    return weather;
            }
            return defaultWeather;
        }
        
        public int getId() {
            return id;
        }
        
        public String getTranslateKey() {
            return translateKey;
        }
    }
    
    public static class WeatherMenuEntry extends FavoriteMenuEntry {
        public final String text;
        public final Weather weather;
        private int x, y, width;
        private boolean selected, containsMouse, rendering;
        private int textWidth = -69;
        
        public WeatherMenuEntry(Weather weather) {
            this.text = I18n.get(weather.getTranslateKey());
            this.weather = weather;
        }
        
        private int getTextWidth() {
            if (textWidth == -69) {
                this.textWidth = Math.max(0, font.width(text));
            }
            return this.textWidth;
        }
        
        @Override
        public int getEntryWidth() {
            return getTextWidth() + 4;
        }
        
        @Override
        public int getEntryHeight() {
            return 12;
        }
        
        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }
        
        @Override
        public void updateInformation(int xPos, int yPos, boolean selected, boolean containsMouse, boolean rendering, int width) {
            this.x = xPos;
            this.y = yPos;
            this.selected = selected;
            this.containsMouse = containsMouse;
            this.rendering = rendering;
            this.width = width;
        }
        
        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            if (selected) {
                fill(matrices, x, y, x + width, y + 12, -12237499);
            }
            if (selected && containsMouse) {
                REIRuntime.getInstance().queueTooltip(Tooltip.create(Component.translatable("text.rei.weather_button.tooltip.entry", text)));
            }
            font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
                Minecraft.getInstance().player.connection.sendCommand(StringUtils.removeStart(ConfigObject.getInstance().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)), "/"));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                closeMenu();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
