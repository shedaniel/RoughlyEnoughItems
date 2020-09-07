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

package me.shedaniel.rei.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.utils.FormattingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FluidEntryStack extends AbstractEntryStack {
    private FluidStack stack;
    
    public FluidEntryStack(FluidStack stack) {
        this.stack = stack;
    }
    
    @Override
    public Optional<ResourceLocation> getIdentifier() {
        return Optional.ofNullable(Registry.FLUID.getKey(getFluid()));
    }
    
    @Override
    public Type getType() {
        return Type.FLUID;
    }
    
    @Override
    public int getAmount() {
        return stack.getAmount();
    }
    
    @Override
    public void setAmount(int amount) {
        stack.setAmount(amount);
    }
    
    private <T extends Comparable<T>> T max(T o1, T o2) {
        return o1.compareTo(o2) > 0 ? o1 : o2;
    }
    
    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    @Override
    public EntryStack copy() {
        EntryStack stack = EntryStack.create(this.stack.copy());
        for (Map.Entry<Settings<?>, Object> entry : getSettings().entrySet()) {
            stack.setting((Settings<? super Object>) entry.getKey(), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return this.stack;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreTagsAndAmount);
        if (stack.getType() != Type.FLUID)
            return false;
        return this.stack.getFluid() == stack.getFluid();
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreTags);
        if (stack.getType() != Type.FLUID)
            return false;
        return this.stack.getFluid() == stack.getFluid() && this.stack.getAmount() == stack.getAmount();
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return EntryStack.copyItemToFluids(stack).anyMatch(this::equalsIgnoreAmount);
        if (stack.getType() != Type.FLUID)
            return false;
        return this.stack.getFluid() == stack.getFluid() && Objects.equals(this.stack.getTag(), stack.getFluidStack().getTag());
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return this.stack.getFluid() == stack.getFluid() && this.stack.getAmount() == stack.getAmount() && Objects.equals(this.stack.getTag(), stack.getFluidStack().getTag());
    }
    
    @Override
    public int hashOfAll() {
        return stack.hashCode();
    }
    
    @Override
    public int hashIgnoreAmountAndTags() {
        return 31 + getFluid().hashCode();
    }
    
    @Override
    public int hashIgnoreTags() {
        int code = hashIgnoreAmountAndTags();
        code = 31 * code + stack.getAmount();
        return code;
    }
    
    @Override
    public int hashIgnoreAmount() {
        int code = hashIgnoreAmountAndTags();
        code = 31 * code;
        CompoundNBT tag = stack.getTag();
        if (tag != null)
            code = 31 * code + tag.hashCode();
        return code;
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        if (!get(Settings.TOOLTIP_ENABLED).get() || isEmpty())
            return null;
        List<ITextComponent> toolTip = Lists.newArrayList(asFormattedText());
        if (stack.getAmount() > 0) {
            String amountTooltip = get(Settings.Fluid.AMOUNT_TOOLTIP).apply(this);
            if (amountTooltip != null)
                toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(StringTextComponent::new).collect(Collectors.toList()));
        }
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
            ResourceLocation id = Registry.FLUID.getKey(stack.getFluid());
            final String modId = ClientHelper.getInstance().getModFromIdentifier(id);
            boolean alreadyHasMod = false;
            for (ITextComponent s : toolTip)
                if (FormattingUtils.stripFormatting(s.getString()).equalsIgnoreCase(modId)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(ClientHelper.getInstance().getFormattedModFromIdentifier(id));
        }
        return Tooltip.create(toolTip);
    }
    
    public static RenderType createFluid(ResourceLocation location) {
        return RenderType.create(
                "roughlyenoughitems:fluid_type",
                DefaultVertexFormats.POSITION_TEX_COLOR, 7, 256, true, false,
                RenderType.State.builder()
                        .setShadeModelState(RenderState.SMOOTH_SHADE)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .setTextureState(new RenderState.TextureState(location, false, false))
                        .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(false));
    }
    
    @Override
    public void render(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (get(Settings.RENDER).get()) {
            FluidAttributes attributes = stack.getFluid().getAttributes();
            ResourceLocation texture = attributes.getStillTexture();
            RenderMaterial blockMaterial = ForgeHooksClient.getBlockMaterial(texture);
            TextureAtlasSprite sprite = blockMaterial.sprite();
            int color = attributes.getColor(Minecraft.getInstance().level, BlockPos.ZERO);
            int a = 255;
            int r = (color >> 16 & 255);
            int g = (color >> 8 & 255);
            int b = (color & 255);
            Minecraft.getInstance().getTextureManager().bind(texture);
            IRenderTypeBuffer.Impl source = Minecraft.getInstance().renderBuffers().bufferSource();
            IVertexBuilder builder = blockMaterial.buffer(source, FluidEntryStack::createFluid);
            Matrix4f matrix = matrices.last().pose();
            builder.vertex(matrix, bounds.getMaxX(), bounds.y, getZ()).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.x, bounds.y, getZ()).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.x, bounds.getMaxY(), getZ()).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
            builder.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), getZ()).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
            source.endBatch();
        }
    }
    
    @NotNull
    @Override
    public ITextComponent asFormattedText() {
        return stack.getDisplayName();
    }
}
