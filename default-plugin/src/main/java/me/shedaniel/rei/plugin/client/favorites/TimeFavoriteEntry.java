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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimeFavoriteEntry extends FavoriteEntry {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "time");
    public static final String TRANSLATION_KEY = "favorite.section.time";
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("roughlyenoughitems", "textures/gui/recipecontainer.png");
    public static final String KEY = "mode";
    @Nullable
    private final Time time;
    
    public enum Time {
        MORN("1000"),
        NOON("6000"),
        EVENING("12000"),
        NIGHT("18000"),
        ;
        
        String part;
        
        Time(String part) {
            this.part = part;
        }
        
        public String getPart() {
            return part;
        }
    }
    
    public TimeFavoriteEntry(@Nullable Time time) {
        this.time = time;
    }
    
    @Override
    public boolean isInvalid() {
        return false;
    }
    
    @Override
    public Renderer getRenderer(boolean showcase) {
        if (time == null) {
            List<Renderer> renderers = IntStream.range(0, 4).mapToObj(TimeFavoriteEntry::getRenderer).collect(Collectors.toList());
            return new CompoundFavoriteRenderer(showcase, renderers, () -> nextTime().ordinal()) {
                @Override
                @Nullable
                public Tooltip getTooltip(TooltipContext context) {
                    return Tooltip.create(context.getPoint(), Component.translatable("text.rei.time_button.tooltip.dropdown"));
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
        return getRenderer(time.ordinal());
    }
    
    private Time nextTime() {
        ClientLevel level = Minecraft.getInstance().level;
        long dayTime = level.getDayTime();
        if (dayTime <= 1000) {
            return Time.MORN;
        } else if (dayTime <= 6000) {
            return Time.NOON;
        } else if (dayTime <= 12000) {
            return Time.EVENING;
        } else if (dayTime <= 18000) {
            return Time.NIGHT;
        } else {
            return Time.MORN;
        }
    }
    
    private static Renderer getRenderer(int id) {
        Time time = Time.values()[id];
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                int color = bounds.contains(mouseX, mouseY) ? 0xFFEEEEEE : 0xFFAAAAAA;
                if (bounds.width > 4 && bounds.height > 4) {
                    matrices.pushPose();
                    matrices.translate(bounds.getCenterX(), bounds.getCenterY(), 0);
                    matrices.scale(bounds.getWidth() / 18f, bounds.getHeight() / 18f, 1);
                    renderTimeIcon(matrices, time, 0, 0, color);
                    matrices.popPose();
                }
            }
            
            private void renderTimeIcon(PoseStack matrices, Time time, int centerX, int centerY, int color) {
                RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
                blit(matrices, centerX - 7, centerY - 7, time.ordinal() * 14 + 42, 14, 14, 14, 256, 256);
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(TooltipContext context) {
                return Tooltip.create(context.getPoint(), Component.translatable("text.rei.time_button.tooltip.entry", Component.translatable("text.rei.time_button.name." + time.name().toLowerCase(Locale.ROOT))));
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return hashCode() == o.hashCode();
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(getClass(), false, time);
            }
        };
    }
    
    @Override
    public boolean doAction(int button) {
        if (button == 0) {
            Time time = this.time;
            if (time == null) {
                time = nextTime();
            }
            Minecraft.getInstance().player.connection.sendCommand(StringUtils.removeStart(ConfigObject.getInstance().getTimeCommand().replaceAll("\\{time}", time.getPart().toLowerCase(Locale.ROOT)), "/"));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }
    
    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        if (time == null)
            return Optional.of(this::_getMenuEntries);
        return Optional.empty();
    }
    
    private Collection<FavoriteMenuEntry> _getMenuEntries() {
        return CollectionUtils.map(Time.values(), TimeMenuEntry::new);
    }
    
    @Override
    public long hashIgnoreAmount() {
        return time == null ? 31290831290L : time.ordinal();
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
        if (!(other instanceof TimeFavoriteEntry that)) return false;
        return Objects.equals(time, that.time);
    }
    
    public enum Type implements FavoriteEntryType<TimeFavoriteEntry> {
        INSTANCE;
        
        @Override
        public DataResult<TimeFavoriteEntry> read(CompoundTag object) {
            String stringValue = object.getString(KEY);
            Time type = stringValue.equals("NOT_SET") ? null : Time.valueOf(stringValue);
            return DataResult.success(new TimeFavoriteEntry(type), Lifecycle.stable());
        }
        
        @Override
        public DataResult<TimeFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error("Cannot create GameModeFavoriteEntry from empty args!");
            if (!(args[0] instanceof Time type))
                return DataResult.error("Creation of GameModeFavoriteEntry from args expected Time as the first argument!");
            return DataResult.success(new TimeFavoriteEntry(type), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(TimeFavoriteEntry entry, CompoundTag tag) {
            tag.putString(KEY, entry.time == null ? "NOT_SET" : entry.time.name());
            return tag;
        }
    }
    
    public static class TimeMenuEntry extends FavoriteMenuEntry {
        public final String text;
        public final Time time;
        private int x, y, width;
        private boolean selected, containsMouse, rendering;
        private int textWidth = -69;
        
        public TimeMenuEntry(Time time) {
            this.text = I18n.get("text.rei.time_button.name." + time.name().toLowerCase(Locale.ROOT));
            this.time = time;
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
                REIRuntime.getInstance().queueTooltip(Tooltip.create(Component.translatable("text.rei.time_button.tooltip.entry", text)));
            }
            font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
                Minecraft.getInstance().player.connection.sendCommand(StringUtils.removeStart(ConfigObject.getInstance().getTimeCommand().replaceAll("\\{time}", time.getPart().toLowerCase(Locale.ROOT)), "/"));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                closeMenu();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
