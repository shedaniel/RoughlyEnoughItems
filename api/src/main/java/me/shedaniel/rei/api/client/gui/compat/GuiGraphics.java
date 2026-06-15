package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compatibility shim for code that still targets the pre-26.1 GuiGraphics type.
 */
public class GuiGraphics extends GuiGraphicsExtractor {
    private static final Field MINECRAFT_FIELD = findField("minecraft");
    private static final Field GUI_RENDER_STATE_FIELD = findField("guiRenderState");
    private static final Field MOUSE_X_FIELD = findField("mouseX");
    private static final Field MOUSE_Y_FIELD = findField("mouseY");
    private static final Field SCISSOR_STACK_FIELD = findField("scissorStack");
    private static final Method INNER_TEXTURED_BLIT_METHOD = findMethod("innerBlit",
            RenderPipeline.class, GpuTextureView.class, GpuSampler.class,
            int.class, int.class, int.class, int.class,
            float.class, float.class, float.class, float.class, int.class);

    public GuiGraphics(Minecraft minecraft, GuiRenderState renderState, int guiWidth, int guiHeight) {
        super(minecraft, renderState, guiWidth, guiHeight);
    }

    public GuiGraphics(GuiGraphicsExtractor extractor) {
        this(readField(MINECRAFT_FIELD, extractor), readField(GUI_RENDER_STATE_FIELD, extractor),
                readIntField(MOUSE_X_FIELD, extractor), readIntField(MOUSE_Y_FIELD, extractor));
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
        extractDeferredElements(readIntField(MOUSE_X_FIELD, this), readIntField(MOUSE_Y_FIELD, this), 0.0F);
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

    public void innerBlit(RenderPipeline pipeline, Identifier location, int xStart, int xEnd, int yStart, int yEnd, float u0, float u1, float v0, float v1, int color) {
        Minecraft minecraft = readField(MINECRAFT_FIELD, this);
        AbstractTexture texture = minecraft.getTextureManager().getTexture(location);
        invoke(INNER_TEXTURED_BLIT_METHOD, this, pipeline, texture.getTextureView(), texture.getSampler(),
                xStart, yStart, xEnd, yEnd, u0, u1, v0, v1, color);
    }

    public void withFreshScissorStack(Runnable runnable) {
        Object previous = readField(SCISSOR_STACK_FIELD, this);
        try {
            Constructor<?> constructor = SCISSOR_STACK_FIELD.getType().getDeclaredConstructor();
            constructor.setAccessible(true);
            SCISSOR_STACK_FIELD.set(this, constructor.newInstance());
            runnable.run();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to swap GuiGraphicsExtractor scissor stack", e);
        } finally {
            try {
                SCISSOR_STACK_FIELD.set(this, previous);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to restore GuiGraphicsExtractor scissor stack", e);
            }
        }
    }

    private static Field findField(String name) {
        try {
            Field field = GuiGraphicsExtractor.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve GuiGraphicsExtractor field: " + name, e);
        }
    }

    private static Method findMethod(String name, Class<?>... parameterTypes) {
        try {
            Method method = GuiGraphicsExtractor.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve GuiGraphicsExtractor method: " + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T readField(Field field, Object instance) {
        try {
            return (T) field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read GuiGraphicsExtractor field: " + field.getName(), e);
        }
    }

    private static int readIntField(Field field, Object instance) {
        try {
            return field.getInt(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read GuiGraphicsExtractor field: " + field.getName(), e);
        }
    }

    private static void invoke(Method method, Object instance, Object... args) {
        try {
            method.invoke(instance, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke GuiGraphicsExtractor method: " + method.getName(), e);
        }
    }
}
