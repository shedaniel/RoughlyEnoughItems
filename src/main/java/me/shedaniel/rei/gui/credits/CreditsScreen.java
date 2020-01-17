/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui.credits;

import com.google.common.collect.Lists;
import me.shedaniel.rei.gui.credits.CreditsEntryListWidget.CreditsItem;
import me.shedaniel.rei.impl.ScreenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class CreditsScreen extends Screen {
    
    private Screen parent;
    private AbstractPressableButtonWidget buttonDone;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(Screen parent) {
        super(new LiteralText(""));
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            this.minecraft.openScreen(parent);
            if (parent instanceof AbstractContainerScreen)
                ScreenHelper.getLastOverlay().init();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void init() {
        children.add(entryListWidget = new CreditsEntryListWidget(minecraft, width, height, 32, height - 32));
        entryListWidget.creditsClearEntries();
        List<String> translators = Lists.newArrayList();
        FabricLoader.getInstance().getModContainer("roughlyenoughitems").ifPresent(rei -> {
            try {
                if (rei.getMetadata().containsCustomValue("rei:translators")) {
                    CustomValue.CvObject jsonObject = rei.getMetadata().getCustomValue("rei:translators").getAsObject();
                    jsonObject.forEach(entry -> {
                        CustomValue value = entry.getValue();
                        String behind = value.getType() == CustomValue.CvType.ARRAY ? Lists.newArrayList(value.getAsArray().iterator()).stream().map(json -> json.getAsString()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", ")) : value.getAsString();
                        translators.add(String.format("  %s - %s", entry.getKey(), behind));
                    });
                }
                translators.sort(String::compareToIgnoreCase);
            } catch (Exception e) {
                translators.clear();
                translators.add("Failed to get translators: " + e.toString());
                for (StackTraceElement traceElement : e.getStackTrace())
                    translators.add("  at " + traceElement);
                e.printStackTrace();
            }
        });
        List<String> actualTranslators = Lists.newArrayList();
        int i = width - 80 - 6;
        translators.forEach(s -> font.wrapStringToWidthAsList(s, i).forEach(actualTranslators::add));
        for (String line : I18n.translate("text.rei.credit.text", FabricLoader.getInstance().getModContainer("roughlyenoughitems").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("Unknown"), String.join("\n", actualTranslators)).split("\n"))
            entryListWidget.creditsAddEntry(new CreditsItem(new LiteralText(line)));
        entryListWidget.creditsAddEntry(new CreditsItem(new LiteralText("")));
        children.add(buttonDone = new AbstractPressableButtonWidget(width / 2 - 100, height - 26, 200, 20, I18n.translate("gui.done")) {
            @Override
            public void onPress() {
                CreditsScreen.this.minecraft.openScreen(parent);
                if (parent instanceof AbstractContainerScreen)
                    ScreenHelper.getLastOverlay().init();
            }
        });
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (entryListWidget.mouseScrolled(double_1, double_2, double_3))
            return true;
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        this.renderDirtBackground(0);
        this.entryListWidget.render(int_1, int_2, float_1);
        this.drawCenteredString(this.font, I18n.translate("text.rei.credits"), this.width / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
        buttonDone.render(int_1, int_2, float_1);
    }
    
}
