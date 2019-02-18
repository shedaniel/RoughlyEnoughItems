package me.shedaniel.rei.mixin;

//@Mixin(GuiCrafting.class)
//public abstract class MixinGuiCrafting extends GuiContainer implements IRecipeShownListener {
//
//    @Shadow
//    @Final
//    private GuiRecipeBook recipeBookGui;
//
//    public MixinGuiCrafting(Container inventorySlotsIn) {
//        super(inventorySlotsIn);
//    }
//
//    @Override
//    public IGuiEventListener getFocused() {
//        return super.getFocused();
//    }
//
//    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
//    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
//        if (recipeBookGui.mouseClicked(mouseX, mouseY, button)) {
//            focusOn(recipeBookGui);
//            ci.setReturnValue(true);
//            ci.cancel();
//        }
//    }
//
//}

public class MixinGuiCrafting {}