package me.shedaniel.rei.impl.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.LinkedHashSet;

public class DisplayScreenStack {
    private static final LinkedHashSet<DisplayScreen> LAST_DISPLAY_SCREENS = Sets.newLinkedHashSetWithExpectedSize(10);
    
    public static void storeDisplayScreen(DisplayScreen screen) {
        while (LAST_DISPLAY_SCREENS.size() >= 10)
            LAST_DISPLAY_SCREENS.remove(Iterables.get(LAST_DISPLAY_SCREENS, 0));
        LAST_DISPLAY_SCREENS.add(screen);
    }
    
    public static boolean hasLastDisplayScreen() {
        return !LAST_DISPLAY_SCREENS.isEmpty();
    }
    
    public static Screen getLastDisplayScreen() {
        DisplayScreen screen = Iterables.getLast(LAST_DISPLAY_SCREENS);
        LAST_DISPLAY_SCREENS.remove(screen);
        screen.recalculateCategoryPage();
        return (Screen) screen;
    }
    
    public static void clear() {
        LAST_DISPLAY_SCREENS.clear();
    }
}
