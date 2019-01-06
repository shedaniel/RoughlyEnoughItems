package me.shedaniel.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import me.shedaniel.ClientListener;
import me.shedaniel.Core;
import me.shedaniel.config.REIItemListOrdering;
import me.shedaniel.gui.widget.Button;
import me.shedaniel.gui.widget.Control;
import me.shedaniel.gui.widget.REISlot;
import me.shedaniel.gui.widget.TextBox;
import me.shedaniel.impl.REIRecipeManager;
import me.shedaniel.listenerdefinitions.IMixinContainerGui;
import net.fabricmc.fabric.client.itemgroup.FabricCreativeGuiComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ContainerGui;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiItemList extends Drawable {
    
    public final int FOOTERSIZE;
    private ContainerGui overlayedGui;
    private static int page = 0;
    private ArrayList<REISlot> displaySlots;
    protected ArrayList<Control> controls;
    private boolean needsResize = false;
    Button buttonLeft, buttonRight, buttonCheating, buttonConfig;
    private TextBox searchBox;
    private ArrayList<ItemStack> view;
    private Control lastHovered;
    protected boolean visible = true;
    private int oldGuiLeft = 0;
    private boolean cheatMode = false;
    
    public GuiItemList(ContainerGui overlayedGui) {
        super(calculateRect(overlayedGui));
        FOOTERSIZE = Core.centreSearchBox ? 18 : 44;
        displaySlots = new ArrayList<>();
        controls = new ArrayList<>();
        this.overlayedGui = overlayedGui;
        view = new ArrayList<>();
        resize();
    }
    
    public boolean canCheat() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (cheatMode) {
            if (!player.allowsPermissionLevel(1)) {
                cheatClicked(0);
                return false;
            }
            return true;
        }
        return false;
    }
    
    private static Rectangle calculateRect(ContainerGui overlayedGui) {
        Window res = REIRenderHelper.getResolution();
        int startX = (((IMixinContainerGui) overlayedGui).getGuiLeft() + ((IMixinContainerGui) overlayedGui).getXSize()) + 10;
        int width = res.getScaledWidth() - startX;
        return new Rectangle(startX, 0, width, res.getScaledHeight());
    }
    
    protected void resize() {
        Window res = REIRenderHelper.getResolution();
        
        if (overlayedGui != MinecraftClient.getInstance().currentGui) {
            if (MinecraftClient.getInstance().currentGui instanceof ContainerGui) {
                overlayedGui = (ContainerGui) MinecraftClient.getInstance().currentGui;
            } else {
                needsResize = true;
                return;
            }
        }
        oldGuiLeft = ((IMixinContainerGui) overlayedGui).getGuiLeft();
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
        searchBox = new TextBox(getSearchBoxArea());
        searchBox.setText(savedText);
        controls.add(searchBox);
        buttonCheating = new Button(5, 5, 45, 20, getCheatModeText());
        buttonCheating.onClick = this::cheatClicked;
        buttonConfig = new Button(5, 28, 45, 20, I18n.translate("text.rei.config"));
        buttonConfig.onClick = i -> {
            MinecraftClient.getInstance().openGui(new ConfigGui(overlayedGui));
            return true;
        };
        controls.add(buttonConfig);
        controls.add(buttonCheating);
        calculateSlots();
        updateView();
        fillSlots();
        controls.addAll(displaySlots);
    }
    
    private Rectangle getSearchBoxArea() {
        int ch = ((IMixinContainerGui) overlayedGui).getContainerHeight(), cw = ((IMixinContainerGui) overlayedGui).getContainerWidth();
        if (Core.centreSearchBox) {
            if (ch + 4 + 18 > MinecraftClient.getInstance().window.getScaledHeight()) //Will be out of bounds
                return new Rectangle(overlayedGui.width / 2 - cw / 2, rect.height + 100, cw, 18);
            return new Rectangle(overlayedGui.width / 2 - cw / 2, rect.height - 31, cw, 18);
        }
        return new Rectangle(rect.x, rect.height - 31, rect.width - 4, 18);
    }
    
    private void fillSlots() {
        page = MathHelper.clamp(page, 0, MathHelper.ceil(view.size() / displaySlots.size()));
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
        Window res = REIRenderHelper.getResolution();
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
            if (REIRecipeManager.instance().canAddSlot(MinecraftClient.getInstance().currentGui.getClass(), slot.rect))
                displaySlots.add(slot);
            xOffset += 18;
            currentX++;
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
        if (needsResize == true || oldGuiLeft != ((IMixinContainerGui) overlayedGui).getGuiLeft())
            resize();
        GlStateManager.pushMatrix();
        updateButtons();
        controls.forEach(Control::draw);
        String header = String.format("%s/%s", page + 1, MathHelper.ceil(view.size() / displaySlots.size()) + 1);
        MinecraftClient.getInstance().fontRenderer.drawWithShadow(header, rect.x + (rect.width / 2) - (MinecraftClient.getInstance().fontRenderer.getStringWidth(header) / 2), rect.y + 10, -1);
        GlStateManager.popMatrix();
    }
    
    private void updateButtons() {
        buttonLeft.setEnabled(MathHelper.ceil(view.size() / displaySlots.size()) > 1);
        buttonRight.setEnabled(MathHelper.ceil(view.size() / displaySlots.size()) > 1);
    }
    
    
    public boolean btnRightClicked(int button) {
        if (button == 0) {
            page++;
            if (page > MathHelper.ceil(view.size() / displaySlots.size()))
                page = 0;
            fillSlots();
            return true;
        }
        return false;
    }
    
    public boolean btnLeftClicked(int button) {
        if (button == 0) {
            page--;
            if (page < 0)
                page = MathHelper.ceil(view.size() / displaySlots.size());
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
            TextComponent cheat = new TranslatableTextComponent("text.rei.cheat", new Object[]{null});
            return cheat.getFormattedText();
        }
        TextComponent noCheat = new TranslatableTextComponent("text.rei.nocheat", new Object[]{null});
        return noCheat.getFormattedText();
    }
    
    protected void updateView() {
        String searchText = searchBox.getText();
        view.clear();
        List<ItemStack> stacks = new ArrayList<>();
        if (ClientListener.stackList == null && !Registry.ITEM.isEmpty())
            Core.getListeners(ClientListener.class).forEach(ClientListener::onDoneLoading);
        if (ClientListener.stackList != null) {
            List<ItemStack> stackList = new LinkedList<>(ClientListener.stackList);
            List<ItemGroup> itemGroups = new LinkedList<>(Arrays.asList(ItemGroup.GROUPS));
            FabricCreativeGuiComponents.COMMON_GROUPS.forEach(itemGroups::add);
            itemGroups.add(null);
            if (Core.config.itemListOrdering != REIItemListOrdering.REGISTRY)
                Collections.sort(stackList, (itemStack, t1) -> {
                    switch (Core.config.itemListOrdering) {
                        case NAME:
                            return itemStack.getDisplayName().getFormattedText().compareToIgnoreCase(t1.getDisplayName().getFormattedText());
                        case ITEM_GROUPS:
                            return itemGroups.indexOf(itemStack.getItem().getItemGroup()) - itemGroups.indexOf(t1.getItem().getItemGroup());
                    }
                    return 0;
                });
            if (!Core.config.isAscending)
                Collections.reverse(stackList);
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
                stackList.stream().filter(itemStack -> filterItem(itemStack, arguments)).forEachOrdered(stacks::add);
            });
        }
        view.addAll(stacks.stream().distinct().collect(Collectors.toList()));
        page = 0;
        fillSlots();
    }
    
    private boolean filterItem(ItemStack itemStack, List<SearchArgument> arguments) {
        String mod = getMod(itemStack);
        List<String> toolTipsList = REIRenderHelper.getOverlayedGui().getStackTooltip(itemStack);
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
            Identifier location = Registry.ITEM.getId(stack.getItem());
            return location.getNamespace();
        }
        return "";
    }
    
}
