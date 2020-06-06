package me.shedaniel.rei.api;

import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.widgets.Tooltip;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public interface TextRepresentable {
    @NotNull
    default Text asFormattedText() {
        if (this instanceof EntryStack) {
            Tooltip tooltip = ((EntryStack) this).getTooltip(PointHelper.ofMouse());
            if (tooltip != null && !tooltip.getText().isEmpty())
                return tooltip.getText().get(0);
        }
        return new LiteralText("");
    }
    
    @NotNull
    default Text asFormatStrippedText() {
        return new LiteralText(Formatting.strip(asFormattedText().getString()));
    }
}
