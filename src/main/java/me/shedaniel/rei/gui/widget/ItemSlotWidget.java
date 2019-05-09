package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.Renderer;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class ItemSlotWidget extends SlotWidget {
    public ItemSlotWidget(int x, int y, ItemStack itemStack, boolean drawBackground, boolean showToolTips) {
        super(x, y, itemStack, drawBackground, showToolTips);
    }
    
    public ItemSlotWidget(int x, int y, Collection<ItemStack> itemList, boolean drawBackground, boolean showToolTips) {
        super(x, y, itemList, drawBackground, showToolTips);
    }
    
    public ItemSlotWidget(int x, int y, List<Renderer> renderers, boolean drawBackground, boolean showToolTips) {
        super(x, y, renderers, drawBackground, showToolTips);
    }
    
    public ItemSlotWidget(int x, int y, List<ItemStack> itemList, boolean drawBackground, boolean showToolTips, boolean clickToMoreRecipes) {
        super(x, y, itemList, drawBackground, showToolTips, clickToMoreRecipes);
    }
}
