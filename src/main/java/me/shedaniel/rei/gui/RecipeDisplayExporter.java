/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceImpl;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL11;

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
    }
    
    private static File getExportFilename(File directory) {
        String string = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        int i = 1;
        
        while (true) {
            File file = new File(directory, "REI_" + string + (i == 1 ? "" : "_" + i) + ".png");
            if (!file.exists()) {
                return file;
            }
            
            ++i;
        }
    }
    
    @SuppressWarnings("deprecation")
    private void exportRecipe(Rectangle rectangle, List<Widget> widgets) {
        Framebuffer framebuffer = new Framebuffer(rectangle.width * 8, rectangle.height * 8, true, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.setClearColor(0, 0, 0, 0);
        //        int color = ScreenHelper.isDarkModeEnabled() ? -13750738 : -3750202;
        //        framebuffer.setClearColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, ((color >> 24) & 0xFF) / 255f);
        framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.pushMatrix();
        //        RenderSystem.clear(16640, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.beginWrite(true);
        
        // Fresh matrices
        //        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        //        RenderSystem.ortho(0.0D, rectangle.width * 8, rectangle.height * 8, 0.0D, -1, 1);
        //        RenderSystem.scalef(1, -1,0);
        //        RenderSystem.ortho(-1, 1, 1, -1, -1, 1);
        //        RenderSystem.ortho(-1, 1, 1, -1, -1, 1);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        //        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        //        RenderSystem.rotatef(180, 1, 0, 0);
        RenderSystem.scalef(2f / rectangle.width, -2f / rectangle.height, 0);
        //        RenderSystem.scalef(1f / rectangle.width, -1f / rectangle.height, 0);
        //        RenderSystem.translatef(10,10,0);
        RenderSystem.translatef(-rectangle.x, -rectangle.y, 0);
        //        RenderSystem.translatef(rectangle.x, rectangle.y, 0);
        RenderSystem.translatef(-rectangle.width / 2f, -rectangle.height / 2f, 0);
        
        //        RenderSystem.enableAlphaTest();
        //        RenderSystem.alphaFunc(516, 0.1F);
        //        RenderSystem.enableBlend();
        //        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DstFactor.DST_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.pushLightingAttributes();
        for (Widget widget : widgets) {
            widget.render(-1, -1, minecraft.getTickDelta());
        }
        {
            ItemStack stack = new ItemStack(Items.OAK_STAIRS);
            final BakedModel model = minecraft.getItemRenderer().getHeldItemModel(stack, minecraft.world, minecraft.player);
            minecraft.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            minecraft.getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX).setFilter(false, false);
            
            RenderSystem.enableRescaleNormal();
            RenderSystem.enableAlphaTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            MatrixStack matrixStack = new MatrixStack();
            
            matrixStack.translate(rectangle.x + 8, rectangle.y + 8, 0);
            matrixStack.scale(16, -16, 1F);
            
            boolean disableGuiLight = !model.isSideLit();
            if (disableGuiLight) {
                DiffuseLighting.disableGuiDepthLighting();
            }
            
            VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            minecraft.getItemRenderer().renderItem(stack, ModelTransformation.Mode.GUI, false, matrixStack, immediate, 15728880, OverlayTexture.DEFAULT_UV, model);
            immediate.draw();
            
            RenderSystem.enableDepthTest();
            
            if (disableGuiLight) {
                DiffuseLighting.enableGuiDepthLighting();
            }
            
            RenderSystem.disableAlphaTest();
            RenderSystem.disableRescaleNormal();
        }
        //        fillGradient(0, 0, 10, 10, -1, -1);
        //        fillGradient(rectangle.x, rectangle.y, rectangle.x + 10, rectangle.y + 10, -16777216, -16777216);
        RenderSystem.depthMask(true);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        //        RenderSystem.disableAlphaTest();
        //        RenderSystem.disableBlend();
        
        // Reset matrices
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.popMatrix();
        
        framebuffer.endWrite();
        RenderSystem.popMatrix();
        RenderSystem.viewport(0, 0, minecraft.getWindow().getFramebufferWidth(), minecraft.getWindow().getFramebufferHeight());
        
        NativeImage nativeImage = new NativeImage(rectangle.width * 8, rectangle.height * 8, false);
        RenderSystem.bindTexture(framebuffer.colorAttachment);
        nativeImage.loadFromTextureImage(0, false);
        {
            int width = rectangle.width * 8;
            int height = rectangle.height * 8;
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    if (x > 24 && x < width - 24 && y > 24 && y < height - 24)
                        nativeImage.setPixelRgba(x, y, nativeImage.getPixelRgba(x, y) | 255 << NativeImage.Format.RGBA.getAlphaChannelOffset());
                }
            }
        }
        
        nativeImage.mirrorVertically();
        ResourceImpl.RESOURCE_IO_EXECUTOR.execute(() -> {
            try {
                File export = new File(minecraft.runDirectory, "export");
                //noinspection ResultOfMethodCallIgnored
                export.mkdirs();
                nativeImage.writeFile(getExportFilename(export));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                nativeImage.close();
                RenderSystem.recordRenderCall(framebuffer::delete);
            }
        });
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
    
    }
    
    @Override
    public List<? extends Element> children() {
        return null;
    }
}
