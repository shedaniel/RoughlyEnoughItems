package me.shedaniel.rei.gui.credits;

import me.shedaniel.rei.client.ScreenHelper;
import me.shedaniel.rei.gui.widget.ButtonWidget;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringTextComponent;

public class CreditsScreen extends Screen {
    
    private Screen parent;
    private ButtonWidget buttonDone;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(Screen parent) {
        super(new StringTextComponent(""));
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            this.minecraft.openScreen(parent);
            if (parent instanceof ContainerScreen)
                ScreenHelper.getLastOverlay().onInitialized();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void init() {
        children.add(entryListWidget = new CreditsEntryListWidget(minecraft, width, height, 32, height - 32, 12));
        entryListWidget.creditsClearEntries();
        for(String line : I18n.translate("text.rei.credit.text").split("\n"))
            entryListWidget.creditsAddEntry(new CreditsItem(new StringTextComponent(line)));
        entryListWidget.creditsAddEntry(new CreditsItem(new StringTextComponent("")));
        children.add(buttonDone = new ButtonWidget(width / 2 - 100, height - 26, 200, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed() {
                CreditsScreen.this.minecraft.openScreen(parent);
                ScreenHelper.getLastOverlay().onInitialized();
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
    }
    
}
