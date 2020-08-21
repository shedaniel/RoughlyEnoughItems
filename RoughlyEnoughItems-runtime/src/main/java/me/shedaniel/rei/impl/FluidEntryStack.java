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
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.ConfigObject;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.fractions.Fraction;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.utils.CollectionUtils;
import me.shedaniel.rei.utils.FormattingUtils;
import me.shedaniel.rei.utils.ImmutableLiteralText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class FluidEntryStack extends AbstractEntryStack {
    private static final Fraction IGNORE_AMOUNT = Fraction.of(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)).simplify();
    private Fluid fluid;
    private Fraction amount;
    private int hashIgnoreAmount;
    private int hash;
    
    public FluidEntryStack(Fluid fluid) {
        this(fluid, IGNORE_AMOUNT);
    }
    
    public FluidEntryStack(Fluid fluid, Fraction amount) {
        this.fluid = fluid;
        this.amount = amount;
        
        rehash();
    }
    
    private void rehash() {
        hashIgnoreAmount = 31 + getType().ordinal();
        hashIgnoreAmount = 31 * hashIgnoreAmount + fluid.hashCode();
        
        hash = 31 * hashIgnoreAmount + amount.hashCode();
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
    public Fraction getAccurateAmount() {
        return amount;
    }
    
    @Override
    public void setAmount(Fraction amount) {
        this.amount = amount.equals(IGNORE_AMOUNT) ? IGNORE_AMOUNT : max(amount, Fraction.empty());
        if (isEmpty()) {
            fluid = Fluids.EMPTY;
        }
        
        rehash();
    }
    
    private <T extends Comparable<T>> T max(T o1, T o2) {
        return o1.compareTo(o2) > 0 ? o1 : o2;
    }
    
    @Override
    public boolean isEmpty() {
        return (!amount.equals(IGNORE_AMOUNT) && !amount.isGreaterThan(Fraction.empty())) || fluid == Fluids.EMPTY;
    }
    
    @Override
    public EntryStack copy() {
        EntryStack stack = EntryStack.create(fluid, amount);
        for (Map.Entry<Settings<?>, Object> entry : getSettings().entrySet()) {
            stack.setting((Settings<? super Object>) entry.getKey(), entry.getValue());
        }
        return stack;
    }
    
    @Override
    public Object getObject() {
        return fluid;
    }
    
    @Override
    public boolean equalsIgnoreTagsAndAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return equalsIgnoreTagsAndAmount(EntryStack.copyItemToFluid(stack));
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsIgnoreTags(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return equalsIgnoreTags(EntryStack.copyItemToFluid(stack));
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && (amount.equals(IGNORE_AMOUNT) || stack.getAccurateAmount().equals(IGNORE_AMOUNT) || amount.equals(stack.getAccurateAmount()));
    }
    
    @Override
    public boolean equalsIgnoreAmount(EntryStack stack) {
        if (stack.getType() == Type.ITEM)
            return equalsIgnoreAmount(EntryStack.copyItemToFluid(stack));
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid();
    }
    
    @Override
    public boolean equalsAll(EntryStack stack) {
        if (stack.getType() != Type.FLUID)
            return false;
        return fluid == stack.getFluid() && (amount.equals(IGNORE_AMOUNT) || stack.getAccurateAmount().equals(IGNORE_AMOUNT) || amount.equals(stack.getAccurateAmount()));
    }
    
    @Override
    public int hashOfAll() {
        return hash;
    }
    
    @Override
    public int hashIgnoreAmountAndTags() {
        return hashIgnoreAmount;
    }
    
    @Override
    public int hashIgnoreTags() {
        return hash;
    }
    
    @Override
    public int hashIgnoreAmount() {
        return hashIgnoreAmount;
    }
    
    @Nullable
    @Override
    public Tooltip getTooltip(Point point) {
        if (!get(Settings.TOOLTIP_ENABLED).get() || isEmpty())
            return null;
        List<Text> toolTip = Lists.newArrayList(asFormattedText());
        if (!amount.isLessThan(Fraction.empty()) && !amount.equals(IGNORE_AMOUNT)) {
            String amountTooltip = get(Settings.Fluid.AMOUNT_TOOLTIP).apply(this);
            if (amountTooltip != null)
                toolTip.addAll(Stream.of(amountTooltip.split("\n")).map(LiteralText::new).collect(Collectors.toList()));
        }
        toolTip.addAll(get(Settings.TOOLTIP_APPEND_EXTRA).apply(this));
        if (get(Settings.TOOLTIP_APPEND_MOD).get() && ConfigObject.getInstance().shouldAppendModNames()) {
            Identifier id = Registry.FLUID.getId(fluid);
            final String modId = ClientHelper.getInstance().getModFromIdentifier(id);
            boolean alreadyHasMod = false;
            for (Text s : toolTip)
                if (FormattingUtils.stripFormatting(s.getString()).equalsIgnoreCase(modId)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(ClientHelper.getInstance().getFormattedModFromIdentifier(id));
        }
        return Tooltip.create(toolTip);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void render(MatrixStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        if (get(Settings.RENDER).get()) {
            SimpleFluidRenderer.FluidRenderingData renderingData = SimpleFluidRenderer.fromFluid(fluid);
            if (renderingData != null) {
                Sprite sprite = renderingData.getSprite();
                int color = renderingData.getColor();
                int a = 255;
                int r = (color >> 16 & 255);
                int g = (color >> 8 & 255);
                int b = (color & 255);
                MinecraftClient.getInstance().getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder bb = tess.getBuffer();
                Matrix4f matrix = matrices.peek().getModel();
                bb.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                bb.vertex(matrix, bounds.getMaxX(), bounds.y, getZ()).texture(sprite.getMaxU(), sprite.getMinV()).color(r, g, b, a).next();
                bb.vertex(matrix, bounds.x, bounds.y, getZ()).texture(sprite.getMinU(), sprite.getMinV()).color(r, g, b, a).next();
                bb.vertex(matrix, bounds.x, bounds.getMaxY(), getZ()).texture(sprite.getMinU(), sprite.getMaxV()).color(r, g, b, a).next();
                bb.vertex(matrix, bounds.getMaxX(), bounds.getMaxY(), getZ()).texture(sprite.getMaxU(), sprite.getMaxV()).color(r, g, b, a).next();
                tess.draw();
            }
        }
    }
    
    @NotNull
    @Override
    public Text asFormattedText() {
        Identifier id = Registry.FLUID.getId(fluid);
        if (I18n.hasTranslation("block." + id.toString().replaceFirst(":", ".")))
            return new ImmutableLiteralText(I18n.translate("block." + id.toString().replaceFirst(":", ".")));
        return new ImmutableLiteralText(CollectionUtils.mapAndJoinToString(id.getPath().split("_"), StringUtils::capitalize, " "));
    }
}
