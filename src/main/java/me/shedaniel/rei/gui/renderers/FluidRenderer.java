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
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;

public abstract class FluidRenderer extends Renderer {
    public boolean drawTooltip = false;
    public Lazy<Sprite> sprite = new Lazy<>(() -> {
        try {
            FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(getFluid());
            if (fluidRenderHandler == null)
                return null;
            Sprite[] sprites = fluidRenderHandler.getFluidSprites(null, null, getFluid().getDefaultState());
            return sprites[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    });
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        x = x - 8;
        y = y - 6;
        Sprite f = this.sprite.get();
        if (f != null) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            GuiLighting.disable();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder bb = tess.getBufferBuilder();
            bb.begin(7, VertexFormats.POSITION_UV_COLOR);
            bb.vertex(x + 16, y, blitOffset).texture(f.getMaxU(), f.getMinV()).color(255, 255, 255, 255).next();
            bb.vertex(x, y, blitOffset).texture(f.getMinU(), f.getMinV()).color(255, 255, 255, 255).next();
            bb.vertex(x, y + 16, blitOffset).texture(f.getMinU(), f.getMaxV()).color(255, 255, 255, 255).next();
            bb.vertex(x + 16, y + 16, blitOffset).texture(f.getMaxU(), f.getMaxV()).color(255, 255, 255, 255).next();
            tess.draw();
        }
        this.blitOffset = 0;
        if (drawTooltip && mouseX >= x - 8 && mouseX <= x + 8 && mouseY >= y - 6 && mouseY <= y + 10)
            queueTooltip(getFluid(), delta);
        this.drawTooltip = false;
    }
    
    protected void queueTooltip(Fluid fluid, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(fluid)));
    }
    
    private List<String> getTooltip(Fluid fluid) {
        List<String> toolTip = Lists.newArrayList(EntryListWidget.tryGetFluidName(fluid));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromIdentifier(Registry.FLUID.getId(fluid));
            toolTip.addAll(getExtraToolTips(fluid));
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
    
    protected List<String> getExtraToolTips(Fluid stack) {
        return Collections.emptyList();
    }
    
    public abstract Fluid getFluid();
}
