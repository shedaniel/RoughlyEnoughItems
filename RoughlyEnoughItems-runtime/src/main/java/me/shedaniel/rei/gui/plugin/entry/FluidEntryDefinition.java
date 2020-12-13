package me.shedaniel.rei.gui.plugin.entry;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import me.shedaniel.architectury.fluid.FluidStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.entry.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.impl.SimpleFluidRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FluidEntryDefinition implements EntryDefinition<FluidStack> {
    private final EntryRenderer<FluidStack> renderer = new FluidEntryRenderer();
    
    @Override
    public @NotNull Class<FluidStack> getValueType() {
        return FluidStack.class;
    }
    
    @Override
    public @NotNull EntryType<FluidStack> getType() {
        return VanillaEntryTypes.FLUID;
    }
    
    @Override
    public @NotNull EntryRenderer<FluidStack> getRenderer() {
        return renderer;
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getIdentifier(EntryStack<FluidStack> entry, FluidStack value) {
        return Optional.ofNullable(Registry.FLUID.getKey(value.getFluid()));
    }
    
    @Override
    public @NotNull Fraction getAmount(EntryStack<FluidStack> entry, FluidStack value) {
        return value.getAmount();
    }
    
    @Override
    public void setAmount(EntryStack<FluidStack> entry, FluidStack value, Fraction amount) {
        value.setAmount(amount);
    }
    
    @Override
    public boolean isEmpty(EntryStack<FluidStack> entry, FluidStack value) {
        return value.isEmpty();
    }
    
    @Override
    public @NotNull FluidStack copy(EntryStack<FluidStack> entry, FluidStack value) {
        return value.copy();
    }
    
    @Override
    public int hash(EntryStack<FluidStack> entry, FluidStack value, ComparisonContext context) {
        int code = 1;
        code = 31 * code + value.getFluid().hashCode();
        code = 31 * code + (context.isIgnoresCount() ? 0 : value.getAmount().hashCode());
        code = 31 * code + (context.isIgnoresNbt() || !value.hasTag() ? 0 : value.getTag().hashCode());
        return code;
    }
    
    @Override
    public boolean equals(FluidStack o1, FluidStack o2, ComparisonContext context) {
        if (o1.getFluid() != o2.getFluid())
            return false;
        if (!context.isIgnoresCount() && !o1.getAmount().equals(o2.getAmount()))
            return false;
        return context.isIgnoresNbt() || isTagEqual(o1, o2);
    }
    
    private boolean isTagEqual(FluidStack o1, FluidStack o2) {
        CompoundTag tag1 = o1.getTag();
        CompoundTag tag2 = o2.getTag();
        return Objects.equals(tag1, tag2);
    }
    
    @Override
    public @NotNull CompoundTag toTag(EntryStack<FluidStack> entry, FluidStack value) {
        return value.write(new CompoundTag());
    }
    
    @Override
    public @NotNull FluidStack fromTag(@NotNull CompoundTag tag) {
        return FluidStack.read(tag);
    }
    
    @Override
    public @NotNull Component asFormattedText(EntryStack<FluidStack> entry, FluidStack value) {
        return value.getFluid().defaultFluidState().createLegacyBlock().getBlock().getName();
    }
    
    public static class FluidEntryRenderer extends AbstractEntryRenderer<FluidStack> {
        @Override
        public void render(EntryStack<FluidStack> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
            if (entry.get(EntryStack.Settings.RENDER).get()) {
                FluidStack stack = entry.getValue();
                SimpleFluidRenderer.FluidRenderingData renderingData = SimpleFluidRenderer.fromFluid(stack.getFluid());
                if (renderingData != null) {
                    TextureAtlasSprite sprite = renderingData.getSprite();
                    int color = renderingData.getColor();
                    int a = 255;
                    int r = (color >> 16 & 255);
                    int g = (color >> 8 & 255);
                    int b = (color & 255);
                    Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
                    Tesselator tess = Tesselator.getInstance();
                    BufferBuilder bb = tess.getBuilder();
                    Matrix4f matrix = matrices.last().pose();
                    bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                    int z = entry.getZ();
                    bb.vertex(matrix, bounds.getMaxX(), bounds.y, z).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
                    bb.vertex(matrix, bounds.x, bounds.y, z).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
                    bb.vertex(matrix, bounds.x, bounds.getMaxY(), z).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
                    bb.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), z).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
                    tess.end();
                }
            }
        }
        
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<FluidStack> entry, Point mouse) {
            if (!entry.get(EntryStack.Settings.TOOLTIP_ENABLED).get() || entry.isEmpty())
                return null;
            List<Component> toolTip = Lists.newArrayList(entry.asFormattedText());
            Fraction amount = entry.getAmount();
            if (!amount.isLessThan(Fraction.zero())) {
                String amountTooltip = entry.get(EntryStack.Settings.Fluid.AMOUNT_TOOLTIP).apply(entry);
                if (amountTooltip != null)
                    toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(TextComponent::new).collect(Collectors.toList()));
            }
            ResourceLocation fluidId = null;
            if (Minecraft.getInstance().options.advancedItemTooltips) {
                if (fluidId == null) {
                    fluidId = Registry.FLUID.getKey(entry.getValue().getFluid());
                }
                toolTip.add((new TextComponent(fluidId.toString())).withStyle(ChatFormatting.DARK_GRAY));
            }
            toolTip.addAll(entry.get(EntryStack.Settings.TOOLTIP_APPEND_EXTRA).apply(entry));
            if (entry.get(EntryStack.Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
                if (fluidId == null) {
                    fluidId = Registry.FLUID.getKey(entry.getValue().getFluid());
                }
                ClientHelper.getInstance().appendModIdToTooltips(toolTip, fluidId.getNamespace());
            }
            return Tooltip.create(toolTip);
        }
    }
}
