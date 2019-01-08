package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.IFocusable;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.listenerdefinitions.IMixinContainerGui;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Created by James on 7/28/2018.
 */
public class REIRenderHelper {
    
    static Point mouseLoc;
    static public GuiItemList reiGui;
    static ContainerGui overlayedGui;
    static List<TooltipData> tooltipsToRender = new ArrayList<>();
    
    public static void setMouseLoc(int x, int y) {
        mouseLoc = new Point(x, y);
    }
    
    static public IFocusable focusedControl;
    
    public static Point getMouseLoc() {
        return mouseLoc;
    }
    
    public static Window getResolution() {
        return MinecraftClient.getInstance().window;
    }
    
    public static String tryGettingModName(String modid) {
        if (modid.equalsIgnoreCase("minecraft"))
            return "Minecraft";
        return FabricLoader.INSTANCE.getModContainers().stream()
                .map(modContainer -> {
                    return modContainer.getInfo();
                })
                .filter(modInfo -> modInfo.getId().equals(modid) || (modInfo.getName() != null && modInfo.getName().equals(modid)))
                .findFirst().map(modInfo -> {
                    if (modInfo.getName() != null)
                        return modInfo.getName();
                    return modid;
                }).orElse(modid);
    }
    
    public static void drawREI(ContainerGui overlayedGui) {
        REIRenderHelper.overlayedGui = overlayedGui;
        if (reiGui == null) {
            reiGui = new GuiItemList(overlayedGui);
        }
        reiGui.draw();
        renderTooltips();
    }
    
    public static void resize(int scaledWidth, int scaledHeight) {
        if (reiGui != null) {
            reiGui.resize();
        }
        if (overlayedGui instanceof RecipeGui) {
            overlayedGui.onScaleChanged(MinecraftClient.getInstance(), scaledWidth, scaledHeight);
        }
    }
    
    public static ItemRenderer getItemRender() {
        return MinecraftClient.getInstance().getItemRenderer();
    }
    
    public static FontRenderer getFontRenderer() {
        return MinecraftClient.getInstance().fontRenderer;
    }
    
    public static ContainerGui getOverlayedGui() {
        if (overlayedGui instanceof ContainerGui)
            return overlayedGui;
        return null;
    }
    
    public static void addToolTip(List<String> text, int x, int y) {
        tooltipsToRender.add(new TooltipData(text, x, y));
    }
    
    
    private static void renderTooltips() {
        tooltipsToRender.forEach(tooltipData -> getOverlayedGui().drawTooltip(tooltipData.text, tooltipData.x, tooltipData.y));
        tooltipsToRender.clear();
    }
    
    public static boolean mouseClick(int x, int y, int button) {
        if (reiGui.visible) {
            for(Control control : reiGui.controls) {
                if (control.isHighlighted() && control.isEnabled() && control.onClick != null) {
                    if (focusedControl != null)
                        focusedControl.setFocused(false);
                    if (control instanceof IFocusable) {
                        focusedControl = (IFocusable) control;
                        ((IFocusable) control).setFocused(true);
                    }
                    return control.onClick.apply(button);
                }
            }
            if (focusedControl != null) {
                focusedControl.setFocused(false);
                focusedControl = null;
            }
        }
        if (overlayedGui instanceof RecipeGui) {
            List<Control> controls = new LinkedList<>(((RecipeGui) overlayedGui).controls);
            if (((RecipeGui) overlayedGui).slots != null)
                controls.addAll(((RecipeGui) overlayedGui).slots);
            controls.addAll(reiGui.controls.stream().filter(control -> {
                return control instanceof REISlot;
            }).collect(Collectors.toList()));
            Optional<Control> ctrl = controls.stream().filter(Control::isHighlighted).filter(Control::isEnabled).findFirst();
            if (ctrl.isPresent()) {
                try {
                    return ctrl.get().onClick.apply(button);
                } catch (Exception e) {
                }
            }
        }
        return false;
    }
    
    public static boolean keyDown(int typedChar, int keyCode, int unknown) {
        boolean handled = false;
        if (focusedControl != null && focusedControl instanceof Control) {
            Control control = (Control) focusedControl;
            if (control.onKeyDown != null) {
                handled = control.onKeyDown.accept(typedChar, keyCode, unknown);
            }
            if (control.charPressed != null)
                if (typedChar == 256) {
                    ((IFocusable) control).setFocused(false);
                    focusedControl = null;
                }
            handled = true;
        }
        if (!handled) {
            return ClientListener.processGuiKeyBinds(typedChar);
        }
        return handled;
    }
    
