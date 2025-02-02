package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntryIngredientSetting;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SetupDisplayUtils {
    @SuppressWarnings("RedundantCast")
    public static void TransformFiltering(List<? extends GuiEventListener> setupDisplay) {
        for (EntryWidget widget : Widgets.<EntryWidget>walk(setupDisplay, EntryWidget.class::isInstance)) {
            if (widget.getEntries().size() > 1) {
                Collection<EntryStack<?>> refiltered = EntryRegistry.getInstance().refilterNew(false, widget.getEntries());
                EntryIngredient asEntryIngredient = widget.getEntries() instanceof EntryIngredient ingredient ? ingredient : null;
                if (!refiltered.isEmpty() && !widget.getEntries().equals(refiltered)) {
                    widget.clearStacks();
                    EntryIngredient newIngredient = EntryIngredient.of(refiltered);
                    if (asEntryIngredient != null && (Object) asEntryIngredient.getSetting(EntryIngredientSetting.FOCUS_UUID) instanceof UUID uuid) {
                        newIngredient.setting(EntryIngredientSetting.FOCUS_UUID,
                                new UUID(uuid.getMostSignificantBits() ^ refiltered.size(), uuid.getLeastSignificantBits() ^ refiltered.size()));
                    }
                    widget.entries(newIngredient);
                }
            }
        }
    }
}
