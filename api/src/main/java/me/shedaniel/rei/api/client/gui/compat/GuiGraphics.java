package me.shedaniel.rei.api.client.gui.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Compatibility shim for code that still targets the pre-26.1 GuiGraphics type.
 */
public class GuiGraphics extends GuiGraphicsExtractor {
    public GuiGraphics(Minecraft minecraft, GuiRenderState renderState, int guiWidth, int guiHeight) {
        super(minecraft, renderState, guiWidth, guiHeight);
    }

    public GuiGraphics(GuiGraphicsExtractor extractor) {
        this(extractor.minecraft, extractor.guiRenderState, extractor.mouseX, extractor.mouseY);
    }

    /**
     * Returns {@code graphics} as a {@link GuiGraphics} if it already is one, otherwise wraps it in a
     * new compatibility instance. Used at Minecraft boundaries that hand us a {@link GuiGraphicsExtractor}.
     */
    public static GuiGraphics of(GuiGraphicsExtractor graphics) {
        return graphics instanceof GuiGraphics existing ? existing : new GuiGraphics(graphics);
    }

    public void drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        text(font, text, x, y, color);
    }

    public void drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        text(font, text, x, y, color, shadow);
    }

    public void drawString(Font font, Component text, int x, int y, int color) {
        text(font, text, x, y, color);
    }

    public void drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        text(font, text, x, y, color, shadow);
    }

    public void drawString(Font font, String text, int x, int y, int color) {
        text(font, text, x, y, color);
    }

    public void drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        text(font, text, x, y, color, shadow);
    }

    public void drawCenteredString(Font font, String text, int x, int y, int color) {
        centeredText(font, text, x, y, color);
    }

    public void drawCenteredString(Font font, Component text, int x, int y, int color) {
        centeredText(font, text, x, y, color);
    }

    public void drawWordWrap(Font font, Component text, int x, int y, int width, int color) {
        textWithWordWrap(font, text, x, y, width, color);
    }

    public void renderDeferredElements() {
        extractDeferredElements(this.mouseX, this.mouseY, 0.0F);
    }

    public void renderOutline(int x, int y, int width, int height, int color) {
        outline(x, y, width, height, color);
    }

    public void hLine(int minX, int maxX, int y, int color) {
        horizontalLine(minX, maxX, y, color);
    }

    public void vLine(int x, int minY, int maxY, int color) {
        verticalLine(x, minY, maxY, color);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        item(stack, x, y);
    }

    public void renderItem(ItemStack stack, int x, int y, int seed) {
        item(stack, x, y, seed);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y) {
        itemDecorations(font, stack, x, y);
    }

    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text) {
        itemDecorations(font, stack, x, y, text);
    }

    public void withFreshScissorStack(Runnable runnable) {
        GuiGraphicsExtractor.ScissorStack previous = this.scissorStack;
        try {
            this.scissorStack = new GuiGraphicsExtractor.ScissorStack();
            runnable.run();
        } finally {
            this.scissorStack = previous;
        }
    }
}
