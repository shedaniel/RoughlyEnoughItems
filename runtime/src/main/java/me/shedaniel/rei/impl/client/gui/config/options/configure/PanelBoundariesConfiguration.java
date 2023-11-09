/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020, 2021, 2022, 2023 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.client.gui.config.options.configure;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.animator.NumberAnimator;
import me.shedaniel.clothconfig2.api.animator.ValueAnimator;
import me.shedaniel.math.FloatingRectangle;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.config.DisplayPanelLocation;
import me.shedaniel.rei.api.client.gui.config.PanelBoundary;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.impl.client.gui.config.ConfigAccess;
import me.shedaniel.rei.impl.client.gui.config.options.AllREIConfigOptions;
import me.shedaniel.rei.impl.client.gui.config.options.CompositeOption;
import me.shedaniel.rei.impl.client.gui.config.options.OptionValueEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static me.shedaniel.rei.impl.client.gui.config.options.ConfigUtils.*;

public enum PanelBoundariesConfiguration implements OptionValueEntry.Configurator<PanelBoundary> {
    INSTANCE;
    
    @Override
    public void configure(ConfigAccess access, CompositeOption<PanelBoundary> option, Runnable onClose) {
        Minecraft.getInstance().setScreen(new BoundariesScreen(access, option, onClose));
    }
    
    private static class BoundariesScreen extends Screen {
        private final ConfigAccess access;
        private final CompositeOption<PanelBoundary> option;
        private final Runnable onClose;
        private Checkbox horizontalLimit, verticalLimit;
        private AbstractSliderButton horizontalSlider, verticalSlider;
        private AbstractSliderButton horizontalAlignmentSlider, verticalAlignmentSlider;
        private boolean horizontalUsePercentage, verticalUsePercentage;
        private ValueAnimator<FloatingRectangle> boundsAnimator = ValueAnimator.ofFloatingRectangle();
        private NumberAnimator<Float> innerAlphaAnimator = ValueAnimator.ofFloat(1.0F);
        
        public BoundariesScreen(ConfigAccess access, CompositeOption<PanelBoundary> option, Runnable onClose) {
            super(literal(""));
            this.access = access;
            this.option = option;
            this.onClose = onClose;
            
            this.horizontalUsePercentage = access.get(option).horizontalPercentage() != 1.0;
            this.verticalUsePercentage = access.get(option).verticalPercentage() != 1.0;
        }
        
