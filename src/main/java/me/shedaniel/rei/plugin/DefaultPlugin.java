package me.shedaniel.rei.plugin;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.IRecipeCategoryCraftable;
import me.shedaniel.rei.api.IRecipePlugin;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.client.GuiHelper;
import me.shedaniel.rei.client.RecipeHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import me.shedaniel.rei.gui.widget.IWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import me.shedaniel.rei.listeners.IMixinRecipeBookGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.container.BlastFurnaceGui;
import net.minecraft.client.gui.container.CraftingTableGui;
import net.minecraft.client.gui.container.FurnaceGui;
import net.minecraft.client.gui.container.SmokerGui;
import net.minecraft.client.gui.ingame.PlayerInventoryGui;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.crafting.ShapedRecipe;
import net.minecraft.recipe.crafting.ShapelessRecipe;
import net.minecraft.recipe.smelting.BlastingRecipe;
import net.minecraft.recipe.smelting.SmeltingRecipe;
import net.minecraft.recipe.smelting.SmokingRecipe;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DefaultPlugin implements IRecipePlugin {
    
    static final Identifier CRAFTING = new Identifier("roughlyenoughitems", "plugins/crafting");
    static final Identifier SMELTING = new Identifier("roughlyenoughitems", "plugins/smelting");
    static final Identifier SMOKING = new Identifier("roughlyenoughitems", "plugins/smoking");
    static final Identifier BLASTING = new Identifier("roughlyenoughitems", "plugins/blasting");
    static final Identifier BREWING = new Identifier("roughlyenoughitems", "plugins/brewing");
    
    static final List<DefaultBrewingDisplay> BREWING_DISPLAYS = Lists.newArrayList();
    
    public static void registerBrewingDisplay(DefaultBrewingDisplay display) {
        BREWING_DISPLAYS.add(display);
    }
    
    @Override
    public void registerPluginCategories() {
        RecipeHelper.registerCategory(new DefaultCraftingCategory());
        RecipeHelper.registerCategory(new DefaultSmeltingCategory());
        RecipeHelper.registerCategory(new DefaultSmokingCategory());
        RecipeHelper.registerCategory(new DefaultBlastingCategory());
        RecipeHelper.registerCategory(new DefaultBrewingCategory());
    }
    
    @Override
    public void registerRecipes() {
        for(Recipe value : RecipeHelper.getRecipeManager().values())
            if (value instanceof ShapelessRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapelessDisplay((ShapelessRecipe) value));
            else if (value instanceof ShapedRecipe)
                RecipeHelper.registerRecipe(CRAFTING, new DefaultShapedDisplay((ShapedRecipe) value));
            else if (value instanceof SmeltingRecipe)
                RecipeHelper.registerRecipe(SMELTING, new DefaultSmeltingDisplay((SmeltingRecipe) value));
            else if (value instanceof SmokingRecipe)
                RecipeHelper.registerRecipe(SMOKING, new DefaultSmokingDisplay((SmokingRecipe) value));
            else if (value instanceof BlastingRecipe)
                RecipeHelper.registerRecipe(BLASTING, new DefaultBlastingDisplay((BlastingRecipe) value));
        BREWING_DISPLAYS.forEach(display -> RecipeHelper.registerRecipe(BREWING, display));
    }
    
    @Override
    public void registerAutoCraftingGui() {
        RecipeHelper.registerCategoryCraftable(new Class[]{DefaultShapelessDisplay.class, DefaultShapedDisplay.class}, new IRecipeCategoryCraftable<DefaultCraftingDisplay>() {
            @Override
            public boolean canAutoCraftHere(Class<? extends Gui> guiClass, DefaultCraftingDisplay recipe) {
                return guiClass.isAssignableFrom(CraftingTableGui.class) || (guiClass.isAssignableFrom(PlayerInventoryGui.class) && recipe.getHeight() < 3 && recipe.getWidth() < 3);
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultCraftingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(CraftingTableGui.class))
                    ((IMixinRecipeBookGui) (((CraftingTableGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else if (gui.getClass().isAssignableFrom(PlayerInventoryGui.class))
                    ((IMixinRecipeBookGui) (((PlayerInventoryGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerGui parentGui, DefaultCraftingDisplay recipe) {
                Point startPoint = new Point((int) rectangle.getCenterX() - 58, (int) rectangle.getCenterY() - 27);
                ButtonWidget widget;
                widgets.add(widget = new ButtonWidget(rectangle.x + 134, startPoint.y + 45, 10, 10, "+") {
                    @Override
                    public void onPressed(int button, double mouseX, double mouseY) {
                        MinecraftClient.getInstance().openGui(parentGui.getContainerGui());
                        if (canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe))
                            performAutoCraft(parentGui.getContainerGui(), recipe);
                    }
                    
                    @Override
                    public void draw(int mouseX, int mouseY, float partialTicks) {
                        super.draw(mouseX, mouseY, partialTicks);
                        List<String> tooltips = getToolTip(parentGui.getContainerGui(), recipe);
                        if (tooltips.size() > 0 && getBounds().contains(mouseX, mouseY))
                            GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), tooltips));
                    }
                });
                widget.enabled = canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe);
            }
            
            private List<String> getToolTip(ContainerGui parentGui, DefaultCraftingDisplay recipe) {
                if (!(parentGui instanceof CraftingTableGui || parentGui instanceof PlayerInventoryGui))
                    return Arrays.asList(I18n.translate("text.auto_craft.wrong_gui"));
                if (parentGui instanceof PlayerInventoryGui && !(recipe.getHeight() < 3 && recipe.getWidth() < 3))
                    return Arrays.asList(I18n.translate("text.auto_craft.crafting.too_small"));
                return Lists.newArrayList();
            }
        });
        RecipeHelper.registerCategoryCraftable(DefaultSmeltingDisplay.class, new IRecipeCategoryCraftable<DefaultSmeltingDisplay>() {
            @Override
            public boolean canAutoCraftHere(Class<? extends Gui> guiClass, DefaultSmeltingDisplay recipe) {
                return guiClass.isAssignableFrom(FurnaceGui.class);
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmeltingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(FurnaceGui.class))
                    ((IMixinRecipeBookGui) (((FurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerGui parentGui, DefaultSmeltingDisplay recipe) {
                Point startPoint = new Point((int) rectangle.getCenterX() - 58, (int) rectangle.getCenterY() - 27);
                ButtonWidget widget;
                widgets.add(widget = new ButtonWidget(rectangle.x + 134, startPoint.y + 45, 10, 10, "+") {
                    @Override
                    public void onPressed(int button, double mouseX, double mouseY) {
                        MinecraftClient.getInstance().openGui(parentGui.getContainerGui());
                        if (canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe))
                            performAutoCraft(parentGui.getContainerGui(), recipe);
                    }
                    
                    @Override
                    public void draw(int mouseX, int mouseY, float partialTicks) {
                        super.draw(mouseX, mouseY, partialTicks);
                        List<String> tooltips = getToolTip(parentGui.getContainerGui(), recipe);
                        if (tooltips.size() > 0 && getBounds().contains(mouseX, mouseY))
                            GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), tooltips));
                    }
                });
                widget.enabled = canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe);
            }
            
            private List<String> getToolTip(ContainerGui parentGui, DefaultSmeltingDisplay recipe) {
                if (!(parentGui instanceof FurnaceGui))
                    return Arrays.asList(I18n.translate("text.auto_craft.wrong_gui"));
                return Lists.newArrayList();
            }
        });
        RecipeHelper.registerCategoryCraftable(DefaultSmokingDisplay.class, new IRecipeCategoryCraftable<DefaultSmokingDisplay>() {
            @Override
            public boolean canAutoCraftHere(Class<? extends Gui> guiClass, DefaultSmokingDisplay recipe) {
                return guiClass.isAssignableFrom(SmokerGui.class);
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultSmokingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(SmokerGui.class))
                    ((IMixinRecipeBookGui) (((SmokerGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerGui parentGui, DefaultSmokingDisplay recipe) {
                Point startPoint = new Point((int) rectangle.getCenterX() - 58, (int) rectangle.getCenterY() - 27);
                ButtonWidget widget;
                widgets.add(widget = new ButtonWidget(rectangle.x + 134, startPoint.y + 45, 10, 10, "+") {
                    @Override
                    public void onPressed(int button, double mouseX, double mouseY) {
                        MinecraftClient.getInstance().openGui(parentGui.getContainerGui());
                        if (canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe))
                            performAutoCraft(parentGui.getContainerGui(), recipe);
                    }
                    
                    @Override
                    public void draw(int mouseX, int mouseY, float partialTicks) {
                        super.draw(mouseX, mouseY, partialTicks);
                        List<String> tooltips = getToolTip(parentGui.getContainerGui(), recipe);
                        if (tooltips.size() > 0 && getBounds().contains(mouseX, mouseY))
                            GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), tooltips));
                    }
                });
                widget.enabled = canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe);
            }
            
            private List<String> getToolTip(ContainerGui parentGui, DefaultSmokingDisplay recipe) {
                if (!(parentGui instanceof SmokerGui))
                    return Arrays.asList(I18n.translate("text.auto_craft.wrong_gui"));
                return Lists.newArrayList();
            }
        });
        RecipeHelper.registerCategoryCraftable(DefaultBlastingDisplay.class, new IRecipeCategoryCraftable<DefaultBlastingDisplay>() {
            @Override
            public boolean canAutoCraftHere(Class<? extends Gui> guiClass, DefaultBlastingDisplay recipe) {
                return guiClass.isAssignableFrom(BlastFurnaceGui.class);
            }
            
            @Override
            public boolean performAutoCraft(Gui gui, DefaultBlastingDisplay recipe) {
                if (gui.getClass().isAssignableFrom(BlastFurnaceGui.class))
                    ((IMixinRecipeBookGui) (((BlastFurnaceGui) gui).getRecipeBookGui())).getGhostSlots().reset();
                else return false;
                MinecraftClient.getInstance().interactionManager.clickRecipe(MinecraftClient.getInstance().player.container.syncId, recipe.getRecipe(), Gui.isShiftPressed());
                return true;
            }
            
            @Override
            public void registerAutoCraftButton(List<IWidget> widgets, Rectangle rectangle, IMixinContainerGui parentGui, DefaultBlastingDisplay recipe) {
                Point startPoint = new Point((int) rectangle.getCenterX() - 58, (int) rectangle.getCenterY() - 27);
                ButtonWidget widget;
                widgets.add(widget = new ButtonWidget(rectangle.x + 134, startPoint.y + 45, 10, 10, "+") {
                    @Override
                    public void onPressed(int button, double mouseX, double mouseY) {
                        MinecraftClient.getInstance().openGui(parentGui.getContainerGui());
                        if (canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe))
                            performAutoCraft(parentGui.getContainerGui(), recipe);
                    }
                    
                    @Override
                    public void draw(int mouseX, int mouseY, float partialTicks) {
                        super.draw(mouseX, mouseY, partialTicks);
                        List<String> tooltips = getToolTip(parentGui.getContainerGui(), recipe);
                        if (tooltips.size() > 0 && getBounds().contains(mouseX, mouseY))
                            GuiHelper.getLastOverlay().addTooltip(new QueuedTooltip(ClientHelper.getMouseLocation(), tooltips));
                    }
                });
                widget.enabled = canAutoCraftHere(parentGui.getContainerGui().getClass(), recipe);
            }
            
            private List<String> getToolTip(ContainerGui parentGui, DefaultBlastingDisplay recipe) {
                if (!(parentGui instanceof BlastFurnaceGui))
                    return Arrays.asList(I18n.translate("text.auto_craft.wrong_gui"));
                return Lists.newArrayList();
            }
        });
    }
    
}
