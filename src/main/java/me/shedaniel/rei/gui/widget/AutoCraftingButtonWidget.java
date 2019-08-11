/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.widget;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.RecipeDisplay;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Optional;
import java.util.function.Supplier;

public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private String extraTooltip;
    private String errorTooltip;
    private AbstractContainerScreen<?> containerScreen;
    
    public AutoCraftingButtonWidget(Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier) {
        super(rectangle, text);
        this.displaySupplier = () -> displaySupplier.get();
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
    }
    
    @Override
    public void onPressed() {
        AutoTransferHandler.Context context = AutoTransferHandler.Context.create(true, containerScreen, displaySupplier.get());
        for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler())
            try {
                AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                if (result.isSuccessful())
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        minecraft.openScreen(containerScreen);
        ScreenHelper.getLastOverlay().init();
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.enabled = false;
        String error = null;
        AutoTransferHandler.Context context = AutoTransferHandler.Context.create(false, containerScreen, displaySupplier.get());
        for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler()) {
            AutoTransferHandler.Result result = autoTransferHandler.handle(context);
            if (result.isSuccessful()) {
                enabled = true;
                error = null;
                break;
            } else if (error == null) {
                error = result.getErrorKey();
            }
        }
        errorTooltip = error;
        super.render(mouseX, mouseY, delta);
    }
    
    @Override
    public Optional<String> getTooltips() {
        if (this.minecraft.options.advancedItemTooltips)
            if (errorTooltip == null)
                return Optional.ofNullable(I18n.translate("text.auto_craft.move_items") + extraTooltip);
            else
                return Optional.ofNullable("§c" + I18n.translate(errorTooltip) + extraTooltip);
        if (errorTooltip == null)
            return Optional.ofNullable(I18n.translate("text.auto_craft.move_items"));
        else
            return Optional.ofNullable("§c" + I18n.translate(errorTooltip));
    }
}
