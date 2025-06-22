package me.shedaniel.rei.impl.client.gui.widget.search;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;

import java.util.LinkedList;

@ApiStatus.Internal
public class OverlayCalculatorField extends TextFieldWidget {
    private static final Style CALC_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(ChatFormatting.RED);
    private static final int MAX_HISTORY = 5;
    private final LinkedList<HistoryEntry> history = new LinkedList<>();
    private String lastCalcInput = "";
    private double calcOutput = -1;
    private boolean showCalcResult = false;
    public OverlayCalculatorField(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMaxLength(100);
        setFormatter(this::formatCalculator);
        super.setResponder(this::updateCalculator);
    }

    private void updateCalculator(String input) {
        showCalcResult = false;
        input = input.trim();

        if (input.isEmpty()) {
            calcOutput = -1;
            lastCalcInput = input;
            return;
        }

        if (!input.equals(lastCalcInput)) {
            try {
                calcOutput = SearchCalculatorUtil.evaluate(input);
                showCalcResult = true;
            } catch (Exception e) {
                calcOutput = -1;
                showCalcResult = false;
            }
            lastCalcInput = input;
        } else if (calcOutput != -1) {
            showCalcResult = true;
        }
    }

    private FormattedCharSequence formatCalculator(TextFieldWidget widget, String text, int index) {
        Style style;
        if (calcOutput == -1 && !text.trim().isEmpty()) {
            style = ERROR_STYLE;
        } else if (showCalcResult) {
            style = CALC_STYLE;
        } else {
            style = Style.EMPTY;
        }

        return FormattedCharSequence.forward(text, style);
    }

    public void renderCalculatorWithHistory(GuiGraphics graphics) {
        if (history.isEmpty() && (!showCalcResult || calcOutput == -1)) return;

        try {
            int maxHistoryLines = Math.min(history.size(), MAX_HISTORY);
            int totalHeight = 0;

            if (maxHistoryLines > 0) {
                totalHeight += (font.lineHeight + 2) * maxHistoryLines + 4;
            }

            if (showCalcResult && calcOutput != -1) {
                totalHeight += font.lineHeight + 4;
            }

            if (totalHeight == 0) return;

            Rectangle bounds = getBounds();
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            int x = bounds.getX();
            int y = bounds.getY() - totalHeight - 2;
            int width = bounds.getWidth();

            x = Math.max(4, Math.min(x, screenWidth - width - 4));
            y = Math.max(4, Math.min(y, screenHeight - totalHeight - 4));

            int backgroundColor = 0xF0100010;
            int borderColor = 0xFF404040;

            graphics.fill(x, y, x + width, y + totalHeight, backgroundColor);

            graphics.fill(x - 1, y - 1, x + width + 1, y - 1, borderColor);
            graphics.fill(x - 1, y + totalHeight, x + width + 1, y + totalHeight + 1, borderColor);
            graphics.fill(x - 1, y, x, y + totalHeight, borderColor);
            graphics.fill(x + width, y, x + width + 1, y + totalHeight, borderColor);

            graphics.pose().pushPose();
            graphics.pose().translate(0.0D, 0.0D, 450.0D);

            int currentY = y + 2;

            for (int i = 0; i < maxHistoryLines; i++) {
                HistoryEntry entry = history.get(i);

                float ageRatio = (float) i / (float) Math.max(1, maxHistoryLines - 1);
                TextColor color = interpolateColor(ageRatio);

                String historyText = entry.equation + " = " + entry.result;
                Component historyComponent = Component.literal(historyText).withStyle(Style.EMPTY.withColor(color));

                graphics.drawString(font, historyComponent, x + 4, currentY, -1);
                currentY += font.lineHeight + 2;
            }

            if (showCalcResult && calcOutput != -1) {
                String currentText = getText() + " = " + formatCalculatorOutput(calcOutput);
                Component currentComponent = Component.literal(currentText).withStyle(ChatFormatting.GREEN);

                graphics.drawString(font, currentComponent, x + 4, currentY, -1);
            }

            graphics.pose().popPose();

        } catch (Exception e) {

        }
    }

    private TextColor interpolateColor(float ratio) {
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));

        if (ratio <= 0.5f) {
            float localRatio = ratio * 2.0f;

            int red = 255;
            int green = (int) (140 + (210 - 140) * localRatio);
            int blue = (int) (140 + (120 - 140) * localRatio);

            return TextColor.fromRgb((red << 16) | (green << 8) | blue);
        } else {
            float localRatio = (ratio - 0.5f) * 2.0f;

            int red = (int) (255 - (255 - 140) * localRatio);
            int green = (int) (210 + (255 - 210) * localRatio);
            int blue = (int) (120 + (140 - 120) * localRatio);

            return TextColor.fromRgb((red << 16) | (green << 8) | blue);
        }
    }

    private String formatCalculatorOutput(double output) {
        if (Double.isNaN(output) || Double.isInfinite(output)) {
            return "Error";
        }

        if (output == Math.floor(output) && !Double.isInfinite(output)) {
            if (Math.abs(output) >= 1000) {
                return String.format("%,d", (long) output);
            } else {
                return String.valueOf((long) output);
            }
        } else {
            double rounded = Math.round(output * 100.0) / 100.0;
            if (Math.abs(rounded) >= 1000) {
                return String.format("%,.2f", rounded);
            } else {
                return String.valueOf(rounded);
            }
        }
    }

    public String getCalculatorResult() {
        if (calcOutput == -1) {
            return getText();
        }
        return formatCalculatorOutput(calcOutput);
    }

    private void addToHistory(String equation, String result) {
        if (equation.trim().isEmpty() || result.equals("Error")) {
            return;
        }

        history.addLast(new HistoryEntry(equation, result));

        while (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isVisible() && this.isFocused()) {
            if (keyCode == 257 || keyCode == 335) {
                if (showCalcResult && calcOutput != -1) {
                    String equation = getText().trim();
                    String result = getCalculatorResult();
                    addToHistory(equation, result);

                    Minecraft.getInstance().keyboardHandler.setClipboard(result);

                    setText("");
                }

                setFocused(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!isFocused() && getText().isEmpty()) {
            setSuggestion("put numbers into me...");
        } else {
            setSuggestion(null);
        }

        super.render(graphics, mouseX, mouseY, delta);
        renderCalculatorWithHistory(graphics);
    }

    @Override
    public void renderBorder(GuiGraphics graphics) {
        int borderColor;
        if (showCalcResult && calcOutput != -1) {
            borderColor = 0xff00ff00;
        } else if (!getText().trim().isEmpty() && calcOutput == -1) {
            borderColor = 0xffff5555;
        } else {
            borderColor = containsMouse(mouse()) || isFocused() ? 0xffffffff : 0xffa0a0a0;
        }

        Rectangle bounds = getBounds();
        graphics.fill(bounds.x - 1, bounds.y - 1, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1, 0xff000000);
        graphics.fill(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, borderColor);
        graphics.fill(bounds.x + 1, bounds.y + 1, bounds.x + bounds.width - 1, bounds.y + bounds.height - 1, 0xff000000);
    }

    private static class HistoryEntry {
        final String equation;
        final String result;

        HistoryEntry(String equation, String result) {
            this.equation = equation;
            this.result = result;
        }
    }
}