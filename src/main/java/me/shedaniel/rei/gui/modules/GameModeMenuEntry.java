package me.shedaniel.rei.gui.modules;

import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.gui.subsets.SubsetsMenuEntry;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameModeMenuEntry extends SubsetsMenuEntry {
    public final String text;
    public final GameMode gameMode;
    private int x, y, width;
    private boolean selected, containsMouse, rendering;
    private int textWidth = -69;
    
    public GameModeMenuEntry(GameMode gameMode) {
        this.text = gameMode.getTranslatableName().getString();
        this.gameMode = gameMode;
    }
    
    private int getTextWidth() {
        if (textWidth == -69) {
            this.textWidth = Math.max(0, font.getStringWidth(text));
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
    public List<? extends Element> children() {
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (selected) {
            fill(matrices, x, y, x + width, y + 12, -12237499);
        }
        if (selected && containsMouse) {
            REIHelper.getInstance().queueTooltip(Tooltip.create(new TranslatableText("text.rei.gamemode_button.tooltip.entry", text)));
        }
        font.draw(matrices, text, x + 2, y + 2, selected ? 16777215 : 8947848);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (rendering && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 12) {
            MinecraftClient.getInstance().player.sendChatMessage(ConfigObject.getInstance().getGamemodeCommand().replaceAll("\\{gamemode}", gameMode.name().toLowerCase(Locale.ROOT)));
            minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            ScreenHelper.getLastOverlay().removeGameModeMenu();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
