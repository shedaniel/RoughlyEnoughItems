package me.shedaniel.rei.mixin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsClient;
import me.shedaniel.rei.api.Identifier;
import me.shedaniel.rei.api.ScreenHooks;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.ContainerScreenOverlay;
import me.shedaniel.rei.api.RecipeBookButtonWidgetHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiScreen.class)
public class MixinScreen implements ScreenHooks {
    
    @Shadow public int width;
    @Shadow @Final protected List<GuiButton> buttons;
    @Shadow @Final protected List<IGuiEventListener> children;
    
    @Override
    public List<GuiButton> cloth_getButtonWidgets() {
        return buttons;
    }
    
    @Override
    public List<IGuiEventListener> cloth_getChildren() {
        return (List<IGuiEventListener>) ((GuiScreen) (Object) this).getChildren();
    }
    
    @Inject(method = "render(IIF)V", at = @At("RETURN"))
    public void onPostDraw(int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (((Object) this) instanceof GuiContainer)
            ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
    }
    
    @Inject(method = "setWorldAndResolution", at = @At("HEAD"))
    public void onPreInit(Minecraft minecraftClient_1, int int_1, int int_2, CallbackInfo info) {
        GuiScreen screen = (GuiScreen) (Object) this;
        if (ScreenHelper.lastContainerScreen != screen && screen instanceof GuiContainer)
            ScreenHelper.lastContainerScreen = (GuiContainer) screen;
    }
    
    @Inject(method = "setWorldAndResolution", at = @At("RETURN"))
    public void onPostInit(Minecraft minecraftClient_1, int int_1, int int_2, CallbackInfo info) {
        GuiScreen screen = (GuiScreen) (Object) this;
        if (screen instanceof GuiContainer) {
            if (screen instanceof GuiInventory && minecraftClient_1.playerController.isInCreativeMode())
                return;
            ScreenHelper.setLastContainerScreen((GuiContainer) screen);
            boolean alreadyAdded = false;
            for(IGuiEventListener element : Lists.newArrayList(cloth_getChildren()))
                if (ContainerScreenOverlay.class.isAssignableFrom(element.getClass()))
                    if (alreadyAdded)
                        cloth_getChildren().remove(element);
                    else
                        alreadyAdded = true;
            if (!alreadyAdded)
                cloth_getChildren().add(ScreenHelper.getLastOverlay(true, false));
        }
    }
    
    @Inject(method = "addButton", at = @At("HEAD"), cancellable = true)
    public void onAddButton(GuiButton widget, CallbackInfoReturnable<GuiButton> info) {
        final Identifier recipeButtonTex = new Identifier("textures/gui/recipe_button.png");
        GuiScreen screen = (GuiScreen) (Object) this;
        if (!info.isCancelled()) {
            if (RoughlyEnoughItemsClient.getConfigManager().getConfig().disableRecipeBook && screen instanceof GuiContainer && widget instanceof GuiButtonImage)
                if (((RecipeBookButtonWidgetHooks) widget).rei_getTexture().equals(recipeButtonTex))
                    info.cancel();
        }
    }
    
}
