/*
 * Copyright (c) 2018, 2019, 2020 shedaniel
 * Licensed under the MIT License (the "License").
 */

package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.toast.CopyRecipeIdentifierToast;
import me.shedaniel.rei.impl.ClientHelperImpl;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public class AutoCraftingButtonWidget extends ButtonWidget {
    
    private final Supplier<RecipeDisplay> displaySupplier;
    private String extraTooltip;
    private List<String> errorTooltip;
    private List<Widget> setupDisplay;
    private AbstractContainerScreen<?> containerScreen;
    private boolean visible = false;
    private RecipeCategory<?> category;
    private Rectangle displayBounds;
    
    public AutoCraftingButtonWidget(Rectangle displayBounds, Rectangle rectangle, String text, Supplier<RecipeDisplay> displaySupplier, List<Widget> setupDisplay, RecipeCategory<?> recipeCategory) {
        super(rectangle, text);
        this.displayBounds = displayBounds;
        this.displaySupplier = displaySupplier;
        Optional<Identifier> recipe = displaySupplier.get().getRecipeLocation();
        extraTooltip = recipe.isPresent() ? I18n.translate("text.rei.recipe_id", Formatting.GRAY.toString(), recipe.get().toString()) : "";
        this.containerScreen = ScreenHelper.getLastContainerScreen();
        this.setupDisplay = setupDisplay;
        this.category = recipeCategory;
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
        List<String> error = null;
        int color = 0;
        visible = false;
        IntList redSlots = null;
        AutoTransferHandler.Context context = AutoTransferHandler.Context.create(false, containerScreen, displaySupplier.get());
        for (AutoTransferHandler autoTransferHandler : RecipeHelper.getInstance().getSortedAutoCraftingHandler()) {
            try {
                AutoTransferHandler.Result result = autoTransferHandler.handle(context);
                if (result.isApplicable())
                    visible = true;
                if (result.isSuccessful()) {
                    enabled = true;
                    error = null;
                    color = 0;
                    redSlots = null;
                    break;
                } else if (result.isApplicable()) {
                    if (error == null) {
                        error = Lists.newArrayList();
                    }
                    error.add(result.getErrorKey());
                    color = result.getColor();
                    if (result.getIntegers() != null && !result.getIntegers().isEmpty())
                        redSlots = result.getIntegers();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!visible) {
            enabled = false;
            if (error == null) {
                error = Lists.newArrayList();
            } else {
                error.clear();
            }
            error.add("error.rei.no.handlers.applicable");
        }
        if (isHovered(mouseX, mouseY) && category instanceof TransferRecipeCategory && redSlots != null) {
            ((TransferRecipeCategory<RecipeDisplay>) category).renderRedSlots(setupDisplay, displayBounds, displaySupplier.get(), redSlots);
        }
        errorTooltip = error == null || error.isEmpty() ? null : Lists.newArrayList();
        if (errorTooltip != null) {
            for (String s : error) {
                if (errorTooltip.stream().noneMatch(ss -> ss.equalsIgnoreCase(s)))
                    errorTooltip.add(s);
            }
        }
        int x = getBounds().x, y = getBounds().y, width = getBounds().width, height = getBounds().height;
        renderBackground(x, y, width, height, this.getTextureId(isHovered(mouseX, mouseY)));
        
        int colour = 14737632;
        if (!this.visible) {
            colour = 10526880;
        } else if (enabled && isHovered(mouseX, mouseY)) {
            colour = 16777120;
        }
        
        fillGradient(x, y, x + width, y + height, color, color);
        this.drawCenteredString(font, getText(), x + width / 2, y + (height - 8) / 2, colour);
        
        if (getTooltips().isPresent())
            if (!focused && containsMouse(mouseX, mouseY))
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltips().get().split("\n")));
            else if (focused)
                ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(new Point(x + width / 2, y + height / 2), getTooltips().get().split("\n")));
    }
    
    @Override
    protected int getTextureId(boolean boolean_1) {
        return !visible ? 0 : boolean_1 && enabled ? 4 : 1;
    }
    
    @Override
    public Optional<String> getTooltips() {
        String str = "";
        if (errorTooltip == null) {
            if (((ClientHelperImpl) ClientHelper.getInstance()).isYog.get())
                str += I18n.translate("text.auto_craft.move_items.yog");
            else
                str += I18n.translate("text.auto_craft.move_items");
        } else {
            if (errorTooltip.size() > 1)
                str += Formatting.RED.toString() + I18n.translate("error.rei.multi.errors") + "\n";
            str += CollectionUtils.mapAndJoinToString(errorTooltip, s -> Formatting.RED.toString() + (errorTooltip.size() > 1 ? "- " : "") + I18n.translate(s), "\n");
        }
        if (this.minecraft.options.advancedItemTooltips) {
            str += extraTooltip;
        }
        return Optional.of(str);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (displaySupplier.get().getRecipeLocation().isPresent() && ConfigObject.getInstance().getCopyRecipeIdentifierKeybind().matchesKey(int_1, int_2) && containsMouse(PointHelper.fromMouse())) {
            minecraft.keyboard.setClipboard(displaySupplier.get().getRecipeLocation().get().toString());
            if (ConfigObject.getInstance().isToastDisplayedOnCopyIdentifier()) {
                CopyRecipeIdentifierToast.addToast(I18n.translate("msg.rei.copied_recipe_id"), I18n.translate("msg.rei.recipe_id_details", displaySupplier.get().getRecipeLocation().get().toString()));
            }
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
}