        @Override
        public void init() {
            super.init();
            PanelBoundary boundary = access.get(option);
            addRenderableWidget(horizontalLimit = new Checkbox(0, 0, 20, 20, literal("config.rei.options.layout.boundaries.desc.limit_by_percentage"), this.horizontalUsePercentage, false) {
                @Override
                public void onPress() {
                    horizontalUsePercentage = !horizontalUsePercentage;
                    PanelBoundary boundary = access.get(option);
                    access.set(option, new PanelBoundary(1.0, boundary.verticalPercentage(), 50, boundary.verticalLimit(), 1.0, boundary.verticalAlign()));
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                    init(minecraft, BoundariesScreen.this.width, BoundariesScreen.this.height);
                }
            });
            double v = horizontalUsePercentage ? boundary.horizontalPercentage() : boundary.horizontalLimit() / 50.0;
            addRenderableWidget(horizontalSlider = new AbstractSliderButton(0, 0, 20, 20, getSliderMessage("config.rei.options.layout.boundaries.desc.limit", horizontalUsePercentage, v, 50), v) {
                @Override
                protected void updateMessage() {
                    setMessage(getSliderMessage("config.rei.options.layout.boundaries.desc.limit", horizontalUsePercentage, value, 50));
                }
                
                @Override
                protected void applyValue() {
                    PanelBoundary boundary = access.get(option);
                    if (horizontalUsePercentage) {
                        access.set(option, new PanelBoundary(value, boundary.verticalPercentage(), 50, boundary.verticalLimit(), boundary.horizontalAlign(), boundary.verticalAlign()));
                    } else {
                        access.set(option, new PanelBoundary(1.0, boundary.verticalPercentage(), valueToLimit(value, 50), boundary.verticalLimit(), boundary.horizontalAlign(), boundary.verticalAlign()));
                    }
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                }
                
                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    if (horizontalUsePercentage) return super.keyPressed(keyCode, scanCode, modifiers);
                    boolean leftArrow = keyCode == 263;
                    double newValue;
                    if (leftArrow) {
                        newValue = Mth.clamp((valueToLimit(value, 50) - 1) / 50.0, 0, 1);
                    } else if (keyCode == 262) {
                        newValue = Mth.clamp((valueToLimit(value, 50) + 1) / 50.0, 0, 1);
                    } else {
                        return super.keyPressed(keyCode, scanCode, modifiers);
                    }
                    
                    if (newValue != value) {
                        value = newValue;
                        applyValue();
                        updateMessage();
                        return true;
                    }
                    
                    return false;
                }
            });
            DisplayPanelLocation location = access.get(AllREIConfigOptions.LOCATION);
            addRenderableWidget(horizontalAlignmentSlider = new AbstractSliderButton(0, 0, 20, 20, getHAlignmentSliderMessage(location == DisplayPanelLocation.LEFT ? 1 - boundary.horizontalAlign() : boundary.horizontalAlign()), location == DisplayPanelLocation.LEFT ? 1 - boundary.horizontalAlign() : boundary.horizontalAlign()) {
                @Override
                protected void updateMessage() {
                    setMessage(getHAlignmentSliderMessage(value));
                }
                
                @Override
                protected void applyValue() {
                    PanelBoundary boundary = access.get(option);
                    access.set(option, new PanelBoundary(boundary.horizontalPercentage(), boundary.verticalPercentage(), boundary.horizontalLimit(), boundary.verticalLimit(), location == DisplayPanelLocation.LEFT ? 1 - snapPercentage(value) : snapPercentage(value), boundary.verticalAlign()));
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                }
            });
            addRenderableWidget(verticalLimit = new Checkbox(0, 0, 20, 20, literal("config.rei.options.layout.boundaries.desc.limit_by_percentage"), this.verticalUsePercentage, false) {
                @Override
                public void onPress() {
                    verticalUsePercentage = !verticalUsePercentage;
                    PanelBoundary boundary = access.get(option);
                    access.set(option, new PanelBoundary(boundary.horizontalPercentage(), 1.0, boundary.horizontalLimit(), 1000, boundary.horizontalAlign(), 0.5));
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                    init(minecraft, BoundariesScreen.this.width, BoundariesScreen.this.height);
                }
            });
            v = verticalUsePercentage ? boundary.verticalPercentage() : boundary.verticalLimit() / 1000.0;
            addRenderableWidget(verticalSlider = new AbstractSliderButton(0, 0, 20, 20, getSliderMessage("config.rei.options.layout.boundaries.desc.limit", verticalUsePercentage, v, 1000), v) {
                @Override
                protected void updateMessage() {
                    setMessage(getSliderMessage("config.rei.options.layout.boundaries.desc.limit", verticalUsePercentage, value, 1000));
                }
                
                @Override
                protected void applyValue() {
                    PanelBoundary boundary = access.get(option);
                    if (verticalUsePercentage) {
                        access.set(option, new PanelBoundary(boundary.horizontalPercentage(), value, boundary.horizontalLimit(), 1000, boundary.horizontalAlign(), boundary.verticalAlign()));
                    } else {
                        access.set(option, new PanelBoundary(boundary.horizontalPercentage(), 1.0, boundary.horizontalLimit(), valueToLimit(value, 1000), boundary.horizontalAlign(), boundary.verticalAlign()));
                    }
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                }
                
                @Override
                public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                    if (verticalUsePercentage) return super.keyPressed(keyCode, scanCode, modifiers);
                    boolean leftArrow = keyCode == 263;
                    double newValue;
                    if (leftArrow) {
                        newValue = Mth.clamp((valueToLimit(value, 1000) - 1) / 1000.0, 0, 1);
                    } else if (keyCode == 262) {
                        newValue = Mth.clamp((valueToLimit(value, 1000) + 1) / 1000.0, 0, 1);
                    } else {
                        return super.keyPressed(keyCode, scanCode, modifiers);
                    }
                    
                    if (newValue != value) {
                        value = newValue;
                        applyValue();
                        updateMessage();
                        return true;
                    }
                    
                    return false;
                }
            });
            addRenderableWidget(verticalAlignmentSlider = new AbstractSliderButton(0, 0, 20, 20, getVAlignmentSliderMessage(boundary.verticalAlign()), boundary.verticalAlign()) {
                @Override
                protected void updateMessage() {
                    setMessage(getVAlignmentSliderMessage(value));
                }
                
                @Override
                protected void applyValue() {
                    PanelBoundary boundary = access.get(option);
                    access.set(option, new PanelBoundary(boundary.horizontalPercentage(), boundary.verticalPercentage(), boundary.horizontalLimit(), boundary.verticalLimit(), boundary.horizontalAlign(), snapPercentage(value)));
                    if (!isReducedMotion()) innerAlphaAnimator.setTo(-1.0F, 200);
                }
            });
        }
        
        private Component getSliderMessage(String translationKey, boolean usePercentage, double percentage, int max) {
            if (usePercentage) {
                return translatable(translationKey, String.format("%.1f%%", percentage * 100));
            } else if (valueToLimit(percentage, max) == max) {
                return translatable(translationKey, translatable("config.rei.value.default", valueToLimit(percentage, max) + ""));
            } else {
                return translatable(translationKey, valueToLimit(percentage, max) + "");
            }
        }
        
        private int valueToLimit(double v, int max) {
            return Mth.clamp((int) Math.round(v * max), 1, max);
        }
        
        private Component getHAlignmentSliderMessage(double percentage) {
            String translationKey = "config.rei.options.layout.boundaries.desc.alignment";
            DisplayPanelLocation location = access.get(AllREIConfigOptions.LOCATION);
            if (percentage <= 0.02) {
                Component component = translatable(translationKey, translatable("config.rei.options.layout.boundaries.desc.horizontal_alignment.left"));
                if (location == DisplayPanelLocation.LEFT) return translatable("config.rei.value.default", component);
                return component;
            } else if (percentage >= 0.98) {
                Component component = translatable(translationKey, translatable("config.rei.options.layout.boundaries.desc.horizontal_alignment.right"));
                if (location == DisplayPanelLocation.RIGHT) return translatable("config.rei.value.default", component);
                return component;
            } else if (percentage >= 0.45 && percentage <= 0.55) {
                return translatable(translationKey, translatable("config.rei.options.layout.boundaries.desc.horizontal_alignment.center"));
            } else {
                return translatable(translationKey, String.format("%.1f%%", percentage * 100));
            }
        }
        
        private Component getVAlignmentSliderMessage(double percentage) {
            String translationKey = "config.rei.options.layout.boundaries.desc.alignment";
            if (percentage <= 0.02) {
                return translatable(translationKey, translatable("config.rei.options.layout.boundaries.desc.vertical_alignment.top"));
            } else if (percentage >= 0.98) {
                return translatable(translationKey, translatable("config.rei.options.layout.boundaries.desc.vertical_alignment.bottom"));
            } else if (percentage >= 0.45 && percentage <= 0.55) {
                return translatable(translationKey, translatable("config.rei.value.default", translatable("config.rei.options.layout.boundaries.desc.vertical_alignment.center")));
            } else {
                return translatable(translationKey, String.format("%.1f%%", percentage * 100));
            }
        }
        
        private double snapPercentage(double percentage) {
            if (percentage <= 0.02) {
                return 0;
            } else if (percentage >= 0.98) {
                return 1;
            } else if (percentage >= 0.45 && percentage <= 0.55) {
                return 0.5;
            } else {
                return percentage;
            }
        }
        
        @Override
        public void render(PoseStack poses, int mouseX, int mouseY, float delta) {
            this.innerAlphaAnimator.setTarget(this.innerAlphaAnimator.target() + (1.0F - this.innerAlphaAnimator.target()) * 0.06F);
            this.innerAlphaAnimator.update(delta);
            this.renderBackground(poses);
            Rectangle panelBounds = new Rectangle(this.width * 3 / 10, this.height * 4 / 40, this.width * 4 / 10, this.height * 32 / 40);
            Widgets.createCategoryBase(panelBounds).render(poses, mouseX, mouseY, delta);
            int y = panelBounds.y + 6;
            this.font.draw(poses, translatable("config.rei.options.layout.boundaries.desc.configure").withStyle(ChatFormatting.UNDERLINE), panelBounds.x + 6, y, 0xff404040);
            y += 14;
            this.font.draw(poses, translatable("config.rei.options.layout.boundaries.desc.horizontal"), panelBounds.x + 6, y, 0xff404040);
            this.horizontalLimit.setX(panelBounds.x + 6);
            this.horizontalLimit.setY(y + 10);
            this.font.draw(poses, translatable("config.rei.options.layout.boundaries.desc.limit_by_percentage"), horizontalLimit.getX() + 24, horizontalLimit.getY() + 6, 0xff404040);
            y += 32;
            this.horizontalSlider.setX(panelBounds.x + 6);
            this.horizontalSlider.setY(y);
            this.horizontalSlider.setWidth(panelBounds.width - 12);
            y += 22;
            this.horizontalAlignmentSlider.setX(panelBounds.x + 6);
            this.horizontalAlignmentSlider.setY(y);
            this.horizontalAlignmentSlider.setWidth(panelBounds.width - 12);
            y += 28;
            
            this.font.draw(poses, translatable("config.rei.options.layout.boundaries.desc.vertical"), panelBounds.x + 6, y, 0xff404040);
            this.verticalLimit.setX(panelBounds.x + 6);
            this.verticalLimit.setY(y + 10);
            this.font.draw(poses, translatable("config.rei.options.layout.boundaries.desc.limit_by_percentage"), verticalLimit.getX() + 24, verticalLimit.getY() + 6, 0xff404040);
            y += 32;
            this.verticalSlider.setX(panelBounds.x + 6);
            this.verticalSlider.setY(y);
            this.verticalSlider.setWidth(panelBounds.width - 12);
            y += 22;
            this.verticalAlignmentSlider.setX(panelBounds.x + 6);
            this.verticalAlignmentSlider.setY(y);
            this.verticalAlignmentSlider.setWidth(panelBounds.width - 12);
            
            super.render(poses, mouseX, mouseY, delta);
            renderPreview(poses, panelBounds, delta);
        }
        
        private void renderPreview(PoseStack poses, Rectangle panelBounds, float delta) {
            int entrySize = Mth.ceil(18 * access.get(AllREIConfigOptions.ZOOM));
            Rectangle overlayBounds;
            DisplayPanelLocation location = access.get(AllREIConfigOptions.LOCATION);
            PanelBoundary boundary = access.get(option);
            if (location == DisplayPanelLocation.LEFT) {
                overlayBounds = new Rectangle(2, 0, panelBounds.x - 2, height);
            } else {
                overlayBounds = new Rectangle(panelBounds.getMaxX() + 2, 0, width - panelBounds.getMaxX() - 4, height);
            }
            
            double hAlign = location == DisplayPanelLocation.LEFT ? 1 - boundary.horizontalAlign() : boundary.horizontalAlign();
            int widthReduction = (int) Math.round(overlayBounds.width * (1 - boundary.horizontalPercentage()));
            overlayBounds.x += (int) Math.round(widthReduction * hAlign);
            overlayBounds.width -= widthReduction;
            int maxWidth = (int) Math.ceil(entrySize * boundary.horizontalLimit() + entrySize * 0.75);
            if (overlayBounds.width > maxWidth) {
                overlayBounds.x += (int) Math.round((overlayBounds.width - maxWidth) * hAlign);
                overlayBounds.width = maxWidth;
            }
            int heightReduction = (int) Math.round(overlayBounds.height * (1 - boundary.verticalPercentage()));
            overlayBounds.height -= heightReduction;
            overlayBounds.y += (int) Math.round(heightReduction * boundary.verticalAlign());
            int maxHeight = (int) Math.ceil(entrySize * boundary.verticalLimit() + entrySize * 0.75);
            if (overlayBounds.height > maxHeight) {
                overlayBounds.y += (int) Math.round((overlayBounds.height - maxHeight) * boundary.verticalAlign());
                overlayBounds.height = maxHeight;
            }
            this.boundsAnimator.update(delta);
            this.boundsAnimator.setTo(overlayBounds.getFloatingBounds(), Util.make(() -> {
                FloatingRectangle target = boundsAnimator.target();
                return target.x == 0 && target.y == 0 && target.width == 0 && target.height == 0;
            }) || isReducedMotion() ? 0 : 200);
            overlayBounds = this.boundsAnimator.value().getBounds();
            
            this.fillGradient(poses, overlayBounds.x, overlayBounds.y, overlayBounds.getMaxX(), overlayBounds.getMaxY(), 0x80fff0ad, 0x80fff0ad);
            
            int width = Math.max(Mth.floor((overlayBounds.width - 2) / (float) entrySize), 1);
            int height = Math.max(Mth.floor((overlayBounds.height - 2) / (float) entrySize), 1);
            Rectangle innerBounds = new Rectangle((int) (overlayBounds.getCenterX() - width * (entrySize / 2f)), (int) (overlayBounds.getCenterY() - height * (entrySize / 2f)), width * entrySize, height * entrySize);
            int color = 0x823c0a | ((int) (Math.ceil(Mth.clamp(innerAlphaAnimator.floatValue(), 0, 1) * 0x80))) << 24;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int slotX = innerBounds.x + i * entrySize + 1;
                    int slotY = innerBounds.y + j * entrySize + 1;
                    this.fillGradient(poses, slotX, slotY, slotX + entrySize - 2, slotY + entrySize - 2, color, color);
                }
            }
        }
        
        @Override
        public void onClose() {
            this.onClose.run();
        }
    }
}
