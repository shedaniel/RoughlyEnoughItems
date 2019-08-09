package me.shedaniel.rei.gui.renderers;

import com.google.common.collect.Lists;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.Renderer;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.EntryListOverlay;
import me.shedaniel.rei.gui.widget.QueuedTooltip;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;

public abstract class FluidRenderer extends Renderer {
    public boolean drawTooltip = false;
    
    @Override
    public void render(int x, int y, double mouseX, double mouseY, float delta) {
        int l = x - 8, i1 = y - 6;
        // TODO: Render Fluid
        if (drawTooltip && mouseX >= x - 8 && mouseX <= x + 8 && mouseY >= y - 6 && mouseY <= y + 10)
            queueTooltip(getFluid(), delta);
        this.drawTooltip = false;
    }
    
    protected void queueTooltip(Fluid fluid, float delta) {
        ScreenHelper.getLastOverlay().addTooltip(QueuedTooltip.create(getTooltip(fluid)));
    }
    
    private List<String> getTooltip(Fluid fluid) {
        List<String> toolTip = Lists.newArrayList(EntryListOverlay.tryGetFluidName(fluid));
        if (RoughlyEnoughItemsCore.getConfigManager().getConfig().shouldAppendModNames()) {
            final String modString = ClientHelper.getInstance().getFormattedModFromIdentifier(Registry.FLUID.getId(fluid));
            toolTip.addAll(getExtraToolTips(fluid));
            boolean alreadyHasMod = false;
            for (String s : toolTip)
                if (s.equalsIgnoreCase(modString)) {
                    alreadyHasMod = true;
                    break;
                }
            if (!alreadyHasMod)
                toolTip.add(modString);
        }
        return toolTip;
    }
    
    protected List<String> getExtraToolTips(Fluid stack) {
        return Collections.emptyList();
    }
    
    public abstract Fluid getFluid();
}
