/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.entries;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.gui.widget.EntryWidget;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
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

public class SimpleRecipeEntry extends RecipeEntry {

    private static final Comparator<EntryStack> ENTRY_COMPARATOR = Comparator.comparingLong(EntryStack::hashCode);
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private List<EntryWidget> inputWidgets;
    private EntryWidget outputWidget;

    protected SimpleRecipeEntry(List<List<EntryStack>> input, List<EntryStack> output) {
        List<Pair<List<EntryStack>, AtomicInteger>> newList = Lists.newArrayList();
        List<Pair<List<EntryStack>, Integer>> a = CollectionUtils.map(input, stacks -> new Pair<>(stacks, stacks.stream().map(EntryStack::getAmount).max(Integer::compareTo).orElse(1)));
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
        this.inputWidgets = b.stream().filter(stacks -> !stacks.isEmpty()).map(stacks -> {
            return EntryWidget.create(0, 0).entries(stacks).noBackground().noHighlight().noTooltips();
        }).collect(Collectors.toList());
        this.outputWidget = EntryWidget.create(0, 0).entries(CollectionUtils.filter(output, stack -> !stack.isEmpty())).noBackground().noHighlight().noTooltips();
    }

    public static RecipeEntry create(Supplier<List<List<EntryStack>>> input, Supplier<List<EntryStack>> output) {
        return create(input.get(), output.get());
    }

    public static RecipeEntry create(List<List<EntryStack>> input, List<EntryStack> output) {
        return new SimpleRecipeEntry(input, output);
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
    public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
        int xx = bounds.x + 4, yy = bounds.y + 2;
        int j = 0;
        int itemsPerLine = getItemsPerLine();
        for (EntryWidget entryWidget : inputWidgets) {
            entryWidget.setZ(getZ() + 50);
            entryWidget.getBounds().setLocation(xx, yy);
            entryWidget.render(mouseX, mouseY, delta);
            xx += 18;
            j++;
            if (j >= getItemsPerLine() - 2) {
                yy += 18;
                xx = bounds.x + 4;
                j = 0;
            }
        }
        xx = bounds.x + 4 + 18 * (getItemsPerLine() - 2);
        yy = bounds.y + getHeight() / 2 - 8;
        MinecraftClient.getInstance().getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        blit(xx, yy, 0, 28, 18, 18);
        xx += 18;
        outputWidget.setZ(getZ() + 50);
        outputWidget.getBounds().setLocation(xx, yy);
        outputWidget.render(mouseX, mouseY, delta);
    }

    @Nullable
    @Override
    public QueuedTooltip getTooltip(int mouseX, int mouseY) {
        for (EntryWidget widget : inputWidgets) {
            if (widget.containsMouse(mouseX, mouseY))
                return widget.getCurrentTooltip(mouseX, mouseY);
        }
        if (outputWidget.containsMouse(mouseX, mouseY))
            return outputWidget.getCurrentTooltip(mouseX, mouseY);
        return null;
    }

    @Override
    public int getHeight() {
        return 4 + getItemsHeight() * 18;
    }

    public int getItemsHeight() {
        return MathHelper.ceil(((float) inputWidgets.size()) / (getItemsPerLine() - 2));
    }

    public int getItemsPerLine() {
        return MathHelper.floor((getWidth() - 4f) / 18f);
    }

}
