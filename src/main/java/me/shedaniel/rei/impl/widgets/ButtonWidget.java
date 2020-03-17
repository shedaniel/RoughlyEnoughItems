package me.shedaniel.rei.impl.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Button;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ButtonWidget extends Button {
    private static final Identifier BUTTON_LOCATION = new Identifier("roughlyenoughitems", "textures/gui/button.png");
    private static final Identifier BUTTON_LOCATION_DARK = new Identifier("roughlyenoughitems", "textures/gui/button_dark.png");
    @NotNull
    private Rectangle bounds;
    private boolean enabled = true;
    @NotNull
    private String text;
    @Nullable
    private Integer tint;
    @Nullable
    private Consumer<Button> onClick;
    @Nullable
    private Consumer<Button> onRender;
    private boolean focusable = false;
    private boolean focused = false;
    @Nullable
    private Function<@NotNull Button, @Nullable String> tooltipFunction;
    @Nullable
    private BiFunction<@NotNull Button, @NotNull Point, @NotNull Integer> textColorFunction;
    @Nullable
    private BiFunction<@NotNull Button, @NotNull Point, @NotNull Integer> textureIdFunction;
    
    public ButtonWidget(me.shedaniel.math.Rectangle rectangle, Text text) {
        this(rectangle, Objects.requireNonNull(text).asFormattedString());
    }
    
    public ButtonWidget(me.shedaniel.math.Rectangle rectangle, String text) {
        this.bounds = new Rectangle(Objects.requireNonNull(rectangle));
        this.text = Objects.requireNonNull(text);
    }
    
    @Override
    public final boolean isFocused() {
        return focused;
    }
    
    @Override
    public final boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public final OptionalInt getTint() {
        return OptionalInt.empty();
    }
    
    @Override
    public final void setTint(int tint) {
        this.tint = tint;
    }
    
    @Override
    public final void removeTint() {
        this.tint = null;
    }
    
    @Override
    @NotNull
    public final String getText() {
        return text;
    }
    
    @Override
    public final void setText(@NotNull String text) {
        this.text = text;
    }
    
    @Override
    public final @Nullable Consumer<Button> getOnClick() {
        return onClick;
    }
    
    @Override
    public final void setOnClick(@Nullable Consumer<Button> onClick) {
        this.onClick = onClick;
    }
    
    @Override
    public final @Nullable Consumer<Button> getOnRender() {
        return onRender;
    }
    
    @Override
    public final void setOnRender(@Nullable Consumer<Button> onRender) {
        this.onRender = onRender;
    }
    
    @Override
    public final boolean isFocusable() {
        return focusable;
    }
    
    @Override
    public final void setFocusable(boolean focusable) {
        this.focusable = focusable;
    }
    
    @Override
    public final @Nullable String getTooltip() {
        if (tooltipFunction == null)
            return null;
        return tooltipFunction.apply(this);
    }
    
    @Override
    public final void setTooltip(@Nullable Function<@NotNull Button, @Nullable String> tooltip) {
        this.tooltipFunction = tooltip;
    }
    
    @Override
    public final void setTextColor(@Nullable BiFunction<@NotNull Button, @NotNull Point, @NotNull Integer> textColorFunction) {
        this.textColorFunction = textColorFunction;
    }
    
    @Override
    public final void setTextureId(@Nullable BiFunction<@NotNull Button, @NotNull Point, @NotNull Integer> textureIdFunction) {
        this.textureIdFunction = textureIdFunction;
    }
    
    @Override
    public final int getTextColor(Point mouse) {
        if (this.textColorFunction != null) {
            Integer apply = this.textColorFunction.apply(this, mouse);
            if (apply != null)
                return apply;
        }
        if (!this.enabled) {
            return 10526880;
        } else if (isFocused(mouse.x, mouse.y)) {
            return 16777120;
        }
        return 14737632;
    }
    
    @Override
    public final @NotNull Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        if (onRender != null) {
            onRender.accept(this);
        }
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        renderBackground(x, y, width, height, this.getTextureId(new Point(mouseX, mouseY)));
        
        int color = 14737632;
        if (!this.enabled) {
            color = 10526880;
        } else if (isFocused(mouseX, mouseY)) {
            color = 16777120;
        }
        
        if (tint != null)
            fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, tint, tint);
        
        this.drawCenteredString(font, getText(), x + width / 2, y + (height - 8) / 2, color);
        
        String tooltip = getTooltip();
        if (tooltip != null)
            if (!focused && containsMouse(mouseX, mouseY))
                Tooltip.create(tooltip.split("\n")).queue();
            else if (focused)
                Tooltip.create(new Point(x + width / 2, y + height / 2), tooltip.split("\n")).queue();
    }
    
    protected boolean isFocused(int mouseX, int mouseY) {
        return containsMouse(mouseX, mouseY) || focused;
    }
    
    @Override
    public boolean changeFocus(boolean boolean_1) {
        if (!enabled || !focusable)
            return false;
        this.focused = !this.focused;
        return true;
    }
    
    @Override
    public void onClick() {
        Consumer<Button> onClick = getOnClick();
        if (onClick != null)
            onClick.accept(this);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (containsMouse(mouseX, mouseY) && isEnabled() && button == 0) {
            minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            onClick();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isEnabled() && focused) {
            if (int_1 != 257 && int_1 != 32 && int_1 != 335) {
                return false;
            } else {
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClick();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<? extends Element> children() {
        return Collections.emptyList();
    }
    
    @Override
    public final int getTextureId(Point mouse) {
        if (this.textureIdFunction != null) {
            Integer apply = this.textureIdFunction.apply(this, mouse);
            if (apply != null)
                return apply;
        }
        if (!this.isEnabled()) {
            return 0;
        } else if (containsMouse(mouse) || focused) {
            return 4; // 2 is the old blue highlight, 3 is the 1.15 outline, 4 is the 1.15 online + light hover
        }
        return 1;
    }
    
    protected void renderBackground(int x, int y, int width, int height, int textureOffset) {
        minecraft.getTextureManager().bindTexture(REIHelper.getInstance().isDarkThemeEnabled() ? BUTTON_LOCATION_DARK : BUTTON_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        //Four Corners
        blit(x, y, getZOffset(), 0, textureOffset * 80, 4, 4, 512, 256);
        blit(x + width - 4, y, getZOffset(), 252, textureOffset * 80, 4, 4, 512, 256);
        blit(x, y + height - 4, getZOffset(), 0, textureOffset * 80 + 76, 4, 4, 512, 256);
        blit(x + width - 4, y + height - 4, getZOffset(), 252, textureOffset * 80 + 76, 4, 4, 512, 256);
        
        //Sides
        blit(x + 4, y, getZOffset(), 4, textureOffset * 80, MathHelper.ceil((width - 8) / 2f), 4, 512, 256);
        blit(x + 4, y + height - 4, getZOffset(), 4, textureOffset * 80 + 76, MathHelper.ceil((width - 8) / 2f), 4, 512, 256);
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y + height - 4, getZOffset(), 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80 + 76, MathHelper.floor((width - 8) / 2f), 4, 512, 256);
        blit(x + 4 + MathHelper.ceil((width - 8) / 2f), y, getZOffset(), 252 - MathHelper.floor((width - 8) / 2f), textureOffset * 80, MathHelper.floor((width - 8) / 2f), 4, 512, 256);
        for (int i = y + 4; i < y + height - 4; i += 76) {
            blit(x, i, getZOffset(), 0, 4 + textureOffset * 80, MathHelper.ceil(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76), 512, 256);
            blit(x + MathHelper.ceil(width / 2f), i, getZOffset(), 256 - MathHelper.floor(width / 2f), 4 + textureOffset * 80, MathHelper.floor(width / 2f), MathHelper.clamp(y + height - 4 - i, 0, 76), 512, 256);
        }
    }
}
