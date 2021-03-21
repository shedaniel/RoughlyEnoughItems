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

package me.shedaniel.rei.plugin.favorites;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.config.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.favorites.FavoriteEntry;
import me.shedaniel.rei.api.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.gui.AbstractRenderer;
import me.shedaniel.rei.api.gui.Renderer;
import me.shedaniel.rei.api.gui.widgets.Tooltip;
import me.shedaniel.rei.api.util.CollectionUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class WeatherFavoriteEntry extends FavoriteEntry {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "weather");
    public static final String TRANSLATION_KEY = "favorite.section.weather";
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final String KEY = "type";
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
        return new AbstractRenderer() {
            private Animator notSetOffset = new Animator(0);
            private Rectangle notSetScissorArea = new Rectangle();
            private long nextSwitch = -1;
            
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                int color = bounds.contains(mouseX, mouseY) ? 0xFFEEEEEE : 0xFFAAAAAA;
                if (bounds.width > 4 && bounds.height > 4) {
                    if (weather == null) {
                        matrices.pushPose();
                        updateAnimator(delta);
                        notSetScissorArea.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
//                        ScissorsHandler.INSTANCE.scissor(notSetScissorArea);
                        int offset = Math.round(notSetOffset.floatValue() * bounds.getHeight());
                        for (int i = 0; i <= 2; i++) {
                            Weather type = Weather.byId(i);
                            renderWeatherIcon(matrices, type, bounds.getCenterX(), bounds.getCenterY() + bounds.getHeight() * i - offset, color);
                        }
//                        ScissorsHandler.INSTANCE.removeLastScissor();
                        matrices.popPose();
                    } else {
                        renderWeatherIcon(matrices, weather, bounds.getCenterX(), bounds.getCenterY(), color);
                    }
                }
            }
            
            private void updateAnimator(float delta) {
                notSetOffset.update(delta);
                if (showcase) {
                    if (nextSwitch == -1) {
                        nextSwitch = Util.getMillis();
                    }
                    if (Util.getMillis() - nextSwitch > 1000) {
                        nextSwitch = Util.getMillis();
                        notSetOffset.setTo(((int) notSetOffset.target() + 1) % 3, 500);
                    }
                } else {
                    notSetOffset.setTo((Minecraft.getInstance().gameMode.getPlayerMode().getId() + 1) % 3, 500);
                }
            }
            
            private void renderWeatherIcon(PoseStack matrices, Weather type, int centerX, int centerY, int color) {
                Minecraft.getInstance().getTextureManager().bind(CHEST_GUI_TEXTURE);
                Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                Matrix4f matrix = matrices.last().pose();
                float j = centerX - 6.5f;
                float i = j + 14;
                float k = centerY - 6.5f;
                float l = k + 14;
                float m = getZ();
                float f = type.getId() * 14 / 256f;
                float g = f + 14 / 256f;
                float h = 14 / 256f;
                float n = h + 14 / 256f;
                
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
                bufferBuilder.vertex(matrix, i, l, m).uv(f, n).endVertex();
                bufferBuilder.vertex(matrix, j, l, m).uv(g, n).endVertex();
                bufferBuilder.vertex(matrix, j, k, m).uv(g, h).endVertex();
                bufferBuilder.vertex(matrix, i, k, m).uv(f, h).endVertex();
                bufferBuilder.end();
                RenderSystem.enableAlphaTest();
                BufferUploader.end(bufferBuilder);
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(Point mouse) {
                if (weather == null)
                    return Tooltip.create(mouse, new TranslatableComponent("text.rei.weather_button.tooltip.dropdown"));
                return Tooltip.create(mouse, new TranslatableComponent("text.rei.weather_button.tooltip.entry", new TranslatableComponent(weather.getTranslateKey())));
            }
    
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return hashCode() == o.hashCode();
            }
    
            @Override
            public int hashCode() {
                return Objects.hash(getClass(), showcase, weather);
            }
        };
    }
    
    @Override
    public boolean doAction(int button) {
        if (button == 0) {
            if (weather != null) {
                Minecraft.getInstance().player.chat(ConfigObject.getInstance().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return false;
    }
    
    @Override
    public @NotNull Optional<Supplier<Collection<@NotNull FavoriteMenuEntry>>> getMenuEntries() {
        if (weather == null)
            return Optional.of(this::_getMenuEntries);
        return Optional.empty();
    }
    
    private Collection<FavoriteMenuEntry> _getMenuEntries() {
        return CollectionUtils.map(Weather.values(), WeatherMenuEntry::new);
    }
    
    @Override
    public int hashIgnoreAmount() {
        return weather == null ? -1 : weather.ordinal();
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
        if (!(other instanceof WeatherFavoriteEntry)) return false;
        WeatherFavoriteEntry that = (WeatherFavoriteEntry) other;
        return Objects.equals(weather, that.weather);
    }
    
    public enum Type implements FavoriteEntryType<WeatherFavoriteEntry> {
        INSTANCE;
        
        @Override
        public @NotNull WeatherFavoriteEntry fromJson(@NotNull JsonObject object) {
            String stringValue = GsonHelper.getAsString(object, KEY);
            Weather type = stringValue.equals("NOT_SET") ? null : Weather.valueOf(stringValue);
            return new WeatherFavoriteEntry(type);
        }
        
        @Override
        public @NotNull WeatherFavoriteEntry fromArgs(Object... args) {
            return new WeatherFavoriteEntry((Weather) args[0]);
        }
        
        @Override
        public @NotNull JsonObject toJson(@NotNull WeatherFavoriteEntry entry, @NotNull JsonObject object) {
            object.addProperty(KEY, entry.weather == null ? "NOT_SET" : entry.weather.name());
            return object;
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
                REIHelper.getInstance().queueTooltip(Tooltip.create(new TranslatableComponent("text.rei.weather_button.tooltip.entry", text)));
            }
            font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
                Minecraft.getInstance().player.chat(ConfigObject.getInstance().getWeatherCommand().replaceAll("\\{weather}", weather.name().toLowerCase(Locale.ROOT)));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                closeMenu();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