    public static boolean charInput(long num, int keyCode, int unknown) {
        if (focusedControl != null && focusedControl instanceof Control) {
            Control control = (Control) focusedControl;
            if (control.charPressed != null) {
                int numChars = Character.charCount(keyCode);
                if (num == numChars)
                    control.charPressed.accept((char) keyCode, unknown);
                else {
                    char[] chars = Character.toChars(keyCode);
                    for(int x = 0; x < chars.length; x++) {
                        control.charPressed.accept(chars[x], unknown);
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public static boolean mouseScrolled(double direction) {
        if (!reiGui.visible)
            return false;
        if (MinecraftClient.getInstance().currentGui instanceof RecipeGui) {
            Window window = REIRenderHelper.getResolution();
            Point mouse = new Point((int) MinecraftClient.getInstance().mouse.getX(), (int) MinecraftClient.getInstance().mouse.getY());
            int mouseX = (int) (mouse.x * (double) window.getScaledWidth() / (double) window.method_4480());
            int mouseY = (int) (mouse.y * (double) window.getScaledHeight() / (double) window.method_4507());
            mouse = new Point(mouseX, mouseY);
            
            RecipeGui recipeGui = (RecipeGui) MinecraftClient.getInstance().currentGui;
            if (mouse.getX() < window.getScaledWidth() / 2 + recipeGui.guiWidth / 2 && mouse.getX() > window.getScaledWidth() / 2 - recipeGui.guiWidth / 2 &&
                    mouse.getY() < window.getScaledHeight() / 2 + recipeGui.guiHeight / 2 && mouse.getY() > window.getScaledHeight() / 2 - recipeGui.guiHeight / 2 &&
                    recipeGui.recipes.get(recipeGui.selectedCategory).size() > 2) {
                boolean failed = false;
                if (direction > 0 && reiGui.buttonLeft.isEnabled())
                    recipeGui.btnRecipeLeft.onClick.apply(0);
                else if (direction < 0 && reiGui.buttonRight.isEnabled())
                    recipeGui.btnRecipeRight.onClick.apply(0);
                else failed = true;
                if (!failed)
                    return true;
            }
        }
        if (direction > 0 && reiGui.buttonLeft.isEnabled())
            reiGui.buttonLeft.onClick.apply(0);
        else if (direction < 0 && reiGui.buttonRight.isEnabled())
            reiGui.buttonRight.onClick.apply(0);
        return true;
    }
    
    private static class TooltipData {
        
        private final List<String> text;
        private final int x;
        private final int y;
        
        public TooltipData(List<String> text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }
    
    public static void updateSearch() {
        reiGui.updateView();
    }
    
    public static void tick() {
        if (reiGui != null && MinecraftClient.getInstance().currentGui == overlayedGui)
            reiGui.tick();
    }
    
    public static boolean isGuiVisible() {
        return reiGui != null && reiGui.visible;
    }
    
    public static boolean recipeKeyBind() {
        if (!(MinecraftClient.getInstance().currentGui instanceof ContainerGui))
            return false;
        Control control = reiGui.getLastHovered();
        if (control != null && control.isHighlighted() && control instanceof REISlot) {
            REISlot slot = (REISlot) control;
            REIRecipeManager.instance().displayRecipesFor(slot.getStack());
            return true;
        }
        if (((IMixinContainerGui) overlayedGui).getHoveredSlot() != null) {
            ItemStack stack = ((IMixinContainerGui) overlayedGui).getHoveredSlot().getStack();
            REIRecipeManager.instance().displayRecipesFor(stack);
            return true;
        }
        return false;
    }
    
    public static boolean useKeyBind() {
        if (!(MinecraftClient.getInstance().currentGui instanceof ContainerGui))
            return false;
        Control control = reiGui.getLastHovered();
        if (control != null && control.isHighlighted() && control instanceof REISlot) {
            REISlot slot = (REISlot) control;
            REIRecipeManager.instance().displayUsesFor(slot.getStack());
            return true;
        }
        if (((IMixinContainerGui) overlayedGui).getHoveredSlot() != null) {
            ItemStack stack = ((IMixinContainerGui) overlayedGui).getHoveredSlot().getStack();
            REIRecipeManager.instance().displayUsesFor(stack);
            return true;
        }
        return false;
    }
    
    public static boolean hideKeyBind() {
        if (MinecraftClient.getInstance().currentGui == overlayedGui && reiGui != null) {
            reiGui.visible = !reiGui.visible;
            return true;
        }
        return false;
    }
    
}
