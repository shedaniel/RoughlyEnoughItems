package me.shedaniel.rei.gui.widget;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.client.ClientHelper;
import me.shedaniel.rei.gui.ContainerGuiOverlay;
import me.shedaniel.rei.listeners.ClientLoaded;
import me.shedaniel.rei.listeners.IMixinContainerGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ItemListOverlay extends Drawable implements IWidget {
    
    private ContainerGuiOverlay containerGuiOverlay;
    private IMixinContainerGui containerGui;
    private List<IWidget> widgets = new ArrayList<>();
    private int width, height, page;
    private Rectangle rectangle;
    
    public ItemListOverlay(ContainerGuiOverlay containerGuiOverlay, IMixinContainerGui containerGui, int page) {
        this.containerGuiOverlay = containerGuiOverlay;
        this.containerGui = containerGui;
        this.width = 0;
        this.height = 0;
        this.page = page;
    }
    
    public int getTotalSlotsPerPage() {
        return width * height;
    }
    
    @Override
    public void draw(int int_1, int int_2, float float_1) {
        widgets.forEach(widget -> widget.draw(int_1, int_2, float_1));
    }
    
    public void updateList(int page) {
        updateList(rectangle, page);
    }
    
    public void updateList(Rectangle rect, int page) {
        this.rectangle = rect;
        if (ClientHelper.getItemList().isEmpty())
            RoughlyEnoughItemsCore.getListeners(ClientLoaded.class).forEach(ClientLoaded::clientLoaded);
        List<ItemStack> stacks = ClientHelper.getItemList();
        this.widgets.clear();
        this.page = page;
        calculateListSize(rect);
        double startX = rect.getCenterX() - width * 9;
        double startY = rect.getCenterY() - height * 9;
        for(int i = 0; i < getTotalSlotsPerPage(); i++) {
            int j = i + page * getTotalSlotsPerPage();
            if (j >= stacks.size())
                break;
            widgets.add(new ItemSlotWidget((int) (startX + (i % width) * 18), (int) (startY + MathHelper.floor(i / width) * 18),
                    stacks.get(j), false, true, containerGui) {
                @Override
                protected List<String> getTooltip(ItemStack itemStack) {
                    if (!ClientHelper.isCheating() || MinecraftClient.getInstance().player.inventory.getCursorStack().isEmpty())
                        return super.getTooltip(itemStack);
                    List<String> list = Lists.newArrayList();
                    list.add(I18n.translate("text.rei.delete_items"));
                    return list;
                }
                
                @Override
                public boolean onMouseClick(int button, double mouseX, double mouseY) {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (getBounds().contains(mouseX, mouseY)) {
                        if (ClientHelper.isCheating() && !player.inventory.getCursorStack().isEmpty()) {
                            ClientHelper.sendDeletePacket();
                            return true;
                        }
                        if (!player.inventory.getCursorStack().isEmpty())
                            return false;
                        if (ClientHelper.isCheating()) {
                            if (getCurrentStack() != null && !getCurrentStack().isEmpty()) {
                                ItemStack cheatedStack = getCurrentStack().copy();
                                cheatedStack.setAmount(button == 0 ? 1 : button == 1 ? cheatedStack.getMaxAmount() : cheatedStack.getAmount());
                                return ClientHelper.tryCheatingStack(cheatedStack);
                            }
                        } else {
                            if (button == 0)
                                return ClientHelper.executeRecipeKeyBind();
                            else if (button == 1)
                                return ClientHelper.executeUsageKeyBind();
                        }
                    }
                    return false;
                }
            });
        }
    }
    
    private void calculateListSize(Rectangle rect) {
        int xOffset = 0, yOffset = 0;
        this.width = 0;
        this.height = 0;
        while (true) {
            xOffset += 18;
            if (height == 0)
                width++;
            if (xOffset + 19 > rect.width) {
                xOffset = 0;
                yOffset += 18;
                height++;
            }
            if (yOffset + 19 > rect.height)
                break;
        }
    }
    
    @Override
    public List<IWidget> getListeners() {
        return widgets;
    }
    
}
