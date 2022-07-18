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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameModeFavoriteEntry extends FavoriteEntry {
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "gamemode");
    public static final String TRANSLATION_KEY = "favorite.section.gamemode";
    public static final String KEY = "mode";
    @Nullable
    private final GameType gameMode;
    
    public GameModeFavoriteEntry(@Nullable GameType gameMode) {
        this.gameMode = gameMode;
    }
    
    @Override
    public boolean isInvalid() {
        return false;
    }
    
    @Override
    public Renderer getRenderer(boolean showcase) {
        if (gameMode == null) {
            List<Renderer> renderers = IntStream.range(0, 4).mapToObj(GameModeFavoriteEntry::getRenderer).collect(Collectors.toList());
            return new CompoundFavoriteRenderer(showcase, renderers, () -> Minecraft.getInstance().gameMode.getPlayerMode().getId()) {
                @Override
                @Nullable
                public Tooltip getTooltip(TooltipContext context) {
                    return Tooltip.create(context.getPoint(), Component.translatable("text.rei.gamemode_button.tooltip.dropdown"));
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
        return getRenderer(gameMode.getId());
    }
    
    private static Renderer getRenderer(int id) {
        GameType type = GameType.byId(id);
        return new AbstractRenderer() {
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                int color = bounds.contains(mouseX, mouseY) ? 0xFFEEEEEE : 0xFFAAAAAA;
                if (bounds.width > 4 && bounds.height > 4) {
                    matrices.pushPose();
                    matrices.translate(bounds.getCenterX(), bounds.getCenterY(), 0);
                    matrices.scale(bounds.getWidth() / 18f, bounds.getHeight() / 18f, 1);
                    renderGameModeText(matrices, type, 0, 0, color);
                    matrices.popPose();
                }
            }
            
            private void renderGameModeText(PoseStack matrices, GameType type, int centerX, int centerY, int color) {
                Component s = Component.translatable("text.rei.short_gamemode." + type.getName());
                Font font = Minecraft.getInstance().font;
                font.draw(matrices, s, centerX - font.width(s) / 2f + 0.5f, centerY - 3.5f, color);
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(TooltipContext context) {
                return Tooltip.create(context.getPoint(), Component.translatable("text.rei.gamemode_button.tooltip.entry", type.getLongDisplayName().getString()));
            }
            
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return hashCode() == o.hashCode();
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(getClass(), false, type);
            }
        };
    }
    
    @Override
    public boolean doAction(int button) {
        if (button == 0) {
            GameType mode = gameMode;
            if (mode == null) {
                mode = GameType.byId(Minecraft.getInstance().gameMode.getPlayerMode().getId() + 1 % 4);
            }
            Minecraft.getInstance().player.command(StringUtils.removeStart(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", mode.name().toLowerCase(Locale.ROOT)), "/"));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }
    
    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        if (gameMode == null)
            return Optional.of(this::_getMenuEntries);
        return Optional.empty();
    }
    
    private Collection<FavoriteMenuEntry> _getMenuEntries() {
        return CollectionUtils.filterAndMap(Arrays.asList(GameType.values()), type -> type.getId() >= 0, GameModeMenuEntry::new);
    }
    
    @Override
    public long hashIgnoreAmount() {
        return gameMode == null ? 31290831290L : gameMode.ordinal();
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
        if (!(other instanceof GameModeFavoriteEntry that)) return false;
        return Objects.equals(gameMode, that.gameMode);
    }
    
    public enum Type implements FavoriteEntryType<GameModeFavoriteEntry> {
        INSTANCE;
        
        @Override
        public DataResult<GameModeFavoriteEntry> read(CompoundTag object) {
            String stringValue = object.getString(KEY);
            GameType type = stringValue.equals("NOT_SET") ? null : GameType.valueOf(stringValue);
            return DataResult.success(new GameModeFavoriteEntry(type), Lifecycle.stable());
        }
        
        @Override
        public DataResult<GameModeFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error("Cannot create GameModeFavoriteEntry from empty args!");
            if (!(args[0] instanceof GameType type))
                return DataResult.error("Creation of GameModeFavoriteEntry from args expected GameType as the first argument!");
            return DataResult.success(new GameModeFavoriteEntry(type), Lifecycle.stable());
        }
        
        @Override
        public CompoundTag save(GameModeFavoriteEntry entry, CompoundTag tag) {
            tag.putString(KEY, entry.gameMode == null ? "NOT_SET" : entry.gameMode.name());
            return tag;
        }
    }
    
    public static class GameModeMenuEntry extends FavoriteMenuEntry {
        public final String text;
        public final GameType gameMode;
        private int x, y, width;
        private boolean selected, containsMouse, rendering;
        private int textWidth = -69;
        
        public GameModeMenuEntry(GameType gameMode) {
            this.text = gameMode.getLongDisplayName().getString();
            this.gameMode = gameMode;
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
            boolean disabled = this.minecraft.gameMode.getPlayerMode() == gameMode;
            if (selected && !disabled) {
                fill(matrices, x, y, x + width, y + 12, -12237499);
            }
            if (!disabled && selected && containsMouse) {
                REIRuntime.getInstance().queueTooltip(Tooltip.create(Component.translatable("text.rei.gamemode_button.tooltip.entry", text)));
            }
            String s = text;
            if (disabled) {
                s = ChatFormatting.STRIKETHROUGH + s;
            }
            font.draw(matrices, s, x + 2, y + 2, selected && !disabled ? 16777215 : 8947848);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean disabled = this.minecraft.gameMode.getPlayerMode() == gameMode;
            if (!disabled && rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
                Minecraft.getInstance().player.command(StringUtils.removeStart(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", gameMode.name().toLowerCase(Locale.ROOT)), "/"));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                closeMenu();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
