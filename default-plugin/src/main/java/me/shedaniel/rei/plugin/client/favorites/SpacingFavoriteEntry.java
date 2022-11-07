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
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.region.RegionEntry;
import me.shedaniel.rei.api.client.favorites.CompoundFavoriteRenderer;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType;
import me.shedaniel.rei.api.client.favorites.FavoriteMenuEntry;
import me.shedaniel.rei.api.client.gui.AbstractRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.util.CollectionUtils;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpacingFavoriteEntry extends FavoriteEntry {
    private static int uniqueKey;
    public static final ResourceLocation ID = new ResourceLocation("roughlyenoughitems", "spacing");
    public static final String TRANSLATION_KEY = "favorite.section.spacing";
    public static final String SPACE_KEY = "space";
    public static final String KEY_KEY = "key";
    @Nullable
    private final SpacingFavoriteEntry.Spacing spacing;
    private final int key;

    public SpacingFavoriteEntry(@Nullable SpacingFavoriteEntry.Spacing spacing) {
        this.spacing = spacing;
        synchronized (SpacingFavoriteEntry.class) {
            key = ++uniqueKey;
        }
    }

    private SpacingFavoriteEntry(@Nullable SpacingFavoriteEntry.Spacing spacing, int key) {
        this.key = key;
        this.spacing = spacing;
        synchronized (SpacingFavoriteEntry.class) {
            uniqueKey = Math.max(uniqueKey, key);
        }
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public Renderer getRenderer(boolean showcase) {
        if (spacing == null) {
            List<Renderer> renderers = IntStream.range(0, 2).mapToObj(this::getRenderer).collect(Collectors.toList());

            return new CompoundFavoriteRenderer(showcase, renderers, () -> 0) {
                @Override
                @Nullable
                public Tooltip getTooltip(Point mouse) {
                    return Tooltip.create(mouse, new TranslatableComponent("text.rei.weather_button.tooltip.dropdown"));
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
        return getRenderer(spacing.getId());
    }

    private Renderer getRenderer(int id) {
        Spacing spacing = Spacing.values()[id];

        return new AbstractRenderer() {

            @Override
            public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
                if (bounds.contains(mouseX, mouseY)) {
                    fill(matrices, bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY(), -12237499);
                }
            }

            @Override
            @Nullable
            public Tooltip getTooltip(Point mouse) {
                return Tooltip.create(mouse, new TranslatableComponent("text.rei.spacing_button.tooltip.entry", new TranslatableComponent(spacing.getTranslateKey())));
            }

            @Override
            public boolean causeNewLine() {
                return spacing == Spacing.LINE;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                return false;
            }

            @Override
            public int hashCode() {
                return Objects.hash(getClass(), false, spacing, key);
            }
        };
    }

    @Override
    public boolean doAction(int button) {
        return true;
    }

    @Override
    public boolean shouldDeepCopy() {
        return true;
    }

    @Override
    public RegionEntry<FavoriteEntry> deepCopy() {
        return new SpacingFavoriteEntry(spacing);
    }

    @Override
    public long hashIgnoreAmount() {
        return key;
    }

    @Override
    public Optional<Supplier<Collection<FavoriteMenuEntry>>> getMenuEntries() {
        if (spacing == null)
            return Optional.of(this::_getMenuEntries);
        return Optional.empty();
    }

    private Collection<FavoriteMenuEntry> _getMenuEntries() {
        return CollectionUtils.map(Spacing.values(), SpacingFavoriteEntry.SpacingMenuEntry::new);
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
        if (!(other instanceof SpacingFavoriteEntry that)) return false;
        return Objects.equals(spacing, that.spacing);
    }

    public enum Spacing {
        ITEM(0, "text.rei.spacing.item"),
        LINE(1, "text.rei.spacing.line");


        private final int id;
        private final String translateKey;

        Spacing(int id, String translateKey) {
            this.id = id;
            this.translateKey = translateKey;
        }

        public static Spacing byId(int id) {
            return byId(id, ITEM);
        }

        public static Spacing byId(int id, Spacing defaultWeather) {
            for (Spacing spacing : values()) {
                if (spacing.id == id)
                    return spacing;
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

    public enum Type implements FavoriteEntryType<SpacingFavoriteEntry> {
        INSTANCE;

        @Override
        public DataResult<SpacingFavoriteEntry> read(CompoundTag object) {
            String stringValue = object.getString(SPACE_KEY);
            Spacing type = stringValue.equals("NOT_SET") ? null : Spacing.valueOf(stringValue);
            if (!object.contains(KEY_KEY))
                return DataResult.success(new SpacingFavoriteEntry(type), Lifecycle.stable());
            int key = object.getInt(KEY_KEY);
            return DataResult.success(new SpacingFavoriteEntry(type, key), Lifecycle.stable());
        }

        @Override
        public DataResult<SpacingFavoriteEntry> fromArgs(Object... args) {
            if (args.length == 0) return DataResult.error("Cannot create SpacingFavoriteEntry from empty args!");
            if (!(args[0] instanceof Spacing type))
                return DataResult.error("Creation of SpacingFavoriteEntry from args expected Spacing as the first argument!");
            if (args.length == 1)
                return DataResult.success(new SpacingFavoriteEntry(type), Lifecycle.stable());
            if (!(args[1] instanceof Integer key))
                return DataResult.error("Creation of SpacingFavoriteEntry from args expected Integer as the second argument!");
            return DataResult.success(new SpacingFavoriteEntry(type, key), Lifecycle.stable());
        }

        @Override
        public CompoundTag save(SpacingFavoriteEntry entry, CompoundTag tag) {
            tag.putString(SPACE_KEY, entry.spacing == null ? "NOT_SET" : entry.spacing.name());
            tag.putInt(KEY_KEY, entry.key);
            return tag;
        }
    }

    public static class SpacingMenuEntry extends FavoriteMenuEntry {
        public final String text;
        public final Spacing spacing;
        private int x, y, width;
        private boolean selected, containsMouse, rendering;
        private int textWidth = -69;

        public SpacingMenuEntry(Spacing spacing) {
            this.text = I18n.get(spacing.getTranslateKey());
            this.spacing = spacing;
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
            if (rendering) {
                if (selected) {
                    fill(matrices, x, y, x + width, y + 12, -12237499);
                }
                if (selected && containsMouse) {
                    REIRuntime.getInstance().queueTooltip(Tooltip.create(new TranslatableComponent("text.rei.spacing_button.tooltip.entry", text)));
                }
                font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

    }
}
