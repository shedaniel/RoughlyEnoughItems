package me.shedaniel.rei.api.entry;

import me.shedaniel.rei.impl.Internals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BuiltinEntryTypes {
    ResourceLocation EMPTY_ID = new ResourceLocation("empty");
    ResourceLocation RENDERING_ID = new ResourceLocation("rendering");
    EntryType<Unit> EMPTY = Internals.getEntryStackProvider().emptyType(EMPTY_ID);
    EntryType<Unit> RENDERING = Internals.getEntryStackProvider().renderingType(RENDERING_ID);
}
