package me.shedaniel.rei.impl.client.gui.hints;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.ClientHelper;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryImpl;
import me.shedaniel.rei.impl.common.entry.type.EntryRegistryListener;
import me.shedaniel.rei.impl.common.util.InstanceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportantWarningsWidget extends WidgetWithBounds {
    private static final EntryRegistryListener LISTENER = new EntryRegistryListener() {
    };
    private static String prevId = "";
    private static boolean dirty = false;
    private boolean visible;
    private final Rectangle bounds;
    private final Rectangle buttonBounds = new Rectangle();
    private List<Component> texts;

    private static boolean dontShowPartialRecipeWarning = false;

    static {
        loadConfig();
    }

    public ImportantWarningsWidget() {
        if (((EntryRegistryImpl) EntryRegistry.getInstance()).listeners.add(LISTENER)) {
            String newId = Minecraft.getInstance().hasSingleplayerServer() ?
                    "integrated:" + Minecraft.getInstance().getSingleplayerServer().getWorldData().getLevelName()
                    : InstanceHelper.connectionFromClient() != null ? "server:" + InstanceHelper.connectionFromClient().getId()
                    : "null";
            if (!newId.equals(prevId)) {
                prevId = newId;
                dirty = true;
            }
            dirty = dirty && !ClientHelper.getInstance().canUseMovePackets();
        }

        this.visible = dirty && !dontShowPartialRecipeWarning;
        this.texts = List.of(
                Component.translatable("text.rei.recipes.not.full.title").withStyle(ChatFormatting.RED),
                Component.translatable("text.rei.recipes.not.full.desc",
                                Component.translatable("text.rei.recipes.not.full.desc.command")
                                        .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE))
                        .withStyle(ChatFormatting.GRAY)
        );
        this.bounds = ScreenRegistry.getInstance()
                .getOverlayBounds(DisplayPanelLocation.LEFT, Minecraft.getInstance().screen);
        this.bounds.setBounds(this.bounds.x + 10, this.bounds.y + 10,
                this.bounds.width - 20, this.bounds.height - 20);
        int heightRequired = -5;
        for (Component text : texts) {
            heightRequired += Minecraft.getInstance().font.wordWrapHeight(text, this.bounds.width * 2) / 2;
            heightRequired += 5;
        }
        this.bounds.height = Math.min(heightRequired + 20, this.bounds.height);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!visible)
            return;
        graphics.pose().pushMatrix();
        graphics.fill(bounds.x - 5, bounds.y - 5, bounds.getMaxX() + 5,
                bounds.getMaxY() + 5, 0x90111111);
        int y = bounds.y;
        for (Component text : texts) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(bounds.x, y);
            graphics.pose().scale(0.5f, 0.5f);
            graphics.drawWordWrap(Minecraft.getInstance().font, text,
                    0, 0, bounds.width * 2, -1);
            y += Minecraft.getInstance().font.wordWrapHeight(text, bounds.width * 2) / 2 + 5;
            graphics.pose().popMatrix();
        }

        MutableComponent dontShowText = Component.literal("Okay, don't show it again");
        graphics.pose().pushMatrix();
        graphics.pose().translate(bounds.x + bounds.width / 2 -
                        Minecraft.getInstance().font.width(dontShowText) * 0.75f / 2,
                bounds.getMaxY() - 9);
        graphics.pose().scale(0.75f, 0.75f);
        this.buttonBounds.setBounds(bounds.x, bounds.getMaxY() - 20, bounds.width, 20);
        graphics.drawString(Minecraft.getInstance().font, dontShowText, 0, 0,
                buttonBounds.contains(mouseX, mouseY) ? 0xfffff8de : 0xAAFFFFFF);
        graphics.pose().popMatrix();
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.visible && button == 0 && buttonBounds.contains(mouseX, mouseY)) {
            dontShowPartialRecipeWarning = true; // dauerhaft merken
            saveConfig(); // Speichern
            this.visible = false;
            Widgets.produceClickSound();
            return true;
        }
        return false;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }

    private static Path getConfigPath() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config")
                .resolve("roughlyenoughitems")
                .resolve("rei_warning_config.json");
    }

    private static void saveConfig() {
        try {
            Path path = getConfigPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, "{\"dontShowPartialRecipeWarning\":true}");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig() {
        try {
            Path path = getConfigPath();
            if (Files.exists(path)) {
                String json = Files.readString(path);
                dontShowPartialRecipeWarning = json.contains("true");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
