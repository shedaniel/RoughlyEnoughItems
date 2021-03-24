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
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIHelper;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

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
        return new AbstractRenderer() {
            private Animator notSetOffset = new Animator(0);
            private Rectangle notSetScissorArea = new Rectangle();
            private long nextSwitch = -1;
            
            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                int color = bounds.contains(mouseX, mouseY) ? 0xFFEEEEEE : 0xFFAAAAAA;
                fillGradient(matrices, bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getY() + 1, color, color);
                fillGradient(matrices, bounds.getX(), bounds.getMaxY() - 1, bounds.getMaxX(), bounds.getMaxY(), color, color);
                fillGradient(matrices, bounds.getX(), bounds.getY(), bounds.getX() + 1, bounds.getMaxY(), color, color);
                fillGradient(matrices, bounds.getMaxX() - 1, bounds.getY(), bounds.getMaxX(), bounds.getMaxY(), color, color);
                if (bounds.width > 4 && bounds.height > 4) {
                    if (gameMode == null) {
                        updateAnimator(delta);
                        notSetScissorArea.setBounds(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
                        ScissorsHandler.INSTANCE.scissor(notSetScissorArea);
                        int offset = Math.round(notSetOffset.floatValue() * bounds.getHeight());
                        for (int i = 0; i <= 3; i++) {
                            GameType type = GameType.byId(i);
                            renderGameModeText(matrices, type, bounds.getCenterX(), bounds.getCenterY() + bounds.getHeight() * i - offset, color);
                        }
                        ScissorsHandler.INSTANCE.removeLastScissor();
                    } else {
                        renderGameModeText(matrices, gameMode, bounds.getCenterX(), bounds.getCenterY(), color);
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
                        notSetOffset.setTo(((int) notSetOffset.target() + 1) % 4, 500);
                    }
                } else {
                    notSetOffset.setTo((Minecraft.getInstance().gameMode.getPlayerMode().getId() + 1) % 4, 500);
                }
            }
            
            private void renderGameModeText(PoseStack matrices, GameType type, int centerX, int centerY, int color) {
                Component s = new TranslatableComponent("text.rei.short_gamemode." + type.getName());
                Font font = Minecraft.getInstance().font;
                font.draw(matrices, s, centerX - font.width(s) / 2f + 1, centerY - 3.5f, color);
            }
            
            @Override
            @Nullable
            public Tooltip getTooltip(Point mouse) {
                if (gameMode == null)
                    return Tooltip.create(mouse, new TranslatableComponent("text.rei.gamemode_button.tooltip.dropdown"));
                return Tooltip.create(mouse, new TranslatableComponent("text.rei.gamemode_button.tooltip.entry", gameMode.getDisplayName().getString()));
            }
    
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return hashCode() == o.hashCode();
            }
    
            @Override
            public int hashCode() {
                return Objects.hash(getClass(), showcase, gameMode);
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
            Minecraft.getInstance().player.chat(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", mode.name().toLowerCase(Locale.ROOT)));
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
        return CollectionUtils.map(GameType.values(), GameModeMenuEntry::new);
    }
    
    @Override
    public int hashIgnoreAmount() {
        return gameMode == null ? -1 : gameMode.ordinal();
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
        if (!(other instanceof GameModeFavoriteEntry)) return false;
        GameModeFavoriteEntry that = (GameModeFavoriteEntry) other;
        return Objects.equals(gameMode, that.gameMode);
    }
    
    public enum Type implements FavoriteEntryType<GameModeFavoriteEntry> {
        INSTANCE;
        
        @Override
        public GameModeFavoriteEntry fromJson(JsonObject object) {
            String stringValue = GsonHelper.getAsString(object, KEY);
            GameType type = stringValue.equals("NOT_SET") ? null : GameType.valueOf(stringValue);
            return new GameModeFavoriteEntry(type);
        }
        
        @Override
        public GameModeFavoriteEntry fromArgs(Object... args) {
            return new GameModeFavoriteEntry((GameType) args[0]);
        }
        
        @Override
        public JsonObject toJson(GameModeFavoriteEntry entry, JsonObject object) {
            object.addProperty(KEY, entry.gameMode == null ? "NOT_SET" : entry.gameMode.name());
            return object;
        }
    }
    
    public static class GameModeMenuEntry extends FavoriteMenuEntry {
        public final String text;
        public final GameType gameMode;
        private int x, y, width;
        private boolean selected, containsMouse, rendering;
        private int textWidth = -69;
        
        public GameModeMenuEntry(GameType gameMode) {
            this.text = gameMode.getDisplayName().getString();
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
                REIHelper.getInstance().queueTooltip(Tooltip.create(new TranslatableComponent("text.rei.gamemode_button.tooltip.entry", text)));
            }
            String s = text;
            if (disabled) {
                s = ChatFormatting.STRIKETHROUGH.toString() + s;
            }
            font.draw(matrices, s, x + 2, y + 2, selected && !disabled ? 16777215 : 8947848);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean disabled = this.minecraft.gameMode.getPlayerMode() == gameMode;
            if (!disabled && rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
                Minecraft.getInstance().player.chat(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", gameMode.name().toLowerCase(Locale.ROOT)));
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                closeMenu();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
