/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.*;

@Deprecated
public class FluidEntryStack extends AbstractEntryStack {
    private static final Map<Fluid, Pair<Sprite, Integer>> FLUID_SPRITE_CACHE = new HashMap<>();
    private static final int EMPTY_AMOUNT = -1319182373;
    private Fluid fluid;
    private int amount;
    
    public FluidEntryStack(Fluid fluid) {
        this(fluid, EMPTY_AMOUNT);
    }
    
    public FluidEntryStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }
    
    protected static Pair<Sprite, Integer> getOrLoadSprite(Fluid fluid) {
        Pair<Sprite, Integer> possibleCached = FLUID_SPRITE_CACHE.get(fluid);
        if (possibleCached != null)
            return possibleCached;
        
        FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (fluidRenderHandler == null)
            return null;
        Sprite[] sprites = fluidRenderHandler.getFluidSprites(MinecraftClient.getInstance().world, MinecraftClient.getInstance().world == null ? null : BlockPos.ORIGIN, fluid.getDefaultState());
        int color = -1;
        if (MinecraftClient.getInstance().world != null)
            color = fluidRenderHandler.getFluidColor(MinecraftClient.getInstance().world, BlockPos.ORIGIN, fluid.getDefaultState());
        Pair<Sprite, Integer> pair = new Pair<>(sprites[0], color);
        FLUID_SPRITE_CACHE.put(fluid, pair);
        return pair;
    }
    
    @Override
    public Optional<Identifier> getIdentifier() {
        return Optional.ofNullable(Registry.FLUID.getId(getFluid()));
    }
    
    @Override
    public Type getType() {
        return Type.FLUID;
    }
    
    @Override
    public int getAmount() {
        return amount;
    }
    
    @Override
    public void setAmount(int amount) {
        this.amount = amount == EMPTY_AMOUNT ? EMPTY_AMOUNT : Math.max(amount, 0);
        if (isEmpty()) {
            fluid = Fluids.EMPTY;
        }
    }
    
    @Override
    public boolean isEmpty() {
        return (amount != EMPTY_AMOUNT && amount <= 0) || fluid == Fluids.EMPTY;
    }
    
    @Override
    public EntryStack copy() {
        EntryStack stack = EntryStack.create(fluid, amount);
        for (Map.Entry<Settings, Object> entry : getSettings().entrySet()) {
            stack.setting(entry.getKey(), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return fluid;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && amount == stack.getAmount();
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && amount == stack.getAmount();
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + getType().ordinal();
        result = 31 * result + fluid.hashCode();
        result = 31 * result + amount;
        result = 31 * result;
        return result;
    }
    
    @Nullable
    @Override
    public QueuedTooltip getTooltip(int mouseX, int mouseY) {
        if (!get(Settings.TOOLTIP_ENABLED).get() || isEmpty())
            return null;
        List<String> toolTip = Lists.newArrayList(SearchArgument.tryGetEntryStackName(this));
        if (amount >= 0) {
            String amountTooltip = get(Settings.Fluid.AMOUNT_TOOLTIP).apply(this);
            if (amountTooltip != null)
                toolTip.addAll(Arrays.asList(amountTooltip.split("\n")));
        }
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
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
        return QueuedTooltip.create(toolTip);
    }
    
    @Override
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (get(Settings.RENDER).get()) {
            Pair<Sprite, Integer> pair = getOrLoadSprite(getFluid());
            if (pair != null) {
                Sprite sprite = pair.getLeft();
                int color = pair.getRight();
                int a = 255;
                int r = (color >> 16 & 255);
                int g = (color >> 8 & 255);
                int b = (color & 255);
                MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder bb = tess.getBuffer();
                bb.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                bb.vertex(bounds.getMaxX(), bounds.y, getZ()).texture(sprite.getMaxU(), sprite.getMinV()).color(r, g, b, a).next();
                bb.vertex(bounds.x, bounds.y, getZ()).texture(sprite.getMinU(), sprite.getMinV()).color(r, g, b, a).next();
                bb.vertex(bounds.x, bounds.getMaxY(), getZ()).texture(sprite.getMinU(), sprite.getMaxV()).color(r, g, b, a).next();
                bb.vertex(bounds.getMaxX(), bounds.getMaxY(), getZ()).texture(sprite.getMaxU(), sprite.getMaxV()).color(r, g, b, a).next();
                tess.draw();
            }
        }
    }
}
