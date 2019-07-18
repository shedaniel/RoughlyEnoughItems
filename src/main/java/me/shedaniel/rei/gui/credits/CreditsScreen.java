/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.credits;

import com.google.common.collect.Lists;
import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.credits.CreditsEntryListWidget.CreditsItem;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class CreditsScreen extends GuiScreen {
    
    private GuiScreen parent;
    private GuiButton buttonDone;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(GuiScreen parent) {
        super();
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.allowCloseWithEscape()) {
            this.mc.displayGuiScreen(parent);
            if (parent instanceof GuiContainer)
                ScreenHelper.getLastOverlay().init();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void initGui() {
        children.add(entryListWidget = new CreditsEntryListWidget(mc, width, height, 32, height - 32));
        entryListWidget.creditsClearEntries();
        List<String> translators = Lists.newArrayList();
        // TODO: Get Translators
        //        FabricLoader.getInstance().getModContainer("roughlyenoughitems").ifPresent(rei -> {
        //            try {
        //                if (rei.getMetadata().containsCustomElement("rei:translators")) {
        //                    JsonObject jsonObject = rei.getMetadata().getCustomElement("rei:translators").getAsJsonObject();
        //                    for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
        //                        JsonElement value = entry.getValue();
        //                        String behind = value.isJsonArray() ? Lists.newArrayList(value.getAsJsonArray().iterator()).stream().map(json -> json.getAsString()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", ")) : value.getAsString();
        //                        translators.add(String.format("  %s - %s", entry.getKey(), behind));
        //                    }
        //                }
        //                translators.sort(String::compareToIgnoreCase);
        //            } catch (Exception e) {
        //                translators.clear();
        //                translators.add("Failed to get translators: " + e.toString());
        //                for(StackTraceElement traceElement : e.getStackTrace())
        //                    translators.add("  at " + traceElement);
        //                e.printStackTrace();
        //            }
        //        });
        List<String> actualTranslators = Lists.newArrayList();
        int i = width - 80 - 6;
        translators.forEach(s -> fontRenderer.listFormattedStringToWidth(s, i).forEach(actualTranslators::add));
        //        for(String line : I18n.format("text.rei.credit.text", FabricLoader.getInstance().getModContainer("roughlyenoughitems").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("Unknown"), String.join("\n", actualTranslators)).split("\n"))
        //            entryListWidget.creditsAddEntry(new CreditsItem(new LiteralText(line)));
        entryListWidget.creditsAddEntry(new CreditsItem(new TextComponentString("")));
        children.add(buttonDone = new GuiButton(12341, width / 2 - 100, height - 26, 200, 20, I18n.format("gui.done")) {
            @Override
            public void onClick(double daidw, double ia) {
                super.onClick(daidw, ia);
                CreditsScreen.this.mc.displayGuiScreen(parent);
                if (parent instanceof GuiContainer)
                    ScreenHelper.getLastOverlay().init();
            }
        });
    }
    
    @Override
    public boolean mouseScrolled(double double_3) {
        if (entryListWidget.mouseScrolled(double_3 * 3))
            return true;
        return super.mouseScrolled(double_3);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.drawWorldBackground(0);
        this.entryListWidget.render(int_1, int_2, float_1);
        this.drawCenteredString(this.fontRenderer, I18n.format("text.rei.credits"), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
        buttonDone.render(int_1, int_2, float_1);
    }
    
}
