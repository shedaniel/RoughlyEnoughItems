/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
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

package me.shedaniel.rei.impl.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.impl.client.gui.toast.ExportRecipeIdentifierToast;
import me.shedaniel.rei.impl.display.DisplaySpec;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ApiStatus.Internal
public final class RecipeDisplayExporter {
    private RecipeDisplayExporter() {}
    
    public static void exportRecipeDisplay(Rectangle rectangle, DisplaySpec display, List<Widget> widgets, boolean toast) {
        exportRecipe(rectangle, display, widgets);
        if (toast) {
            ExportRecipeIdentifierToast.addToast(I18n.get("msg.rei.exported_recipe"), I18n.get("msg.rei.exported_recipe.desc"));
        }
    }
    
    private static File getExportFilename(DisplaySpec display, File directory) {
        Collection<ResourceLocation> locations = display.provideInternalDisplayIds();
        String string = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        if (!locations.isEmpty()) {
            string = locations.iterator().next().toString().replace('/', '_').replace(':', '_');
        }
        int i = 1;
        
        while (true) {
            File file = new File(directory, string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }
            
            ++i;
        }
    }
    
    private static void exportRecipe(Rectangle rectangle, DisplaySpec display, List<Widget> widgets) {
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        RenderTarget renderTarget = new TextureTarget(window.getWidth(), window.getHeight(), true, false);
        renderTarget.bindWrite(true);
        RenderSystem.clear(256, Minecraft.ON_OSX);
        Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()), 0.0F, (float) ((double) window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        PoseStack matrices = new PoseStack();
        for (Widget widget : widgets) {
            widget.render(matrices, -1, -1, 0);
        }
        
        NativeImage nativeImage = new NativeImage(renderTarget.width, renderTarget.height, false);
        RenderSystem.bindTexture(renderTarget.getColorTextureId());
        nativeImage.downloadTexture(0, false);
        nativeImage.flipY();
        int outWidth = (int) (rectangle.width * window.getGuiScale());
        int outHeight = (int) (rectangle.height * window.getGuiScale());
        NativeImage strippedImage = new NativeImage(outWidth, outHeight, false);
        for (int y = 0; y < outHeight; ++y) {
            for (int x = 0; x < outWidth; ++x) {
                strippedImage.setPixelRGBA(x, y, nativeImage.getPixelRGBA(x + (int) (rectangle.x * window.getGuiScale()), y + (int) (rectangle.y * window.getGuiScale())));
            }
        }
        Util.ioPool().execute(() -> {
            try {
                File export = new File(Minecraft.getInstance().gameDirectory, "rei_exports/" + display.provideInternalDisplay().getCategoryIdentifier().toString().replace('/', '_').replace(':', '_'));
                export.mkdirs();
                strippedImage.writeToFile(getExportFilename(display, export));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                nativeImage.close();
                strippedImage.close();
            }
        });
        
        renderTarget.destroyBuffers();
        Minecraft.getInstance().levelRenderer.graphicsChanged();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
