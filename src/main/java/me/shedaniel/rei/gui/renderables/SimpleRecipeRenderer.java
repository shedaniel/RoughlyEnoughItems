/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderables;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.Renderable;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimpleRecipeRenderer extends RecipeRenderer {
    
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<ItemStackRenderer> inputRenderer;
    private ItemStackRenderer outputRenderer;
    
    public SimpleRecipeRenderer(Supplier<List<List<ItemStack>>> input, Supplier<List<ItemStack>> output) {
        List<Pair<List<ItemStack>, AtomicInteger>> newList = Lists.newArrayList();
        List<Pair<List<ItemStack>, Integer>> a = input.get().stream().map(stacks -> new Pair<>(stacks, stacks.stream().map(ItemStack::getAmount).max(Integer::compareTo).orElse(1))).collect(Collectors.toList());
        for(Pair<List<ItemStack>, Integer> pair : a) {
            Optional<Pair<List<ItemStack>, AtomicInteger>> any = newList.stream().filter(pairr -> pair.getLeft().equals(pairr.getLeft())).findAny();
            if (any.isPresent()) {
                any.get().getRight().addAndGet(pair.getRight());
            } else
                newList.add(new Pair<>(pair.getLeft(), new AtomicInteger(pair.getRight())));
        }
        List<List<ItemStack>> b = Lists.newArrayList();
        for(Pair<List<ItemStack>, AtomicInteger> pair : newList)
            b.add(pair.getLeft().stream().map(stack -> {
                ItemStack s = stack.copy();
                s.setAmount(pair.getRight().get());
                return s;
            }).collect(Collectors.toList()));
        this.inputRenderer = b.stream().filter(stacks -> !stacks.isEmpty()).map(stacks -> Renderable.fromItemStacks(stacks)).collect(Collectors.toList());
        this.outputRenderer = Renderable.fromItemStacks(output.get().stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList()));
    }
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int xx = x + 5, yy = y + 5;
        int j = 0;
        int itemsPerLine = getItemsPerLine();
        for(ItemStackRenderer itemStackRenderer : inputRenderer) {
            itemStackRenderer.setBlitOffset(getBlitOffset() + 50);
            itemStackRenderer.drawTooltip = MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen;
            itemStackRenderer.render(xx + 8, yy + 6, mouseX, mouseY, delta);
            xx += 18;
            j++;
            if (j >= getItemsPerLine() - 3) {
                yy += 18;
                xx = x + 5;
                j = 0;
            }
        }
        xx = x + 5 + 18 * (getItemsPerLine() - 3);
        yy = y + getHeight() / 2 - 8;
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        blit(xx, yy, 0, 28, 36, 18);
        xx += 36;
        outputRenderer.setBlitOffset(getBlitOffset() + 50);
        outputRenderer.drawTooltip = MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen;
        outputRenderer.render(xx + 8, yy + 6, mouseX, mouseY, delta);
    }
    
    @Override
    public int getHeight() {
        return 10 + getItemsHeight() * 18;
    }
    
    public int getItemsHeight() {
        return MathHelper.ceil(((float) inputRenderer.size()) / (getItemsPerLine() - 3));
    }
    
    public int getItemsPerLine() {
        return MathHelper.floor((getWidth() - 10f) / 18f);
    }
    
}
