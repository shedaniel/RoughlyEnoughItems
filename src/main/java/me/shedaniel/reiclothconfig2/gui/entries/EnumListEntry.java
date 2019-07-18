package me.shedaniel.reiclothconfig2.gui.entries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumListEntry<T extends Enum<?>> extends TooltipListEntry<T> {
    
    public static final Function<Enum, String> DEFAULT_NAME_PROVIDER = t -> I18n.format(t instanceof Translatable ? ((Translatable) t).getKey() : t.toString());
    private ImmutableList<T> values;
    private AtomicInteger index;
    private GuiButton buttonWidget, resetButton;
    private Consumer<T> saveConsumer;
    private Supplier<T> defaultValue;
    private List<IGuiEventListener> widgets;
    private Function<Enum, String> enumNameProvider;
    
    @Deprecated
    public EnumListEntry(String fieldName, Class<T> clazz, T value, Consumer<T> saveConsumer) {
        this(fieldName, clazz, value, "text.cloth-config.reset_value", null, saveConsumer);
    }
    
    @Deprecated
    public EnumListEntry(String fieldName, Class<T> clazz, T value, String resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer) {
        this(fieldName, clazz, value, resetButtonKey, defaultValue, saveConsumer, DEFAULT_NAME_PROVIDER);
    }
    
    @Deprecated
    public EnumListEntry(String fieldName, Class<T> clazz, T value, String resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<Enum, String> enumNameProvider) {
        this(fieldName, clazz, value, resetButtonKey, defaultValue, saveConsumer, enumNameProvider, null);
    }
    
    @Deprecated
    public EnumListEntry(String fieldName, Class<T> clazz, T value, String resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<Enum, String> enumNameProvider, Supplier<Optional<String[]>> tooltipSupplier) {
        this(fieldName, clazz, value, resetButtonKey, defaultValue, saveConsumer, enumNameProvider, tooltipSupplier, false);
    }
    
    @Deprecated
    public EnumListEntry(String fieldName, Class<T> clazz, T value, String resetButtonKey, Supplier<T> defaultValue, Consumer<T> saveConsumer, Function<Enum, String> enumNameProvider, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        T[] valuesArray = clazz.getEnumConstants();
        if (valuesArray != null)
            this.values = ImmutableList.copyOf(valuesArray);
        else
            this.values = ImmutableList.of(value);
        this.defaultValue = defaultValue;
        this.index = new AtomicInteger(this.values.indexOf(value));
        this.index.compareAndSet(-1, 0);
        this.buttonWidget = new GuiButton(2131, 0, 0, 150, 20, "") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                EnumListEntry.this.index.incrementAndGet();
                EnumListEntry.this.index.compareAndSet(EnumListEntry.this.values.size(), 0);
                getScreen().setEdited(true, isRequiresRestart());
            }
        };
        this.resetButton = new GuiButton(313124, 0, 0, Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(resetButtonKey)) + 6, 20, I18n.format(resetButtonKey)) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                EnumListEntry.this.index.set(getDefaultIndex());
                getScreen().setEdited(true, isRequiresRestart());
            }
        };
        this.saveConsumer = saveConsumer;
        this.widgets = Lists.newArrayList(buttonWidget, resetButton);
        this.enumNameProvider = enumNameProvider;
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    public T getValue() {
        return this.values.get(this.index.get());
    }
    
    @Override
    public Optional<T> getDefaultValue() {
        return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        MainWindow window = Minecraft.getInstance().mainWindow;
        this.resetButton.enabled = isEditable() && getDefaultValue().isPresent() && getDefaultIndex() != this.index.get();
        this.resetButton.y = y;
        this.buttonWidget.enabled = isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.displayString = (enumNameProvider.apply(getValue()));
        if (Minecraft.getInstance().fontRenderer.getBidiFlag()) {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), window.getScaledWidth() - x - Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(getFieldName())), y + 5, 16777215);
            this.resetButton.x = x;
            this.buttonWidget.x = x + resetButton.getWidth() + 2;
            this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2);
        } else {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), x, y + 5, 16777215);
            this.resetButton.x = x + entryWidth - resetButton.getWidth();
            this.buttonWidget.x = x + entryWidth - 150;
            this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2);
        }
        resetButton.render(mouseX, mouseY, delta);
        buttonWidget.render(mouseX, mouseY, delta);
    }
    
    private int getDefaultIndex() {
        return Math.max(0, this.values.indexOf(this.defaultValue.get()));
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
    public static interface Translatable {
        String getKey();
    }
    
}
