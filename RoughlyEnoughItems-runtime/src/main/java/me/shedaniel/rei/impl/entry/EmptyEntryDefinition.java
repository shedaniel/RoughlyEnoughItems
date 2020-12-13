package me.shedaniel.rei.impl.entry;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.utils.Fraction;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.entry.*;
import me.shedaniel.rei.api.widgets.Tooltip;
import me.shedaniel.rei.utils.ImmutableLiteralText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.Internal
public enum EmptyEntryDefinition implements EntryDefinition<Unit> {
    EMPTY(BuiltinEntryTypes.EMPTY, true),
    RENDERING(BuiltinEntryTypes.RENDERING, false);
    
    private final EntryType<Unit> type;
    private final boolean empty;
    
    EmptyEntryDefinition(EntryType<Unit> type, boolean empty) {
        this.type = type;
        this.empty = empty;
    }
    
    @Override
    public @NotNull Class<Unit> getValueType() {
        return Unit.class;
    }
    
    @Override
    public @NotNull EntryType<Unit> getType() {
        return type;
    }
    
    @Override
    public @NotNull EntryRenderer<Unit> getRenderer() {
        return EmptyRenderer.INSTANCE;
    }
    
    @Override
    public @NotNull Optional<ResourceLocation> getIdentifier(EntryStack<Unit> entry, Unit value) {
        return Optional.empty();
    }
    
    @Override
    public @NotNull Fraction getAmount(EntryStack<Unit> entry, Unit value) {
        return Fraction.zero();
    }
    
    @Override
    public void setAmount(EntryStack<Unit> entry, Unit value, Fraction amount) {
        
    }
    
    @Override
    public boolean isEmpty(EntryStack<Unit> entry, Unit value) {
        return empty;
    }
    
    @Override
    public @NotNull Unit copy(EntryStack<Unit> entry, Unit value) {
        return value;
    }
    
    @Override
    public int hash(EntryStack<Unit> entry, Unit value, ComparisonContext context) {
        return 0;
    }
    
    @Override
    public boolean equals(Unit o1, Unit o2, ComparisonContext context) {
        return true;
    }
    
    @Override
    public @NotNull CompoundTag toTag(EntryStack<Unit> entry, Unit value) {
        return new CompoundTag();
    }
    
    @Override
    public @NotNull Unit fromTag(@NotNull CompoundTag tag) {
        return Unit.INSTANCE;
    }
    
    @Override
    public @NotNull Component asFormattedText(EntryStack<Unit> entry, Unit value) {
        return ImmutableLiteralText.EMPTY;
    }
    
    private enum EmptyRenderer implements EntryRenderer<Unit> {
        INSTANCE;
    
        @Override
        public void render(EntryStack<Unit> entry, PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        
        }
    
        @Override
        public @Nullable Tooltip getTooltip(EntryStack<Unit> entry, Point mouse) {
            return null;
        }
    }
}
