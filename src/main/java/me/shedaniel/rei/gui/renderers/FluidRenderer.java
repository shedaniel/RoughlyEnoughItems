/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.widget.EntryListWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.impl.ScreenHelper;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Lazy;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public abstract class FluidRenderer extends Renderer {
    /**
     * @deprecated This boolean is no longer used
     */
    @Deprecated
    public boolean drawTooltip = false;
    public Lazy<Pair<Sprite, Integer>> sprite = new Lazy<>(() -> {
        try {
            FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(getFluid());
            if (fluidRenderHandler == null)
                return null;
            Sprite[] sprites = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().world == null ? null : BlockPos.ORIGIN, getFluid().getDefaultState());
            int color = -1;
            if (MinecraftClient.getInstance().world != null)
                color = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, BlockPos.ORIGIN, getFluid().getDefaultState());
            return new Pair<>(sprites[0], color);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    });
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        x = x - 8;
        y = y - 6;
        Pair<Sprite, Integer> pair = this.sprite.get();
        if (pair != null) {
            Sprite sprite = pair.getLeft();
            Integer int_5 = pair.getRight();
            int a = 255;
            int r = (int_5 >> 16 & 255);
            int g = (int_5 >> 8 & 255);
            int b = (int_5 & 255);
            MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            GuiLighting.disable();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder bb = tess.getBufferBuilder();
            bb.begin(7, VertexFormats.POSITION_UV_COLOR);
            bb.method_22912(x + 16, y, getBlitOffset()).method_22913(sprite.getMaxU(), sprite.getMinV()).color(r, g, b, a).next();
            bb.method_22912(x, y, getBlitOffset()).method_22913(sprite.getMinU(), sprite.getMinV()).color(r, g, b, a).next();
            bb.method_22912(x, y + 16, getBlitOffset()).method_22913(sprite.getMinU(), sprite.getMaxV()).color(r, g, b, a).next();
            bb.method_22912(x + 16, y + 16, getBlitOffset()).method_22913(sprite.getMaxU(), sprite.getMaxV()).color(r, g, b, a).next();
            tess.draw();
        }
        setBlitOffset(0);
    }
    
    @Nullable
    @Override
    public QueuedTooltip getQueuedTooltip(float delta) {
        return QueuedTooltip.create(getTooltip(getFluid()));
    }
    
    /**
     * Queue a tooltip to the REI overlay
     *
     * @param fluid the fluid to queue
     * @param delta the delta
     * @deprecated Use {@link Renderer#getQueuedTooltip(float)} instead and queue manually
     */
    @Deprecated
    protected void queueTooltip(Fluid fluid, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(getQueuedTooltip(delta));
    }
    
    protected List<String> getTooltip(Fluid fluid) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetFluidName(fluid));
        toolTip.addAll(getExtraToolTips(fluid));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromIdentifier(Registry.FLUID.getId(fluid));
            boolean alreadyHasMod = false;
            for (String s : toolTip)
                if (s.equalsIgnoreCase(modString)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(modString);
        }
        return toolTip;
    }
    
    protected List<String> getExtraToolTips(Fluid fluid) {
        return Collections.emptyList();
    }
    
    public abstract Fluid getFluid();
}
