package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.TextBox;
import me.shedaniel.listenerdefinitions.IMixinGuiContainer;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.text.TextComponentTranslation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiItemList extends Drawable {
    
    public static final int FOOTERSIZE = 44;
    private GuiContainer overlayedGui;
    private static int page = 0;
    private ArrayList<REISlot> displaySlots;
    protected ArrayList<Control> controls;
    private boolean needsResize = false;
    Button buttonLeft;
    Button buttonRight;
    Button buttonCheating;
    TextBox searchBox;
    private ArrayList<ItemStack> view;
    private Control lastHovered;
    protected boolean visible = true;
    private int oldGuiLeft = 0;
    private boolean cheatMode = false;
    
    public GuiItemList(GuiContainer overlayedGui) {
        super(calculateRect(overlayedGui));
        displaySlots = new ArrayList<>();
        controls = new ArrayList<>();
        this.overlayedGui = overlayedGui;
        view = new ArrayList<>();
        resize();
    }
    
    public boolean canCheat() {
        EntityPlayer player = Minecraft.getInstance().player;
        if (cheatMode) {
            if (!player.hasPermissionLevel(1)) {
                cheatClicked(0);
                return false;
            }
            return true;
        }
        return false;
    }
    
    private static Rectangle calculateRect(GuiContainer overlayedGui) {
        MainWindow res = REIRenderHelper.getResolution();
        int startX = (((IMixinGuiContainer) overlayedGui).getGuiLeft() + ((IMixinGuiContainer) overlayedGui).getXSize()) + 10;
        int width = res.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, res.getScaledHeight());
    }
    
    protected void resize() {
        MainWindow res = REIRenderHelper.getResolution();
        
        if (overlayedGui != Minecraft.getInstance().currentScreen) {
            if (Minecraft.getInstance().currentScreen instanceof GuiContainer) {
                overlayedGui = (GuiContainer) Minecraft.getInstance().currentScreen;
                
            } else {
                needsResize = true;
                return;
            }
        }
        oldGuiLeft = ((IMixinGuiContainer) overlayedGui).getGuiLeft();
        rect = calculateRect(overlayedGui);
        page = 0;
        buttonLeft = new Button(rect.x, rect.y + 3, 16, 20, "<");
        buttonLeft.onClick = this::btnLeftClicked;
        buttonRight = new Button(rect.x + rect.width - 18, rect.y + 3, 16, 20, ">");
        buttonRight.onClick = this::btnRightClicked;
        controls.clear();
        controls.add(buttonLeft);
        controls.add(buttonRight);
        String savedText = "";
        if (searchBox != null) {
            savedText = searchBox.getText();
        }
        searchBox = new TextBox(rect.x, rect.height - 31, rect.width - 4, 18);
        searchBox.setText(savedText);
        controls.add(searchBox);
        buttonCheating = new Button(5, 5, 45, 20, getCheatModeText());
        buttonCheating.onClick = this::cheatClicked;
        controls.add(buttonCheating);
        calculateSlots();
        updateView();
        fillSlots();
        controls.addAll(displaySlots);
    }
    
    private void fillSlots() {
        page = MathHelper.clamp(page, 0, (int) Math.floor(view.size() / displaySlots.size()));
        int firstSlot = page * displaySlots.size();
        for(int i = 0; i < displaySlots.size(); i++) {
            if (firstSlot + i < view.size() && firstSlot + i >= 0) {
                displaySlots.get(i).setStack(view.get(firstSlot + i));
            } else {
                displaySlots.get(i).setStack(ItemStack.EMPTY);
            }
        }
    }
    
    private void calculateSlots() {
        int x = rect.x, y = rect.y + 20;
        MainWindow res = REIRenderHelper.getResolution();
        displaySlots.clear();
        int xOffset = 0, yOffset = 0, row = 0, perRow = 0, currentX = 0, currentY = 0;
        while (true) {
            xOffset += 18;
            if (row == 0)
                perRow++;
            if (x + xOffset + 22 > res.getScaledWidth()) {
                xOffset = 0;
                yOffset += 18;
                row++;
            }
            if (y + yOffset + 9 + FOOTERSIZE > rect.height) {
                xOffset = 0;
                yOffset = 0;
                break;
            }
        }
        x += (rect.width - perRow * 18) / 2;
        y += (rect.height - FOOTERSIZE - 2 - row * 18) / 2;
        while (true) {
            REISlot slot = new REISlot(x + xOffset, y + yOffset);
            slot.setCheatable(true);
            xOffset += 18;
            currentX++;
            displaySlots.add(slot);
            if (currentX >= perRow) {
                xOffset = 0;
                yOffset += 18;
                currentX = 0;
                currentY++;
            }
            if (currentY >= row)
                break;
        }
    }
    
    @Override
    public void draw() {
        if (!visible)
            return;
        if (needsResize == true)
            resize();
        if (oldGuiLeft != ((IMixinGuiContainer) overlayedGui).getGuiLeft())
            resize();
        GlStateManager.pushMatrix();
        updateButtons();
        controls.forEach(Control::draw);
        String header = String.format("%s/%s", page + 1, ((int) Math.floor(view.size() / displaySlots.size())) + 1);
        Minecraft.getInstance().fontRenderer.drawStringWithShadow(header, rect.x + (rect.width / 2) - (Minecraft.getInstance().fontRenderer.getStringWidth(header) / 2), rect.y + 10, -1);
        GlStateManager.popMatrix();
    }
    
    private void updateButtons() {
        if (page == 0)
            buttonLeft.setEnabled(false);
        else
            buttonLeft.setEnabled(true);
        if (displaySlots.size() + displaySlots.size() * page >= view.size())
            buttonRight.setEnabled(false);
        else
            buttonRight.setEnabled(true);
    }
    
    
    public boolean btnRightClicked(int button) {
        if (button == 0) {
            page++;
            fillSlots();
            return true;
        }
        return false;
    }
    
    public boolean btnLeftClicked(int button) {
        if (button == 0) {
            page--;
            fillSlots();
            return true;
        }
        return false;
    }
    
    public boolean cheatClicked(int button) {
        if (button == 0) {
            cheatMode = !cheatMode;
            
            buttonCheating.setString(getCheatModeText());
            return true;
        }
        return false;
    }
    
    private String getCheatModeText() {
        if (cheatMode) {
            TextComponentTranslation cheat = new TextComponentTranslation("text.rei.cheat", new Object[]{null});
            return cheat.getFormattedText();
        }
        TextComponentTranslation noCheat = new TextComponentTranslation("text.rei.nocheat", new Object[]{null});
        return noCheat.getFormattedText();
    }
    
    protected void updateView() {
        String searchText = searchBox.getText();
        view.clear();
        List<ItemStack> stacks = new ArrayList<>();
        Arrays.stream(searchText.split("\\|")).forEachOrdered(s -> {
            List<SearchArgument> arguments = new ArrayList<>();
            while (s.startsWith(" ")) s = s.substring(1);
            while (s.endsWith(" ")) s = s.substring(0, s.length());
            if (s.startsWith("@-") || s.startsWith("-@"))
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s.substring(2), false));
            else if (s.startsWith("@"))
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.MOD, s.substring(1), true));
            else if (s.startsWith("#-") || s.startsWith("-#"))
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s.substring(2), false));
            else if (s.startsWith("#"))
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.TOOLTIP, s.substring(1), true));
            else if (s.startsWith("-"))
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s.substring(1), false));
            else
                arguments.add(new SearchArgument(SearchArgument.ArgumentType.TEXT, s, true));
            ClientListener.stackList.stream().filter(itemStack -> filterItem(itemStack, arguments)).forEachOrdered(stacks::add);
        });
        view.addAll(stacks.stream().distinct().collect(Collectors.toList()));
        page = 0;
        fillSlots();
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = getMod(itemStack);
        List<String> toolTipsList = REIRenderHelper.getOverlayedGui().getItemToolTip(itemStack);
        String toolTipsMixed = toolTipsList.stream().skip(1).collect(Collectors.joining()).toLowerCase();
        String allMixed = Stream.of(itemStack.getDisplayName().getString(), toolTipsMixed).collect(Collectors.joining()).toLowerCase();
        for(SearchArgument searchArgument : arguments.stream().filter(searchArgument -> !searchArgument.isInclude()).collect(Collectors.toList())) {
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.MOD))
                if (mod.toLowerCase().contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TOOLTIP))
                if (toolTipsMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TEXT))
                if (allMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
        }
        for(SearchArgument searchArgument : arguments.stream().filter(SearchArgument::isInclude).collect(Collectors.toList())) {
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.MOD))
                if (!mod.toLowerCase().contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TOOLTIP))
                if (!toolTipsMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
            if (searchArgument.getArgumentType().equals(SearchArgument.ArgumentType.TEXT))
                if (!allMixed.contains(searchArgument.getText().toLowerCase()))
                    return false;
        }
        return true;
    }
    
    public void tick() {
        controls.forEach(f -> f.tick());
    }
    
    public void setLastHovered(Control ctrl) {
        lastHovered = ctrl;
    }
    
    public Control getLastHovered() {
        return lastHovered;
    }
    
    private String getMod(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            ResourceLocation location = IRegistry.ITEM.getKey(stack.getItem());
            return location.getNamespace();
        }
        return "";
    }
    
}
