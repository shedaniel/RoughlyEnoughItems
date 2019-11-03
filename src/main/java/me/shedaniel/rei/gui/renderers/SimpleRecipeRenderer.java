/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.gui.VillagerRecipeViewingScreen;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimpleRecipeRenderer extends RecipeRenderer {
    
    private static final Comparator<EntryStack> ENTRY_COMPARATOR = (o1, o2) -> {
        if (o1.getType() == EntryStack.Type.FLUID) {
            if (o2.getType() == EntryStack.Type.ITEM)
                return -1;
            return o1.getFluid().hashCode() - o2.getFluid().hashCode();
        } else if (o2.getType() == EntryStack.Type.FLUID) {
            if (o1.getType() == EntryStack.Type.ITEM)
                return 1;
            return o1.getFluid().hashCode() - o2.getFluid().hashCode();
        }
        ItemStack i1 = o1.getItemStack();
        ItemStack i2 = o2.getItemStack();
        if (i1.getItem() == i2.getItem()) {
            if (i1.getCount() != i2.getCount())
                return i1.getCount() - i2.getCount();
            int compare = Boolean.compare(i1.hasTag(), i2.hasTag());
            if (compare != 0)
                return compare;
            if (i1.getTag().getSize() != i2.getTag().getSize())
                return i1.getTag().getSize() - i2.getTag().getSize();
            return i1.getTag().hashCode() - i2.getTag().hashCode();
        }
        return i1.getItem().hashCode() - i2.getItem().hashCode();
    };
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<Renderer> inputRenderer;
    private Renderer outputRenderer;
    private QueuedTooltip lastTooltip;
    
    @Deprecated
    public SimpleRecipeRenderer(Supplier<List<List<ItemStack>>> input, Supplier<List<ItemStack>> output) {
        this(() -> (List<List<EntryStack>>) input.get().stream().map(s -> s.stream().map(EntryStack::create).collect(Collectors.toList())).collect(Collectors.toList()),
                () -> output.get().stream().map(EntryStack::create).collect(Collectors.toList()), 0);
    }
    
    public SimpleRecipeRenderer(Supplier<List<List<EntryStack>>> input, Supplier<List<EntryStack>> output, int forDifferentConstructor) {
        List<Pair<List<EntryStack>, AtomicInteger>> newList = Lists.newArrayList();
        List<Pair<List<EntryStack>, Integer>> a = input.get().stream().map(stacks -> new Pair<>(stacks, stacks.stream().map(EntryStack::getAmount).max(Integer::compareTo).orElse(1))).collect(Collectors.toList());
        for (Pair<List<EntryStack>, Integer> pair : a) {
            Optional<Pair<List<EntryStack>, AtomicInteger>> any = newList.stream().filter(pairr -> equalsList(pair.getLeft(), pairr.getLeft())).findAny();
            if (any.isPresent()) {
                any.get().getRight().addAndGet(pair.getRight());
            } else
                newList.add(new Pair<>(pair.getLeft(), new AtomicInteger(pair.getRight())));
        }
        List<List<EntryStack>> b = Lists.newArrayList();
        for (Pair<List<EntryStack>, AtomicInteger> pair : newList)
            b.add(pair.getLeft().stream().map(stack -> {
                EntryStack s = stack.copy();
                s.setAmount(pair.getRight().get());
                return s;
            }).collect(Collectors.toList()));
        this.inputRenderer = b.stream().filter(stacks -> !stacks.isEmpty()).map(stacks -> fromEntries(stacks)).collect(Collectors.toList());
        this.outputRenderer = fromEntries(output.get().stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList()));
    }
    
    @Deprecated
    private static Renderer fromEntries(List<EntryStack> entries) {
        boolean isItem = true;
        for (EntryStack entry : entries) {
            if (entry.getType() != EntryStack.Type.ITEM)
                isItem = false;
        }
        if (isItem)
            return Renderer.fromItemStacks(entries.stream().map(EntryStack::getItemStack).collect(Collectors.toList()));
        boolean isFluid = true;
        for (EntryStack entry : entries) {
            if (entry.getType() != EntryStack.Type.FLUID)
                isFluid = false;
        }
        
        if (isFluid) {
            List<Fluid> fluids = entries.stream().map(EntryStack::getFluid).collect(Collectors.toList());
            if (!fluids.isEmpty())
                return Renderer.fromFluid(fluids.get(0));
        }
        return Renderer.empty();
    }
    
    public static boolean equalsList(List<EntryStack> list_1, List<EntryStack> list_2) {
        List<EntryStack> stacks_1 = list_1.stream().distinct().sorted(ENTRY_COMPARATOR).collect(Collectors.toList());
        List<EntryStack> stacks_2 = list_2.stream().distinct().sorted(ENTRY_COMPARATOR).collect(Collectors.toList());
        if (stacks_1.equals(stacks_2))
            return true;
        if (stacks_1.size() != stacks_2.size())
            return false;
        for (int i = 0; i < stacks_1.size(); i++)
            if (!stacks_1.get(i).equalsIgnoreTagsAndAmount(stacks_2.get(i)))
                return false;
        return true;
    }
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        lastTooltip = null;
        int xx = x + 4, yy = y + 2;
        int j = 0;
        int itemsPerLine = getItemsPerLine();
        for (Renderer itemStackRenderer : inputRenderer) {
            itemStackRenderer.setBlitOffset(getBlitOffset() + 50);
            if (lastTooltip == null && MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen && mouseX >= xx && mouseX <= xx + 16 && mouseY >= yy && mouseY <= yy + 16) {
                lastTooltip = itemStackRenderer.getQueuedTooltip(delta);
            }
            itemStackRenderer.render(xx + 8, yy + 6, mouseX, mouseY, delta);
            xx += 18;
            j++;
            if (j >= getItemsPerLine() - 2) {
                yy += 18;
                xx = x + 5;
                j = 0;
            }
        }
        xx = x + 5 + 18 * (getItemsPerLine() - 2);
        yy = y + getHeight() / 2 - 8;
        GuiLighting.disable();
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        blit(xx, yy, 0, 28, 18, 18);
        xx += 18;
        outputRenderer.setBlitOffset(getBlitOffset() + 50);
        outputRenderer.render(xx + 8, yy + 6, mouseX, mouseY, delta);
        if (lastTooltip == null && MinecraftClient.getInstance().currentScreen instanceof VillagerRecipeViewingScreen && mouseX >= xx && mouseX <= xx + 16 && mouseY >= yy && mouseY <= yy + 16) {
            lastTooltip = outputRenderer.getQueuedTooltip(delta);
        }
    }
    
    @Nullable
    @Override
    public QueuedTooltip getQueuedTooltip(float delta) {
        return lastTooltip;
    }
    
    @Override
    public int getHeight() {
        return 4 + getItemsHeight() * 18;
    }
    
    public int getItemsHeight() {
        return MathHelper.ceil(((float) inputRenderer.size()) / (getItemsPerLine() - 2));
    }
    
    public int getItemsPerLine() {
        return MathHelper.floor((getWidth() - 4f) / 18f);
    }
    
}
