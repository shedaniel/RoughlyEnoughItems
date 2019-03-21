package me.shedaniel.rei.gui.credits;

import me.shedaniel.rei.client.ScreenHelper;
import net.minecraft.client.gui.ContainerScreen;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.StringTextComponent;

public class CreditsScreen extends Screen {
    
    private Screen parent;
    private CreditsEntryListWidget entryListWidget;
    
    public CreditsScreen(Screen parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.doesEscapeKeyClose()) {
            this.client.openScreen(parent);
            if (parent instanceof ContainerScreen)
                ScreenHelper.getLastOverlay().onInitialized();
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void onInitialized() {
        listeners.add(entryListWidget = new CreditsEntryListWidget(client, screenWidth, screenHeight, 32, screenHeight - 32, 12));
        entryListWidget.creditsClearEntries();
        for(String line : I18n.translate("text.rei.credit.text").split("\n"))
            entryListWidget.creditsAddEntry(new CreditsEntry(new StringTextComponent(line)));
        entryListWidget.creditsAddEntry(new CreditsEntry(new StringTextComponent("")));
        addButton(new ButtonWidget(screenWidth / 2 - 100, screenHeight - 26, I18n.translate("gui.done")) {
            @Override
            public void onPressed() {
                CreditsScreen.this.client.openScreen(parent);
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
        this.drawTextureBackground(0);
        this.entryListWidget.render(int_1, int_2, float_1);
        this.drawStringCentered(this.fontRenderer, I18n.translate("text.rei.credits"), this.screenWidth / 2, 16, 16777215);
        super.render(int_1, int_2, float_1);
    }
    
}
