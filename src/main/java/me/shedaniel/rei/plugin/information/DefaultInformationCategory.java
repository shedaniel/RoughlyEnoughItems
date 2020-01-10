package me.shedaniel.rei.plugin.information;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.widget.DynamicEntryListWidget;
import me.shedaniel.clothconfig2.gui.widget.DynamicNewSmoothScrollingEntryListWidget;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeCategory;
import me.shedaniel.rei.gui.entries.RecipeEntry;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.RenderingEntry;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.plugin.DefaultPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.Texts;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DefaultInformationCategory implements RecipeCategory<DefaultInformationDisplay> {
    @Override
    public Identifier getIdentifier() {
        return DefaultPlugin.INFO;
    }
    
    @Override
    public String getCategoryName() {
        return I18n.translate("category.rei.information");
    }
    
    @Override
    public RecipeEntry getSimpleRenderer(DefaultInformationDisplay recipe) {
        Text name = recipe.getName();
        return new RecipeEntry() {
            @Override
            public int getHeight() {
                return 10 + MinecraftClient.getInstance().textRenderer.fontHeight;
            }
            
            @Nullable
            @Override
            public QueuedTooltip getTooltip(int mouseX, int mouseY) {
                return null;
            }
            
            @Override
            public void render(Rectangle rectangle, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().textRenderer.draw(name.asFormattedString(), rectangle.x + 5, rectangle.y + 6, -1);
            }
        };
    }
    
    @Override
    public EntryStack getLogo() {
        return new RenderingEntry() {
            @Override
            public void render(Rectangle bounds, int mouseX, int mouseY, float delta) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(DefaultPlugin.getDisplayTexture());
                Matrix4f matrix4f = Matrix4f.method_24021(-1.2f, -1, 0);
                DefaultInformationCategory.innerBlit(matrix4f, bounds.getCenterX() - 8, bounds.getCenterX() + 8, bounds.getCenterY() - 8, bounds.getCenterY() + 8, 0, 116f / 256f, (116f + 16f) / 256f, 0f, 16f / 256f);
            }
        };
    }
    
    protected static void innerBlit(Matrix4f matrix4f, int xStart, int xEnd, int yStart, int yEnd, int z, float uStart, float uEnd, float vStart, float vEnd) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, xStart, yEnd, z).texture(uStart, vEnd).next();
        bufferBuilder.vertex(matrix4f, xEnd, yEnd, z).texture(uEnd, vEnd).next();
        bufferBuilder.vertex(matrix4f, xEnd, yStart, z).texture(uEnd, vStart).next();
        bufferBuilder.vertex(matrix4f, xStart, yStart, z).texture(uStart, vStart).next();
        bufferBuilder.end();
        RenderSystem.enableAlphaTest();
        BufferRenderer.draw(bufferBuilder);
    }
    
    @Override
    public List<Widget> setupDisplay(Supplier<DefaultInformationDisplay> recipeDisplaySupplier, Rectangle bounds) {
        DefaultInformationDisplay display = recipeDisplaySupplier.get();
        List<Widget> widgets = Lists.newArrayList();
        widgets.add(new LabelWidget(new Point(bounds.getCenterX(), bounds.y + 3), display.getName().asFormattedString()).noShadow().color(ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF404040));
        widgets.add(EntryWidget.create(bounds.getCenterX() - 8, bounds.y + 15).entries(display.getEntryStacks()));
        Rectangle rectangle = new Rectangle(bounds.getCenterX() - (bounds.width / 2), bounds.y + 35, bounds.width, bounds.height - 40);
        widgets.add(new SlotBaseWidget(rectangle));
        widgets.add(new ScrollableTextWidget(rectangle, display.getTexts()));
        return widgets;
    }
    
    @Override
    public int getDisplayHeight() {
        return 140;
    }
    
    @Override
    public int getFixedRecipesPerPage() {
        return 1;
    }
    
    private static class ScrollableTextWidget extends WidgetWithBounds {
        private Rectangle bounds;
        private List<Text> texts;
        private double target;
        private double scroll;
        private long start;
        private long duration;
        
        public ScrollableTextWidget(Rectangle bounds, List<Text> texts) {
            this.bounds = bounds;
            this.texts = Lists.newArrayList();
            for (Text text : texts) {
                if (!this.texts.isEmpty())
                    this.texts.add(null);
                this.texts.addAll(Texts.wrapLines(text, bounds.width - 11, MinecraftClient.getInstance().textRenderer, true, false));
            }
        }
        
        @Override
        public boolean mouseScrolled(double double_1, double double_2, double double_3) {
            if (containsMouse(double_1, double_2)) {
                offset(ClothConfigInitializer.getScrollStep() * -double_3, true);
                return true;
            }
            return false;
        }
        
        public void offset(double value, boolean animated) {
            scrollTo(target + value, animated);
        }
        
        public void scrollTo(double value, boolean animated) {
            scrollTo(value, animated, ClothConfigInitializer.getScrollDuration());
        }
        
        public void scrollTo(double value, boolean animated, long duration) {
            target = clamp(value);
            
            if (animated) {
                start = System.currentTimeMillis();
                this.duration = duration;
            } else
                scroll = target;
        }
        
        public final double clamp(double v) {
            return clamp(v, DynamicEntryListWidget.SmoothScrollingSettings.CLAMP_EXTENSION);
        }
        
        public final double clamp(double v, double clampExtension) {
            return MathHelper.clamp(v, -clampExtension, getMaxScroll() + clampExtension);
        }
        
        protected int getMaxScroll() {
            return Math.max(0, this.getMaxScrollPosition() - this.getBounds().height + 4);
        }
        
        protected int getMaxScrollPosition() {
            int i = 0;
            for (Text entry : texts) {
                i += entry == null ? 4 : font.fontHeight;
            }
            return i;
        }
        
        @Override
        public Rectangle getBounds() {
            return bounds;
        }
        
        @Override
        public void render(int mouseX, int mouseY, float delta) {
            updatePosition(delta);
            Rectangle innerBounds = new Rectangle(bounds.x + 1, bounds.y + 1, bounds.width - 7, bounds.height - 2);
            ScissorsHandler.INSTANCE.scissor(innerBounds);
            int currentY = (int) -scroll + innerBounds.y;
            for (Text text : texts) {
                if (text != null && currentY + font.fontHeight >= innerBounds.y && currentY <= innerBounds.getMaxY()) {
                    font.draw(text.asFormattedString(), innerBounds.x + 2, currentY + 2, ScreenHelper.isDarkModeEnabled() ? 0xFFBBBBBB : 0xFF090909);
                }
                currentY += text == null ? 4 : font.fontHeight;
            }
            ScissorsHandler.INSTANCE.removeLastScissor();
            ScissorsHandler.INSTANCE.scissor(bounds);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(770, 771, 0, 1);
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(7425);
            RenderSystem.disableTexture();
            renderScrollBar();
            RenderSystem.enableTexture();
            RenderSystem.shadeModel(7424);
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            ScissorsHandler.INSTANCE.removeLastScissor();
        }
        
        @SuppressWarnings("deprecation")
        private void renderScrollBar() {
            int maxScroll = getMaxScroll();
            int scrollbarPositionMinX = getBounds().getMaxX() - 7;
            int scrollbarPositionMaxX = scrollbarPositionMinX + 6;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            if (maxScroll > 0) {
                int height = (int) (((this.getBounds().height - 2f) * (this.getBounds().height - 2f)) / this.getMaxScrollPosition());
                height = MathHelper.clamp(height, 32, this.getBounds().height - 2);
                height -= Math.min((scroll < 0 ? (int) -scroll : scroll > maxScroll ? (int) scroll - maxScroll : 0), height * .95);
                height = Math.max(10, height);
                int minY = Math.min(Math.max((int) scroll * (this.getBounds().height - 2 - height) / maxScroll + getBounds().y + 1, getBounds().y + 1), getBounds().getMaxY() -1 - height);
    
                boolean hovered = new Rectangle(scrollbarPositionMinX, minY, scrollbarPositionMaxX - scrollbarPositionMinX, height).contains(PointHelper.fromMouse());
                int bottomC = hovered ? 168 : 128;
                int topC = hovered ? 222 : 172;
                
                // Black Bar
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, this.getBounds().y + 1, 0.0D).texture(0, 1).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMaxX, this.getBounds().y + 1, 0.0D).texture(1, 1).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMaxX, getBounds().getMaxY() - 1, 0.0D).texture(1, 0).color(0, 0, 0, 255).next();
                buffer.vertex(scrollbarPositionMinX, getBounds().getMaxY() - 1, 0.0D).texture(0, 0).color(0, 0, 0, 255).next();
                tessellator.draw();
                
                // Bottom
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, minY + height, 0.0D).texture(0, 1).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMaxX, minY + height, 0.0D).texture(1, 1).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMaxX, minY, 0.0D).texture(1, 0).color(bottomC, bottomC, bottomC, 255).next();
                buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0, 0).color(bottomC, bottomC, bottomC, 255).next();
                tessellator.draw();
                
                // Top
                buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(scrollbarPositionMinX, (minY + height - 1), 0.0D).texture(0, 1).color(topC, topC, topC, 255).next();
                buffer.vertex((scrollbarPositionMaxX - 1), (minY + height - 1), 0.0D).texture(1, 1).color(topC, topC, topC, 255).next();
                buffer.vertex((scrollbarPositionMaxX - 1), minY, 0.0D).texture(1, 0).color(topC, topC, topC, 255).next();
                buffer.vertex(scrollbarPositionMinX, minY, 0.0D).texture(0, 0).color(topC, topC, topC, 255).next();
                tessellator.draw();
            }
        }
        
        private void updatePosition(float delta) {
            target = clamp(target);
            if (target < 0) {
                target -= target * (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3;
            } else if (target > getMaxScroll()) {
                target = (target - getMaxScroll()) * (1 - (1 - ClothConfigInitializer.getBounceBackMultiplier()) * delta / 3) + getMaxScroll();
            }
            if (!DynamicNewSmoothScrollingEntryListWidget.Precision.almostEquals(scroll, target, DynamicNewSmoothScrollingEntryListWidget.Precision.FLOAT_EPSILON))
                scroll = (float) DynamicNewSmoothScrollingEntryListWidget.Interpolation.expoEase(scroll, target, Math.min((System.currentTimeMillis() - start) / ((double) duration), 1));
            else
                scroll = target;
        }
        
        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }
}
