package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.SpeedCraftFunctional;
import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.resource.language.I18n;

import java.awt.*;
import java.util.Optional;
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
    public void onPressed() {
        minecraft.openScreen(ScreenHelper.getLastContainerScreen());
        ScreenHelper.getLastOverlay().onInitialized();
        functional.performAutoCraft(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
    }
    
    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.enabled = functional != null && functional.acceptRecipe(ScreenHelper.getLastContainerScreen(), displaySupplier.get());
        super.render(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (enabled)
            return Optional.ofNullable(I18n.translate("text.speed_craft.move_items"));
        else
            return Optional.ofNullable(I18n.translate("text.speed_craft.failed_move_items"));
    }
}
