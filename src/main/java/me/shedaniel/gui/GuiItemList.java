package me.shedaniel.gui;

import me.shedaniel.ClientListener;
import me.shedaniel.gui.widget.AEISlot;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.Control;
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

public class GuiItemList extends Drawable {
    
    public static final int FOOTERSIZE = 44;
    private GuiContainer overlayedGui;
    private static int page = 0;
    private ArrayList<AEISlot> displaySlots;
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
        MainWindow res = AEIRenderHelper.getResolution();
        int startX = (((IMixinGuiContainer) overlayedGui).getGuiLeft() + ((IMixinGuiContainer) overlayedGui).getXSize()) + 10;
        int width = res.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, res.getScaledHeight());
    }
    
    protected void resize() {
        MainWindow res = AEIRenderHelper.getResolution();
        
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
        int x = rect.x;
        int y = rect.y + 20;
        MainWindow res = AEIRenderHelper.getResolution();
        displaySlots.clear();
        int xOffset = 4;
        int yOffset = 4;
        while (true) {
            AEISlot slot = new AEISlot(x + xOffset, y + yOffset);
            slot.setCheatable(true);
            xOffset += 18;
            displaySlots.add(slot);
            if (x + xOffset + 18 > res.getScaledWidth()) {
                xOffset = 4;
                yOffset += 18;
            }
            if (y + yOffset + 9 + FOOTERSIZE > rect.height) {
                break;
            }
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
            TextComponentTranslation cheat = new TextComponentTranslation("text.aei.cheat", new Object[]{null});
            return cheat.getFormattedText();
        }
        TextComponentTranslation noCheat = new TextComponentTranslation("text.aei.nocheat", new Object[]{null});
        return noCheat.getFormattedText();
    }
    
    protected void updateView() {
        String searchText = searchBox.getText();
        String modText = null;
        if (searchText.contains("@")) {
            int nextBreak = searchText.indexOf(' ', searchText.indexOf('@'));
            if (nextBreak == 0 || nextBreak == -1)
                nextBreak = searchText.length();
            modText = searchText.substring(searchText.indexOf('@'), nextBreak);
            searchText = searchText.replace(modText, "").trim();
            modText = modText.replace("@", "").toLowerCase();
        }
        
        view.clear();
        if (searchText.equals("") || searchText == null) {
            for(ItemStack stack : ClientListener.stackList) {
                if (modText != null) {
                    if (getMod(stack).contains(modText)) {
                        view.add(stack);
                    }
                } else {
                    view.add(stack);
                }
            }
        } else {
            for(ItemStack stack : ClientListener.stackList) {
                if (stack.getItem().getName().getString().toLowerCase().contains(searchText))
                    if (modText != null) {
                        if (getMod(stack).contains(modText)) {
                            view.add(stack);
                        }
                    } else {
                        view.add(stack);
                    }
            }
        }
        page = 0;
        fillSlots();
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
