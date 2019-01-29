package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Arrays;

public class SpeedCraftingButtonWidget extends ButtonWidget {
    
    private final IRecipeDisplay display;
    private final SpeedCraftFunctional functional;
    
    public SpeedCraftingButtonWidget(Rectangle rectangle, String text, SpeedCraftFunctional functional, IRecipeDisplay display) {
        super(rectangle, text);
        this.display = display;
        //RoughlyEnoughItemsCore.LOGGER.info("Registered %s.", ((ItemStack) this.display.getOutput().get(0)).getDisplayName().getFormattedText());
        this.functional = functional;
    }
    
    @Override
    public void onPressed(int button, double mouseX, double mouseY) {
        //RoughlyEnoughItemsCore.LOGGER.info("Crafting %s.", ((ItemStack) this.display.getOutput().get(0)).getDisplayName().getFormattedText());
        MinecraftClient.getInstance().openGui(GuiHelper.getLastContainerGui());
        functional.performAutoCraft(GuiHelper.getLastContainerGui(), display);
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.enabled = functional != null && functional.acceptRecipe(GuiHelper.getLastContainerGui(), display);
        super.draw(mouseX, mouseY, partialTicks);
        if (getBounds().contains(mouseX, mouseY))
            if (enabled)
                GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.translate("text.speed_craft.move_items"))));
            else
                GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.translate("text.speed_craft.failed_move_items"))));
    }
    
}
