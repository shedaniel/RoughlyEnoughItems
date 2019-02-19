package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.IRecipeDisplay;
import me.shedaniel.rei.api.ISpeedCraftFunctional;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;

public class SpeedCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<IRecipeDisplay> displaySupplier;
    private final ISpeedCraftFunctional functional;
    
    public SpeedCraftingButtonWidget(Rectangle rectangle, String text, ISpeedCraftFunctional functional, Supplier<IRecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = displaySupplier;
        this.functional = functional;
    }
    
    @Override
    public void onPressed(int button, double mouseX, double mouseY) {
        Minecraft.getInstance().displayGuiScreen(GuiHelper.getLastGuiContainer());
        GuiHelper.getLastOverlay().onInitialized();
        functional.performAutoCraft(GuiHelper.getLastGuiContainer(), displaySupplier.get());
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.enabled = functional != null && functional.acceptRecipe(GuiHelper.getLastGuiContainer(), displaySupplier.get());
        super.draw(mouseX, mouseY, partialTicks);
        if (getBounds().contains(mouseX, mouseY))
            if (enabled)
                GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.format("text.speed_craft.move_items"))));
            else
                GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.format("text.speed_craft.failed_move_items"))));
    }
    
}
