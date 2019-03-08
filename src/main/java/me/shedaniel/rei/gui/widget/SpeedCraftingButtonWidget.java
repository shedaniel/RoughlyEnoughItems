package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.Arrays;
import java.util.function.Supplier;

public class SpeedCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private final SpeedCraftFunctional functional;
    
    public SpeedCraftingButtonWidget(Rectangle rectangle, String text, SpeedCraftFunctional functional, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = displaySupplier;
        this.functional = functional;
    }
    
    @Override
    public void onPressed(int button, double mouseX, double mouseY) {
        MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
        ScreenHelper.getLastOverlay().onInitialized();
        functional.performAutoCraft(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
    }
    
    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.enabled = functional != null && functional.acceptRecipe(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
        super.draw(mouseX, mouseY, partialTicks);
        if (getBounds().contains(mouseX, mouseY))
            if (enabled)
                ScreenHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.translate("text.speed_craft.move_items"))));
            else
                ScreenHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), Arrays.asList(I18n.translate("text.speed_craft.failed_move_items"))));
    }
    
}
