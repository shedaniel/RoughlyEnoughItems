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

package me.shedaniel.rei.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.toast.ExportRecipeIdentifierToast;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@ApiStatus.Internal
@ApiStatus.Experimental
public final class RecipeDisplayExporter extends Widget {
    private static final RecipeDisplayExporter INSTANCE = new RecipeDisplayExporter();
    
    private RecipeDisplayExporter() {}
    
    public static void exportRecipeDisplay(Rectangle rectangle, List<Widget> widgets) {
        INSTANCE.exportRecipe(rectangle, widgets);
        ExportRecipeIdentifierToast.addToast(I18n.translate("msg.rei.exported_recipe"), I18n.translate("msg.rei.exported_recipe.desc"));
    }
    
    private static File getExportFilename(File directory) {
        String string = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        int i = 1;
        
        while (true) {
            File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }
            
            ++i;
        }
    }
    
    @SuppressWarnings("deprecation")
    private void exportRecipe(Rectangle rectangle, List<Widget> widgets) {
        RenderSystem.pushMatrix();
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        Framebuffer framebuffer = new Framebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true, false);
        framebuffer.beginWrite(true);
        RenderSystem.viewport(0, 0, window.getFramebufferWidth(), window.getFramebufferHeight());
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, (double) window.getFramebufferWidth() / window.getScaleFactor(), (double) window.getFramebufferHeight() / window.getScaleFactor(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        DiffuseLighting.enableGuiDepthLighting();
        MatrixStack matrices = new MatrixStack();
        for (Widget widget : widgets) {
            widget.render(matrices, -1, -1, 0);
        }
        RenderSystem.popMatrix();
        
        NativeImage nativeImage = new NativeImage(framebuffer.textureWidth, framebuffer.textureHeight, false);
        RenderSystem.bindTexture(framebuffer.colorAttachment);
        nativeImage.loadFromTextureImage(0, false);
        nativeImage.mirrorVertically();
        int outWidth = (int) (rectangle.width * window.getScaleFactor());
        int outHeight = (int) (rectangle.height * window.getScaleFactor());
        NativeImage strippedImage = new NativeImage(outWidth, outHeight, false);
        for (int y = 0; y < outHeight; ++y) {
            for (int x = 0; x < outWidth; ++x) {
                strippedImage.setPixelRgba(x, y, nativeImage.getPixelRgba(x + (int) (rectangle.x * window.getScaleFactor()), y + (int) (rectangle.y * window.getScaleFactor())));
            }
        }
        Util.method_27958().execute(() -> {
            try {
                File export = new File(minecraft.runDirectory, "rei_exports");
                export.mkdirs();
                strippedImage.writeFile(getExportFilename(export));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                nativeImage.close();
                strippedImage.close();
                RenderSystem.recordRenderCall(framebuffer::delete);
            }
        });
    }
    
    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        
    }
    
    @Override
    public List<? extends Element> children() {
        return null;
    }
}
