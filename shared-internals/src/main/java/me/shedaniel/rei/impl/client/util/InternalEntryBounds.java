package me.shedaniel.rei.impl.client.util;

import me.shedaniel.rei.api.client.config.ConfigObject;
import net.minecraft.util.Mth;

public class InternalEntryBounds {
    private static final int SIZE = 18;
    
    public static int entrySize() {
        return Mth.ceil(SIZE * ConfigObject.getInstance().getEntrySize());
    }
}
