package me.shedaniel.rei.impl.client.gui.widget.favorites.panel;

import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ProgressValueAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.impl.client.gui.widget.favorites.FavoritesListWidget;
import me.shedaniel.rei.impl.client.gui.widget.search.CalculatorDisplayUtils;
import me.shedaniel.rei.impl.client.gui.widget.search.OverlaySearchField;
import me.shedaniel.rei.impl.client.gui.widget.search.TextCalculator;
import me.shedaniel.rei.impl.client.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CalculatorPanel extends FavoritesPanel {
    private final List<Row> rows = new ArrayList<>();
    private final OverlaySearchField textField = new OverlaySearchField(0, 0, 0, 0, null);
    private final NumberAnimator<Integer> previewHeight = ValueAnimator.ofDouble().asInt();
    private CalculatorDisplayUtils utils = new CalculatorDisplayUtils(7);
    private double eval;
    
    public CalculatorPanel(FavoritesListWidget parent) {
        super(parent);
        this.textField.setHasBorder(false);
        this.textField.isMain = false;
        this.textField.setAutoPrefixEquals(true);
        this.textField.setSuggestion(Component.empty());
        this.textField.setResponder(text -> {
            if (!text.isBlank() && TextCalculator.isValid('=' + text)) {
                this.eval = new TextCalculator('=' + text).eval();
                this.previewHeight.setTo(Minecraft.getInstance().font.lineHeight + 1, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            } else {
                this.previewHeight.setTo(0, ConfigObject.getInstance().isReducedMotion() ? 0 : 400);
            }
        });
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.previewHeight.update(delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.innerBounds.setBounds(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 8);
        
        int previewLength = Math.max(7, (this.innerBounds.width - 4) / font.width("="));
        if (previewLength != this.utils.maxLength()) this.utils = new CalculatorDisplayUtils(previewLength);
        
        int buttonColor = 0xFFFFFF | (Math.round(0x34 * Math.min((float) expendState.progress() * 2, 1)) << 24);
        graphics.fillGradient(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), buttonColor, buttonColor);
        
        if (expendState.progress() > 0.05f) {
            Rectangle availableFullBounds = parent.favoritesBounds.clone();
            availableFullBounds.height -= 20;
            Rectangle scissorBounds = new Rectangle(innerBounds.x - 1, innerBounds.y - 1, innerBounds.width + 2, innerBounds.height + 2).intersection(availableFullBounds);
            graphics.enableScissor(scissorBounds.x, scissorBounds.y, scissorBounds.getMaxX(), scissorBounds.getMaxY());
            graphics.pose().pushPose();
            
            // Text Field Background
            Rectangle textFieldBorder = new Rectangle(innerBounds.x, innerBounds.getMaxY() - 9 - 5, innerBounds.width, 9 + 5);
            boolean textFieldFocused = textFieldBorder.contains(mouseX, mouseY) || this.textField.isFocused();
            int borderColor = ColorUtils.withAlpha(textFieldFocused ? 0x77FFFFFF : 0x77A0A0A0, expendState.progress());
            int backgroundColor = ColorUtils.withAlpha(textFieldFocused ? 0xFF000000 : 0xC0000000, expendState.progress());
            graphics.fillGradient(textFieldBorder.x - 1, textFieldBorder.y - 1, textFieldBorder.getMaxX() + 1, textFieldBorder.getMaxY() + 1, borderColor, borderColor);
            graphics.fillGradient(textFieldBorder.x, textFieldBorder.y, textFieldBorder.getMaxX(), textFieldBorder.getMaxY(), backgroundColor, backgroundColor);
            int leftPad = font.width("=");
            
            // Equals
            graphics.pose().pushPose();
            graphics.pose().translate(-0.5, 0, 0);
            graphics.drawString(font, "=", innerBounds.x + 2, innerBounds.getMaxY() - 9 - 2, ColorUtils.withAlpha(0xFFAAAAAA, expendState.progress()));
            graphics.pose().popPose();
            
            // Text Field
            this.textField.getBounds().setBounds(innerBounds.x + 2 + leftPad, innerBounds.getMaxY() - 9 - 2, innerBounds.width - 4 - leftPad, 9);
            this.textField.render(graphics, mouseX, mouseY, delta);
            
            // Preview
            if (this.previewHeight.value() > 0) {
                int previewTop = textFieldBorder.y - 1 - this.previewHeight.value();
                graphics.enableScissor(textFieldBorder.x - 1, previewTop, textFieldBorder.getMaxX() + 1, textFieldBorder.y - 1);
                graphics.fillGradient(textFieldBorder.x - 1, previewTop, textFieldBorder.getMaxX() + 1, textFieldBorder.y - 1, borderColor, borderColor);
                graphics.drawString(font, this.utils.fmt(this.eval), textFieldBorder.x + 2, previewTop + 2, 0xFF000000, false);
                graphics.disableScissor();
            }
            
            graphics.pose().popPose();
            graphics.disableScissor();
        } else {
            this.textField.getBounds().setBounds(0, 0, 0, 0);
        }
    }
    
    @Override
    protected Rectangle getButtonArea() {
        return this.parent.calculatorPanelButton.getBounds();
    }
    
    @Override
    protected Rectangle getTargetArea(Rectangle fullArea) {
        return new Rectangle(
                fullArea.x + 4,
                fullArea.getMaxY() - 4 - 16 - fullArea.height * 0.4f,
                fullArea.width - 8,
                fullArea.height * 0.4f
        );
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.textField);
    }
    
    private record Row(String equation, double result, ProgressValueAnimator<Boolean> set) {
        public Row(String equation, double result) {
            this(equation, result, ValueAnimator.ofBoolean(false));
            this.set().setTo(true, ConfigObject.getInstance().isReducedMotion() ? 0 : 500);
        }
    }
}
